/**
 *    Copyright 2016-today Software Craftmanship Toledo
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package es.craftsmanship.toledo.katangapp.activities;

import es.craftsmanship.toledo.katangapp.R;
import es.craftsmanship.toledo.katangapp.adapters.BusStopsAdapter;
import es.craftsmanship.toledo.katangapp.interactors.InvalidInteractorException;
import es.craftsmanship.toledo.katangapp.interactors.KatangaInteractor;
import es.craftsmanship.toledo.katangapp.interactors.KatangaInteractorFactoryUtil;
import es.craftsmanship.toledo.katangapp.models.BusStopResult;
import es.craftsmanship.toledo.katangapp.models.QueryResult;
import es.craftsmanship.toledo.katangapp.models.Route;
import es.craftsmanship.toledo.katangapp.subscribers.BusStopsSubscriber;
import es.craftsmanship.toledo.katangapp.subscribers.RouteSubscriber;
import es.craftsmanship.toledo.katangapp.utils.ExtrasConstants;

import android.content.Intent;

import android.graphics.Color;

import android.os.Bundle;

import android.support.v4.content.res.ResourcesCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;

import android.widget.Toast;

import com.squareup.otto.Subscribe;

import java.util.List;

/**
 * @author Cristóbal Hermida
 * @author Manuel de la Peña
 */
public class ShowBusStopsActivity
    extends BaseGeoLocatedActivity
    implements BusStopsSubscriber, RouteSubscriber {

    private RecyclerView recyclerView;
    private SwipeRefreshLayout swipeRefreshLayout;

    @Override
    @Subscribe
    public void busStopsReceived(Error error) {
    }

    @Override
    @Subscribe
    public void busStopsReceived(QueryResult queryResult) {
        List<BusStopResult> results = queryResult.getResults();

        recyclerView.setAdapter(new BusStopsAdapter(results));

        swipeRefreshLayout.setRefreshing(false);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_show_bus_stops);

        Intent intent = getIntent();

        if (intent.hasExtra(ExtrasConstants.QUERY_RESULT) &&
            (intent.getSerializableExtra(ExtrasConstants.QUERY_RESULT) != null)) {

            QueryResult queryResult =
                (QueryResult) intent.getSerializableExtra(ExtrasConstants.QUERY_RESULT);

            List<BusStopResult> results = queryResult.getResults();

            if (results.isEmpty()) {
                processEmptyResults();

                return;
            }

            recyclerView = (RecyclerView) findViewById(R.id.stops);

            swipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.swipeRefreshLayout);

            initializeSwipeRefreshLayout(intent.getExtras());

            recyclerView.setLayoutManager(new LinearLayoutManager(this));
            recyclerView.setAdapter(new BusStopsAdapter(results));
        }
        else {
            processEmptyResults();
        }
    }

    @Override
    @Subscribe
    public void routeReceived(Error error) {
        processEmptyResults();
    }

    @Override
    @Subscribe
    public void routeReceived(Route route) {
        if (route != null) {
            Intent intent = new Intent(this, RouteMapActivity.class);

            intent.putExtra(ExtrasConstants.ROUTE, route);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

            startActivity(intent);
        }
    }

    private void initializeSwipeRefreshLayout(final Bundle extras) {
        int color = ResourcesCompat.getColor(getResources(), R.color.katanga_yellow, getTheme());

        swipeRefreshLayout.setProgressBackgroundColorSchemeColor(color);

        swipeRefreshLayout.setColorSchemeColors(Color.BLACK);

        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {

            @Override
            public void onRefresh() {
                swipeRefreshLayout.setRefreshing(true);

                extras.putDouble(ExtrasConstants.LATITUDE, getLatitude());
                extras.putDouble(ExtrasConstants.LONGITUDE, getLongitude());

                try {
                    KatangaInteractor interactor =
                        KatangaInteractorFactoryUtil.getInstance().getInteractor(extras);

                    new Thread(interactor).start();
                }
                catch (InvalidInteractorException e) {
                    processErrors(getString(R.string.bustop_results_invalid_args) + e.getMessage());
                }
            }

        });
    }

    private void processEmptyResults() {
        processErrors(getString(R.string.bustop_results_empty));
    }

    private void processErrors(String message) {
        Toast.makeText(ShowBusStopsActivity.this, message, Toast.LENGTH_SHORT).show();

        Intent intent = new Intent(getApplicationContext(), MainActivity.class);

        startActivity(intent);
    }

}
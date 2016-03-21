package es.craftsmanship.toledo.katangapp.activities;

import es.craftsmanship.toledo.katangapp.models.BusStopResult;
import es.craftsmanship.toledo.katangapp.models.QueryResult;

import android.content.Intent;

import android.os.Bundle;

import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;

import java.util.List;

/**
 * @author Cristóbal Hermida
 */
public class ShowStopsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_show_stops);

        Intent intent = getIntent();

        if (!intent.hasExtra("queryResult")) {

            QueryResult queryResult = (QueryResult) intent.getSerializableExtra("queryResult");

            List<BusStopResult> busStopResults = queryResult.getResults();

            RecyclerView recyclerView = (RecyclerView) findViewById(R.id.stops);

            recyclerView.setLayoutManager(new LinearLayoutManager(this));

            recyclerView.setAdapter(new StopsAdapter(busStopResults));
        }
    }

}
package com.GRP.Group5.RoadTripApp.Activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.GRP.Group5.RoadTripApp.Activities.Adapters.RecylerAdapter;
import com.GRP.Group5.RoadTripApp.R;
import com.GRP.Group5.RoadTripApp.Scheduling.SchedulablePlace;
import com.GRP.Group5.RoadTripApp.Scheduling.Scheduler;
import com.GRP.Group5.RoadTripApp.Scheduling.SchedulerEvent;
import com.GRP.Group5.RoadTripApp.utils.Route;
import com.google.android.gms.maps.model.LatLng;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

import static com.GRP.Group5.RoadTripApp.Scheduling.SchedulerEvent.SchedulerEventType.SCHEDULER;

/**
 * Created by Yi Shen on 2018/3/30.
 */

public class RankActivity extends AppCompatActivity implements SchedulerEvent.SchedulerEventListener {
    private static RankActivity rankInstance;
    private RecyclerView recyclerView;
    private RecylerAdapter adapter;
    private List<Route> routes;
    private Scheduler scheduler;

    private Date startDate;
    private int timeStartMins;

    public RankActivity getContext() {return this;}

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rank);

        recyclerView = (RecyclerView) findViewById(R.id.recyclerView);  //get the recycler view
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        Intent intentExtras = getIntent();
        Bundle extrasBundle = intentExtras.getExtras();
        ArrayList<SchedulablePlace> locationList = new ArrayList();  //the locations user will pass by
        SchedulablePlace startLoc = null, endLoc = null;  //the start point and end point
        int numPlaces = extrasBundle.getInt("numPlaces");

        try {
            SimpleDateFormat ft = new SimpleDateFormat("dd-MM-yyyy");
            this.startDate = ft.parse(extrasBundle.getString("startDate"));
            this.timeStartMins = extrasBundle.getInt("startTime");
        } catch(ParseException e){
            e.printStackTrace();
        }

        //get all the location information from the FrontActivity
        for(int i = 0; i < numPlaces; i++){
            if(extrasBundle.containsKey("long-" + i) && extrasBundle.containsKey("lat-" + i)) {
                double longitude = extrasBundle.getDouble("long-" + i);  //get the value of longitude pf location that user entered from front page
                double latitude = extrasBundle.getDouble("lat-" + i);  //get the value of latitude of location that user entered from front page
                boolean isStart = extrasBundle.getBoolean("start-" + i);  //check if the location is start point
                boolean isDest = extrasBundle.getBoolean("dest-" + i);  //check if the location is end point

                String name = extrasBundle.getString("name-" + i);

                //Log.i("longitude$$$$$$$ " + longitude, "latitude$$$$$$ " + latitude);  //for debug

                SchedulablePlace pos = new SchedulablePlace(new LatLng(latitude, longitude), name);

                if(isStart){
                    startLoc = pos;
                }
                if(isDest){
                    endLoc = pos;
                }
                locationList.add(pos);
            }
        }

        this.scheduler = new Scheduler(locationList, startLoc, endLoc, this.startDate);
        Scheduler.addStaticListener(this);
        this.scheduler.runScheduler();

        routes = new ArrayList<>();

        adapter = new RecylerAdapter(routes, this);
        recyclerView.setAdapter(adapter);

        Button btnDistance = (Button) this.findViewById(R.id.btn_distance);
        Button btnTime = (Button) this.findViewById(R.id.btn_time);

        btnDistance.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Collections.sort(routes, new Comparator<Route>() {
                    @Override
                    public int compare(Route route1, Route route2) {
                        double i = route1.getTotalDistance() - route2.getTotalDistance();
                        if(i >= 0){
                            return 1;
                        }
                        else{
                            return -1;
                        }
                    }
                });

                adapter = new RecylerAdapter(routes, getContext());
                recyclerView.setAdapter(adapter);
            }
        });

        btnTime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Collections.sort(routes, new Comparator<Route>() {
                    @Override
                    public int compare(Route route1, Route route2) {
                        double i = route1.getTotalTime() - route2.getTotalTime();
                        if(i >= 0){
                            return 1;
                        }
                        else{
                            return -1;
                        }
                    }
                });

                adapter = new RecylerAdapter(routes, getContext());
                recyclerView.setAdapter(adapter);
            }
        });
    }

    @Override
    public void onSchedulerComplete(SchedulerEvent event) {
        //Convert to appropriate format
        if(event.getType() == SCHEDULER) {
            List<Route> schedules = event.getRoutes();
            for(Route r : schedules){
                r.setStartTime(this.timeStartMins);
            }

            routes = new ArrayList<>();
            if (schedules != null) {
                this.routes = schedules;
                this.adapter.setRoutes(this.routes);
                this.recyclerView.setAdapter(this.adapter);
            } else {
                Toast.makeText(RankActivity.this, "null schedules", Toast.LENGTH_SHORT).show();
            }
        }
    }
}

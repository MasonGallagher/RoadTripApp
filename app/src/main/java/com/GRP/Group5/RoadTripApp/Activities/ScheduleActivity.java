package com.GRP.Group5.RoadTripApp.Activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.GRP.Group5.RoadTripApp.Activities.Adapters.ScheduleAdapter;
import com.GRP.Group5.RoadTripApp.R;
import com.GRP.Group5.RoadTripApp.Scheduling.Scheduler;
import com.GRP.Group5.RoadTripApp.Scheduling.SchedulerEvent;
import com.GRP.Group5.RoadTripApp.utils.DebugingTools;
import com.GRP.Group5.RoadTripApp.utils.Plan;
import com.GRP.Group5.RoadTripApp.utils.Route;

import org.json.JSONException;
import org.json.JSONObject;

import java.security.InvalidParameterException;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by Yi Shen on 2018/3/2 0002.
 */

public class ScheduleActivity extends AppCompatActivity implements SchedulerEvent.SchedulerEventListener{

    private Route currentRoute;
    private ScheduleAdapter adapter;
    private Scheduler scheduler;
    private List<Plan> exclusionList;

    /**
     * Called when the activity is created
     * @param savedInstanceState - required for super call
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scheduler);  //get the layout

        try {
            Scheduler.addStaticListener(this);

            this.scheduler = Scheduler.getStaticScheduler();

            Intent intentExtras = getIntent();
            Bundle extrasBundle = intentExtras.getExtras();

            try {
                this.currentRoute = Route.fromJSON(new JSONObject(extrasBundle.getString("route")));
            } catch (JSONException e) {
                e.printStackTrace();
            }

            this.adapter = new ScheduleAdapter(this, R.layout.activity_scheduler);

            this.adapter.route = this.currentRoute;

            if (this.currentRoute != null) {
                this.adapter.add(this.currentRoute.getPlanLink(), this.currentRoute.getPlaces());
            }

            if(this.scheduler == null || !Scheduler.isTaskRunning()){
                this.scheduler = Scheduler.setStaticScheduler(new Scheduler(this.currentRoute));
            }

            ListView list = this.findViewById(R.id.routeList);
            list.setAdapter(this.adapter);

            Toolbar toolbar = this.findViewById(R.id.toolbarScheduler);
            this.setSupportActionBar(toolbar);

            this.runSpinner(Scheduler.isTaskRunning());

            this.exclusionList = new LinkedList<>();

        } catch (Exception e){
            DebugingTools.exception(e, this);
        }
    }

    @Override
    public void onSchedulerComplete(SchedulerEvent event) {

        this.scheduler = event.getScheduler();

        if(event.getType() == SchedulerEvent.SchedulerEventType.SUB_SCHEDULER){
            this.runSpinner(false);
            if(this.currentRoute != null){
                this.currentRoute = event.getRoute();
                this.exclusionList.addAll(this.currentRoute.getPlans());
                this.adapter.route = this.currentRoute;
                this.adapter.clear();
                this.adapter.add(this.currentRoute.getPlanLink(), this.currentRoute.getPlaces());
            } else {
                if(this.exclusionList.size() > 0){
                    this.exclusionList.clear();
                }
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        this.getMenuInflater().inflate(R.menu.menu_scheduler, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_refresh) {
            if (this.scheduler != null) {
                this.scheduler.doSubSchedule(this.currentRoute, this.exclusionList);
                this.runSpinner(true);
                return true;
            } else {
                DebugingTools.exception(new InvalidParameterException("Scheduler is null. Re-open GUI"), this);
            }
        }
        return false;
    }

    public void runSpinner(boolean enabled){
        ProgressBar spinner = this.findViewById(R.id.schedulerSpinner);
        TextView spinnerText = this.findViewById(R.id.spinnerText);
        TextView spinnerInfo = this.findViewById(R.id.spinnerInfo);

        int visibility = View.INVISIBLE;

        if(enabled){
            visibility = View.VISIBLE;
        }

        spinner.setVisibility(visibility);
        spinner.bringToFront();

        spinnerText.setVisibility(visibility);
        spinnerText.setTextColor(ContextCompat.getColor(this, R.color.white));
        spinnerText.bringToFront();

        spinnerInfo.setVisibility(visibility);
        spinnerInfo.setTextColor(ContextCompat.getColor(this, R.color.white_50));
        spinnerInfo.bringToFront();
    }
}


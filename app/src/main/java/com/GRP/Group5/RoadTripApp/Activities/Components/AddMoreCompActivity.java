package com.GRP.Group5.RoadTripApp.Activities.Components;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;

import com.GRP.Group5.RoadTripApp.R;
import com.GRP.Group5.RoadTripApp.utils.WayPointSearch;

/**
 * Created by Ben Clark on 07/12/2017.
 *
 * This class is a activity component that is to be added and removed from other GUI/activities
 * This specific class links to the add more or plus button
 */

public class AddMoreCompActivity extends AppCompatActivity {

    /**
     * Function to create the activity
     * @param savedInstanceState
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.add_more_comp_layout);
    }
}

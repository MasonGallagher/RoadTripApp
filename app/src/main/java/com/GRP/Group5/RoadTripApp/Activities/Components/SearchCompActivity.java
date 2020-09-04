package com.GRP.Group5.RoadTripApp.Activities.Components;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.GRP.Group5.RoadTripApp.R;

/**
 * Created by Ben Clark on 07/12/2017.
 *
 * This class is a component, hence implemented to be added and removed from another activity
 * This component is the search functionality of the main front page providing a search bar
 * and a radio button to select if the current search is the destination or not
 */

public class SearchCompActivity extends AppCompatActivity {
    /**
     * Function to create the GUI
     * @param savedInstanceState
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.search_layout_comp);
    }
}

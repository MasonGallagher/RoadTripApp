package com.GRP.Group5.RoadTripApp.Activities.GetNearbyPlaces;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.widget.Toast;

import com.GRP.Group5.RoadTripApp.Activities.PlaceDetailActivity;
import com.GRP.Group5.RoadTripApp.Activities.RankActivity;
import com.GRP.Group5.RoadTripApp.Scheduling.SchedulablePlace;
import com.GRP.Group5.RoadTripApp.Scheduling.SchedulerType;
import com.GRP.Group5.RoadTripApp.utils.HTTPAccess;
import com.GRP.Group5.RoadTripApp.utils.Route;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.maps.android.PolyUtil;

import org.json.JSONException;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;

/**
 * Created by Yi Shen on 08/04/2018..
 *
 * This Class is used to start a async task to get all the place details (name, address, phone number etc) from
 * a google request url and then send all these data to the PlaceDetailActivity and also start this activity from the MapActivity
 */

public class GetPlaceDetail extends AsyncTask<Object,String,String> {
    private Context context;  //receive the context from Map Activity
    private String googlePlaceDetailData;  //the formatted data string generated from the url request passed from MapActivity
    private SchedulablePlace markerPlace;
    private Route route;

    /**
     * Return the context of MapActivity
     * @return - The context of MapActivity
     */
    public Context getContext() {
        return context;
    }

    /**
     * Receive the context of MapActivity
     * @param context - The MapActivity context
     */
    public GetPlaceDetail(Context context) {
       this.context = context;
    }

    /**
     * Receive the data from MapActivity and extract the data of the url and return the corresponding JSON data
     * @param objects - the data transferred from MapActivity (url of the nearby places)
     * @return - the extracted JSON data
     */
    @Override
    protected String doInBackground(Object... objects) {
        try {
            this.googlePlaceDetailData = HTTPAccess.htmlGet((String) objects[0]);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return this.googlePlaceDetailData;
    }

    /**
     * This function will be called after the function doInBackground() finished.
     * It will get all the data (place name, rating, address etc) from the extracted JSON data which is return by the function doInBackground()
     * and then save all of them in the bundle.
     * Finally sending these data to the PlaceDetailActivity which will show all the data about this place on a pop-up window
     * @param urlString - the extracted JSON data return from the function doInBackground()
     */
    @Override
    protected void onPostExecute(String urlString) {
        String place_name;  //the place name
        String rating;  //the rating of the place(1 to 5)
        String address;  //the address of the place
        String phone_number;  //the phone number rof the place
        String website;  //the website of the place
        String[] photos;  //the photo array to store all the photo references of the place
        String openOrNot;  //to show if the place is open right now
        String weekDayHours;  //the working hours of the place

        //create a hash map to store all the place related information which is return from the JSON data parser
        HashMap<String, String[]> placeDetailList;
        DataParser parser = new DataParser();
        placeDetailList = parser.parsePlaceDetail(urlString);


        Intent intent = new Intent(this.context, PlaceDetailActivity.class);  //the intent to start the PlaceDetailActivity from MapActivity
        Bundle bundle = new Bundle();  //the bundle which store all the place detail data and it will be put into the intent and be sent to PlaceDetailActivity

        //put all the place detail into the bundle one by one
        if(placeDetailList != null && placeDetailList.size() != 0){

            //get the place number from the hash map and put it into the bundle
            if(placeDetailList.containsKey("place_name")){
                place_name = placeDetailList.get("place_name")[0];
                bundle.putString("place_name", place_name);
            }

            //get the place rating from the hash map and put it into the bundle
            if(placeDetailList.containsKey("rating")){
                rating = placeDetailList.get("rating")[0];
                bundle.putString("rating", rating);
            }

            //get the place address from the hash map and put it into the bundle
            if(placeDetailList.containsKey("address")){
                address  = placeDetailList.get("address")[0];
                bundle.putString("address", address);
            }

            //get the place phone number from the hash map and put it into the bundle
            if(placeDetailList.containsKey("phone_number")){
                phone_number  = placeDetailList.get("phone_number")[0];
                bundle.putString("phone_number", phone_number);
            }

            //get the place website from the hash map and put it into the bundle
            if(placeDetailList.containsKey("website")){
                website  = placeDetailList.get("website")[0];
                bundle.putString("website", website);
            }

            //get the place photos from the hash map and put it into the bundle
            if(placeDetailList.containsKey("photos")){
                photos = placeDetailList.get("photos");
                bundle.putStringArray("photos", photos);
            }

            //get the place information about if it is open right now from the hash map and put it into the bundle
            if(placeDetailList.containsKey("openOrNot")){
                openOrNot =  placeDetailList.get("openOrNot")[0];
                bundle.putString("openOrNot", openOrNot);
            }

            //get the place week working hour information  from the hash map and put it into the bundle
            if(placeDetailList.containsKey("weekDayHours")){
                weekDayHours =  placeDetailList.get("weekDayHours")[0];
                bundle.putString("weekDayHours", weekDayHours);
            }
        }

        //Add scheduler data to bundle
        try {
            bundle.putString("markerPlaceJSON", this.markerPlace.toJSON().toString());
            bundle.putString("routeJSON", this.route.toJSON().toString());
        } catch (JSONException e) {
            e.printStackTrace();
        }

        //put the bundle(data holder) to the intent and start this intent(start the PlaceDetailActivity from MapActivity)
        intent.putExtras(bundle);
        this.context.startActivity(intent);
    }

    public void passSchedulerData(Route route, SchedulerType currentAddingType, Marker marker) {
        this.markerPlace = new SchedulablePlace(marker.getPosition(), marker.getTitle(), currentAddingType, null);
        this.route = route;
    }
}


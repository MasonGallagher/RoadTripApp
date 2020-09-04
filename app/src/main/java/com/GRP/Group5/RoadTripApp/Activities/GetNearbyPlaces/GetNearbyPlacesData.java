package com.GRP.Group5.RoadTripApp.Activities.GetNearbyPlaces;

import android.annotation.SuppressLint;
import android.os.AsyncTask;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;

import com.GRP.Group5.RoadTripApp.Activities.MapsActivity;
import com.GRP.Group5.RoadTripApp.Scheduling.SchedulablePlace;
import com.GRP.Group5.RoadTripApp.Scheduling.SchedulerType;
import com.GRP.Group5.RoadTripApp.utils.HTTPAccess;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;

import java.util.HashMap;
import java.util.List;

/**
 * Created by Yi Shen on 25/02/2018.
 */
public class GetNearbyPlacesData extends AsyncTask<Object, String, String> {

    private String googlePlacesData;
    private GoogleMap mMap;
    private String url;
    private SchedulerType category;
    @SuppressLint("StaticFieldLeak")
    private ProgressBar bar;
    private SchedulablePlace placeToView;

    @Override
    protected String doInBackground(Object... params) {
        try {
            //Log.d("GetNearbyPlacesData", "doInBackground entered");
            this.mMap = (GoogleMap) params[0];
            this.url = (String) params[1];
            this.bar = (ProgressBar) params[2];
            this.category = (SchedulerType) params[3];
            this.placeToView = (SchedulablePlace) params[4];
            this.googlePlacesData = HTTPAccess.htmlGet(url);  //to retrieve data from URL
            //Log.d("GooglePlacesReadTask", "doInBackground Exit");
        } catch (Exception e) {
            //Log.d("GooglePlacesReadTask", e.toString());
        }
        return this.googlePlacesData;
    }

    @Override
    protected void onPostExecute(String result) {
        //Log.d("GooglePlacesReadTask", "onPostExecute Entered");
        List<HashMap<String, String>> nearbyPlaceList;
        DataParser parser = new DataParser();
        nearbyPlaceList = parser.parse(result);
        showNearbyPlaces(nearbyPlaceList);
        //Log.d("GooglePlacesReadTask", "onPostExecute Exit");
    }

    private void showNearbyPlaces(List<HashMap<String, String>> nearbyPlaceList) {
        for (int i = 0; i < nearbyPlaceList.size(); i++) {
            HashMap<String, String> googlePlace = nearbyPlaceList.get(i);
            String placeName = googlePlace.get("place_name");
            double lat = Double.parseDouble(googlePlace.get("lat"));
            double lng = Double.parseDouble(googlePlace.get("lng"));
            String place_id = googlePlace.get("place_id");
            LatLng latLng = new LatLng(lat, lng);

            SchedulablePlace place = MapsActivity.getStaticContext().getRoute().getClosestRoutePlace(latLng);
            MapsActivity.getStaticContext().addMarker(place, place_id, latLng, placeName, this.category);
        }

        MapsActivity.getStaticContext().showMarkerType(this.placeToView, this.category);

        //removes loading bar
        if (this.bar != null) {
            this.bar.setVisibility(View.INVISIBLE);
        }
    }
}
package com.GRP.Group5.RoadTripApp.Activities.GetNearbyPlaces;

import android.graphics.Color;
import android.os.AsyncTask;

import com.GRP.Group5.RoadTripApp.utils.HTTPAccess;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.maps.model.RoundCap;
import com.google.maps.android.PolyUtil;

import java.io.IOException;

/**
 * Created by Yi Shen on 13/03/2018.
 */
public class GetDirectionsData extends AsyncTask<Object,String,String> {
    GoogleMap mMap;
    String url;
    String googleDirectionsData;

    @Override
    protected String doInBackground(Object... objects) {
        mMap = (GoogleMap)objects[0];
        url = (String)objects[1];


        try {
            googleDirectionsData = HTTPAccess.htmlGet(url);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return googleDirectionsData;
    }

    @Override
    protected void onPostExecute(String s) {
        String[] directionsList;

        DataParser parser = new DataParser();
        directionsList = parser.parseDirections(s);
        displayDirection(directionsList);  //to show polyline(path) on the map
    }

    public void displayDirection(String[] directionsList) {
        int count = directionsList.length;

        for(int i = 0;i<count;i++) {
            /*
            mMap.addPolyline(new PolylineOptions()
                    .addAll(PolyUtil.decode(directionsList[i]))
                    .width(20)
                    .endCap(new RoundCap())
                    .color(Color.rgb(77, 77, 255)));*/

            mMap.addPolyline(new PolylineOptions()
                    .addAll(PolyUtil.decode(directionsList[i]))
                    .startCap(new RoundCap())
                    .width(8)
                    .endCap(new RoundCap())
                    .color(Color.rgb(128, 128, 255)));}
    }
}


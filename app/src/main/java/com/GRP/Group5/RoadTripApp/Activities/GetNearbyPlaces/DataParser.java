package com.GRP.Group5.RoadTripApp.Activities.GetNearbyPlaces;

import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by Yi Shen on 25/02/2018.
 */
public class DataParser {

    private HashMap<String,String[]> getPlaceDetail(JSONObject googlePlaceDetailJson) {
        HashMap<String,String[]> googlePlaceDetailMap = new HashMap<>();
        String[] place_name = new String[1];
        String[] rating = new String[1];
        String[] address = new String[1];
        String[] phone_number = new String[1];
        String[] website = new String[1];
        String[] weekdayHours = new String[1];
        String[] openOrnot = new String[1];

        try {
            if (!googlePlaceDetailJson.isNull("name")) {
                place_name[0] = googlePlaceDetailJson.getString("name");
                googlePlaceDetailMap.put("place_name", place_name);
            }
            if (!googlePlaceDetailJson.isNull("rating")) {
                rating[0] = googlePlaceDetailJson.getString("rating");
                googlePlaceDetailMap.put("rating", rating);
            }
            if (!googlePlaceDetailJson.isNull("formatted_address")) {
                address[0] = googlePlaceDetailJson.getString("formatted_address");
                googlePlaceDetailMap.put("address", address);
            }
            if (!googlePlaceDetailJson.isNull("formatted_phone_number")) {
                phone_number[0] = googlePlaceDetailJson.getString("formatted_phone_number");
                googlePlaceDetailMap.put("phone_number", phone_number);
            }
            if (!googlePlaceDetailJson.isNull("website")) {
                website[0] = googlePlaceDetailJson.getString("website");
                googlePlaceDetailMap.put("website", website);
            }
            if (!googlePlaceDetailJson.isNull("photos")) {
                JSONArray photosArray = googlePlaceDetailJson.getJSONArray("photos");

                //System.out.println("photo length:    "+ photosArray.length());
                String[] photo = new String[photosArray.length()];

                for (int i = 0; i< photosArray.length(); i++){
                    photo[i] = photosArray.getJSONObject(i).getString("photo_reference");
                    //System.out.println(photo[i]);
                    googlePlaceDetailMap.put("photos", photo);
                }
            }
            if (!googlePlaceDetailJson.isNull("opening_hours")) {
                openOrnot[0] = String.valueOf(googlePlaceDetailJson.getJSONObject("opening_hours").getBoolean("open_now"));
                JSONArray weekday_text_array = googlePlaceDetailJson.getJSONObject("opening_hours").getJSONArray("weekday_text");
                weekdayHours[0] = "";

                for(int i = 0; i < weekday_text_array.length(); i++){
                    if(i == weekday_text_array.length()){
                        weekdayHours[0] += weekday_text_array.get(i);
                    }
                    else{
                        weekdayHours[0] += weekday_text_array.get(i) + "\n";
                    }
                }
                //System.out.println("open now? " + openOrnot);
                //System.out.println("weekday hour: " +weekday_text_array.length());
                //System.out.println("weekday hour =  " + weekdayHours[0]);

                googlePlaceDetailMap.put("openOrNot", openOrnot);
                googlePlaceDetailMap.put("weekDayHours", weekdayHours);
            }
            else{
                //System.out.println("opening hour: empty " );
            }

        }
        catch (JSONException e) {
            e.printStackTrace();
        }

        return googlePlaceDetailMap;
    }

    private HashMap<String,String> getDuration(JSONArray googleDirectionsJson) {
        HashMap<String,String> googleDirectionsMap = new HashMap<>();
        String duration;
        String distance;

        try {
            duration = googleDirectionsJson.getJSONObject(0).getJSONObject("duration").getString("text");
            distance = googleDirectionsJson.getJSONObject(0).getJSONObject("distance").getString("text");

            googleDirectionsMap.put("duration" , duration);
            googleDirectionsMap.put("distance", distance);
        }
        catch (JSONException e) {
            e.printStackTrace();
        }

        return googleDirectionsMap;
    }

    private HashMap<String, String> getPlace(JSONObject googlePlaceJson) {
        HashMap<String, String> googlePlaceMap = new HashMap<>();
        String placeName;
        String vicinity;
        String latitude;
        String longitude;
        String reference;
        String place_id;

        try {
            if (!googlePlaceJson.isNull("name")) {
                placeName = googlePlaceJson.getString("name");
                googlePlaceMap.put("place_name", placeName);
            }
            if (!googlePlaceJson.isNull("vicinity")) {
                vicinity = googlePlaceJson.getString("vicinity");
                googlePlaceMap.put("vicinity", vicinity);
            }
            if (!googlePlaceJson.isNull("place_id")) {
                place_id = googlePlaceJson.getString("place_id");
                googlePlaceMap.put("place_id", place_id);
            }

            latitude = googlePlaceJson.getJSONObject("geometry").getJSONObject("location").getString("lat");
            googlePlaceMap.put("lat", latitude);

            longitude = googlePlaceJson.getJSONObject("geometry").getJSONObject("location").getString("lng");
            googlePlaceMap.put("lng", longitude);

            reference = googlePlaceJson.getString("reference");
            googlePlaceMap.put("reference", reference);
        }
        catch (JSONException e) {
            e.printStackTrace();
        }
        return googlePlaceMap;
    }

    private List<HashMap<String, String>>getPlaces(JSONArray jsonArray) {
        int count = jsonArray.length();
        List<HashMap<String, String>> placelist = new ArrayList<>();
        HashMap<String, String> placeMap;

        for(int i = 0; i<count;i++) {
            try {
                placeMap = getPlace((JSONObject) jsonArray.get(i));
                placelist.add(placeMap);
            }
            catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return placelist;
    }

    public HashMap<String, String[]> parsePlaceDetail(String jsonData) {
        JSONObject jsonObject = null;

        try {
            jsonObject = new JSONObject(jsonData).getJSONObject("result");
        }
        catch (JSONException e) {
            e.printStackTrace();
        }
        return getPlaceDetail(jsonObject);
    }

    public List<HashMap<String, String>> parse(String jsonData) {
        JSONArray jsonArray = null;
        JSONObject jsonObject;

        //Log.d("json data", jsonData);

        try {
            jsonObject = new JSONObject(jsonData);
            jsonArray = jsonObject.getJSONArray("results");

            return getPlaces(jsonArray);
        } catch (JSONException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public String[] parseDirections(String jsonData)
    {
        JSONArray jsonArray = null;
        JSONObject jsonObject;

        try {
            jsonObject = new JSONObject(jsonData);
            jsonArray = jsonObject.getJSONArray("routes").getJSONObject(0).getJSONArray("legs").getJSONObject(0).getJSONArray("steps");
        }
        catch (JSONException e) {
            e.printStackTrace();
        }
        return getPaths(jsonArray);
    }

    public String[] getPaths(JSONArray googleStepsJson ) {
        int count = googleStepsJson.length();
        String[] polylines = new String[count];

        for(int i = 0;i<count;i++) {
            try {
                polylines[i] = getPath(googleStepsJson.getJSONObject(i));
            }
            catch (JSONException e) {
                e.printStackTrace();
            }
        }

        return polylines;
    }

    public String getPath(JSONObject googlePathJson) {
        String polyline = "";
        try {
            polyline = googlePathJson.getJSONObject("polyline").getString("points");
        }
        catch (JSONException e) {
            e.printStackTrace();
        }
        return polyline;
    }
}

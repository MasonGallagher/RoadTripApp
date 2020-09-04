package com.GRP.Group5.RoadTripApp.Scheduling;

import android.support.annotation.NonNull;

import com.GRP.Group5.RoadTripApp.utils.Functions;
import com.google.android.gms.maps.model.LatLng;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by Ben Clark on 09/04/2018.
 */

public class SchedulablePlace implements Serializable {

    private static int[][] OPEN_ALL_DAY = new int[][]{{0, 24 * 60}};
    private LatLng location;
    private List<int[][]> openingTimes; //time in mins
    private String name;
    private SchedulerType type;

    public SchedulablePlace(@NonNull LatLng loc,@NonNull String name){
        this(loc, name, SchedulerType.TIME, null);
    }

    public SchedulablePlace(@NonNull LatLng loc, @NonNull String name, @NonNull SchedulerType type){
        this(loc, name, type, null);
    }

    public SchedulablePlace(@NonNull LatLng loc,@NonNull String name,@NonNull SchedulerType type, List<int[][]> times){
        this.location = loc;
        this.name = name;
        this.type = type;
        this.openingTimes = times;


        if (this.openingTimes == null){
            this.openingTimes = new LinkedList<>();
            for(int i = 0; i < 7; i++) {
                this.openingTimes.add(i, this.OPEN_ALL_DAY);
            }
        }

        if(this.type == null){
            this.type = SchedulerType.TIME;
        }
    }

    /**
     * returns true if the place is open at a given time and day
     * @param day - 0 = monday
     * @param time - in minutes
     * @return
     */
    public boolean isOpen(int day, int time) {
        if(this.openingTimes.get(day) == null){
            return false;
        }
        //loops though the day to check the opening times
        for (int i = 0; i < this.openingTimes.get(day).length; i++) {
            int open = this.openingTimes.get(day)[i][0];
            int close = this.openingTimes.get(day)[i][1];

            if (time <= close && time >= open) {
                return true;
            }
        }
        return false;
    }

    /**
     * returns the location of this place
     * @return
     */
    public LatLng getLocation() {
        return this.location;
    }

    /**
     * returns the opening times for all days
     * @return
     */
    public List<int[][]> getOpeningTimes() {
        return this.openingTimes;
    }

    /**
     * returns the open times for a given day
     * @param day 0 = monday
     * @return
     */
    public int[][] getOpeningTimes(int day) {
        return this.openingTimes.get(day);
    }

    /**
     * returns the name of the place
     * @return
     */
    public String getName() {
        return this.name;
    }

    /**
     * returns the type that the place is - lodge, food or attraction
     * @return
     */
    public SchedulerType getType() {
        return this.type;
    }

    /**
     * returns true if obj is equal to current object
     * @param obj
     * @return
     */
    @Override
    public boolean equals(Object obj) {
        if(obj != null && obj instanceof SchedulablePlace){
            SchedulablePlace place = (SchedulablePlace) obj;
            if((place.getLocation() != null && place.getLocation().equals(this.getLocation())) ||
                    (place.getLocation() == null && this.getLocation() == null)){
                if(this.checkOpeningTimesEqual(place)) {
                    if(place.getName() == place.getName()){
                        if(place.getType() == this.getType()){
                            return true;
                        }
                    }
                }
            }
        }
        return false;
    }

    /**
     * Checks that two opening times are equal
     * @param place second schedulablePlace
     * @return true if they are equal
     */
    private boolean checkOpeningTimesEqual(SchedulablePlace place) {
        for(int day = 0; day < 7; day++){
            int[][] time1 = this.getOpeningTimes(day);
            int[][] time2 = place.getOpeningTimes(day);

            if(time1.length != time2.length){
                return false;
            }
            for(int i = 0; i < time1.length; i++){
                int start1 = time1[i][0];
                int stop1 = time1[i][1];

                int start2 = time2[i][0];
                int stop2 = time2[i][1];

                if (start1 != start2 || stop1 != stop2) {
                    return false;
                }
            }
        }
        return true;
    }

    /**
     * gets the has code of openingTimesList
     * @return returns the hashcode of the values in the opening times
     */
    private int openingTimeHashCode() {
        int hash = 17;
        for(int day = 0; day < 7; day++){
            int[][] time1 = this.getOpeningTimes(day);

            for(int i = 0; i < time1.length; i++){
                int start = time1[i][0];
                int stop = time1[i][1];

                hash = hash * 31 + start;
                hash = hash * 31 + stop;
            }
        }
        return hash;
    }

    /**
     * returns the hashcode of the current object
     * @return
     */
    @Override
    public int hashCode() {
        int hash = 17;
        hash = hash * 31 + this.getLocation().hashCode();
        hash = hash * 31 + this.openingTimeHashCode();
        hash = hash * 31 + this.getName().hashCode();
        hash = hash * 31 + this.getType().getID();

        return hash;
    }

    /**
     * converts the current object into a json object
     * Allows the object to passed thoughout the system without issues
     * also allows for the object to be transmitted via the internet
     * @return
     * @throws JSONException
     */
    public JSONObject toJSON() throws JSONException {
        JSONObject root = new JSONObject();

        //adds base variables to root
        root.put("name", this.getName());
        root.put("type", this.type != null ? this.getType().getID() : -1);
        root.put("latitude", this.getLocation().latitude);
        root.put("longitude", this.getLocation().longitude);

        JSONObject allOpenClose = new JSONObject();

        //loops doubly though the list of 2D arrays to get all the periods that the place
        //is open and adds them to the JSON appropiately
        for(int i = 0; i < this.getOpeningTimes().size(); i++) {
            allOpenClose.put("openCloseLength", this.getOpeningTimes(i).length);
            JSONObject openClose = new JSONObject();
            for (int p = 0; p < this.getOpeningTimes(i).length; p++) {
                JSONObject period = new JSONObject();
                int start = this.getOpeningTimes(i)[p][0];
                int stop = this.getOpeningTimes(i)[p][1];

                period.put("start", start);
                period.put("stop", stop);
                openClose.put("period-" + p, period);
            }
            allOpenClose.put(this.getDay(i), openClose);
        }

        root.put("allOpenClose", allOpenClose);
        return root;
    }

    /**
     * static function that creates a SchedulablePlace object from a JSON object
     * @param root
     * @return
     * @throws JSONException
     */
    public static SchedulablePlace fromJSON(JSONObject root) throws JSONException {
        //loads base variables
        String name = root.getString("name");
        SchedulerType type = SchedulerType.fromID(root.getInt("type"));
        double latitude = root.getDouble("latitude");
        double longitude = root.getDouble("longitude");

        List<int[][]> opening = new LinkedList<>();
        JSONObject allOpenClose = root.getJSONObject("allOpenClose");

        //Just like in toJSON loops doubly, using provided variables to load in the open and close times
        //in the reverse of how it was done to add the format to a JSON
        for(int d = 0; d < 7; d++) {
            int openCloseLength = allOpenClose.getInt("openCloseLength");
            int[][] subOpening = new int[openCloseLength][];
            JSONObject openClose = allOpenClose.getJSONObject(getDay(d));

            for (int i = 0; i < openCloseLength; i++) {
                JSONObject period = openClose.getJSONObject("period-" + i);
                int start = period.getInt("start");
                int stop = period.getInt("stop");

                subOpening[i] = new int[]{start, stop};
            }
            opening.add(d, subOpening);
        }
        //creates and returns a new object
        return new SchedulablePlace(new LatLng(latitude, longitude), name, type, opening);
    }

    /**
     * gets the text version of the day, given the index of the day
     * @param i day index - 0 = monday
     * @return
     */
    private static String getDay(int i) {
        switch (i) {
            case 0: {
                return "monday";
            }
            case 1: {
                return "tuesday";
            }
            case 2: {
                return "wednesday";
            }
            case 3: {
                return "thursday";
            }
            case 4: {
                return "friday";
            }
            case 5: {
                return "saturday";
            }
            case 6: {
                return "sunday";
            }
            default:
                return "unknown";
        }
    }

    /**
     * converts the object into a string
     * @return
     */
    @Override
    public String toString() {
        //eg
        //[  Food  ]        Lincoln Castle
        return "[ " + Functions.toTitleCase(fitTypeName(this.getType())) + " ]\t\t" + this.name;
    }

    /**
     * fits the scheduler type name into a specific size
     * allows for correct text formatting
     * @param type
     * @return
     */
    public String fitTypeName(SchedulerType type){
        //gets the max length that any type (in SchedulerTypes) could be
        int maxLength = 0;
        for(SchedulerType t : SchedulerType.values()){
            maxLength = Math.max(maxLength, t.getName().length());
        }

        //checks if this is shorter or longer than the longest type
        if(type.getName().length() < maxLength) {
            return "\t\t" + type.getName() + "\t\t";
        }
        return "\t" + type.getName() + "\t";
    }
}

package com.GRP.Group5.RoadTripApp.utils;

import android.location.Location;

import com.GRP.Group5.RoadTripApp.Scheduling.SchedulablePlace;
import com.GRP.Group5.RoadTripApp.Scheduling.Scheduler;
import com.google.android.gms.maps.model.LatLng;

import org.json.JSONException;
import org.json.JSONObject;

import java.security.InvalidParameterException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by Yi Shen on 2018/3/29.
 */

public class Route{

    private String name;

    private String ID;

    private SchedulablePlace[] places;

    private Double totalDistance;
    private Double totalTime;
    private int pictureId;

    //Sub-Scheduler Variables
    private int startTime = 12*60;
    private Date date;
    private HashMap<SchedulablePlace, Plan> locationPlans;
    private HashMap<SchedulablePlace, List<SchedulablePlace>> subLocations;
    private List<SchedulablePlace> lockedPlaces;

    /**
     * Initialise for Route
     * Time isn't passed here, but MUST be passed into the object at some point
     * Typically done after the first scheduler
     * @param head name of the route
     * @param places places (in order) that the route will be made up of
     * @param totalDistance total distance (by road) of the route
     * @param totalTime total time (by car) of the route
     * @param startDate date that the route will start on
     */
    public Route(String head, SchedulablePlace[] places, double totalDistance, double totalTime, Date startDate){
        this.name = head;
        this.places = places;
        this.totalDistance = totalDistance;
        this.totalTime = totalTime;
        this.date = startDate;

        this.locationPlans = new HashMap<>();
        this.subLocations = new HashMap<>();
        this.lockedPlaces = new LinkedList<>();
    }

    /**
     * Gets the name of the route
     * @return string value of the name of the route
     */
    public String getName() {return this.name;}

    /**
     * gets the total distance of the route
     * @return double value of the distance
     */
    public Double getTotalDistance() {return this.totalDistance;}

    /**
     * gets total time of the route - by car
     * @return double value of time, in hours
     */
    public Double getTotalTime() {return this.totalTime;}

    /**
     * generates the description of the route
     * @return description of the route
     */
    public String getDesc() {
        String desc = "";

        if(places.length > 0){
            for(int i = 0; i< places.length; i++){
                if(i == places.length -1){
                    desc += places[i].getName();
                } else {
                    desc += places[i].getName() + " -> ";
                }
            }
            return desc;
        }
        return "";
    }

    /**
     * Gets the places, in order, that make up the route
     * @return array of places that make up the route
     */
    public SchedulablePlace[] getPlaces() {
        return this.places;
    }

    /**
     * sets the name of the route
     * @param name value to become the name
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * converts the route object into a JSON object
     * this allows the object to be sent more easily between different activities and sent to the backend
     * @return json object that can be converted to a string
     * @throws JSONException thrown if there is error reading the json format
     */
    public JSONObject toJSON() throws JSONException {
        JSONObject root = new JSONObject();

        root.put("name", this.name);

        if(this.ID != null){
            root.put("id", this.ID);
        }

        root.put("totalTime", this.getTotalTime());
        root.put("totalDistance", this.getTotalDistance());

        SimpleDateFormat ft = new SimpleDateFormat ("dd-MM-yyyy");
        root.put("date", ft.format(this.date));

        root.put("numberPlaces", this.getPlaces().length);

        JSONObject places = new JSONObject();

        for(int i = 0; i < this.getPlaces().length; i++){
            JSONObject place = this.getPlaces()[i].toJSON();
            places.put("place-" + i, place);
        }

        root.put("places", places);

        //new stuff

        root.put("startTime", this.startTime);

        root.put("planSize", this.locationPlans.size());

        JSONObject plans = new JSONObject();

        for(int i = 0; i < this.locationPlans.size(); i++){
            SchedulablePlace place = (SchedulablePlace) this.locationPlans.keySet().toArray()[i];
            Plan plan = (Plan) this.locationPlans.values().toArray()[i];

            JSONObject entry = new JSONObject();
            entry.put("key", place.toJSON());
            entry.put("value", plan.toJSON());

            plans.put("entry-" + i, entry);
        }

        root.put("plans", plans);

        //NEW STUFF AGAIN

        //HashMap<SchedulablePlace, List<SchedulablePlace>> subLocations;

        JSONObject subLocationJSON = new JSONObject();

        for(int i = 0; i < this.getPlaces().length; i++){
            List<SchedulablePlace> linkedPlaces = this.subLocations.get(this.getPlaces()[i]);
            if(linkedPlaces != null && linkedPlaces.size() > 0){
                JSONObject list = new JSONObject();
                list.put("size", linkedPlaces.size());
                for(int p = 0; p < linkedPlaces.size(); p++){
                    list.put("place-" + p, linkedPlaces.get(p).toJSON());
                }
                subLocationJSON.put("keyValue-" + i, list);
            }
        }
        root.put("subLocations", subLocationJSON);

        //even more new things

        JSONObject locked = new JSONObject();

        locked.put("size", this.lockedPlaces.size());

        for(int i = 0; i < this.lockedPlaces.size(); i++){
            locked.put("place-" + i, this.lockedPlaces.get(i).toJSON());
        }

        root.put("locked", locked);

        return root;
    }

    /**
     * Converts a json object into a route object
     * @param root json object that is to be converted into a route object
     * @return a valid route object, from the json object
     * @throws JSONException thrown if there is an error with reading the json format
     * @throws ParseException thrown if there is an error with the parsing of the date when loading from the json
     */
    public static Route fromJSON(JSONObject root) throws JSONException, ParseException {

        String name = root.getString("name");
        double time = root.getDouble("totalTime");
        double distance = root.getDouble("totalDistance");

        String id = null;
        if (root.has("id")){
            id = root.getString("id");
        }

        SimpleDateFormat ft = new SimpleDateFormat("dd-MM-yyyy");
        Date date = ft.parse(root.getString("date"));

        int numPlaces = root.getInt("numberPlaces");

        SchedulablePlace[] schedulablePlaces = new SchedulablePlace[numPlaces];

        JSONObject places = root.getJSONObject("places");

        for(int i = 0; i < numPlaces; i++){
            schedulablePlaces[i] = SchedulablePlace.fromJSON(places.getJSONObject("place-" + i));
        }

        //new stuff

        Route route = new Route(name, schedulablePlaces, time, distance, date);

        route.startTime = root.getInt("startTime");

        int planSize = root.getInt("planSize");

        JSONObject plans = root.getJSONObject("plans");

        for(int i = 0; i < planSize; i++){
            JSONObject entry = plans.getJSONObject("entry-" + i);
            SchedulablePlace key = SchedulablePlace.fromJSON(entry.getJSONObject("key"));
            Plan value = Plan.fromJSON(entry.getJSONObject("value"));

            route.addPlan(key, value);
        }

        //MORE NEW STUFF

        JSONObject subLocationJSON = root.getJSONObject("subLocations");

        for(int i = 0; i < route.getPlaces().length; i++){
            if(subLocationJSON.has("keyValue-" + i)) {
                JSONObject list = subLocationJSON.getJSONObject("keyValue-" + i);
                int size = list.getInt("size");
                for (int p = 0; p < size; p++) {
                    route.addSubLocation(route.getPlaces()[i], SchedulablePlace.fromJSON(list.getJSONObject("place-" + p)));
                }
            }
        }

        //more stuff

        JSONObject locked = root.getJSONObject("locked");

        int lockedSize = locked.getInt("size");

        for(int i = 0; i < lockedSize; i++){
            route.lockPlace(SchedulablePlace.fromJSON(locked.getJSONObject("place-" + i)));
        }

        return route;
    }

    /**
     * gets the start time of the route
     * @return integer start time
     */
    public int getStartTime() {
        return this.startTime;
    }

    /**
     * This function gets the closest place, to the sub place, that is a main point on the route
     * @param child sub place - not a main location
     * @return main location that is closest to the child
     */
    public SchedulablePlace getClosestRoutePlace(SchedulablePlace child) {
        return getClosestRoutePlace(child.getLocation());
    }

    /**
     * This function gets the closest place, to the sub place, that is a main point on the route
     * @param loc location to get the closest place of
     * @return main location that is closest to the child
     */
    public SchedulablePlace getClosestRoutePlace(LatLng loc) {
        double smallestDistance = Double.MAX_VALUE;
        SchedulablePlace closest = null;

        for(int i = 0; i < this.getPlaces().length; i++){

            float distance = Functions.getDistanceBetween(loc, this.getPlaces()[i].getLocation());

            if(distance < smallestDistance){
                smallestDistance = distance;
                closest = this.getPlaces()[i];
            }
        }
        return closest;
    }

    /**
     * Adds a plan to the route for a specific main location
     * @param schedulablePlace specific main location - should be in this.getPlaces()
     * @param plan plan to be assigned to this location
     */
    public void addPlan(SchedulablePlace schedulablePlace, Plan plan) {
        this.locationPlans.put(schedulablePlace, plan);
    }

    /**
     * Gets a list / collection of all the plans for every location without the location link
     * @return collection of plans
     */
    public Collection<Plan> getPlans() {
        return this.locationPlans.values();
    }

    /**
     * Gets the hashmap of plans, linked with the location that they apply to
     * @return hashmap showing the plan for each location
     */
    public HashMap<SchedulablePlace, Plan> getPlanLink() {
        return this.locationPlans;
    }

    /**
     * Adds a smaller place that the user wants to visit while at a main location
     * @param parent main location that is generally where the user 'is'
     * @param child smaller place that the user wants to visit
     */
    public void addSubLocation(SchedulablePlace parent, SchedulablePlace child) {
        if (Scheduler.toList(this.getPlaces()).contains(parent)) {
            List<SchedulablePlace> currentSet = this.subLocations.get(parent);
            if (currentSet == null) {
                currentSet = new LinkedList<>();
            }
            currentSet.add(child);
            this.subLocations.put(parent, currentSet);
        } else {
            throw new InvalidParameterException("Invalid parent");
        }
    }

    /**
     * removes the smaller place from the list of "visiting places" that that user wants to visit
     * given the general place, parent, that the user is at
     * @param parent main location
     * @param child sub location that they wish to visit
     */
    public void removeSubLocation(SchedulablePlace parent, SchedulablePlace child) {
        if (Scheduler.toList(this.getPlaces()).contains(parent)) {
            List<SchedulablePlace> currentSet = this.subLocations.get(parent);
            currentSet.remove(child);
        } else {
            throw new InvalidParameterException("Invalid parent");
        }
    }

    /**
     * gets a hashmap of lists that details the smaller places (sub places) that the user wants
     * to visit while at a larger, main location
     * @return hashmap with key of the mail location, value as the list of sub places they want to visit
     */
    public HashMap<SchedulablePlace, List<SchedulablePlace>> getSubPlaces() {
        return this.subLocations;
    }

    /**
     * Checks if a general place is locked from being scheduled using the sub scheduler
     * @param place general location to be checked
     * @return true if its locked, false otherwise
     */
    public boolean isPlaceLocked(SchedulablePlace place){
        return this.lockedPlaces.contains(place);
    }

    /**
     * Locks a place so that the sub-scheduler cannot recreate a plan for that general location
     * @param place place to be locked
     */
    public void lockPlace(SchedulablePlace place){
        if (Scheduler.toList(this.getPlaces()).contains(place)) {
            if(!this.isPlaceLocked(place)){
                this.lockedPlaces.add(place);
            }
        } else {
            throw new InvalidParameterException("Invalid place");
        }
    }

    /**
     * Unlocks a place so that the sub-scheduler can recreate a plan for that general place
     * @param place place to unlocked
     */
    public void unlockPlace(SchedulablePlace place){
        this.lockedPlaces.remove(place);
    }

    /**
     * Gets the date that this route starts on
     * @return start date of route
     */
    public Date getDate() {
        return this.date;
    }

    /**
     * sets the start time of the route
     * THIS MUST BE CALLED - as there is no parameter in the object init
     * @param startTime time in minutes to set the start time too
     */
    public void setStartTime(int startTime) {
        this.startTime = startTime;
    }

    public void setID(String id){
        this.ID = id;
    }

    public String getID(){
        return this.ID;
    }
}

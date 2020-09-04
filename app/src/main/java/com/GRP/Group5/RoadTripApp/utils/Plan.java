package com.GRP.Group5.RoadTripApp.utils;

import com.GRP.Group5.RoadTripApp.Scheduling.SchedulablePlace;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by Ben Clark on 10/04/2018.
 */

public class Plan {

    int id;
    double evalutation;
    int startTime;
    int duration;
    Date startDate; //NOT IN JSON
    List<Tuple<SchedulablePlace, Integer>> plan;

    public Plan(int id, List<Tuple<SchedulablePlace, Integer>> plan, int duration, int startTime, double eval, Date date){
        this.id = id;
        this.plan = plan;
        this.evalutation = eval;
        this.duration = duration;
        this.startTime = startTime;
        this.startDate = date;
    }

    /**
     * Creates a plan from passed variables
     * @param id permutation index of the plan
     * @param hourlyPlan hourly plan generated by the generator
     * @param places list of all places that should be visited - not just the ones in the hourly plan
     * @param startTime time that this plan will start
     * @param startDate data that this plan will start
     * @return a valid plan
     */
    public static Plan convert(int id, SchedulablePlace[] hourlyPlan, List<SchedulablePlace> places, int startTime, Date startDate){
        //Converts the hourly plan into the correct format - list of tuples of a place and integer
        List<Tuple<SchedulablePlace, Integer>> plan = new LinkedList<>();
        SchedulablePlace lastPlace = null;
        int duration = 0;
        //iterates though the array finding the time that each place starts, then adding that to the list
        //also calcuates plan duration by getting the final item to be added to the list then adding the minimum time
        //that it can be scheduled for onto the end
        for(int i = 0; i < hourlyPlan.length; i++){
            if(hourlyPlan[i] != lastPlace){
                lastPlace = hourlyPlan[i];
                plan.add(new Tuple<>(hourlyPlan[i], i));
                if(hourlyPlan[i] != null) {
                    duration = i + hourlyPlan[i].getType().getMinTime();
                }
            }
        }
        double count = 0;

        //counts the number of places, from the list of all places that SHOULD be added, that made it
        //into the final plan - this will be used to calculate the evaluation of the plan
        for(Tuple<SchedulablePlace, Integer> pair : plan){
            if(places.contains(pair.a)) {
                count++;
            }
        }

        //creates and returns the object
        return new Plan(id, plan, duration, startTime, count / places.size(), startDate);
    }

    /**
     * gets the evaluation value of the object
     * @return value between 1 and 0
     */
    public double evaluate() {
        return this.evalutation;
    }

    /**
     * creates the hashcode for the plan
     * @return integer value
     */
    @Override
    public int hashCode() {
        int hash = 17;
        hash = hash * 31 + this.startTime;
        hash = hash * 31 + (this.startDate != null ? this.startDate.hashCode() : 0);

        if(this.plan != null){
            for(int i = 0; i < this.plan.size(); i++){
                hash = hash * 31 + this.plan.get(i).hashCode();
            }
        }
        return hash;
    }

    /**
     * checks if an object is equal to this object
     * @param obj object to check
     * @return true if they are equal
     */
    @Override
    public boolean equals(Object obj) {
        if(obj instanceof Plan){
            Plan plan = (Plan) obj;
            if((plan.plan != null && plan.plan.equals(this.plan)) || (plan.plan == null && this.plan == null)){
                if(plan.startTime == this.startTime){
                    return plan.duration == this.duration && plan.startDate == this.startDate;
                }
            }
            return plan.toPlanString().equals(this.toPlanString());
        }
        return false;
    }

    /**
     * gets the duration in hours of this plan
     * @return duration of plan in hours
     */
    public int getDuration() {
        return this.duration;
    }

    /**
     * converts this plan to a string
     * @return string representation of this plan
     */
    @Override
    public String toString() {
        String str = "Location Plan #" + this.id;
        str += "\nDuration: " + this.duration;
        str += "\nPlan: ";

        for(Tuple<SchedulablePlace, Integer> loc : this.plan){
            if(loc.a != null) {
                str += "\n\t- " + loc.b + " - " + loc.a.toString();
            }
        }
        return str + "\n";
    }

    /**
     * displays the time based plan, for this plan as a string
     * @return string representing the time that each place will be visited
     */
    public String toPlanString() {
        String str = "";

        SimpleDateFormat ft = new SimpleDateFormat ("E dd");

        //loops thought the plan
        for(Tuple<SchedulablePlace, Integer> loc : this.plan){
            if(loc.a != null) {
                //gets the start time and gets the number of days that this plans spans
                int time = loc.b + this.startTime; //time in hours
                int dayCount = 0;
                while(time >= 24){
                    time -= 24;
                    dayCount++;
                }

                //increments the date in correspondence to the current event
                Calendar c = Calendar.getInstance();
                c.setTime(this.startDate);
                c.add(Calendar.DATE, dayCount);

                //creates the string representation in the format
                //DAY DAY-DATE  TIME    NAME-OF-PLACE
                if(String.valueOf(time).toCharArray().length == 2) {
                    str += "\n" + ft.format(c.getTime()) + "\t\t" + time + ":00\t\t" + loc.a.toString();
                } else {
                    str += "\n" + ft.format(this.startDate) + "\t\t0" + time + ":00\t\t" + loc.a.toString();
                }
            }
        }
        return str;
    }

    /**
     * Converts this object into a JSON object
     * @return returns a json object that represents this object
     * @throws JSONException thrown if there is an error writing to the json format
     */
    public JSONObject toJSON() throws JSONException {
        JSONObject root = new JSONObject();

        root.put("id", this.id);
        root.put("evaluation", this.evalutation);
        root.put("duration", this.duration);
        root.put("startTime", this.startTime);
        root.put("planSize", this.plan.size());

        SimpleDateFormat ft = new SimpleDateFormat ("dd-MM-yyyy");
        root.put("date", ft.format(this.startDate));

        JSONObject planJSON = new JSONObject();

        for(int i = 0; i < this.plan.size(); i++){
            Tuple<SchedulablePlace, Integer> point = this.plan.get(i);
            if(point.a != null) {
                JSONObject tuple = new JSONObject();

                tuple.put("place", point.a.toJSON());
                tuple.put("time", point.b);

                planJSON.put("tuple-" + i, tuple);
            }
        }

        root.put("plans", planJSON);

        return root;
    }

    /**
     * Creates a plan object from a json object
     * @param root json object that will respresent a plan object
     * @return a plan object that was generated from the root
     * @throws JSONException thrown if there are errors reading the JSON  format
     * @throws ParseException thrown if there is an error parsing the date format from the json
     */
    public static Plan fromJSON(JSONObject root) throws JSONException, ParseException {
        int id = root.getInt("id");
        double eval = root.getDouble("evaluation");
        int duration = root.getInt("duration");
        int startTime = root.getInt("startTime");
        int size = root.getInt("planSize");

        SimpleDateFormat ft = new SimpleDateFormat("dd-MM-yyyy");
        Date date = ft.parse(root.getString("date"));

        JSONObject plansObj = root.getJSONObject("plans");
        List<Tuple<SchedulablePlace, Integer>> list = new LinkedList<>();

        for (int i = 0; i < size; i++) {
            if(plansObj.has("tuple-" + i)) {
                JSONObject tuple = plansObj.getJSONObject("tuple-" + i);
                SchedulablePlace place = SchedulablePlace.fromJSON(tuple.getJSONObject("place"));
                int time = tuple.getInt("time");
                list.add(new Tuple<>(place, time));
            }
        }

        return new Plan(id, list, duration, startTime, eval, date);
    }

    /**
     * gets the permutation iteration of the plan ie the id
     * @return and integer value - max value 10,420
     */
    public int getID() {
        return this.id;
    }

    /**
     * Checks if this plan is contained in the exclusion list
     * @param exclusion list to be compared against
     * @return true if this plan is contained in the list
     */
    public boolean isIn(List<Plan> exclusion) {
        for(Plan p : exclusion){
            if(this.equals(p)){
                return true;
            }
        }
        return false;
    }

    /**
     * Gets a list of places within the plan
     * @return list of place a that are within the plan
     */
    public List<SchedulablePlace> getPlaces(){
        List<SchedulablePlace> places = new LinkedList<>();
        for(Tuple<SchedulablePlace, Integer> tuple : this.plan){
            if(tuple.a != null) {
                places.add(tuple.a);
            }
        }
        return places;
    }
}
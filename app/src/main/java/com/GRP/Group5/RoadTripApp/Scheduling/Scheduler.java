package com.GRP.Group5.RoadTripApp.Scheduling;

import android.os.AsyncTask;
import android.support.annotation.NonNull;

import com.GRP.Group5.RoadTripApp.utils.HTTPAccess;
import com.GRP.Group5.RoadTripApp.utils.Plan;
import com.GRP.Group5.RoadTripApp.utils.Route;
import com.GRP.Group5.RoadTripApp.utils.Tuple;
import com.google.android.gms.maps.model.LatLng;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by Ben on 01/12/2017.
 */

public class Scheduler {

    //Key to access Google Distance Matrix
    private static final String googleAPIKey = "AIzaSyB1gamPoZReCOdC6d3xGo0Q9RzKjD3Uq6M";

    //Locations passed by user
    private ArrayList<SchedulablePlace> locations;

    //Start and end locations passed by user
    private SchedulablePlace startLocation, endLocation;

    //List of all possible routes
    private List<SchedulablePlace[]> routes;

    private Date startDate;

    private static List<SchedulerEvent.SchedulerEventListener> listenersStatic = new LinkedList<>();;

    private List<Route> schedules;

    private Route currentRoute;

    private static SubScheduleTask subTask;
    private static ScheduleTask task;

    private static Scheduler staticScheduler;
    private List<Plan> exclusionPlans;

    //region Inits and listeners

    public Scheduler(Route currentRoute) {
        this(currentRoute, toList(currentRoute.getPlaces()), currentRoute.getPlaces()[0], currentRoute.getPlaces()[currentRoute.getPlaces().length - 1], currentRoute.getDate());
    }

    /**
     * Initialisation of scheduler
     *
     * @param latLngPairs - list of all locations
     * @param start       - start location
     * @param end         - end location
     */
    public Scheduler(ArrayList<SchedulablePlace> latLngPairs, SchedulablePlace start, SchedulablePlace end, Date startDate) {
        this(null, latLngPairs, start, end, startDate);
    }

    /**
     * initialise the scheduler
     * @param route the route, that has already been defined
     * @param latLngPairs all the main locations the user wants to visit
     * @param start the start location
     * @param end ethe end location
     * @param startDate date that the trip starts on
     */
    private Scheduler(Route route, ArrayList<SchedulablePlace> latLngPairs, SchedulablePlace start, SchedulablePlace end, Date startDate){
        this.currentRoute = route;
        this.locations = latLngPairs;
        this.startLocation = start;
        this.endLocation = end;
        this.startDate = startDate;
    }

    /**
     * calls all event listeners with the event that is passed
     * @param event event to pass to listeners
     */
    public static void passEventToListeners(@NonNull SchedulerEvent event){
        for(SchedulerEvent.SchedulerEventListener listeners : Scheduler.listenersStatic){
            listeners.onSchedulerComplete(event);
        }
    }

    /**
     * adds a listener to the system
     * @param listener the class that is the listener
     */
    public static void addStaticListener(SchedulerEvent.SchedulerEventListener listener) {
        listenersStatic.add(listener);
    }

    /**
     * Checks if the static tasks are running
     * @return true if either of the tasks are running
     */
    public static boolean isTaskRunning(){
        return (task != null && task.isRunning()) || (subTask != null && subTask.isRunning());
    }

    /**
     * sets the static scheduler variable to the passed variable - used to pass the scheduler between
     * classes
     * @param scheduler the scheduler to be stored
     * @return the scheduler that was stored
     */
    public static Scheduler setStaticScheduler(Scheduler scheduler){
        return staticScheduler = scheduler;
    }

    /**
     * sets the static scheduler to this class
     */
    public void setStaticScheduler(){
        staticScheduler = this;
    }

    /**
     * gets a scheduler object
     * @return scheduler object from static scheduler
     */
    public static Scheduler getStaticScheduler() {
        return staticScheduler;
    }

    /**
     * converts an array of places into an array list - general use
     * @param places array of places
     * @return arraylist of the same places
     */
    public static ArrayList<SchedulablePlace> toList(SchedulablePlace[] places) {
        ArrayList<SchedulablePlace> list = new ArrayList<>();
        for(int i = 0; i < places.length; i++){
            list.add(places[i]);
        }
        return list;
    }

    //endregion

    //region Scheduler

    /**
     * set locations
     *
     * @param locations arraylist of of places
     */
    public void setLocations(ArrayList<SchedulablePlace> locations) {
        this.locations = locations;
    }

    /**
     * Set start location
     *
     * @param startLocation a locations - must be contained within the list of locations that are to be scheduled
     */
    public void setStartLocation(SchedulablePlace startLocation) {
        this.startLocation = startLocation;
    }

    /**
     * Sets end location
     *
     * @param endLocation a location - must be contained within the list of locations that are to be scheduled
     */
    public void setEndLocation(SchedulablePlace endLocation) {
        this.endLocation = endLocation;
    }

    /**
     * Runs the scheduling algorithm
     */
    public void runScheduler() {
        if(this.task == null || (this.task != null && !this.task.isRunning())) {
            this.task = new ScheduleTask();
            this.task.execute(this);
        }
    }

    /**
     * Runs a threaded bersion of the scheduler to allow for network usage
     *
     * @throws IOException error caused by issue with networking
     * @throws JSONException cause by not being able to read the returned json format
     */
    private void threadedScheduler() throws IOException, JSONException {
        //matrix of all possible single jump routes and distnaces
        HashMap<Tuple<LatLng, LatLng>, Tuple<Integer, Integer>> matrix = getHashMatrix();

        //initialisation of possSchedules
        //sets this.routes - to all routes
        this.generatePermutations(this.locations);

        this.schedules = new LinkedList<>();

        //Calculates the duration and distance for every route
        for (int i = 0; i < this.routes.size(); i++) {
            int distance = 0, time = 0;
            for (int x = 0; x < this.routes.get(i).length - 1; x++) {
                LatLng loc1 = this.routes.get(i)[x].getLocation();
                LatLng loc2 = this.routes.get(i)[x + 1].getLocation();

                //get the time and location between the two points
                Tuple<Integer, Integer> t = matrix.get(new Tuple<>(loc1, loc2));
                distance += t.a;
                time += t.b;
            }

            this.schedules.add(new Route("", this.routes.get(i), distance / 1000, time / 60.0F / 60.0F, this.startDate));
        }

        Collections.sort(this.schedules, new Comparator<Route>() {
            @Override
            public int compare(Route route1, Route route2) {
                double i = route1.getTotalDistance() - route2.getTotalDistance();
                if (i >= 0) {
                    return 1;
                } else {
                    return -1;
                }
            }
        });

        this.schedules = this.schedules.subList(0, Math.min(25, this.schedules.size()));

        for (int i = 0; i < this.schedules.size(); i++) {
            this.schedules.get(i).setName("Plan " + i);
        }
    }

    /**
     * Generates all permutations of locations / routes
     *
     * @param arr - array for perms to be generated
     */
    private void generatePermutations(ArrayList<SchedulablePlace> arr) {
        if (this.startLocation != null && this.startLocation.equals(this.endLocation)) {
            arr.add(this.endLocation);
        }
        SchedulablePlace[] array = new SchedulablePlace[arr.size()];
        for (int i = 0; i < arr.size(); i++) {
            array[i] = arr.get(i);
        }
        if (this.routes == null) {
            this.routes = new LinkedList<>();
        }
        generatePermutations(array, 0);
    }

    /**
     * Generates all permutations of locations / routes
     *
     * @param arr   - array for perms to be generated
     * @param index - index to start, used for self calling
     */
    private void generatePermutations(SchedulablePlace[] arr, int index) {

        //If we are at the last element - nothing left to permute
        if (index >= arr.length - 1) {

            //checks that permutation is valid
            boolean canAdd = true;
            if (this.startLocation != null) {
                if (!arr[0].getLocation().equals(this.startLocation.getLocation())) {
                    canAdd = false;
                }
            }

            if (this.endLocation != null) {
                if (!arr[arr.length - 1].getLocation().equals(this.endLocation.getLocation())) {
                    canAdd = false;
                }
            }

            //adds to routes
            if (canAdd) {
                this.routes.add(arr.clone());
            }
            //stopping condition
            return;
        }

        for (int i = index; i < arr.length; i++) { //For each index in the sub array arr[index...end]

            //Swap the elements at indices index and i
            SchedulablePlace t = arr[index];
            arr[index] = arr[i];
            arr[i] = t;

            //Recurse on the sub array arr[index+1...end]
            generatePermutations(arr, index + 1);

            //Swap the elements back
            t = arr[index];
            arr[index] = arr[i];
            arr[i] = t;
        }
    }

    /**
     * Generates a matrix of single jump locations with distances and durations
     *
     * @return - matrix
     * @throws JSONException caused by errors reading the json format
     * @throws IOException caused by network issues
     */
    private HashMap<Tuple<LatLng, LatLng>, Tuple<Integer, Integer>> getHashMatrix() throws JSONException, IOException {
        //Get all distances or times from Google
        HashMap<Tuple<LatLng, LatLng>, Tuple<Integer, Integer>> distancePairs = new HashMap<>();

        //interates though all possible location pairs
        for (int x = 0; x < this.locations.size(); x++) {
            for (int y = this.locations.size() - 1; y >= 0; y--) {
                if (x != y) {
                    //creates variables that store these locations and sets them up for string parsing
                    Tuple<LatLng, LatLng> locaPair = new Tuple<>(this.locations.get(x).getLocation(), this.locations.get(y).getLocation());
                    Tuple<Integer, Integer> distPair;
                    String origin = locaPair.a.latitude + "," + locaPair.a.longitude;
                    String destination = locaPair.b.latitude + "," + locaPair.b.longitude;

                    //JSON formatting and accessing - from URL
                    String searchURL = "https://maps.googleapis.com/maps/api/distancematrix/json?origins=" + origin + "&destinations=" + destination + "&key=" + this.googleAPIKey;
                    JSONObject jObject = new JSONObject(HTTPAccess.htmlGet(searchURL));

                    //reads distance and time from returned json
                    JSONObject elements = (JSONObject) ((JSONObject) jObject.getJSONArray("rows").get(0)).getJSONArray("elements").get(0);
                    int distance = elements.getJSONObject("distance").getInt("value");
                    int duration = elements.getJSONObject("duration").getInt("value");
                    distPair = new Tuple<>(distance, duration);

                    //adds to pairs
                    distancePairs.put(locaPair, distPair);
                }
            }
        }
        return distancePairs;
    }

    //endregion

    //region Sub-Scheduler

    /**
     * does the sub schedule
     * @param route current route used to access some variables
     * @param exclusedPlans plans that cannot be used
     */
    public void doSubSchedule(Route route, List<Plan> exclusedPlans) {
        this.currentRoute = route;
        this.exclusionPlans = exclusedPlans;
        if(this.subTask == null || (this.subTask != null && !this.subTask.isRunning())) {
            this.subTask = new SubScheduleTask();
            this.subTask.execute(this);
        }
        //DebugingTools.exception(new IllegalThreadStateException("Tried to create thread while previous thread is running!"));
    }

    /**
     * Called by the thread to compute the schedule
     * @return the altered route, now with added plans
     * @throws IOException caused by network issues - ie no internet
     * @throws JSONException caused by errors reading the json format
     */
    private Route threadedSubScheduler() throws IOException, JSONException {

        //checks that there is a route that is to be sub scheduled
        if(this.currentRoute == null){
            throw new InvalidParameterException("No Current Route");
        }

        //gets start time and start date
        int currentSchedulerTime = this.currentRoute.getStartTime();
        Date date = (Date) this.currentRoute.getDate().clone();

        //gets distance / time matrix
        HashMap<Tuple<LatLng, LatLng>, Tuple<Integer, Integer>> matrix = this.getHashMatrix();

        //loops though places that can be sub-scheduled
        for(int i = 0; i < this.currentRoute.getPlaces().length; i++){
            //Sub places
            List<SchedulablePlace> subPlaces = this.currentRoute.getSubPlaces().get(this.currentRoute.getPlaces()[i]);

            //if this place is locked then move on
            if(this.currentRoute.isPlaceLocked(this.currentRoute.getPlaces()[i])){
                continue;
            }

            if(subPlaces != null && subPlaces.size() > 0) {
                //USER HAS PLANS - generate a plan
                Plan plan = this.createPlan(subPlaces, currentSchedulerTime, date, this.exclusionPlans);

                if(plan == null){
                    plan = this.createPlan(subPlaces, currentSchedulerTime, date, null);
                }

                //add the plan duration and the travel duration to the current time
                currentSchedulerTime += plan.getDuration() * 60;

                if (i + 1 < this.currentRoute.getPlaces().length) {
                    Tuple<LatLng, LatLng> locationTuple = new Tuple<>(this.currentRoute.getPlaces()[i].getLocation(),
                            this.currentRoute.getPlaces()[i + 1].getLocation());
                    int timeAdd = (int) Math.ceil(matrix.get(locationTuple).b / 60.0F / 60.0F);
                    currentSchedulerTime += timeAdd * 60;
                }

                //Increment the date accordingly
                int daysToAdd = currentSchedulerTime / 60 / 24;

                Calendar c = Calendar.getInstance();
                c.setTime(this.currentRoute.getDate());
                c.add(Calendar.DATE, daysToAdd);

                date = c.getTime();

                //add plan to route
                this.currentRoute.addPlan(this.currentRoute.getPlaces()[i], plan);
            }
        }
        return this.currentRoute;
    }

    /**
     * creates a Plan for a loction given the places and time variables
     * @param subPlaces places that should be visited at that location
     * @param currentTime time that scheduling can start at
     * @param currentDate date that scheduling can start at
     * @param exclusion plans that cannot be returned
     * @return a valid plan for what places are going to be visited
     */
    private Plan createPlan(List<SchedulablePlace> subPlaces, int currentTime, Date currentDate, List<Plan> exclusion) {
        //breaks down the subplaces list in to a list of places by their type
        List<SchedulablePlace> places = new LinkedList<>();
        List<SchedulablePlace> lodgings = new LinkedList<>();
        List<SchedulablePlace> food = new LinkedList<>();
        List<SchedulablePlace> attractions = new LinkedList<>();

        for(int i = 0; i < subPlaces.size(); i++){
            switch (subPlaces.get(i).getType()){
                case LODGE:
                    lodgings.add(subPlaces.get(i));
                    break;
                case FOOD:
                    food.add(subPlaces.get(i));
                    break;
                case ATTRACTION:
                    attractions.add(subPlaces.get(i));
                    break;
                default:
                    places.add(subPlaces.get(i));
                    break;
            }
        }
        return simulatedAnnealing(lodgings, food, attractions, places, currentTime, currentDate, exclusion);
    }

    /**
     * THIS ISN'T SIMMULATED ANNEALING - NAME CHANGE REQUIRED - SHOULD BE 'findBestPlan(..){..}
     * Finds the best route for the provided variables. Uses the schedule permutation generator
     * to generate a permutation then finds the one with the best evaluation in a given number of iterations
     * This function can generate up to 10,420 different plans before exiting the execution and returning null,
     * during that time a valid permutation should be found!
     * @param lodgings places that can be stayed in ie hotels
     * @param food places that people can eat at
     * @param attractions places that are classed as attractions
     * @param subPlaces other places - should be empty
     * @param currentTime time to start scheduling at
     * @param currentDate data to start scheduling at
     * @param exclusion plans to exclude
     * @return a valid, hopefully optimum plan
     */
    private Plan simulatedAnnealing(List<SchedulablePlace> lodgings, List<SchedulablePlace> food,
                                    List<SchedulablePlace> attractions, List<SchedulablePlace> subPlaces,
                                    int currentTime, Date currentDate, List<Plan> exclusion) {
        //intialises all variables
        double initialT = 50D;
        double T = initialT;
        int increaseCount = 0;
        double iTarget = 1D, target = iTarget;

        //gets the first permutation
        SchedulePermutationGenerator generator = new SchedulePermutationGenerator(lodgings, food, attractions, subPlaces, currentTime, currentDate);
        Plan best = generator.firstPermutation();

        //checks that it isnt excluded
        if (exclusion != null && best != null && best.isIn(exclusion)) {
            best = null;
        }

        while (T > 1) {

            boolean excluded = false;

            //gets a new permutation and checks it isnt excluded
            Plan newS = generator.nextPermutation();

            if (exclusion != null && newS != null && newS.isIn(exclusion)) {
                newS = null;
                excluded = true;
            }

            //if the new permutation is better (in evaluation) than the current best, then replace
            //the current best with the new permutation
            if (newS != null) {
                if (best == null) {
                    best = newS;
                } else {
                    if (best.evaluate() < newS.evaluate()) {
                        best = newS;
                    }
                }
            }

            //if the best permutation evaluates as being better or equal to the target performance
            //then break from the loop
            if (best != null && best.evaluate() >= target) {
                break;
            }

            T--;
            //if the loop is 1/2 way though and the best value is still null
            //or loop is at the end and the evaluation is worse than the target
            //can only run 10 times and cannot run if the current solution has just been excluded
            if (((T < (initialT / 2) && best == null) || (T < 3 && (best == null || (best != null && best.evaluate() < target)))) && increaseCount < 10 && !excluded) {
                //add more iterations, reduce the target and increase the amount of hours
                //the plan can be in
                T = initialT;
                increaseCount++;
                target -= 0.01 * iTarget;
                iTarget *= 1.2F;
                generator.addExtraHours(6);
            }
        }
        return best;
    }


    //endregion

    //region Threading / Tasks

    private class SubScheduleTask extends AsyncTask<Scheduler, Void, Route> {

        private Scheduler scheduler;
        private boolean running = false;
        private boolean failed = false;

        /**
         * Functions to run in background
         * @param schedulers a scheduler that is being executed
         * @return a valid route
         */
        @Override
        protected Route doInBackground(Scheduler... schedulers) {
            try {
                int length = schedulers.length;
                if (length == 1) {
                    Scheduler scheduler = schedulers[0];
                    this.scheduler = scheduler;
                    return scheduler.threadedSubScheduler();
                }
            } catch (Exception e){
                this.failed = true;
                e.printStackTrace();
            }
            return null;
        }

        /**
         * Called once thread has completed
         * @param route the route that was returned from thread
         */
        @Override
        protected void onPostExecute(Route route) {
            this.running = false;
            SchedulerEvent event;
            if(!this.failed) {
                event = new SchedulerEvent(route, SchedulerEvent.SchedulerEventType.SUB_SCHEDULER, this.scheduler);
            } else {
                event = new SchedulerEvent(SchedulerEvent.SchedulerEventType.PASS_SCHEDULER, this.scheduler);
            }
            Scheduler.passEventToListeners(event);
        }

        /**
         * called before being executed
         */
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            this.failed = false;
            this.running = true;
            SchedulerEvent event = new SchedulerEvent(SchedulerEvent.SchedulerEventType.START_SCHEDULER, this.scheduler);
            Scheduler.passEventToListeners(event);
        }

        /**
         * Checks to see if this thread is running
         * @return true if this thread is running
         */
        public boolean isRunning() {
            return this.running;
        }
    }

    private class ScheduleTask extends AsyncTask<Scheduler, Void, List<Route>> {

        private Scheduler scheduler;
        private boolean isRunning = false;
        private boolean failed = false;

        /**
         * operations to run in background
         * @param schedulers scheduler that is to be ran
         * @return list of valid routes - limited to 25 in length
         */
        @Override
        protected List<Route> doInBackground(Scheduler... schedulers) {
            try {
                int length = schedulers.length;
                if (length == 1) {
                    Scheduler scheduler = schedulers[0];
                    scheduler.threadedScheduler();
                    this.scheduler = scheduler;
                    return scheduler.schedules;
                }
            } catch (IOException e) {
                e.printStackTrace();
            } catch (JSONException e) {
                e.printStackTrace();
            }
            this.failed = true;
            return null;
        }

        /**
         * run once the thread is finished
         * @param routes valid routes to be displayed
         */
        @Override
        protected void onPostExecute(List<Route> routes) {
            super.onPostExecute(routes);
            this.isRunning = false;
            SchedulerEvent event;

            if(!this.failed) {
                event = new SchedulerEvent(routes, SchedulerEvent.SchedulerEventType.SCHEDULER, this.scheduler);
            } else {
                event = new SchedulerEvent(SchedulerEvent.SchedulerEventType.PASS_SCHEDULER, this.scheduler);
            }
            Scheduler.passEventToListeners(event);
        }

        /**
         * called before the thread is started - initialises variables
         */
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            this.failed = false;
            this.isRunning = true;
            SchedulerEvent event = new SchedulerEvent(SchedulerEvent.SchedulerEventType.START_SCHEDULER, this.scheduler);
            Scheduler.passEventToListeners(event);
        }

        /**
         * checks if this thread is running
         * @return true if this thread is running, false otherwise
         */
        public boolean isRunning() {
            return this.isRunning;
        }
    }

    //endregion

}

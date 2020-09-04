package com.GRP.Group5.RoadTripApp.Scheduling;

import android.support.annotation.NonNull;

import com.GRP.Group5.RoadTripApp.utils.Route;

import java.util.List;

/**
 * Created by Ben Clark on 01/04/2018.
 */

public class SchedulerEvent
{
    private List<Route> routes;
    private Route route;
    private SchedulerEventType type;
    private Scheduler scheduler;

    public SchedulerEvent(SchedulerEventType type, Scheduler scheduler){
        this(null, null, type, scheduler);
    }

    public SchedulerEvent(List<Route> routes, SchedulerEventType type, Scheduler scheduler){
        this(null, routes, type, scheduler);
    }

    public SchedulerEvent(Route route, SchedulerEventType type, Scheduler scheduler) {
        this(route, null, type, scheduler);
    }


    private SchedulerEvent(Route route, List<Route> routes, SchedulerEventType type, Scheduler scheduler){
        this.route = route;
        this.routes = routes;
        this.type = type;
        this.scheduler = scheduler;
    }

    /**
     * returns the scheduler variable
     * @return scheduler object
     */
    public Scheduler getScheduler() {
        return this.scheduler;
    }

    /**
     * sets the scheduler object
     * @param scheduler scheduler object
     */
    public void setScheduler(Scheduler scheduler) {
        this.scheduler = scheduler;
    }

    /**
     * gets the list of possible routes
     * @return list of routes - can be null
     */
    public List<Route> getRoutes() {
        return this.routes;
    }

    /**
     * returns the type of event
     * @return type of event - not null
     */
    @NonNull
    public SchedulerEventType getType() {
        return this.type;
    }

    /**
     * gets the route being passed
     * @return route - can be null
     */
    public Route getRoute() {
        return this.route;
    }


    public enum SchedulerEventType{
        PASS_SCHEDULER(),
        START_SCHEDULER(),
        SCHEDULER(),
        SUB_SCHEDULER()
    }

    /**
     * implemented by classes that are to be listeners to the scheduler
     */
    public interface SchedulerEventListener {

        /**
         * called by scheduler to pass an event to the class
         * @param event event being passed - contains all needed data
         */
        void onSchedulerComplete(SchedulerEvent event);
    }
}

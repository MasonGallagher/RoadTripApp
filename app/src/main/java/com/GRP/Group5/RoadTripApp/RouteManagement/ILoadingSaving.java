package com.GRP.Group5.RoadTripApp.RouteManagement;

import android.app.Activity;

import com.GRP.Group5.RoadTripApp.utils.Route;

public interface ILoadingSaving {

    /**
     * gets the activity/context from the class
     * @return an activity
     */
    Activity getContext();

    /**
     * Should do something based on the type of event being executed
     * @param action type of action that should be ran
     * @param fromBundle if the action is being performed from a bundle / activity change
     */
    void performAction(LoadSaveAction action, boolean fromBundle);

    /**
     * wrapper function to run both setRoute and clearAndRedrawMap together
     * @param route new route object
     */
    void updateRouteMap(Route route);

    enum LoadSaveAction {
        NEW(0),
        LOAD(1),
        LOAD_SERVER(2),
        SAVE(3);

        private int id;

        /**
         * Init
         * @param id unique id for the enum
         */
        LoadSaveAction(int id){
            this.id = id;
        }

        /**
         * returns the id of the enum
         * @return id of current object
         */
        public int getId(){
            return this.id;
        }

        /**
         * Gets the appropriate type from the ID
         * @param runMode id of the type
         * @return the type that corresponds to the passed id
         */
        public static LoadSaveAction read(int runMode) {
            for(LoadSaveAction action : LoadSaveAction.values()){
                if(action.getId() == runMode){
                    return action;
                }
            }
            return null;
        }
    }
}

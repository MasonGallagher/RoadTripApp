package com.GRP.Group5.RoadTripApp.Scheduling;

import java.security.InvalidParameterException;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by Ben Clark on 09/04/2018.
 */

public enum SchedulerType {

    TIME(-1, "place", new int[][] {{0, 24 * 60}}, -1),
    FOOD(0, "food", new int[][] {{9 * 60, 11 * 60}, {12 * 60, 14 * 60}, {18 * 60, 21 * 60}}, 2),
    ATTRACTION(1, "attraction", new int[][] {{9 * 60, 21 * 60}}, 2),
    LODGE(2, "lodge", new int[][] {{20 * 60, 11 * 60}}, 12),
    TRAVEL(3, "travel", new int[][] {{0, 24 * 60}}, -1);

    private int id;
    private String name;
    private int[][] times;
    private int minTime;

    /**
     * Initialiser for scheduler type
     * Time is stored in minutes ie 1am = 60
     * @param id id number
     * @param name name of the type
     * @param times times that this type of place is schedulable for
     * @param minTime minumum time that this place type can be scheduled for
     */
    SchedulerType(int id, String name, int[][] times, int minTime) {
        this.id = id;
        this.name = name;
        this.times = times;
        this.minTime = minTime;
    }

    /**
     * gets the id of the type
     * @return id of the type
     */
    public int getID() {
        return this.id;
    }

    /**
     * gets the times that these places are available for
     * Does not use days - times apply to all days
     * @return 2D array of start and stop times
     */
    public int[][] getTimes() {
        return this.times;
    }

    /**
     * checks if a type can be scheduled for this specific time
     * @param timeMins time in minutes 1am = 60
     * @return true if this type can be scheduled
     */
    public boolean isAllowedTime(int timeMins) {
        while(timeMins >= (24 * 60)){
            timeMins -= 24 * 60;
        }
        for(int i = 0; i < this.times.length; i++){
            int start = this.times[i][0];
            int stop = this.times[i][1];

            if(stop >= start) {
                if (start <= timeMins && stop >= timeMins) {
                    return true;
                }
            } else {
                if((start <= timeMins && (24 * 60) >= timeMins) || (stop >= timeMins && 0 <= timeMins)){
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * gets the minimum time that this type of place can be scheduled for
     * @return minimum schedulable time
     */
    public int getMinTime() {
        return this.minTime;
    }

    /**
     * gets the name of the type
     * @return name of type
     */
    public String getName() {
        return this.name;
    }

    /**
     * gets a scheduler type from the id
     * @param id id of a valid place
     * @return scheduler type corresponding to type
     */
    public static SchedulerType fromID(int id) {
        for(SchedulerType type :SchedulerType.values()){
            if(type.getID() == id){
                return type;
            }
        }
        throw new InvalidParameterException("Invalid ID");
    }

    /**
     * checks if current time is valid for food
     * @param mins time (mins) to check
     * @return true if food places are valid at the time
     */
    public static boolean isFoodTime(int mins) {
        return SchedulerType.FOOD.isAllowedTime(mins);
    }

    /**
     * checks if current time is valid for lodge places
     * @param mins time (mins) to check
     * @return true if lodge places are valid at the time
     */
    public static boolean isLodgeTime(int mins){
        return SchedulerType.LODGE.isAllowedTime(mins);
    }

    /**
     * checks if current time is valid for attraction places
     * @param mins time (mins) to check
     * @return true if attraction places are valid at the time
     */
    public static boolean isAttractionTime(int mins){
        return SchedulerType.ATTRACTION.isAllowedTime(mins);
    }

    /**
     * gets a list of types that are valid for the current time
     * @param time current time in mins
     * @return list of types that are valid for the passed time
     */
    public static List<SchedulerType> getTimeType(int time) {
        List<SchedulerType> types = new LinkedList<>();
        if(isFoodTime(time)){
            types.add(SchedulerType.FOOD);
        }
        if(isLodgeTime(time)){
            types.add(SchedulerType.LODGE);
        }
        if(isAttractionTime(time)){
            types.add(SchedulerType.ATTRACTION);
        }
        return types;
    }
}

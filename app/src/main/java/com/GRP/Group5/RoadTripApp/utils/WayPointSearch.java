package com.GRP.Group5.RoadTripApp.utils;

import com.google.android.gms.maps.model.LatLng;

/**
 * Created by Ben Clark on 07/12/2017.
 *
 * This class is used to store the current search values for an entered destination - this allows
 * the system to store and save the search terms and values when the GUI is redrawn
 */

public class WayPointSearch
{
    //Variables for storage
    private String searchTerm;
    private LatLng latlng;
    private boolean isDestination;
    private boolean isStart;

    public WayPointSearch(String search, boolean dest, boolean start){
        this.searchTerm = search;
        this.isDestination = dest;
        this.isStart = start;
    }

    public LatLng getLatlng() {
        return latlng;
    }

    public void setLatlng(LatLng latlng) {
        this.latlng = latlng;
    }

    public String getSearchTerm() {
        return this.searchTerm;
    }

    public void setSearchTerm(String searchTerm) {
        this.searchTerm = searchTerm;
    }

    public boolean isDestination() {
        return this.isDestination;
    }

    public boolean isStart() {
        return this.isStart;
    }

    public void setStart(boolean start) {
        this.isStart = start;
    }

    public void setDestination(boolean destination) {
        this.isDestination = destination;
    }

    /**
     * gets the unique hash code for this object
     * @return - integer hash
     */
    @Override
    public int hashCode() {
        int hash = 1;
        hash = hash * 17 + (isDestination ? 0 : 1);
        hash = hash * 13 + (searchTerm == null ? 0 : searchTerm.hashCode());
        return hash;
    }

    /**
     * Checks too see if an object is equal to this object
     * @param obj - object to compare to
     * @return - boolean, true if equal, false otherwise
     */
    @Override
    public boolean equals(Object obj) {
        if(obj instanceof WayPointSearch){
            WayPointSearch pnt = (WayPointSearch) obj;
            if(pnt.searchTerm.equals(this.searchTerm) && pnt.isDestination == this.isDestination){
                return true;
            }
        }
        return false;
    }
}

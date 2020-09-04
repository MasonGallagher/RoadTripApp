package com.GRP.Group5.RoadTripApp.utils;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.location.Location;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListAdapter;
import android.widget.ListView;
import com.GRP.Group5.RoadTripApp.R;
import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

public class Functions {

    /**
     * Combines two list into one list
     * @param list1 first list to be added
     * @param list2 second list to be added
     * @param <T> the object that makes up the list
     * @return a list containing all of the values of both lists
     */
    public static <T> List<T> union(List<T> list1, List<T> list2) {
        Set<T> set = new HashSet<>();

        set.addAll(list1);
        set.addAll(list2);

        return new ArrayList<>(set);
    }

    /**
     * Combines multiple lists (stored in an array) into a single list
     * @param listArray array of lists to be combined into one list
     * @param <T> type that the lists are/will be made up from
     * @return a list containing all of the contents of all the lists in the array
     */
    public static <T> List<T> union(List<T>[] listArray) {
        List list = new LinkedList();
        for (int i = 0; i < listArray.length; i++) {
            list = union(list, listArray[i]);
        }
        return list;
    }

    /**
     * Helper function to put any string into title case, ie the start letter of every word is a
     * capital
     * @param str string to be put into title case
     * @return the string with title case applied
     */
    public static String toTitleCase(String str) {
        char[] chars = str.toLowerCase().toCharArray();
        char[] capitals = str.toUpperCase().toCharArray();

        String output = String.valueOf(capitals[0]);

        for (int i = 1; i < chars.length; i++) {
            if (i - 1 >= 0 && (chars[i - 1] == ' ' || chars[i - 1] == '\n' || chars[i - 1] == '\t')) {
                output += String.valueOf(capitals[i]);
            } else {
                output += String.valueOf(chars[i]);
            }
        }
        return output;
    }

    /**
     * Creates a popup dialog box that has one option to cancel the box ie closing it
     * @param title title of the dialog box
     * @param text text to be contained within the box
     * @param items items to be shown in the box
     * @param itemsListener listener for those items
     * @param okayBtn is there an okay button
     * @param positiveListener listener for okay button
     * @param act activity below the box
     * @return an alert dialog that can be opened and displayed
     */
    public static AlertDialog.Builder makePopup(String title, String text, String[] items, DialogInterface.OnClickListener itemsListener, boolean okayBtn, DialogInterface.OnClickListener positiveListener, Activity act) {
        AlertDialog.Builder builder = new AlertDialog.Builder(act);

        builder.setTitle(title);
        if(text != null) {
            builder.setMessage(text);
        }

        if(items != null && itemsListener != null) {
            builder.setItems(items, itemsListener);
        }

        if(okayBtn && positiveListener != null) {
            builder.setPositiveButton("Ok", positiveListener);
        }

        builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
            }
        });

        return builder;
    }



    /**
     * Creates a popup dialog box that has one option to cancel the box ie closing it
     * @param title title of the dialog box
     * @param text text to be contained within the box
     * @param act activity below the box
     * @return an alert dialog that can be opened and displayed
     */
    public static AlertDialog.Builder makePopup(String title, String text, Activity act){
        return makePopup(title, text, null, null, false, null, act);
    }


    /**
     * Gets teh distance between to latlng
     * @param lat1 start point
     * @param lat2 end point
     * @return distance between the two points
     */
    public static float getDistanceBetween(LatLng lat1, LatLng lat2) {
        Location loc1 = new Location("");
        loc1.setLatitude(lat1.latitude);
        loc1.setLongitude(lat1.longitude);

        Location loc2 = new Location("");
        loc2.setLatitude(lat2.latitude);
        loc2.setLongitude(lat2.longitude);

        return loc1.distanceTo(loc2);
    }

    /**
     * WIP Algorithm - still needs work - having issues with the view.measure function!
     * DO NOT USE
     * Is supposed to set the height of a list view to the size of the children
     * ie showing all the children. However this doesn't work due to size measuring issues
     * @param view list view to have height set
     */
    public static void setListViewHeightBasedOnChildren(ListView view){
        ListAdapter adapter = view.getAdapter();
        if(adapter == null){
            return;
        }

        int totalHeight = 0;
        for(int i = 0; i < adapter.getCount(); i++){
            View item = adapter.getView(i, null, view);
            item.measure(0, 0);
            totalHeight += item.getHeight();
        }

        ViewGroup.LayoutParams params = view.getLayoutParams();
        if(totalHeight > 0){
            params.height = totalHeight + (view.getDividerHeight() * (adapter.getCount() - 1));
        } else {
            params.height = adapter.getCount() * 65;
        }

        view.setLayoutParams(params);
    }
}
package com.GRP.Group5.RoadTripApp.utils;

import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;

import com.GRP.Group5.RoadTripApp.Activities.MapsActivity;
import com.GRP.Group5.RoadTripApp.R;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;

/**
 * Contains the static bitmap desciptors for the mark icons
 * This class was going to be used more, but ended up not being required, as much
 */
public class MarkerUtils {

    public static BitmapDescriptor bmdAttraction;

    public static BitmapDescriptor bmdFood;

    public static BitmapDescriptor bmdHotel;

    static {
        int size = 40;

        bmdAttraction = BitmapDescriptorFactory.fromBitmap(Bitmap.createScaledBitmap(
                ((BitmapDrawable) MapsActivity.getStaticContext().getResources().getDrawable(
                        R.mipmap.map_attraction_icon)).getBitmap(), size, size, false));

        bmdFood = BitmapDescriptorFactory.fromBitmap(Bitmap.createScaledBitmap(
                ((BitmapDrawable) MapsActivity.getStaticContext().getResources().getDrawable(
                        R.mipmap.map_food_icon)).getBitmap(), size, size, false));

        bmdHotel = BitmapDescriptorFactory.fromBitmap(Bitmap.createScaledBitmap(
                ((BitmapDrawable) MapsActivity.getStaticContext().getResources().getDrawable(
                        R.mipmap.map_hotel_icon)).getBitmap(), size, size, false));
    }
}

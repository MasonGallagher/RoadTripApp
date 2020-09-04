package com.GRP.Group5.RoadTripApp.utils;

import android.os.Build;
import android.support.annotation.RequiresApi;

/**
 * Created by Ben Clark on 09/04/2018.
 */

public class BooleanWrapper
{
    private boolean b;

    public BooleanWrapper(){
        this(false);
    }

    public BooleanWrapper(boolean b){
        this.b = b;
    }

    public boolean isB() {
        return this.b;
    }

    public void setB(boolean b) {
        this.b = b;
    }

    @Override
    public boolean equals(Object obj) {
        if(obj != null && obj instanceof BooleanWrapper){
            return ((BooleanWrapper) obj).isB() == this.b;
        }
        return false;
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    public int hashCode() {
        return Boolean.hashCode(b);
    }
}

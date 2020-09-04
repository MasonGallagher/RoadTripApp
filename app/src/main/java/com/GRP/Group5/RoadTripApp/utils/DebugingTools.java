package com.GRP.Group5.RoadTripApp.utils;

import android.accounts.NetworkErrorException;
import android.app.Activity;

public class DebugingTools
{
    private static final boolean DEBUGGING = false;

    /**
     * checks if the app is in a degbuging state - set in the DEBUGGING variable
     * @return returns true is DEBUGGING variable is true
     */
    public static boolean isDebugging(){
        if(DEBUGGING){
            System.out.println("=================================\n" +
                               "DOING SOMETHING FOR THE DEBUGGER\n" +
                               "=================================");
        }
        return DEBUGGING;
    }

    /**
     * Creates a popup warning the user that they are currently in developer / debugging mode
     * @param activity current activity top open popup over
     */
    public static void debuggerWarn(Activity activity){
        if(isDebugging()) {
            Functions.makePopup("DEVELOPER MODE", "App in developer mode\n DON'T DISTRIBUTE LIKE THIS!!!!", activity).create().show();
        }
    }

    /**
     * called to show an error screen when an exception occurs
     * Contents depends on whether the user is in debugging mode or not
     * if in debugging mode then the error report will be show
     * otherwise a simple warning, indicating to the user than an error ocured will be show
     * @param e exception that ocured
     * @param activity current activity that the popup will be opened above
     */
    public static void exception(Exception e, Activity activity) {
        String title = "An error occured!";
        String message = "We recommend you restart the app and try again.\n" +
                "If this happens repeatedly people report the issue to our development team.";

        if(e instanceof NetworkErrorException){
            message = "We are having issues with our servers at the moment. Please try again later!";
        }

        if (isDebugging()) {
            e.printStackTrace();
            message = e.getMessage();
        }

        Functions.makePopup(title, message, activity).show();
    }
}

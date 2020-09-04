package com.GRP.Group5.RoadTripApp.RouteManagement;

import android.accounts.NetworkErrorException;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.util.ArraySet;
import android.text.InputType;
import android.widget.EditText;

import com.GRP.Group5.RoadTripApp.Activities.ShareActivity;
import com.GRP.Group5.RoadTripApp.R;
import com.GRP.Group5.RoadTripApp.utils.DebugingTools;
import com.GRP.Group5.RoadTripApp.utils.Functions;
import com.GRP.Group5.RoadTripApp.utils.HTTPAccess;
import com.GRP.Group5.RoadTripApp.utils.Route;
import com.GRP.Group5.RoadTripApp.utils.Tuple;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

public class SaveLoad
{
    /**
     * Sends a route the server for storage. A code to access the route is received
     * @param route route to be stored
     * @return access code
     * @throws JSONException thrown if there is errors converting to JSON
     */
    private static String sendToServer(Route route) throws JSONException, NetworkErrorException {
        if(route.getID() == null) {
            String code = HTTPAccess.makeRequestandReturn("https://grp.ax3.co.uk/trips/add", route.toJSON().toString());
            route.setID(code);
        } else {
            HTTPAccess.makeRequestandReturn("https://grp.ax3.co.uk/trips/" + route.getID() + "/edit", route.toJSON().toString());
        }
        return route.getID();
    }

    /**
     * Uploads the route to the server and opens the gui to share the code
     * @param loadSave interface representing a gui that is capable of performing route management
     * @param route the route to be shared
     */
    public static void shareShare(ILoadingSaving loadSave, Route route) {
        final Intent intentBundle = new Intent(loadSave.getContext(), ShareActivity.class);
        Bundle bundle = new Bundle();
        try {
            bundle.putString("routeCode", sendToServer(route));
        } catch (Exception e){
            DebugingTools.exception(e, loadSave.getContext());
        }
        intentBundle.putExtras(bundle);
        loadSave.getContext().startActivity(intentBundle);
    }

    /**
     * Opens a shared route from the server (requires threads)
     * @param loadSave interface representing a gui that is capable of performing route management
     */
    public static void openShare(final ILoadingSaving loadSave, final boolean fromBundle) {

        final EditText input = new EditText(loadSave.getContext());

        DialogInterface.OnClickListener listener = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String text = input.getText().toString();
                String url = "http://grp.ax3.co.uk/trips/" + text + "/json";
                HTTPLoadTask task = new HTTPLoadTask();
                task.execute(url, loadSave);
            }
        };

        AlertDialog.Builder builder = Functions.makePopup("Open Shared Plan", "Enter the code for the plan that was shared with you below",
                null, null, true, listener, loadSave.getContext());

        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                if(fromBundle){
                    loadSave.getContext().finish();
                }
                dialogInterface.dismiss();
            }
        });

        input.setHint("2f623");
        builder.setView(input);
        builder.show();
    }

    /**
     * Saves the route locally on the the android device! Also uploads it to the server
     * @param loadSave interface representing a gui that is capable of performing route management
     * @param route route to be saved locally
     * @throws JSONException caused if there is an issue converting to json
     */
    public static void saveLocal(ILoadingSaving loadSave, Route route) throws JSONException, NetworkErrorException {
        String code = sendToServer(route);

        SharedPreferences sharedPref = loadSave.getContext().getPreferences(Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();

        Set<String> allCodes = sharedPref.getStringSet("codes", new ArraySet<String>());

        if(!allCodes.contains(code)){
            allCodes.add(code);
        }

        editor.putString(code, route.toJSON().toString());
        editor.putString(code + "-name", route.getName());
        editor.putString(code + "-desc", route.getDesc());
        editor.putStringSet("codes", allCodes);

        editor.apply();
        editor.commit();
    }

    /**
     * Gets all of the IDs/codes that are saved locally
     * @param loadSave interface representing a gui that is capable of performing route management
     * @return a set of all of the ids/codes that are currently stored
     */
    private static Set<String> getAllSavedIDs(ILoadingSaving loadSave){
        SharedPreferences sharedPref = loadSave.getContext().getPreferences(Context.MODE_PRIVATE);
        return sharedPref.getStringSet("codes", null);
    }

    /**
     * Gets the details (name and description) of a route that is saved on the device
     * Without getting all of the JSON data for the entire root
     * @param loadSave interface representing a gui that is capable of performing route management
     * @param code id/code of the route to be loaded
     * @return tuple containing the details of that place - a = name b = description
     */
    private static Tuple<String, String> getShortDetails(ILoadingSaving loadSave, String code){
        SharedPreferences sharedPref = loadSave.getContext().getPreferences(Context.MODE_PRIVATE);
        return new Tuple<>(sharedPref.getString(code + "-name", null), sharedPref.getString(code + "-desc", null));
    }

    /**
     * Gets the details and codes of all of the route saved on the device
     * @param loadSave interface representing a gui that is capable of performing route management
     * @return a list of each route code, name and description - stored in a double tuple structure
     */
    public static List<Tuple<String, Tuple<String, String>>> getAllShortDetails(ILoadingSaving loadSave){
        Set<String> allCodes = getAllSavedIDs(loadSave);
        List<Tuple<String, Tuple<String, String>>> list = new LinkedList<>();
        if(allCodes != null) {
            for (String code : allCodes) {
                list.add(new Tuple<>(code, getShortDetails(loadSave, code)));
            }
        }
        return list;
    }

    /**
     * Loads a route that is saved in the device
     * @param loadSave interface representing a gui that is capable of performing route management
     * @param code id/code of the route that is to be opened/loaded
     * @return a valid (hopefully) route that can be displayed on the device
     * @throws JSONException caused if there is an error reading the JSON format
     * @throws ParseException caused if there is an error reading the date within the route
     */
    public static Route openLocal(ILoadingSaving loadSave, String code) throws JSONException, ParseException {
        SharedPreferences sharedPref = loadSave.getContext().getPreferences(Context.MODE_PRIVATE);

        String loadedJSON = sharedPref.getString(code, null);

        if(loadedJSON != null && loadedJSON.length() > 0){
            return Route.fromJSON(new JSONObject(loadedJSON));
        } else {
            return null;
        }

    }

    /**
     * Async task that retrives data from html source
     */
    private static class HTTPLoadTask extends AsyncTask<Object, Void, Route>{
        private ILoadingSaving loadSave;

        /**
         * Operations to run in background
         * @param objects url and ILoadingSaving object
         * @return a route parsed from the recieved JSON string
         */
        @Override
        protected Route doInBackground(Object... objects) {
            String url = (String) objects[0];
            this.loadSave = (ILoadingSaving) objects[1];
            try {
                String JSONData = HTTPAccess.htmlGet(url);
                return Route.fromJSON(new JSONObject(JSONData).getJSONObject("data"));
            } catch (Exception e) {
                //DebugingTools.exception(e, this.loadSave.getContext());
            }
            return null;
        }

        /**
         * Operations to execute once the thread has finished
         * @param route the result of the thread
         */
        @Override
        protected void onPostExecute(Route route) {
            super.onPostExecute(route);
            if(route != null) {
                this.loadSave.updateRouteMap(route);
            }
        }
    }
}

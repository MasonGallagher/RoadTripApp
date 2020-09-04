package com.GRP.Group5.RoadTripApp.Activities;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.ListView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.GRP.Group5.RoadTripApp.Activities.Adapters.DestinationSearchAdapter;
import com.GRP.Group5.RoadTripApp.Activities.Adapters.PlaceAutocompleteAdapter;
import com.GRP.Group5.RoadTripApp.R;
import com.GRP.Group5.RoadTripApp.utils.WayPointSearch;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.PlaceBuffer;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * Created by Yi Shen, Antony, Nazar, Ben on 08/12/2017.
 *
 * This activity is our front page of the application, which allows user to type the places that they want to go.
 * When the user finish entering all the places, they can click the 'Go' button and go to the MapActivity which will show all the places
 * that the user want to go on the map.
 */
public class FrontActivity extends AppCompatActivity  implements GoogleApiClient.OnConnectionFailedListener, GoogleApiClient.ConnectionCallbacks {

    private static FrontActivity frontInstance;
    private PlaceAutocompleteAdapter placeAutoCompleteAdapter;
    private DestinationSearchAdapter searchAdapter;
    private ListView searchList;
    private Button goBtn;
    private static final LatLngBounds BOUNDS_MOUNTAIN_VIEW = new LatLngBounds(new LatLng(48.799382, -11.030515), new LatLng(60.026761, 3.255413));
    private static final String LOG_TAG = "FrontActivity";
    private static final int GOOGLE_API_CLIENT_ID = 0;
    public GoogleApiClient mGoogleApiClient;

    private Date startDate;
    private int startTimeMins;

    public static FrontActivity getContext() {return frontInstance;}

    public DestinationSearchAdapter getSearchAdapter() {
        return this.searchAdapter;
    }

    public PlaceAutocompleteAdapter getPlaceAutoCompleteAdapter() { return this.placeAutoCompleteAdapter; }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_front);


        int pictureIndex = 1 +(int)(Math.random() * 10);
        String pictureName = "card_view_background" + String.valueOf(pictureIndex);
        int resourceID = getResources().getIdentifier(pictureName, "drawable", getPackageName());


        //create the database when the app starts
        frontInstance = this;

        //SUB SCHEDULER
        this.startDate = new Date();
        this.startTimeMins = 12 * 60;

        //create the google API client
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addApi(Places.GEO_DATA_API)
                .enableAutoManage(this, GOOGLE_API_CLIENT_ID, this)
                .addConnectionCallbacks(this)
                .build();

        placeAutoCompleteAdapter = new PlaceAutocompleteAdapter(this, this.mGoogleApiClient, BOUNDS_MOUNTAIN_VIEW, null);

        initSwipeList();
        initGoButton();
    }

    public void initSwipeList(){
        searchList = this.findViewById(R.id.searchList);

        //Set search adapter - this is what controls the values in the list allowing for items to be removed or edited
        this.searchAdapter = new DestinationSearchAdapter(getApplicationContext(), R.layout.search_layout_comp, this);
        searchList.setAdapter(this.searchAdapter);

        //add 3 base items to the list
        for(int i = 0; i < 3; i++){
            this.getSearchAdapter().add(new WayPointSearch("", false, false));
        }
    }

    public void initGoButton(){
        //get the go button
        goBtn = (Button) this.findViewById(R.id.goBtn);

        //when go button is pressed - open scheduler view
        goBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                List<WayPointSearch> searchList = getSearchAdapter().getDestinationList();
                int numValidInput = 0;

                //check if the input of each item in the search list valid
                for(WayPointSearch location : searchList){
                    //this need a function to check the location's validity
                    if(location.getSearchTerm() != ""){
                        numValidInput++;
                    }
                }

                //System.out.println(searchList.size());  //for debug
                //System.out.println(numValidInput);  //for debug

                if(searchList.size()==0){
                    //tell user to add locations
                }
                else{
                    final Intent intentBundle = new Intent(FrontActivity.this, RankActivity.class);  //from FrontActivity to RankActivity
                    Bundle bundle = new Bundle();  //used to transfer data to the RankActivity
                    bundle.putInt("numPlaces", searchList.size());  //the number of places that user entered

                    SimpleDateFormat ft = new SimpleDateFormat ("dd-MM-yyyy");

                    bundle.putInt("startTime", startTimeMins);
                    bundle.putString("startDate", ft.format(startDate));

                    for(int i = 0; i < searchList.size(); i++) {
                        WayPointSearch point = searchList.get(i);
                        //System.out.println(point.getSearchTerm());  //for debug

                        if(point.getSearchTerm() != "") {
                            //here need to check if it can get the LatLng
                            LatLng latLng = getLocationFromAddress(FrontActivity.this, point.getSearchTerm());

                            if(latLng == null){
                                //warning
                            }
                            else{
                                point.setLatlng(latLng);
                                bundle.putDouble("long-" + i, point.getLatlng().longitude);
                                bundle.putDouble("lat-" + i, point.getLatlng().latitude);
                                bundle.putBoolean("start-" + i, point.isStart());
                                bundle.putBoolean("dest-" + i, point.isDestination());
                                bundle.putString("name-" + i, point.getSearchTerm());
                            }
                        }
                    }

                    intentBundle.putExtras(bundle);
                    startActivity(intentBundle);
                    searchAdapter.clear();
                    finish();
                }
            }
        });

        this.initDateTime();
    }

    /**
     * Called when the back button on a device is pressed
     * Finishes the display and clears the adapter
     */
    @Override
    public void onBackPressed() {
        super.onBackPressed();
        this.searchAdapter.clear();
        this.finish();
    }

    /**
     * Get the latitude and longitude of an address, which will be used to set a marker of this position on the map
     * @param context - FrontActivity
     * @param strAddress - the place that user want to go
     * @return - the latitude and longitude of this place
     */
    public LatLng getLocationFromAddress(Context context, String strAddress) {
        Geocoder coder = new Geocoder(context, Locale.UK);
        List<Address> address;
        LatLng LatLng;

        try {
            address = coder.getFromLocationName(strAddress, 1);

            if (address == null || address.size() == 0) {
                String alteredStr = "";

                String[] adrsTerms = strAddress.split(",");

                if(adrsTerms.length <= 2){
                    return null;
                }

                for(int i = 1; i < adrsTerms.length; i++){
                    alteredStr += adrsTerms[i];
                    if(i != adrsTerms.length - 1){
                        alteredStr += ",";
                    }
                }

                return getLocationFromAddress(context, alteredStr);
            }
            else {
                Address location = address.get(0);
                location.getLatitude();
                location.getLongitude();
                LatLng = new LatLng(location.getLatitude(), location.getLongitude());

                return LatLng;
            }
        } catch (IOException ex) {
            ex.printStackTrace();
            return null;
        }
    }

    /**
     * Return the request result by outputting logs
     */
    public ResultCallback<PlaceBuffer> mUpdatePlaceDetailsCallback = new ResultCallback<PlaceBuffer>() {
        @Override
        public void onResult(PlaceBuffer places) {
            //System.out.println("Test code");
            if (!places.getStatus().isSuccess()) {
                //Log.e(LOG_TAG, "Place query did not complete. Error: " + places.getStatus().toString());
                return;
            }
            // Selecting the first object buffer.
            final Place place = places.get(0);
            CharSequence attributions = places.getAttributions();

            //Log.e(LOG_TAG, "hello " + place.getAddress().toString());

            if (attributions != null) {
                //do nothing
            }
        }
    };

    /**
     * If Google Api connected, output the success log
     * @param bundle
     */
    @Override
    public void onConnected(Bundle bundle) {
        //Log.i(LOG_TAG, "Google Places API connected.");

    }

    /**
     * If failed to connect the Google Api, output the errors
     * @param connectionResult
     */
    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        //Log.e(LOG_TAG, "Google Places API connection failed with error code: " + connectionResult.getErrorCode());

        Toast.makeText(this,
                "Google Places API connection failed with error code:" + connectionResult.getErrorCode(),
                Toast.LENGTH_LONG).show();
    }

    /**
     * If connect suspended, output the suspended log
     * @param i
     */
    @Override
    public void onConnectionSuspended(int i) {
        //Log.e(LOG_TAG, "Google Places API connection suspended.");
    }

    public void initDateTime(){
        final Button btnDatePicker= this.findViewById(R.id.dateSelect);
        final Button btnTimePicker= this.findViewById(R.id.timeSelect);

        btnDatePicker.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //Gets current date
                final Calendar c = Calendar.getInstance();
                int year = 0, month = 0, day = 0;
                if(startDate == null) {
                    year = c.get(Calendar.YEAR);
                    month = c.get(Calendar.MONTH);
                    day = c.get(Calendar.DAY_OF_MONTH);
                } else {
                    SimpleDateFormat ft = new SimpleDateFormat ("dd-MM-yyyy");
                    String[] splitDate = ft.format(startDate).split("-");
                    day = Integer.parseInt(splitDate[0]);
                    month = Integer.parseInt(splitDate[1]) - 1;
                    year = Integer.parseInt(splitDate[2]);
                }

                DatePickerDialog dialog = new DatePickerDialog(getContext(), new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker datePicker, int year, int month, int dayMonth) {
                        String date = "";
                        month++;
                        if(dayMonth < 10){
                            date += "0" + dayMonth + "-";
                        } else {
                            date += dayMonth + "-";
                        }
                        if(month < 10){
                            date += "0" + month + "-" + year;
                        } else {
                            date += month + "-" + year;
                        }
                        btnDatePicker.setText(date);
                        try {
                            SimpleDateFormat ft = new SimpleDateFormat("dd-MM-yyyy");
                            startDate = ft.parse(date);
                        } catch (ParseException e) {
                            e.printStackTrace();
                        }
                    }
                }, year, month, day);
                dialog.show();
            }
        });

        btnTimePicker.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                int hours, mins;
                if(startTimeMins < 0){
                    hours = 12;
                    mins = 0;
                } else {
                    hours = startTimeMins / 60;
                    mins = startTimeMins - (hours * 60);
                }

                TimePickerDialog dialog = new TimePickerDialog(getContext(), new TimePickerDialog.OnTimeSetListener() {
                    @Override
                    public void onTimeSet(TimePicker timePicker, int hours, int mins) {
                        String text = "";
                        if(hours < 10){
                            text += "0" + hours;
                        } else {
                            text += hours;
                        }
                        text += ":";
                        if(mins < 10){
                            text += "0" + mins;
                        } else {
                            text += mins;
                        }
                        btnTimePicker.setText(text);
                        startTimeMins = (hours * 60) + mins;
                    }
                }, hours, mins, false);
                dialog.show();
            }
        });
    }
}

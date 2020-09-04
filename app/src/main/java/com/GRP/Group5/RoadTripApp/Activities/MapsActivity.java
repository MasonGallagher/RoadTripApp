package com.GRP.Group5.RoadTripApp.Activities;

import android.Manifest;
import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.BounceInterpolator;
import android.view.animation.Interpolator;
import android.view.animation.RotateAnimation;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.GRP.Group5.RoadTripApp.Activities.GetNearbyPlaces.GetDirectionsData;
import com.GRP.Group5.RoadTripApp.Activities.GetNearbyPlaces.GetNearbyPlacesData;
import com.GRP.Group5.RoadTripApp.Activities.GetNearbyPlaces.GetPlaceDetail;
import com.GRP.Group5.RoadTripApp.R;
import com.GRP.Group5.RoadTripApp.RouteManagement.ILoadingSaving;
import com.GRP.Group5.RoadTripApp.RouteManagement.SaveLoad;
import com.GRP.Group5.RoadTripApp.Scheduling.SchedulablePlace;
import com.GRP.Group5.RoadTripApp.Scheduling.Scheduler;
import com.GRP.Group5.RoadTripApp.Scheduling.SchedulerEvent;
import com.GRP.Group5.RoadTripApp.Scheduling.SchedulerType;
import com.GRP.Group5.RoadTripApp.utils.BooleanWrapper;
import com.GRP.Group5.RoadTripApp.utils.DebugingTools;
import com.GRP.Group5.RoadTripApp.utils.Functions;
import com.GRP.Group5.RoadTripApp.utils.MarkerUtils;
import com.GRP.Group5.RoadTripApp.utils.Route;
import com.GRP.Group5.RoadTripApp.utils.Tuple;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PointOfInterest;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;


/**
 * Created by Yi Shen, Mason, Ben on 05/12/2017.
 * This map activity will show all the destinations that the user want to go on a re-style google map by using markers.
 */
public class MapsActivity extends AppCompatActivity implements OnMapReadyCallback, GoogleMap.OnPoiClickListener, SchedulerEvent.SchedulerEventListener, ILoadingSaving{
    private static MapsActivity mapsInstance;
    private static GoogleMap mMap;

    private HashMap<SchedulablePlace, List<Tuple<LatLng, Tuple<String, String>>>> attractionsList;
    private HashMap<SchedulablePlace, List<Tuple<LatLng, Tuple<String, String>>>> hotelList;
    private HashMap<SchedulablePlace, List<Tuple<LatLng, Tuple<String, String>>>> foodList;
    private List<Marker> currentMarkers;

    int PROXIMITY_RADIUS = 1000;  //the searching radius

    private Route route;  //to store the latitude and longitude of all destinations that the user want to go
    private SchedulerType currentAddingType;

    //Drawer stuff
    private ActionBarDrawerToggle drawerToggle;

    private String placeId;


    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    private int permsRequest = 0;
    private boolean useLazyIcons;

    public String getPlaceId() { return this.placeId; }

    public void setPlaceId(String placeId) {this.placeId = placeId;}

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        mapFragment.getMapAsync(this);
        mapsInstance = this;

        this.foodList = new HashMap<>();
        this.attractionsList = new HashMap<>();
        this.hotelList = new HashMap<>();

        this.currentMarkers = new LinkedList<>();

        /*
        PRESUMED TO BE DEBUG CODE
         */

        //region Scheduler

        final Button schedulerBtn = this.findViewById(R.id.B_sheduler);  //get the scheduler button

        //when scheduler button is pressed - open scheduler view
        schedulerBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent intentBundle = new Intent(MapsActivity.this, ScheduleActivity.class);
                Bundle bundle = new Bundle();

                try {
                    bundle.putString("route", route.toJSON().toString());
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                intentBundle.putExtras(bundle);
                startActivity(intentBundle);
            }
        });

        Scheduler.addStaticListener(this);
        this.onSchedulerComplete(null);

        //endregion

        //region Top Tool Bar

        final RelativeLayout infoBox = this.findViewById(R.id.notificationBar);
        final ProgressBar spinner = this.findViewById(R.id.progressSpinner);

        spinner.bringToFront();
        spinner.setVisibility(View.INVISIBLE);

        final BooleanWrapper isDown = new BooleanWrapper(false);
        final ValueAnimator infoTextBoxAnimation = ValueAnimator.ofInt(-100, 10);
        infoTextBoxAnimation.setDuration(750);

        infoTextBoxAnimation.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) infoBox.getLayoutParams();
                int value = (int) valueAnimator.getAnimatedValue();
                if(!isDown.isB()) {
                    params.topMargin = value;
                    if(value == 10){
                        isDown.setB(true);
                    }
                }
                infoBox.setLayoutParams(params);
            }
        });

        Toolbar toolbar = this.findViewById(R.id.toolbarScheduler);
        this.setSupportActionBar(toolbar);

        ActionBar bar = getSupportActionBar();

        if (bar != null) {
            bar.setDisplayHomeAsUpEnabled(true);
            bar.setHomeButtonEnabled(true);
        }

        initDraw();

        //endregion

        //region Navigation

        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                try {
                    switch (item.getItemId()) {
                        case R.id.nav_restart: {
                            route = null;
                            finish();
                            return true;
                        }
                        case R.id.nav_save: {
                            performAction(LoadSaveAction.SAVE, false);
                            return true;
                        }
                        case R.id.nav_share: {
                            SaveLoad.shareShare((ILoadingSaving) getContext(), route);
                            return true;
                        }
                        case R.id.nav_open_share: {
                            performAction(LoadSaveAction.LOAD_SERVER, false);
                            return true;
                        }
                        case R.id.nav_load: {
                            performAction(LoadSaveAction.LOAD, false);
                            return true;
                        }
                    }
                } catch (Exception e){
                    DebugingTools.exception(e, getContext());
                }
                return true;
            }
        });

        MenuItem itemSwitch = navigationView.getMenu().getItem(7);
        Switch toggleSwitch = itemSwitch.getActionView().findViewById(R.id.toggle_switch);
        toggleSwitch.setChecked(this.useLazyIcons = false);

        toggleSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                useLazyIcons = b;
                Toast.makeText(getContext(), R.string.lazy_marker_changed, Toast.LENGTH_SHORT);
            }
        });

        //endregion
    }

    public void initDraw(){

        DrawerLayout drawerLayout = findViewById(R.id.drawer_layout);

        this.drawerToggle = new ActionBarDrawerToggle(this, drawerLayout,
                R.string.drawer_open, R.string.drawer_close) {

            /** Called when a drawer has settled in a completely open state. */
            public void onDrawerOpened(View drawerView) {
            }

            /** Called when a drawer has settled in a completely closed state. */
            public void onDrawerClosed(View view) {
            }
        };

        this.drawerToggle.setDrawerIndicatorEnabled(true);
        drawerLayout.addDrawerListener(this.drawerToggle);
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        this.drawerToggle.syncState();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (this.drawerToggle.onOptionsItemSelected(item)) {
            return true;
        }
        switch (item.getItemId()) {
            case R.id.action_clear: {
                clearMarkers();
                return true;
            }
            case R.id.action_attraction: {
                this.currentAddingType = SchedulerType.ATTRACTION;
                this.selectCity(this.attractionsList, new String[] {"amusement_park", "aquarium", "art_gallery", "bowling_alley", "movie_theater", "museum", "shopping_mall", "stadium", "spa", "zoo"});
                return true;
            }
            case R.id.action_food: {
                this.currentAddingType = SchedulerType.FOOD;
                this.selectCity(this.foodList, new String[]{"restaurant", "cafe", "food_takeaway"});
                return true;
            }
            case R.id.action_lodge: {
                this.currentAddingType = SchedulerType.LODGE;
                this.selectCity(this.hotelList, new String[]{"lodging"});
                return true;
            }
        }
        return super.onOptionsItemSelected(item);
    }

    private void selectCity(final HashMap<SchedulablePlace, List<Tuple<LatLng, Tuple<String, String>>>> placeList, final String[] searchParams) {

        String[] cities = new String[this.route.getPlaces().length];

        for(int i = 0; i < cities.length; i++){
            cities[i] = this.route.getPlaces()[i].getName();
        }

        DialogInterface.OnClickListener listener = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                SchedulablePlace place = route.getPlaces()[i];
                if(placeList.get(place) == null){
                    placeList.put(place, new LinkedList<Tuple<LatLng, Tuple<String, String>>>());
                }
                if (placeList.get(place).size() == 0) {
                    clearMarkers();
                    showNearbyPlaces(currentAddingType, searchParams, place);
                }
                if(placeList.get(place).size() != 0){
                    showMarkerType(place, currentAddingType);
                }
            }
        };

        DialogInterface.OnClickListener cancelListener = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
                clearMarkers();
            }
        };

        AlertDialog.Builder builder = Functions.makePopup("Please select a city", null, cities, listener, false, null, this);

        builder.setNegativeButton("Cancel", cancelListener);

        builder.create().show();
    }

    @Override
    public void onBackPressed() {
        DialogInterface.OnClickListener listener = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                finish();
            }
        };

        Functions.makePopup("", this.getResources().getString(R.string.exit_question), null,
                null, true, listener, this).create().show();
    }

    @Override
    public void onSchedulerComplete(SchedulerEvent event) {
        Button working = this.findViewById(R.id.schedulerRunningBtn);

        RotateAnimation rotate = new RotateAnimation(0, 4 * 360, Animation.RELATIVE_TO_SELF,
                0.5f,  Animation.RELATIVE_TO_SELF, 0.5f);
        rotate.setDuration(2500);
        rotate.setRepeatCount(RotateAnimation.INFINITE);
        rotate.setRepeatMode(RotateAnimation.REVERSE);

        if(Scheduler.isTaskRunning()){
            working.setVisibility(View.VISIBLE);
            working.startAnimation(rotate);
        } else {
            working.clearAnimation();
            working.setVisibility(View.INVISIBLE);
        }

        if(event != null && event.getType() == SchedulerEvent.SchedulerEventType.SUB_SCHEDULER) {
            this.route = event.getRoute();
        }
    }

    //region Maps Stuff

    public GoogleMap getmMap() {
        return mMap;
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        try {

            double totalLongitude = 0;
            double totalLatitude = 0;
            mMap = googleMap;

            Intent intentExtras = getIntent();
            Bundle extrasBundle = intentExtras.getExtras();

            if (extrasBundle.containsKey("runMode")) {
                this.performAction(ILoadingSaving.LoadSaveAction.read(extrasBundle.getInt("runMode")), true);
            } else {
                //get the data from RankActivity
                try {
                    this.route = Route.fromJSON(new JSONObject(extrasBundle != null ? extrasBundle.getString("jsonRoute") : null));
                } catch (JSONException e) {
                    e.printStackTrace();
                } catch (NullPointerException e) {
                    e.printStackTrace();
                }
            }

            runMapStartup();
        } catch (Exception e) {
            DebugingTools.exception(e, this);
        }
    }

    public void runMapStartup(){
        double totalLongitude = 0;
        double totalLatitude = 0;

        if(this.route == null){
            return;
        }

        for (int i = 0; i < this.route.getPlaces().length; i++) {
            totalLatitude += this.route.getPlaces()[i].getLocation().latitude;
            totalLongitude += this.route.getPlaces()[i].getLocation().longitude;

            MarkerOptions markerOptions = new MarkerOptions();

            markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN));
            markerOptions.position(this.route.getPlaces()[i].getLocation());
            markerOptions.title(this.route.getPlaces()[i].getName());
            mMap.addMarker(markerOptions);
        }

        //move the camera to the center
        LatLng center = new LatLng(totalLatitude / this.route.getPlaces().length, totalLongitude / this.route.getPlaces().length);
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(center, 5));  //move the camera with zoom in five times

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                if (permsRequest == 0) {
                    ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, 1);
                    permsRequest++;
                }
            } else {
                return;
            }
        }

        try {
            // Customise the styling of the base map using a JSON object defined in a raw resource file.
            boolean success = mMap.setMapStyle(MapStyleOptions.loadRawResourceStyle(this, R.raw.map_theme));

            if (!success) {
                //System.out.println("Style parsing failed.");
            }
        } catch (Resources.NotFoundException e) {
            e.printStackTrace();
        }

        path(); //show the path between cities
        setInfoWindow();
        //mMap.setMyLocationEnabled(true);  //to enable the get my location button
    }

    /**
     * Alert the latitude and longitude of a place
     * @param poi point of interest
     */
    @Override
    public void onPoiClick(PointOfInterest poi) {
        Toast.makeText(getApplicationContext(), "Clicked: " +
                poi.name + "\nPlace ID:" + poi.placeId +
                "\nLatitude:" + poi.latLng.latitude +
                " Longitude:" + poi.latLng.longitude, Toast.LENGTH_SHORT).show();
    }

    /**
     * Get the nearby places' url based on the distinction's latitude and longitude and the nearby places type that user want to show on the map
     * @param latitude, the latitude of the distinction
     * @param longitude, the longitude of the distinction
     * @param typeOfnearbyPlace, the type of places that the user want to show around his distinctions on the map
     */

    private String getUrl(double latitude, double longitude, String typeOfnearbyPlace) {
        //all the codes below is to forming a JASON request to find all the nearby places around one point with a radius
        StringBuilder googlePlaceUrl = new StringBuilder("https://maps.googleapis.com/maps/api/place/nearbysearch/json?");
        googlePlaceUrl.append("location=").append(latitude).append(",").append(longitude);
        googlePlaceUrl.append("&radius=").append(PROXIMITY_RADIUS);
        googlePlaceUrl.append("&type=").append(typeOfnearbyPlace);
        googlePlaceUrl.append("&sensor=true");
        googlePlaceUrl.append("&key="+"AIzaSyCiggO02NK8nE2_LYCodp1RvNDlbj95iGY");

        //Log.d("MapsActivity", "url = "+googlePlaceUrl.toString());
        return googlePlaceUrl.toString();
    }

    /**
     * Get the path' url between two points
     * @param start the latlng of start point
     * @param end the latlng of end point
     * @return the url to get driving directions from
     */
    private String getDirectionsUrl(LatLng start, LatLng end){
        double start_latitude = start.latitude;
        double start_longitude = start.longitude;
        double end_latitude = end.latitude;
        double end_longitude = end.longitude;

        return "https://maps.googleapis.com/maps/api/directions/json?" + "origin=" + start_latitude + "," + start_longitude +
                "&destination=" + end_latitude + "," + end_longitude +
                "&alternatives=true&mode=driving" +
                "&key=" + "AIzaSyB1gamPoZReCOdC6d3xGo0Q9RzKjD3Uq6M";
    }


    /**
     * show the path between cities
     */
    public void path(){
        if(this.route.getPlaces().length >= 2){
            for(int i = 0; i < this.route.getPlaces().length  - 1; i++){
                LatLng pos1 = this.route.getPlaces()[i].getLocation();
                LatLng pos2 = this.route.getPlaces()[i + 1].getLocation();

                Object dataTransfer[] = new Object[2];
                String url = getDirectionsUrl(pos1, pos2);
                GetDirectionsData getDirectionsData = new GetDirectionsData();
                dataTransfer[0] = mMap;
                dataTransfer[1] = url;
                getDirectionsData.execute(dataTransfer);
            }
        }
    }


    /**
     * Form a place detail url
     * @param placeId the place id
     * @return  the url string
     */
    private String getPlaceDetailUrl(String placeId){
        //  https://maps.googleapis.com/maps/api/place/details/json?placeid=ChIJL9kMna0bdkgRBx1cByEZYik&key=AIzaSyB1gamPoZReCOdC6d3xGo0Q9RzKjD3Uq6M
        StringBuilder googleDirectionsUrl = new StringBuilder("https://maps.googleapis.com/maps/api/place/details/json?");
        googleDirectionsUrl.append("placeid="+placeId);
        googleDirectionsUrl.append("&key="+"AIzaSyB1gamPoZReCOdC6d3xGo0Q9RzKjD3Uq6M");

        return googleDirectionsUrl.toString();
    }


    /**
     * set the information window - when user click the marker on the map
     * it will pop up a window to show the place information
     */
    public void setInfoWindow(){
        if(mMap != null){
            mMap.setInfoWindowAdapter(new GoogleMap.InfoWindowAdapter() {
                @Override
                public View getInfoWindow(Marker marker) {
                    return null;
                }

                @SuppressLint("SetTextI18n")
                @Override
                public View getInfoContents(Marker marker) {
                    @SuppressLint("InflateParams") View view = getLayoutInflater().inflate(R.layout.info_marker, null);
                    TextView placeName = view.findViewById(R.id.placeName);

                    LatLng ll = marker.getPosition();
                    placeName.setText(marker.getTitle());

                    if(marker.getTag()== null ){
                        marker.setTag("start");
                        startMarkerAnimation(marker);  //start the animation when the marker is clicked
                    }
                    setPlaceId(marker.getSnippet());
                    return view;
                }
            });

            //when the user click the marker, save its position in the database
            mMap.setOnInfoWindowClickListener(new GoogleMap.OnInfoWindowClickListener() {
                @Override
                public void onInfoWindowClick(Marker marker) {
                    Object dataTransfer[] = new Object[1];  //the structure to store url, which will passed to the class of GetPlaceDetail to get the detail of the place
                    GetPlaceDetail getPlaceDetailData = new GetPlaceDetail(MapsActivity.this);  //used to get the detail of place based on URL

                    getPlaceDetailData.passSchedulerData(route, currentAddingType, marker);

                    String url = getPlaceDetailUrl(getPlaceId());  //get the URL of place detail.
                    dataTransfer[0] = url;

                    //Log.d("get place detail", url);
                    getPlaceDetailData.execute(dataTransfer);  //mark all the places
                }
            });
        }
    }

    /**
     * Start an bouncing animation when a marker on the map was clicked
     * @param marker - the clicked marker
     */
    private void startMarkerAnimation(final Marker marker) {
        if(marker.getTag() == "start"){
            //System.out.println(marker.getTag());
            //System.out.println(marker.getTag());
            final Handler handler = new Handler();
            final long startTime = SystemClock.uptimeMillis();
            final long duration = 2000;
            final Interpolator interpolator = new BounceInterpolator();

            handler.post(new Runnable() {
                @Override
                public void run() {
                    long elapsed = SystemClock.uptimeMillis() - startTime;
                    float t = Math.max(1 - interpolator.getInterpolation((float) elapsed/duration), 0);
                    marker.setAnchor(0.5f, 1.0f +  t);

                    if (t > 0.0) {
                        handler.postDelayed(this, 16);
                    }
                    else {
                        marker.setTag(null);
                        return;
                    }
                }
            });
        }
    }

    /**
     * Runs a list of searches into the nearby algorithm
     * calls -> {@link #showNearby(SchedulerType, String, ProgressBar, SchedulablePlace)}
     * @param category type of places to show
     * @param searches search word
     * @param place parent place
     */
    public void showNearbyPlaces(SchedulerType category, String[] searches, SchedulablePlace place){
        final ProgressBar spinner = this.findViewById(R.id.progressSpinner);

        spinner.bringToFront();
        spinner.setVisibility(View.VISIBLE);

        for(int i = 0; i < searches.length; i++){
            showNearby(category ,searches[i], i == searches.length - 1 ? spinner : null, place);
        }

        path();
    }

    /**
     * Starts the thread to get all of the nearby places
     * @param category type of places to show
     * @param search search word
     * @param bar the progress bar to show while it is working
     * @param placeToView parent place
     */
    public void showNearby(SchedulerType category, String search, ProgressBar bar, SchedulablePlace placeToView){
        //show all the desired places around the destinations on the map
        for(int i = 0; i < this.route.getPlaces().length; i++){
            Object dataTransfer[] = new Object[5];  //the structure to store map and url, which will passed to the class of GetNearbyPlacesData to mark all the places on the map
            GetNearbyPlacesData getNearbyPlacesData = new GetNearbyPlacesData();  //used to mark all the places on the map based on URL

            dataTransfer[0] = mMap;
            //get the URL of all the hotels around the point (latitude, longitude)
            dataTransfer[1] = getUrl(this.route.getPlaces()[i].getLocation().latitude, this.route.getPlaces()[i].getLocation().longitude, search);
            dataTransfer[2] = bar;
            dataTransfer[3] = category;
            dataTransfer[4] = placeToView;
            getNearbyPlacesData.execute(dataTransfer);  //mark all the places
        }
    }

    /**
     * remove all markers from the map
     */
    public void clearMarkers(){
        List<Marker> remove = new LinkedList<>();
        for (Marker m : this.currentMarkers) {
            m.remove();
        }
        for (Marker m : remove) {
            this.currentMarkers.remove(m);
        }
    }

    /**
     * Shows all the markers of a particular type for a particular place
     * @param place place to show
     * @param type type to show
     */
    public void showMarkerType(SchedulablePlace place, SchedulerType type) {
        List<Marker> remove = new LinkedList<>();
        for (Marker m : this.currentMarkers) {
            m.remove();
            remove.add(m);
        }
        for (Marker m : remove) {
            this.currentMarkers.remove(m);
        }

        switch (type) {
            case ATTRACTION: {
                //         location       name   placeid
                for (Tuple<LatLng, Tuple<String, String>> t : this.attractionsList.get(place)) {
                    this.addMarker(place, t.b.b, t.a, t.b.a, type, true);
                }
                break;
            }
            case LODGE: {
                for (Tuple<LatLng, Tuple<String, String>> t : this.hotelList.get(place)) {
                    this.addMarker(place, t.b.b, t.a, t.b.a, type, true);
                }
                break;
            }
            case FOOD: {
                for (Tuple<LatLng, Tuple<String, String>> t : this.foodList.get(place)) {
                    this.addMarker(place, t.b.b, t.a, t.b.a, type, true);
                }
                break;
            }
            default: {
                break;
            }
        }
    }

    /**
     * Adds a marker to the map and/or to the hash map structures, so that it can be removed and re-added
     * @param place parent location
     * @param placeId google id of the place
     * @param latLng location of the marker
     * @param name name of the marker
     * @param type type of marker ie food, lodge, attraction
     */
    public void addMarker(SchedulablePlace place, String placeId, LatLng latLng, String name, SchedulerType type) {
        addMarker(place, placeId, latLng, name, type, false);
    }

    /**
     * Adds a marker to the map and/or to the hash map structures, so that it can be removed and re-added
     * @param place parent location
     * @param latLng location of the marker
     * @param name name of the marker
     * @param type type of marker ie food, lodge, attraction
     * @param show should it be added to the map
     */
    public void addMarker(SchedulablePlace place, String place_id, LatLng latLng, String name, SchedulerType type, boolean show) {
        MarkerOptions markerOptions = null;
        Tuple<LatLng, Tuple<String, String>> tuple = new Tuple<>(latLng, new Tuple<>(name, place_id));

        if(this.attractionsList.get(place) == null){
            this.attractionsList.put(place, new LinkedList<Tuple<LatLng, Tuple<String, String>>>());
        }
        if(this.hotelList.get(place) == null){
            this.hotelList.put(place, new LinkedList<Tuple<LatLng, Tuple<String, String>>>());
        }
        if(this.foodList.get(place) == null){
            this.foodList.put(place, new LinkedList<Tuple<LatLng, Tuple<String, String>>>());
        }

        switch (type) {
            case ATTRACTION: {
                markerOptions = new MarkerOptions().position(latLng).title(name);
                if(!this.useLazyIcons) {
                    markerOptions.icon(MarkerUtils.bmdAttraction);
                }

                if (!this.attractionsList.get(place).contains(tuple)) {
                    this.attractionsList.get(place).add(tuple);
                }
                break;
            }
            case LODGE: {
                markerOptions = new MarkerOptions().position(latLng).title(name);
                if(!this.useLazyIcons) {
                    markerOptions.icon(MarkerUtils.bmdHotel);
                }

                if (!this.hotelList.get(place).contains(tuple)) {
                    this.hotelList.get(place).add(tuple);
                }
                break;
            }
            case FOOD: {
                markerOptions = new MarkerOptions().position(latLng).title(name);
                if(!this.useLazyIcons) {
                    markerOptions.icon(MarkerUtils.bmdFood);
                }

                if (!this.foodList.get(place).contains(tuple)) {
                    this.foodList.get(place).add(tuple);
                }
                break;
            }
        }

        if(show) {
            markerOptions.snippet(place_id);
            Marker marker = this.mMap.addMarker(markerOptions);
            this.currentMarkers.add(marker);
        }
    }
    //endregion

    //region Saving and Loading

    @Override
    public Activity getContext() {
        return mapsInstance;
    }

    public static MapsActivity getStaticContext(){
        return mapsInstance;
    }

    @Override
    public void performAction(LoadSaveAction action, final boolean fromBundle) {
        switch (action) {
            case LOAD: {
                //List all in popup
                List<Tuple<String, Tuple<String, String>>> details = SaveLoad.getAllShortDetails(this);
                String[] names = new String[details.size()];
                final String[] codes = new String[details.size()];
                final BooleanWrapper trigger = new BooleanWrapper(false);
                for (int i = 0; i < details.size(); i++) {
                    names[i] = details.get(i).b.a;
                    codes[i] = details.get(i).a;
                }
                if (names.length == 0) {
                    names = new String[]{"You have nothing saved..."};
                    trigger.setB(true);
                }
                DialogInterface.OnClickListener listener = new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        try {
                            if (!trigger.isB()) {
                                String code = codes[i];
                                route = SaveLoad.openLocal((ILoadingSaving) getContext(), code);
                                updateRouteMap(route);
                            }
                        } catch (Exception e) {
                            DebugingTools.exception(e, getContext());
                        }
                    }
                };
                AlertDialog.Builder builder = Functions.makePopup("Choose a plan to load", null, names, listener, false, null, this);

                builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        if(fromBundle){
                            finish();
                        }
                        dialogInterface.dismiss();
                    }
                });
                builder.create().show();
                break;
            }
            case SAVE: {
                String name = this.route.getName();

                final EditText input = new EditText(getContext());

                input.setText(this.route.getName());

                // Set up the buttons
                DialogInterface.OnClickListener listener = new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        try {
                            route.setName(input.getText().toString());
                            SaveLoad.saveLocal((ILoadingSaving) getContext(), route);
                        } catch (Exception e) {
                            DebugingTools.exception(e, getContext());
                        }
                    }
                };

                AlertDialog.Builder builder = Functions.makePopup("Set a title for your plan", "Enter a name for your plan below:",
                        null, null, true, listener, getContext());

                builder.setView(input);

                builder.create().show();
            }
            case LOAD_SERVER:{
                SaveLoad.openShare(this, fromBundle);
            }
            default: {
                break;
            }
        }
    }

    /**
     * Called when the options menu is created
     * Adds buttons to the action bar
     * @param menu the menu to add
     * @return if the menu has been created
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        this.getMenuInflater().inflate(R.menu.menu_map_tool_bar, menu);
        return super.onCreateOptionsMenu(menu);
    }

    /**
     * gets the obect for the route
     * @return route object
     */
    public Route getRoute() {
        return this.route;
    }

    @Override
    public void updateRouteMap(Route route) {
        this.route = route;
        this.getmMap().clear();//clear the map
        this.path();
        this.runMapStartup();
    }

    public void setRoute(Route route) {
        this.route = route;
    }

    //endregion
}

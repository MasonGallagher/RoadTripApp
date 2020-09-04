package com.GRP.Group5.RoadTripApp.Activities;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;

import com.GRP.Group5.RoadTripApp.R;
import com.GRP.Group5.RoadTripApp.Scheduling.SchedulablePlace;
import com.GRP.Group5.RoadTripApp.utils.DebugingTools;
import com.GRP.Group5.RoadTripApp.utils.Route;
import com.google.android.gms.maps.model.LatLng;
import com.squareup.picasso.Picasso;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;

/**
 * Created by Yi Shen on 2018/4/8.
 *
 * The activity to create a pop-up window which will show all the places details(place number, address, phone number etc)
 * whenever the user click the on of the places'marker on the map
 */

public class PlaceDetailActivity extends AppCompatActivity {
    private TextView weekHoursView;  //the view which shows the place's week working time
    private ImageButton expandWeekHourBtn;  //the button to hide or show the week working time information

    /**
     * This function will be called when this activity is created.
     * Initialising the activity's layout, GUI components and their event listeners
     * @param savedInstanceState
     */
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_place_detail);

        init();  //initialize all the GUI components and their event listeners


    }

    /**
     * Receiving all the data sent from MapActivity and initialising all the GUI components and the event listeners
     */
    public void init() {
        this.weekHoursView = this.findViewById(R.id.week_hour_txt);  // the text view to show the place's week working information
        this.expandWeekHourBtn = this.findViewById(R.id.expand_week_hour_btn);  // the button used to show or hide the week working information

        //get the intent and the bundle(data holder) from the MapActivity
        Intent intentExtras = getIntent();
        Bundle extrasBundle = intentExtras.getExtras();

        try {

            final Route route = Route.fromJSON(new JSONObject(extrasBundle.getString("routeJSON")));
            final SchedulablePlace child = SchedulablePlace.fromJSON(new JSONObject(extrasBundle.getString("markerPlaceJSON")));
            final SchedulablePlace parent = route.getClosestRoutePlace(child);

            Button addBtn = this.findViewById(R.id.addToTrp_btn);
            addBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    route.addSubLocation(parent, child);
                    MapsActivity.getStaticContext().setRoute(route);
                    finish();
                }
            });

        } catch (JSONException e){
            DebugingTools.exception(e, this);
        } catch (ParseException e) {
            e.printStackTrace();
        }

        //extract all the data from the bundle including place name, address, phone number etc.
        String place_name = extrasBundle.getString("place_name");
        String address = extrasBundle.getString("address");
        String phone_number = extrasBundle.getString("phone_number");
        String website = extrasBundle.getString("website");
        String openOrNot = extrasBundle.getString("openOrNot");
        String weekDayHours = extrasBundle.getString("weekDayHours");
        String rating = extrasBundle.getString("rating");
        String[] photos = extrasBundle.getStringArray("photos");

        //if the place name is not null, update it on the corresponding text view
        if (place_name != null) {
            TextView placeNameView = this.findViewById(R.id.placeName_txt);
            placeNameView.setText(place_name);
        }

        //if the place address is not null, update it on the corresponding text view
        if (address != null) {
            TextView addressView = this.findViewById(R.id.address_txt);
            addressView.setText(address);
        }

        //if the place phone number is not null, update it on the corresponding text view
        if (phone_number != null) {
            TextView phoneNumberView = this.findViewById(R.id.phone_txt);
            phoneNumberView.setText(phone_number);
        }

        //if the place website is not null, update it on the corresponding text view
        if (website != null) {
            TextView websiteView = this.findViewById(R.id.website_txt);
            websiteView.setText(website);
        }

        //if the place opening information is not null, update it on the corresponding text view
        if (openOrNot != null) {
            TextView timeView = this.findViewById(R.id.open_or_not_txt);
            if(openOrNot.equals("true")){
                timeView.setText("Open Now");
                timeView.setTextColor(Color.BLUE);
            }
            else{
                timeView.setText("Closed");
                timeView.setTextColor(Color.RED);
            }
        }

        //if the place rating is not null, update it on the corresponding text view
        if (rating != null) {
            TextView ratingView = this.findViewById(R.id.rating_txt);
            RatingBar ratingBar = this.findViewById(R.id.ratingBar);
            ratingView.setText(rating);
            ratingBar.setRating(Float.parseFloat(rating));
        }

        //if the place photos is not null, update it on the corresponding text view
        if (photos != null) {
            ImageView placeImage = this.findViewById(R.id.place_img);
            placeImage.setScaleType(ImageView.ScaleType.CENTER_CROP);
            String photoReference = photos[0];

            //debug
            //System.out.println("reference: " + photoReference);

            String url = getPhotolUrl(photoReference, 1080);
            loadImageFromUrl(placeImage, url);
        }

        //if the week working time infomation is not null, update it on the corresponding text view
        if (weekDayHours != null) {
            weekHoursView = this.findViewById(R.id.week_hour_txt);
            weekHoursView.setText(weekDayHours);
        }
        else{
            expandWeekHourBtn.setVisibility(View.INVISIBLE);
        }

        //allow the user to show or hide the place's week working time information
        expandWeekHourBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(weekHoursView.getVisibility() == view.VISIBLE){
                    weekHoursView.setVisibility(view.INVISIBLE);
                }
                else{
                    weekHoursView.setVisibility(view.VISIBLE);
                }
            }
        });
    }

    /**
     * To load the image from a photo reference
     * @param placeImage - the image view which show the images of the place
     * @param url - the picture's photo reference
     */
    public void loadImageFromUrl(ImageView placeImage, String url){
        Picasso.with(this).load(url).placeholder(R.mipmap.ic_launcher)
                .error(R.mipmap.ic_launcher)
                .into(placeImage, new com.squareup.picasso.Callback() {
                    @Override
                    public void onSuccess() {}

                    @Override
                    public void onError() {}
                });
    }

    /**
     * The google photo request url generator to generate a google photo which will be sent to google service
     * @param photoReference - the photo reference
     * @param maxWidth - the maximum width of the photo
     * @return
     */
    private String getPhotolUrl(String photoReference, int maxWidth){
        StringBuilder photoUrl = new StringBuilder("https://maps.googleapis.com/maps/api/place/photo?");
        photoUrl.append("maxwidth=" + maxWidth);
        photoUrl.append("&photoreference=" + photoReference);
        photoUrl.append("&key="+"AIzaSyB1gamPoZReCOdC6d3xGo0Q9RzKjD3Uq6M");

        return photoUrl.toString();
    }
}

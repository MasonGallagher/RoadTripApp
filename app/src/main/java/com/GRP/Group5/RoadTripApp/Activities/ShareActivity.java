package com.GRP.Group5.RoadTripApp.Activities;

import android.content.Intent;

import android.os.StrictMode;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ShareActionProvider;
import android.widget.TextView;


import com.GRP.Group5.RoadTripApp.R;
import com.GRP.Group5.RoadTripApp.utils.DebugingTools;
import com.GRP.Group5.RoadTripApp.utils.Functions;
import com.GRP.Group5.RoadTripApp.utils.HTTPAccess;
import com.GRP.Group5.RoadTripApp.utils.Route;
import com.google.android.gms.maps.model.LatLng;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.ParseException;
import java.util.ArrayList;

public class ShareActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_save_load);

        Intent intentExtras = getIntent();
        Bundle extrasBundle = intentExtras.getExtras();

        TextView tv = findViewById(R.id.textCode);
        final Button shareButton = this.findViewById(R.id.shareButton);

        final String code = extrasBundle.getString("routeCode");
        tv.setText(code);

        shareButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent shareIntent = new Intent(Intent.ACTION_SEND);
                shareIntent.setType("text/plain");
                shareIntent.putExtra(Intent.EXTRA_SUBJECT, getResources().getText(R.string.share_message_subject));
                shareIntent.putExtra(Intent.EXTRA_TEXT, getResources().getText(R.string.share_message_start) + "http://grp.ax3.co.uk/trips/" + code);
                startActivity(Intent.createChooser(shareIntent, "Share your plan:"));
            }
        });

    }
}
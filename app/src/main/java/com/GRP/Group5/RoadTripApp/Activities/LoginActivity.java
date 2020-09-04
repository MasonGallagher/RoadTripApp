package com.GRP.Group5.RoadTripApp.Activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;

import com.GRP.Group5.RoadTripApp.R;
import com.GRP.Group5.RoadTripApp.RouteManagement.ILoadingSaving;
import com.GRP.Group5.RoadTripApp.utils.DebugingTools;

/**
 * Created by Yi Shen on 2018/4/6..
 */

public class LoginActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        Button start = this.findViewById(R.id.btn_start);
        Button load = this.findViewById(R.id.btn_open);
        Button load_shared = this.findViewById(R.id.btn_shared);

        start.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final Intent intentBundle = new Intent(LoginActivity.this, FrontActivity.class);  //from FrontActivity to RankActivity
                startActivity(intentBundle);
            }
        });

        load.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final Intent intentBundle = new Intent(LoginActivity.this, MapsActivity.class);  //from FrontActivity to RankActivity

                Bundle bundle = new Bundle();

                bundle.putInt("runMode", ILoadingSaving.LoadSaveAction.LOAD.getId());

                intentBundle.putExtras(bundle);

                startActivity(intentBundle);
            }
        });

        load_shared.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final Intent intentBundle = new Intent(LoginActivity.this, MapsActivity.class);  //from FrontActivity to RankActivity

                Bundle bundle = new Bundle();

                bundle.putInt("runMode", ILoadingSaving.LoadSaveAction.LOAD_SERVER.getId());

                intentBundle.putExtras(bundle);

                startActivity(intentBundle);
            }
        });

        DebugingTools.debuggerWarn(this);
    }
}

package com.GRP.Group5.RoadTripApp.Activities.Listeners;

import android.util.Log;
import android.util.Size;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AutoCompleteTextView;
import android.widget.RadioButton;

import com.GRP.Group5.RoadTripApp.Activities.Adapters.DestinationSearchAdapter;
import com.GRP.Group5.RoadTripApp.R;

/**
 * Created by Ben Clark on 14/04/2018.
 */

public class SearchSwipeListener implements View.OnTouchListener {

    private float downX, downY, upX, upY;
    private int position;
    private DestinationSearchAdapter adapter;
    private View row;
    private View[] components;

    private boolean removed;

    private static int MIN_DISTANCE = 100;

    public SearchSwipeListener(DestinationSearchAdapter adapter, View row, int pos, View[] views, Size screenSize) {
        this.adapter = adapter;
        this.row = row;
        this.position = pos;
        this.components = views;

        this.removed = false;

        this.MIN_DISTANCE = (int) (screenSize.getWidth() * 0.5F);
    }

    private void swipe() {
        if(this.adapter.getCount() - 1 > this.position) {
            this.adapter.remove(this.adapter.getItem(this.position));
            this.removed = true;
        }
    }

    private void setAlpha(float alpha){
        if(alpha < 0){
            alpha = 0;
        } else if(alpha > 255){
            alpha = 255;
        }

        final AutoCompleteTextView text = (AutoCompleteTextView) this.components[0];
        final RadioButton destBtn = (RadioButton) this.components[1];
        final RadioButton startBtn = (RadioButton) this.components[2];

        text.getBackground().setAlpha((int) alpha);
        text.setTextColor(text.getTextColors().withAlpha((int) alpha));

        destBtn.setAlpha(alpha / 255.0F);
        startBtn.setAlpha(alpha / 255.0F);

        destBtn.getBackground().setAlpha((int) alpha);
        startBtn.getBackground().setAlpha((int) alpha);
    }

    @Override
    public boolean onTouch(View view, MotionEvent event) {
        int action = event.getAction();
        if (action == MotionEvent.ACTION_DOWN) {
            if(!this.removed) {
                this.row.setX(0);
                this.setAlpha(255);
            }

            downX = event.getX();
            downY = event.getY();
            return true;
        } else if (action == MotionEvent.ACTION_UP) {
            if(!this.removed) {
                this.row.setX(0);
                this.setAlpha(255);
            }

            upX = event.getX();
            upY = event.getY();

            float deltaX = downX - upX;

            // swipe horizontal
            if (Math.abs(deltaX) > MIN_DISTANCE) {
                // left or right
                if (deltaX > 0) {
                    this.swipe();
                    return true;
                }
                if (deltaX < 0) {
                    this.swipe();
                    return true;
                }
            }
        } else if(action == MotionEvent.ACTION_MOVE){
            float x = event.getX();
            float deltaX = downX - x;
            this.setAlpha(255 - ((Math.abs(deltaX) / MIN_DISTANCE) * 255));
            this.row.setX(x);
            return true;
        }
        if(!this.removed) {
            this.row.setX(0);
            this.setAlpha(255);
        }
        return false;
    }
}

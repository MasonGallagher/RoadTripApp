package com.GRP.Group5.RoadTripApp.Activities.Adapters;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.constraint.ConstraintLayout;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.GRP.Group5.RoadTripApp.Activities.MapsActivity;
import com.GRP.Group5.RoadTripApp.R;
import com.GRP.Group5.RoadTripApp.Scheduling.SchedulablePlace;
import com.GRP.Group5.RoadTripApp.utils.Route;

import org.json.JSONException;

import java.util.List;


/**
 * Created by Yi Shen on 2018/3/29.
 */

public class RecylerAdapter extends RecyclerView.Adapter<RecylerAdapter.ViewHolder>{
    private List<Route> Routes;
    private Context context;

    public RecylerAdapter(List<Route> listItems, Context context) {
        this.Routes = listItems;
        this.context = context;
    }

    public void setRoutes(List<Route> routes) {
        this.Routes = routes;
    }

    @Override
    public RecylerAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
       View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.route_card, parent, false);

       return new ViewHolder(v);
    }

    /**
     * bind the data of route with the card view
     * @param holder
     * @param position
     */
    @Override
    public void onBindViewHolder(RecylerAdapter.ViewHolder holder, int position) {

        final Route route = Routes.get(position);
        holder.textViewDesc.setText("Route: " + route.getDesc());
        holder.textViewDistance.setText(route.getTotalDistance().toString());
        //holder.cardViewImage.setImageResource(route.getPictureId()); TODO

        int hrs = (int) Math.floor(route.getTotalTime());
        int mins = (int) Math.floor((route.getTotalTime() - hrs) * 60);
        String timeStr = "";

        if(hrs > 0){
            if(hrs == 1){
                timeStr = hrs + " hr";
            }
            else {
                timeStr = hrs + " hrs";
            }

            if(mins > 0){
                timeStr += " ";
                if(mins == 1){
                    timeStr += mins + " min";
                }
                else {
                    timeStr += mins + " mins";
                }
            }
        }
        else {
            if(mins > 1){
                timeStr = mins + " mins";
            }
            else {
                timeStr = mins + " min";
            }
        }

        holder.textViewTime.setText(" " + timeStr);

        holder.constraintLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SchedulablePlace[] places = route.getPlaces();
                int citiesNum = places.length;

                Intent openMap = new Intent(context, MapsActivity.class);
                Bundle bundle = new Bundle();  //used to transfer data to the RankActivity

                try {
                    bundle.putString("jsonRoute", route.toJSON().toString());
                } catch (JSONException e){
                    e.printStackTrace();
                }

                bundle.putDouble("distance", route.getTotalDistance());
                bundle.putDouble("time", route.getTotalTime());

                openMap.putExtras(bundle);
                context.startActivity(openMap);
                ((Activity)context).finish();
            }
        });
    }

    @Override
    public int getItemCount() {
        return Routes.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder{
        public TextView textViewDistance;
        public TextView textViewTime;
        public TextView textViewDesc;
        public ConstraintLayout constraintLayout;
        public ImageView cardViewImage;
        public int pictureIndex = -1;
        public String pictureName = null;

        public ViewHolder(View itemView) {
            super(itemView);
            textViewDesc = (TextView) itemView.findViewById(R.id.textViewDesc);
            textViewDistance = (TextView) itemView.findViewById(R.id.textViewDistance);
            textViewTime = (TextView) itemView.findViewById(R.id.textViewTime);
            constraintLayout = (ConstraintLayout) itemView.findViewById(R.id.relativeLayout);
            cardViewImage = (ImageView) itemView.findViewById(R.id.cardViewImage);
            cardViewImage.setScaleType(ImageView.ScaleType.CENTER_CROP);

            //System.out.println("131131313131312312312312313");
            if(pictureIndex != -1){
                cardViewImage.setImageResource(
                        context.getResources().getIdentifier(
                                pictureName,
                                "drawable",
                                context.getPackageName()
                        ));
            }
            else{
                pictureIndex = 1 +(int)(Math.random() * 10);
                pictureName = "card_view_background" + String.valueOf(pictureIndex);

                cardViewImage.setImageResource(
                        context.getResources().getIdentifier(
                                pictureName,
                                "drawable",
                                context.getPackageName()
                        ));
            }
        }
    }
}

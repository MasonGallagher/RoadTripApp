package com.GRP.Group5.RoadTripApp.Activities.Adapters;

import android.content.Context;
import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.GRP.Group5.RoadTripApp.R;
import com.GRP.Group5.RoadTripApp.Scheduling.SchedulablePlace;
import com.GRP.Group5.RoadTripApp.utils.Plan;
import com.GRP.Group5.RoadTripApp.utils.Route;
import com.GRP.Group5.RoadTripApp.utils.Tuple;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

public class ScheduleAdapter extends ArrayAdapter<Tuple<SchedulablePlace, Plan>> {

    List<Tuple<SchedulablePlace, Plan>> plans;

    public Route route;

    /**
     * Initialiser
     * @param context
     * @param resource
     */
    public ScheduleAdapter(@NonNull Context context, @LayoutRes int resource) {
        super(context, resource);

        this.plans = new LinkedList<>();
    }

    /**
     * adds a place, plan tuple to adapter
     * @param object
     */
    @Override
    public void add(@Nullable Tuple<SchedulablePlace, Plan> object) {
        this.plans.add(object);
        super.add(object);
    }

    /**
     * adds a place and plan to adapter
     * @param place
     * @param plan
     */
    public void add(SchedulablePlace place, Plan plan){
        this.add(new Tuple<>(place, plan));
    }

    /**
     * adds a hashmap of places, with their assigned plans to the adapter
     * @param map
     * @param visitOrder
     */
    public void add(HashMap<SchedulablePlace, Plan> map, SchedulablePlace[] visitOrder){
        for(int i = 0; i < visitOrder.length; i++){
            Plan plan = map.get(visitOrder[i]);
            if(plan != null) {
                this.add(visitOrder[i], plan);
            }
        }
    }

    /**
     * returns the number of items in the adapter
     * @return
     */
    @Override
    public int getCount() {
        return Math.max(this.plans.size(), 1);
    }

    /**
     * returns the item at a given position
     * @param position
     * @return
     */
    @Nullable
    @Override
    public Tuple<SchedulablePlace, Plan> getItem(int position) {
        if(this.plans.size() == 0){
            return null;
        } else {
            return this.plans.get(position);
        }
    }

    /**
     * clears the adapter of any content
     */
    @Override
    public void clear() {
        this.plans.clear();
        super.clear();
    }

    /**
     * removes the specified object from the adapter
     * @param object
     */
    @Override
    public void remove(@Nullable Tuple<SchedulablePlace, Plan> object) {
        this.plans.remove(object);
        super.remove(object);
    }

    /**
     * Generates / inflates the view for the listview at a give position
     * @param position
     * @param convertView
     * @param parent
     * @return
     */
    @NonNull
    @Override
    public View getView(final int position, @Nullable View convertView, @NonNull ViewGroup parent) {

        //inflates the view
        LayoutInflater inflater = (LayoutInflater) this.getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View row;

        if(this.plans.size() == 0){
            row = inflater.inflate(R.layout.empty_plan_layout_comp, parent, false);
        } else {
            row = inflater.inflate(R.layout.plan_layout_comp, parent, false);

            //initalises variables and configures them
            final TextView placeTitle = row.findViewById(R.id.placeTitle);
            TextView planText = row.findViewById(R.id.planText);
            TextView accuracy = row.findViewById(R.id.accuracy);
            TextView accuracyLabel = row.findViewById(R.id.accuracyLabel);

            Tuple<SchedulablePlace, Plan> tuple = this.getItem(position);
            final SchedulablePlace place = tuple.a;
            final Plan plan = tuple.b;

            //Sets the contents of the above views to represent the data given
            if(this.route != null && this.route.isPlaceLocked(place)) {
                placeTitle.setText(place.getName() + "\t\tLOCKED");
            } else {
                placeTitle.setText(place.getName());
            }
            planText.setText(plan.toPlanString() + "\n");

            //accuracy.setText((int) Math.floor(plan.evaluate() * 100) + "%");

            String planNo = String.valueOf(plan.getID());

            while(planNo.length() < 3){
                planNo = "0" + planNo;
            }

            accuracy.setText(planNo);
            accuracyLabel.setText("Plan #");

            //changes the colour of the text based on the accuracy of the provided solution
            if (plan.evaluate() > 0.85) {
                accuracy.setTextColor(ContextCompat.getColor(this.getContext(), R.color.green));
            } else {
                accuracy.setTextColor(ContextCompat.getColor(this.getContext(), R.color.red));
            }

            //initialises and configures a hidable list that allows users to remove items from the data set
            final ListView removalList = row.findViewById(R.id.removalList);

            removalList.setAdapter(new PlanRemovalListAdapter(getContext(), R.layout.activity_scheduler, plan, this.route, place));

            ViewGroup.LayoutParams params = removalList.getLayoutParams();
            params.height = 0;
            removalList.setLayoutParams(params);

            //If a long press is done then open or close the remove display
            row.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View view) {
                    ViewGroup.LayoutParams params = removalList.getLayoutParams();
                    if(params.height > 0){
                        //hide
                        params.height = 0;
                    } else {
                        //display
                        params.height = removalList.getAdapter().getCount() * 65;
                    }
                    removalList.setLayoutParams(params);
                    return true;
                }
            });

            //Locks or unlocks the place schedule so that it can be prevented from being re-calculated
            row.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (route != null) {
                        if (route.isPlaceLocked(place)) {
                            route.unlockPlace(place);
                            placeTitle.setText(place.getName());
                        } else {
                            route.lockPlace(place);
                            placeTitle.setText(place.getName() + "\t\tLOCKED");
                        }
                    }
                }
            });
        }
        return row;
    }
}

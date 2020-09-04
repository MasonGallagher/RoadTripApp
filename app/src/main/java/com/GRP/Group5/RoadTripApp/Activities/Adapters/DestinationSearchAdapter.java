package com.GRP.Group5.RoadTripApp.Activities.Adapters;

import android.content.Context;
import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.constraint.ConstraintLayout;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.RadioButton;
import android.widget.Toast;

import com.GRP.Group5.RoadTripApp.Activities.FrontActivity;
import com.GRP.Group5.RoadTripApp.R;
import com.GRP.Group5.RoadTripApp.utils.WayPointSearch;

import java.util.LinkedList;
import java.util.List;

/**
 * Created by Ben Clark on 07/12/2017.
 *
 * This class is a screen adapter - this stores and array or list of values, based on this list
 * this class will generate a number of views, which are then added to the screen.
 * This class allows for screen dynamics allows the adding and removal of features or buttons
 * without having to regenerate or reopen a GUI or app.
 */
public class DestinationSearchAdapter extends ArrayAdapter<WayPointSearch> {
    private static List<WayPointSearch> locationList = new LinkedList<>();
    private AppCompatActivity activity;

    /**
     * Default constructor
     * @param context
     * @param resource
     * @param act - activity that this shall be adding to
     */
    public DestinationSearchAdapter(@NonNull Context context, @LayoutRes int resource, AppCompatActivity act) {
        super(context, resource);
        this.activity = act;
    }

    public List<WayPointSearch> getDestinationList() {
        return this.locationList;
    }

    /**
     * adds searches to the list
     * @param object - object to be added
     */
    @Override
    public void add(WayPointSearch object) {
        this.locationList.add(object);
        super.add(object);
    }

    /**
     * gets the count of the adapter arry/list
     * @return - int count
     */
    @Override
    public int getCount() {
        return this.locationList.size() + 1;
    }

    /**
     * gets the search at a specific index
     * @param index - position to return
     * @return - that value in the list
     */
    @Override
    public WayPointSearch getItem(int index) {
        return this.locationList.get(index);
    }

    /**
     * removes everything from the linked list
     */
    @Override
    public void clear() {
        this.locationList.clear();
        super.clear();
    }

    /**
     * Removes a search from the system and redraws the screen
     * @param object
     */
    @Override
    public void remove(@Nullable WayPointSearch object) {
        this.locationList.remove(object);
        super.remove(object);
    }

    /**
     * For every value in the array/list the 'isDestination' is set to val
     * Also every radio box that displays this is either set to checked or unchecked based on the val
     * @param val - boolean to set all checkboxes to
     * @param parent - containing view group - for setting the radioboxes
     */
    public void setAllIsDest(boolean val, ViewGroup parent){
        for(int i = 0; i < this.getCount() - 1; i++){
            locationList.get(i).setDestination(val);
            RadioButton btn = ((RadioButton)parent.findViewById(Integer.parseInt("111" + String.valueOf(i))));
            if(btn != null){
                btn.setChecked(val);
            }
        }
    }

    /**
     * For every value in the array/list the 'isStart' is set to val
     * Also every radio box that displays this is either set to checked or unchecked based on the val
     * @param val - boolean to set all checkboxes to
     * @param parent - containing view group - for setting the radioboxes
     */
    public void setAllIsStart(boolean val, ViewGroup parent){
        for(int i = 0; i < this.getCount() - 1; i++){
            locationList.get(i).setStart(val);
            RadioButton btn = ((RadioButton) parent.findViewById(Integer.parseInt("110" + String.valueOf(i))));
            if(btn != null) {
                btn.setChecked(val);
            }
        }
    }

    /**
     * gets the row for each position in the list
     * @param position - current position to render/generate
     * @param convertView - base view
     * @param parent - the parent view that the row will be added to
     * @return - returns the view to be rendered
     */
    @Override
    public View getView(final int position, View convertView, final ViewGroup parent) {
        //generate inflator and row view
        LayoutInflater inflater = (LayoutInflater) this.getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View row;

        //is current position the search or add button
        if(position < this.locationList.size()) {
            //SEARCH
            //inflate / generate view
            row = inflater.inflate(R.layout.search_layout_comp, parent, false);
            final AutoCompleteTextView text = row.findViewById(R.id.destEditText);
            final RadioButton destBtn = row.findViewById(R.id.isDestButton);
            final RadioButton startBtn = row.findViewById(R.id.isStartButton);
            final ConstraintLayout tableLayout = row.findViewById(R.id.layout);
            final Button removeBtn = row.findViewById(R.id.removeBtn);

            //set ids of both text edit and button - allows getting of all of the buttons/text
            // "100" is prefix for text edits
            // "111" is prefix for dest buttons
            // "110" is prefix for start buttons
            // "101" is prefix for remove buttons
            text.setId(Integer.parseInt("100" + String.valueOf(position)));
            destBtn.setId(Integer.parseInt("111" + String.valueOf(position)));
            startBtn.setId(Integer.parseInt("110" + String.valueOf(position)));
            removeBtn.setId(Integer.parseInt("101" + String.valueOf(position)));

            //sets value if one exists
            startBtn.setChecked(this.locationList.get(position).isStart());
            destBtn.setChecked(this.locationList.get(position).isDestination());
            text.setText(this.locationList.get(position).getSearchTerm());

            //when remove button is added

            removeBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    try {
                        ((FrontActivity) activity).getSearchAdapter().remove(locationList.get(position));
                    } catch (Exception e){
                        //failed to remove (removes issues with clicks)
                    }
                }
            });

            //when the dest button is pressed
            destBtn.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    //clear all buttons, set list value, set this value
                    try {
                        setAllIsDest(false, parent);
                        locationList.get(position).setDestination(isChecked);
                        destBtn.setChecked(isChecked);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            });

            //when the start button is pressed
            startBtn.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    //clear all buttons, set list value, set this value
                    setAllIsStart(false, parent);
                    locationList.get(position).setStart(isChecked);
                    startBtn.setChecked(isChecked);
                }
            });

            //when text is changed
            text.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                    //Do nothing
                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    //edit list value
                    locationList.get(position).setSearchTerm(String.valueOf(s));
                }

                @Override
                public void afterTextChanged(Editable s) {
                    //do nothing
                }
            });

            text.setAdapter(((FrontActivity) activity).getPlaceAutoCompleteAdapter());

            //SearchSwipeListener listener = new SearchSwipeListener(this, tableLayout, position, new View[] {text, startBtn, destBtn}, this.screenSize);

            //row.setOnTouchListener(listener);

        } else {
            //ADD BUTTOn
            //inflate and get button
            row = inflater.inflate(R.layout.add_more_comp_layout, parent, false);
            Button addBtn = (Button) row.findViewById(R.id.addBtn);

            //when button is clicked
            addBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    //check that previous text field was not empty
                    //if not add another search box
                    if(locationList.get(position - 1).getSearchTerm() != "" ) {
                        ((FrontActivity) activity).getSearchAdapter().add(new WayPointSearch("", false, false));
                    } else {
                        Toast.makeText(activity, "Please fill the current searches before adding more!", Toast.LENGTH_LONG).show();
                    }
                }
            });
        }

        //returns final row to be added to screen
        return row;
    }
}
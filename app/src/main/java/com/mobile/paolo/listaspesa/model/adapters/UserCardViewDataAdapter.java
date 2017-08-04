package com.mobile.paolo.listaspesa.model.adapters;

import java.util.List;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;

import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;

import com.mobile.paolo.listaspesa.R;
import com.mobile.paolo.listaspesa.model.objects.User;


/**
 * Created by paolo on 04/07/17.
 */

public class UserCardViewDataAdapter extends RecyclerView.Adapter<UserCardViewDataAdapter.ViewHolder>
{
    // The data to show.
    private List<User> userList;

    // visualizationMode determines how to display single elements
    private int visualizationMode;
    private static final int CREATION_MODE = 1;
    private static final int MANAGEMENT_MODE = 2;

    public UserCardViewDataAdapter(List<User> users, int visualizationMode)
    {
        this.userList = users;
        this.visualizationMode = visualizationMode;
    }

    // Create new views
    @Override
    public UserCardViewDataAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        // Create a new view using the .xml file that defines a row
        View itemLayoutView = null;
        if(visualizationMode == CREATION_MODE)
        {
            itemLayoutView = LayoutInflater.from(parent.getContext()).inflate(R.layout.card_create_group_user_layout, null);
        }
        if(visualizationMode == MANAGEMENT_MODE)
        {
            itemLayoutView = LayoutInflater.from(parent.getContext()).inflate(R.layout.card_manage_group_user_layout, null);
        }


        // Create ViewHolder. (A ViewHolder describes an item view and metadata about its place within the RecyclerView.)
        ViewHolder viewHolder = new ViewHolder(itemLayoutView);

        return viewHolder;
    }

    @Override
    public void onBindViewHolder(ViewHolder viewHolder, int position) {

        final int pos = position;

        // Fill the row fields using data from a single user.
        viewHolder.cardTextViewName.setText(userList.get(position).getUsername());

        viewHolder.cardTextViewAddress.setText(userList.get(position).getAddress());

        if(visualizationMode == CREATION_MODE)
        {
            viewHolder.cardCheckbox.setChecked(userList.get(position).isChecked());

            // Save the user in the tag field of the checkbox, it'll be used later.
            viewHolder.cardCheckbox.setTag(userList.get(position));

            // When a checkbox is clicked:
            viewHolder.cardCheckbox.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {

                    // Retrieve the corresponding user.
                    CheckBox checkbox = (CheckBox) v;
                    User user = (User) checkbox.getTag();

                    // Set the 'checked' field of the user both in the checkbox tag field and in the list
                    user.setChecked(checkbox.isChecked());
                    userList.get(pos).setChecked(checkbox.isChecked());

                }
            });
        }
    }

    // Return the size of the model list.
    @Override
    public int getItemCount() {
        return userList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        public TextView cardTextViewName;
        public TextView cardTextViewAddress;
        public CheckBox cardCheckbox;

        public User user;

        public ViewHolder(View itemLayoutView) {
            super(itemLayoutView);

            cardTextViewName = (TextView) itemLayoutView.findViewById(R.id.cardTextViewName);
            cardTextViewAddress = (TextView) itemLayoutView.findViewById(R.id.cardTextViewAddress);
            cardCheckbox = (CheckBox) itemLayoutView.findViewById(R.id.cardCheckbox);
        }
    }

    // method to access in activity after updating selection
    public List<User> getUserList() {
        return userList;
    }
}

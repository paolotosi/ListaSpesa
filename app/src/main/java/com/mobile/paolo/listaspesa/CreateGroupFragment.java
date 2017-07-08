package com.mobile.paolo.listaspesa;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.VolleyError;
import com.mobile.paolo.listaspesa.database.GroupsDatabaseHelper;
import com.mobile.paolo.listaspesa.database.UsersDatabaseHelper;
import com.mobile.paolo.listaspesa.model.User;
import com.mobile.paolo.listaspesa.model.UserCardViewDataAdapter;
import com.mobile.paolo.listaspesa.network.NetworkResponseHandler;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class CreateGroupFragment extends Fragment
{
    public static CreateGroupFragment newInstance()
    {
        CreateGroupFragment fragment = new CreateGroupFragment();
        return fragment;
    }

    private RecyclerView recyclerView;
    private RecyclerView.Adapter adapter;
    private RecyclerView.LayoutManager mLayoutManager;
    private ListView listView;
    private ArrayList<User> userModelList = new ArrayList<>();
    private ArrayList<String> userList = new ArrayList<>();
    private ArrayList<String> selectedUserList = new ArrayList<>();
    private NetworkResponseHandler networkResponseHandler;
    private UsersDatabaseHelper usersDatabaseHelper;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        // Load fragment.
        View loadedFragment = inflater.inflate(R.layout.fragment_create_group, container, false);

        // Initialize the RecyclerView.
        setupRecyclerView(loadedFragment);

        // Define what to do when the server response is received.
        setupNetworkMessageHandler();

        // Send the network request to get all users.
        UsersDatabaseHelper.sendGetAllUsersRequest(getActivity().getApplicationContext(), networkResponseHandler);

        // Setup the confirm button listener.
        setupConfirmButtonListener(loadedFragment);

        return loadedFragment;
    }

    private void setupNetworkMessageHandler()
    {
        this.networkResponseHandler = new NetworkResponseHandler() {

            @Override
            public void onSuccess(JSONObject jsonResponse) {
                populateUserList(jsonResponse);
            }

            @Override
            public void onError(VolleyError error) {

            }
        };
    }

    // Populate userList with server response
    private void populateUserList(JSONObject serverResponse)
    {
        try {
            // Split the response in a list.
            JSONArray jsonUserList = serverResponse.getJSONArray("users");

            // Get logged user to exclude him from the list
            User loggedUser = getLoggedUser();

            // For each user, create a User object and add it to the list.
            for(int i = 0; i < jsonUserList.length(); i++)
            {
                JSONObject jsonUser = (JSONObject) jsonUserList.get(i);
                User toBeAdded = new User(jsonUser);
                if(loggedUser.getId() != toBeAdded.getId())
                {
                    userModelList.add(toBeAdded);
                }
            }

            // Tell the RecyclerView to reload elements
            adapter.notifyDataSetChanged();

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void setupConfirmButtonListener(final View fragment)
    {
        fragment.findViewById(R.id.confirmButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                checkSelectedItems(fragment);
            }
        });
    }

    private void checkSelectedItems(View fragment)
    {
//        String data = "";
//        List<User> userList = ((UserCardViewDataAdapter) adapter).getUserList();
//
//        for (int i = 0; i < userList.size(); i++) {
//            User singleUser = userList.get(i);
//            if (singleUser.isChecked())
//            {
//                data = data + "\n" + singleUser.getUsername().toString();
//            }
//        }
        sendCreateGroupRequest(fragment);
    }

    private void sendCreateGroupRequest(View fragment)
    {
        JSONObject jsonRequest = new JSONObject();
        try {
            jsonRequest.put("groupName", ((EditText) fragment.findViewById(R.id.groupNameField)).getText().toString());
            JSONArray selectedIDs = new JSONArray();
            List<User> userList = ((UserCardViewDataAdapter) adapter).getUserList();

            int pos = 0;
            for (int i = 0; i < userList.size(); i++)
            {
                User singleUser = userList.get(i);
                if (singleUser.isChecked())
                {
                    selectedIDs.put(pos, singleUser.getId());
                    pos++;
                }
            }
            jsonRequest.put("selectedUsers", selectedIDs);

        } catch (JSONException e) {
            e.printStackTrace();
        }
        Log.d("Json request", jsonRequest.toString());

        NetworkResponseHandler createGroupLogic = new NetworkResponseHandler() {
            @Override
            public void onSuccess(JSONObject response) {
                Log.d("serverResponse", response.toString());
            }

            @Override
            public void onError(VolleyError error) {

            }
        };

        GroupsDatabaseHelper.sendCreateGroupRequest(jsonRequest, getContext(), createGroupLogic);


    }

    private void setupRecyclerView(View loadedFragment)
    {
        recyclerView = (RecyclerView) loadedFragment.findViewById(R.id.recyclerViewUsers);

        // use this setting to improve performance if you know that changes
        // in content do not change the layout size of the RecyclerView
        recyclerView.setHasFixedSize(true);

        // use a linear layout manager
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity().getApplicationContext()));

        // create an Object for Adapter
        adapter = new UserCardViewDataAdapter(userModelList);

        // set the adapter object to the Recyclerview
        recyclerView.setAdapter(adapter);
    }

    private User getLoggedUser()
    {
        User user = null;
        SharedPreferences sharedPref = getActivity().getSharedPreferences("prefs", Context.MODE_PRIVATE);
        String jsonLoggedUser = sharedPref.getString(getResources().getString(R.string.LOGGED_USER), "No user logged");
        try {
            user = new User(new JSONObject(jsonLoggedUser));
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return user;
    }

}

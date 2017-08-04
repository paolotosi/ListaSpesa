package com.mobile.paolo.listaspesa;

import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ListView;

import com.android.volley.VolleyError;
import com.mobile.paolo.listaspesa.database.GroupsDatabaseHelper;
import com.mobile.paolo.listaspesa.database.UsersDatabaseHelper;
import com.mobile.paolo.listaspesa.model.objects.User;
import com.mobile.paolo.listaspesa.model.adapters.UserCardViewDataAdapter;
import com.mobile.paolo.listaspesa.network.NetworkResponseHandler;
import com.mobile.paolo.listaspesa.utility.GlobalValuesManager;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class CreateGroupFragment extends Fragment
{
    // JSON tags
    private static final String TAG_SUCCESS = "success";
    private static final int SUCCESS = 1;

    // Feedback codes
    private static final int GROUP_CREATION_OK = 1;
    private static final int GROUP_CREATION_KO_NO_NAME = 2;
    private static final int GROUP_CREATION_KO_NO_USERS = 3;
    private static final int CONNECTION_ERROR = 4;
    private static final int REMOTE_ERROR = 5;

    private RecyclerView recyclerView;
    private RecyclerView.Adapter adapter;
    private RecyclerView.LayoutManager mLayoutManager;
    private ListView listView;
    private ArrayList<User> userModelList = new ArrayList<>();
    private ArrayList<String> userList = new ArrayList<>();
    private ArrayList<String> selectedUserList = new ArrayList<>();
    private NetworkResponseHandler networkResponseHandler;
    private UsersDatabaseHelper usersDatabaseHelper;

    public static CreateGroupFragment newInstance()
    {
        CreateGroupFragment fragment = new CreateGroupFragment();
        return fragment;
    }

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
            User loggedUser = GlobalValuesManager.getInstance(getContext()).getLoggedUser();

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
        sendCreateGroupRequest(fragment);
    }

    private void sendCreateGroupRequest(View fragment)
    {
        /*
            JSON structure:
                groupName => name
                selectedUsers => [id1, id2...]
         */

        boolean okToSend = true;
        JSONObject jsonRequest = new JSONObject();
        try {
            // Add user inserted group name in JSON
            String groupName = ((EditText) fragment.findViewById(R.id.groupNameField)).getText().toString();
            if(!groupName.isEmpty())
            {
                jsonRequest.put("groupName", groupName);
            }
            else
            {
                okToSend = false;
                showFeedback(GROUP_CREATION_KO_NO_NAME);
            }

            // selectedIDs will contain the IDs of every member
            JSONArray selectedIDs = new JSONArray();

            // The first one is the logged user
            //selectedIDs.put(0, getLoggedUser().getId());
            User loggedUser = GlobalValuesManager.getInstance(getContext()).getLoggedUser();
            selectedIDs.put(0, loggedUser.getId());

            // userList will be used to store checkbox-selected users
            List<User> userList = ((UserCardViewDataAdapter) adapter).getUserList();

            // For each user in the list, if he's checked add him to selectedIDs
            int pos = 1;
            for (int i = 0; i < userList.size(); i++)
            {
                User singleUser = userList.get(i);
                if (singleUser.isChecked())
                {
                    selectedIDs.put(pos, singleUser.getId());
                    pos++;
                }
            }
            // Add all IDs in JSON
            jsonRequest.put("selectedUsers", selectedIDs);

        } catch (JSONException e) {
            e.printStackTrace();
        }
        // Debug JSON
        Log.d("Json request", jsonRequest.toString());

        NetworkResponseHandler createGroupLogic = new NetworkResponseHandler() {
            @Override
            public void onSuccess(JSONObject response) {
                // Debug
                Log.d("serverResponse", response.toString());

                // Read success tag and show feedback
                try {
                    if(response.getInt(TAG_SUCCESS) == SUCCESS)
                    {
                        showFeedback(GROUP_CREATION_OK);
                    }
                    else
                        showFeedback(REMOTE_ERROR);

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onError(VolleyError error) {
                showFeedback(CONNECTION_ERROR);

            }
        };

        if(okToSend)
        {
            GroupsDatabaseHelper.sendCreateGroupRequest(jsonRequest, getContext(), createGroupLogic);
        }

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
        adapter = new UserCardViewDataAdapter(userModelList, 1);

        // set the adapter object to the Recyclerview
        recyclerView.setAdapter(adapter);
    }

    private void showFeedback(int feedbackCode)
    {
        Snackbar snackShowStatus;
        switch (feedbackCode)
        {
            case GROUP_CREATION_OK:            snackShowStatus = Snackbar.make(getActivity().findViewById(R.id.activity_home), R.string.creation_OK, Snackbar.LENGTH_LONG); break;
            case GROUP_CREATION_KO_NO_NAME:    snackShowStatus = Snackbar.make(getActivity().findViewById(R.id.activity_home), R.string.creation_KO_no_name, Snackbar.LENGTH_LONG); break;
            case GROUP_CREATION_KO_NO_USERS:   snackShowStatus = Snackbar.make(getActivity().findViewById(R.id.activity_home), R.string.creation_KO_no_users, Snackbar.LENGTH_LONG); break;
            case CONNECTION_ERROR:             snackShowStatus = Snackbar.make(getActivity().findViewById(R.id.activity_home), R.string.connection_error, Snackbar.LENGTH_LONG); break;
            case REMOTE_ERROR:                 snackShowStatus = Snackbar.make(getActivity().findViewById(R.id.activity_home), R.string.remote_group_creation_error, Snackbar.LENGTH_LONG); break;
            default:                           snackShowStatus = Snackbar.make(getActivity().findViewById(R.id.activity_home), R.string.generic_error, Snackbar.LENGTH_LONG); break;
        }
        snackShowStatus.show();
    }

}

package com.mobile.paolo.listaspesa.view.home.group;

import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.android.volley.VolleyError;
import com.mobile.paolo.listaspesa.R;
import com.mobile.paolo.listaspesa.database.remote.GroupsDatabaseHelper;
import com.mobile.paolo.listaspesa.database.remote.UsersDatabaseHelper;
import com.mobile.paolo.listaspesa.model.adapters.UserCardViewDataAdapter;
import com.mobile.paolo.listaspesa.model.objects.Group;
import com.mobile.paolo.listaspesa.model.objects.User;
import com.mobile.paolo.listaspesa.network.NetworkResponseHandler;
import com.mobile.paolo.listaspesa.utility.GlobalValuesManager;
import com.mobile.paolo.listaspesa.view.home.HomeFragmentContainer;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AddMemberFragment extends Fragment {
    // JSON tags
    private static final String TAG_SUCCESS = "success";
    private static final int SUCCESS = 1;

    // Feedback codes
    private static final int GROUP_ADD_OK = 1;
    private static final int GROUP_ADD_KO_NO_USERS = 3;
    private static final int CONNECTION_ERROR = 4;
    private static final int REMOTE_ERROR = 5;

    // RecyclerView, adapter and model list
    private RecyclerView recyclerView;
    private RecyclerView.Adapter adapter;
    private ArrayList<User> userModelList = new ArrayList<>();

    // Network response logic
    private NetworkResponseHandler fetchUsersResponseHandler;
    private NetworkResponseHandler modifyGroupResponseHandler;
    private NetworkResponseHandler groupDetailsResponseHandler;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        // Load fragment.
        View loadedFragment = inflater.inflate(R.layout.fragment_add_member, container, false);

        // Setup toolbar
        setupToolbar(loadedFragment); //TODO SET AS ACTION BAR

        // Initialize the RecyclerView.
        setupRecyclerView(loadedFragment);

        // Define what to do when the server response to get all users is received.
        setupFetchUsersResponseHandler();

        // Send the network request to get all users.
        UsersDatabaseHelper.sendGetAllUsersRequest(getActivity().getApplicationContext(), fetchUsersResponseHandler);

        // Setup the confirm button listener.
        setupConfirmButtonListener(loadedFragment);

        return loadedFragment;
    }

    private void setupFetchUsersResponseHandler()
    {
        this.fetchUsersResponseHandler = new NetworkResponseHandler() {

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
        if(userModelList.isEmpty()) {
            try {
                // Split the response in a list.
                JSONArray jsonUserList = serverResponse.getJSONArray("users");

                // Get logged user to exclude him from the list
                List<User> groupUser = GlobalValuesManager.getInstance(getContext()).getLoggedUserGroup().getMembers();

                // For each user, create a User object and add it to the list.
                for (int i = 0; i < jsonUserList.length(); i++) {
                    Boolean inGroup = false;
                    User toBeAdded = null;
                    for (int j = 0; j < groupUser.size(); j++) {
                        JSONObject jsonUser = (JSONObject) jsonUserList.get(i);
                        toBeAdded = new User(jsonUser);
                        if (groupUser.get(j).getID() == toBeAdded.getID()) {
                            inGroup = true;
                        }
                    }

                    if (!inGroup) {
                        userModelList.add(toBeAdded);
                    }

                }

                // Tell the RecyclerView to reload elements
                adapter.notifyDataSetChanged();

            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    private void setupConfirmButtonListener(final View fragment)
    {
        fragment.findViewById(R.id.confirmAddButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendUpdateGroupRequest();
            }
        });
    }

    private void sendUpdateGroupRequest()
    {
        JSONObject jsonRequest = new JSONObject();
        try {
            // selectedIDs will contain the IDs of every member
            JSONArray selectedIDs = new JSONArray();

            // Get the user list
            List<User> userList = ((UserCardViewDataAdapter) adapter).getUserList();

            // For each user in the list, if he's checked add him to selectedIDs
            int pos = 0;
            for (int i = 0; i < userList.size(); i++)
            {
                User singleUser = userList.get(i);
                if (singleUser.isChecked())
                {
                    selectedIDs.put(pos, singleUser.getID());
                    pos++;
                }
            }

            // Add all IDs in JSON
            jsonRequest.put("selectedUsers", selectedIDs);
            jsonRequest.put("groupID", GlobalValuesManager.getInstance(getContext()).getLoggedUserGroup().getID());

        } catch (JSONException e) {
            e.printStackTrace();
        }
        // Debug JSON
        Log.d("Json request", jsonRequest.toString());

        // Define what to do on server response
        setupUpdateGroupResponseHandler();

        GroupsDatabaseHelper.sendUpdateGroupRequest(jsonRequest, getContext(), modifyGroupResponseHandler);


    }

    private void setupUpdateGroupResponseHandler()
    {
        this.modifyGroupResponseHandler = new NetworkResponseHandler() {
            @Override
            public void onSuccess(JSONObject response) {
                // Debug
                Log.d("serverResponse", response.toString());

                // Read success tag and show feedback
                try {
                    if(response.getInt(TAG_SUCCESS) == SUCCESS)
                    {
                        showFeedback(GROUP_ADD_OK);
                        sendGetGroupDetailsRequest();
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
    }

    private void setupRecyclerView(View loadedFragment)
    {
        recyclerView = (RecyclerView) loadedFragment.findViewById(R.id.recyclerViewUsersAdd);

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

    private void setupToolbar(View loadedFragment)
    {
        Toolbar toolbar = (Toolbar) loadedFragment.findViewById(R.id.addMembersToolbar);
        toolbar.setTitle(getString(R.string.add_toolbar));
        toolbar.setTitleTextColor(0xFFFFFFFF);
    }

    private void showFeedback(int feedbackCode)
    {
        Snackbar snackShowStatus;
        switch (feedbackCode)
        {
            case GROUP_ADD_OK:            snackShowStatus = Snackbar.make(getActivity().findViewById(R.id.activity_home), R.string.add_OK, Snackbar.LENGTH_LONG); break;
            case GROUP_ADD_KO_NO_USERS:   snackShowStatus = Snackbar.make(getActivity().findViewById(R.id.activity_home), R.string.add_KO_no_users, Snackbar.LENGTH_LONG); break;
            case CONNECTION_ERROR:             snackShowStatus = Snackbar.make(getActivity().findViewById(R.id.activity_home), R.string.connection_error, Snackbar.LENGTH_LONG); break;
            case REMOTE_ERROR:                 snackShowStatus = Snackbar.make(getActivity().findViewById(R.id.activity_home), R.string.remote_group_creation_error, Snackbar.LENGTH_LONG); break;
            default:                           snackShowStatus = Snackbar.make(getActivity().findViewById(R.id.activity_home), R.string.generic_error, Snackbar.LENGTH_LONG); break;
        }
        snackShowStatus.show();
    }

    private void changeFragment()
    {
        FragmentTransaction transaction = getActivity().getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.home_main_content, HomeFragmentContainer.getInstance().getManageGroupFragment());
        transaction.commit();
    }

    private void setupGroupResponseHandler()
    {
        this.groupDetailsResponseHandler = new NetworkResponseHandler() {
            @Override
            public void onSuccess(JSONObject response) {
                // Debug
                Log.d("GROUP_DETAILS", response.toString());

                try {
                    int responseCode = response.getInt("success");
                    if(responseCode == 1)
                    {
                        JSONArray jsonMembers = null;
                        List<User> groupMembers = new ArrayList<>();
                        try {
                            jsonMembers = response.getJSONArray("members");
                            for(int i = 0; i < jsonMembers.length(); i++)
                            {
                                User toBeAdded = new User(jsonMembers.getJSONObject(i));
                                groupMembers.add(toBeAdded);
                            }
                            Group userGroup = GlobalValuesManager.getInstance(getContext()).getLoggedUserGroup();
                            userGroup.setMembers(groupMembers);
                            GlobalValuesManager.getInstance(getContext()).saveLoggedUserGroup(userGroup);
                            changeFragment();
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onError(VolleyError error) {

            }
        };
    }

    // Determine if a user is part of a group; in that case, fetch the group details, including the templates
    private void sendGetGroupDetailsRequest()
    {
        setupGroupResponseHandler();

        // User loggedUser = GlobalValuesManager.getInstance(getContext()).getLoggedUser();

        // The POST parameters.
        Map<String, String> params = new HashMap<>();
        params.put("id", String.valueOf(GlobalValuesManager.getInstance(getContext()).getLoggedUserGroup().getID()));

        // Encapsulate in JSON.
        JSONObject jsonPostParameters = new JSONObject(params);

        // Print parameters to console for debug purposes.
        Log.d("JSON_GET_GROUP_DETAILS", jsonPostParameters.toString());

        GroupsDatabaseHelper.sendGetGroupDetailsRequest(jsonPostParameters, getContext(), groupDetailsResponseHandler);
    }
}

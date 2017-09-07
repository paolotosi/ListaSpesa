package com.mobile.paolo.listaspesa.view.home.group;

import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TextInputLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.android.volley.VolleyError;
import com.doctoror.geocoder.Address;
import com.doctoror.geocoder.Geocoder;
import com.doctoror.geocoder.GeocoderException;
import com.google.android.gms.maps.model.LatLng;
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
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * -- CreateGroupFragment --
 * This fragment is loaded when the user wants to create a group.
 * Specifically, it's loaded when the user selects the tab "Gruppo" and he's not part of any group.
 * It has a text field for the group name.
 * It shows the list of all users (except the logged one); each user as a checkbox.
 * In order to create a group, the user enters a name and checks all the users he wants to add.
 * When he's done, it touches the floating action button in order to confirm.
 */

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

    // Widgets
    private ProgressBar progressBar;
    private TextInputLayout groupNameInputLayout;

    // RecyclerView, adapter and model list
    private RecyclerView recyclerView;
    private UserCardViewDataAdapter adapter;
    private ArrayList<User> userModelList = new ArrayList<>();

    // Network response logic
    private NetworkResponseHandler fetchUsersResponseHandler;
    private NetworkResponseHandler createGroupResponseHandler;
    private NetworkResponseHandler groupDetailsResponseHandler;
    private NetworkResponseHandler groupProductsResponseHandler;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        // Load fragment.
        View loadedFragment = inflater.inflate(R.layout.fragment_create_group, container, false);

        // Setup toolbar
        setupToolbar(loadedFragment);

        initializeWidgets(loadedFragment);

        // Initialize the RecyclerView.
        setupRecyclerView(loadedFragment);

        // Define what to do when the server response to get all users is received.
        setupFetchUsersResponseHandler(loadedFragment);

        // Send the network request to get all users.
        UsersDatabaseHelper.sendGetAllUsersRequest(getActivity().getApplicationContext(), fetchUsersResponseHandler);

        // Setup the confirm button listener.
        setupConfirmButtonListener(loadedFragment);

        return loadedFragment;
    }

    @Override
    public void onResume() {
        if(adapter.getItemCount() > 0)
        {
            progressBar.setVisibility(View.GONE);
        }
        super.onResume();
    }

    private void initializeWidgets(View loadedFragment)
    {
        progressBar = (ProgressBar) loadedFragment.findViewById(R.id.progressBar);
        groupNameInputLayout = (TextInputLayout) loadedFragment.findViewById(R.id.groupNameInputLayout);
    }

    private void setupFetchUsersResponseHandler(final View loadedFragment)
    {
        this.fetchUsersResponseHandler = new NetworkResponseHandler() {

            @Override
            public void onSuccess(JSONObject jsonResponse) {
                populateUserList(jsonResponse, loadedFragment);
            }

            @Override
            public void onError(VolleyError error) {
                error.printStackTrace();
            }
        };
    }

    // Populate userList with server response
    private void populateUserList(JSONObject serverResponse, View loadedFragment)
    {
        if(userModelList.isEmpty())
        {
            try {
                // Split the response in a list.
                JSONArray jsonUserList = serverResponse.getJSONArray("users");

                // Get logged user to exclude him from the list
                User loggedUser = GlobalValuesManager.getInstance(getContext()).getLoggedUser();

                if(jsonUserList.length() == 1)
                {
                    showNoUsersMessage(loadedFragment);
                }
                else
                {
                    // For each user, create a User object and add it to the list.
                    for(int i = 0; i < jsonUserList.length(); i++)
                    {
                        JSONObject jsonUser = (JSONObject) jsonUserList.get(i);
                        User toBeAdded = new User(jsonUser);
                        if(loggedUser.getID() != toBeAdded.getID())
                        {
                            userModelList.add(toBeAdded);
                        }
                    }

                    // Order user list geographically with an AsyncTask
                    Toast.makeText(getContext(), getString(R.string.loading_list_message), Toast.LENGTH_SHORT).show();
                    AddressResolutionTask addressResolutionTask = new AddressResolutionTask();
                    addressResolutionTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);

                }


            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    private void showNoUsersMessage(View loadedFragment)
    {
        loadedFragment.findViewById(R.id.groupNameInputLayout).setVisibility(View.GONE);
        loadedFragment.findViewById(R.id.confirmButton).setVisibility(View.GONE);
        loadedFragment.findViewById(R.id.noUsersTextView).setVisibility(View.VISIBLE);
    }

    private void setupConfirmButtonListener(final View fragment)
    {
        fragment.findViewById(R.id.confirmButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendCreateGroupRequest(fragment);
            }
        });
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
                groupNameInputLayout.setErrorEnabled(false);
            }
            else
            {
                okToSend = false;
                groupNameInputLayout.setError(getString(R.string.group_creation_KO_no_name));
                // showFeedback(GROUP_CREATION_KO_NO_NAME);
            }

            // selectedIDs will contain the IDs of every member
            JSONArray selectedIDs = new JSONArray();

            // The first one is the logged user
            User loggedUser = GlobalValuesManager.getInstance(getContext()).getLoggedUser();
            selectedIDs.put(0, loggedUser.getID());

            // Get the user list
            List<User> userList = adapter.getUserList();

            // For each user in the list, if he's checked add him to selectedIDs
            int pos = 1;
            for (int i = 0; i < userList.size(); i++)
            {
                User singleUser = userList.get(i);
                if (singleUser.isChecked())
                {
                    selectedIDs.put(pos, singleUser.getID());
                    pos++;
                }
            }

            // No users other than me
            if(selectedIDs.length() == 1)
            {
                okToSend = false;
                showFeedback(GROUP_CREATION_KO_NO_USERS);
            }

            // Add all IDs in JSON
            jsonRequest.put("selectedUsers", selectedIDs);

        } catch (JSONException e) {
            e.printStackTrace();
        }
        // Debug JSON
        Log.d("Json request", jsonRequest.toString());

        // Define what to do on server response
        setupCreateGroupResponseHandler();

        if(okToSend)
        {
            GroupsDatabaseHelper.sendCreateGroupRequest(jsonRequest, getContext(), createGroupResponseHandler);
        }

    }

    private void setupCreateGroupResponseHandler()
    {
        this.createGroupResponseHandler = new NetworkResponseHandler() {
            @Override
            public void onSuccess(JSONObject response) {
                // Debug
                Log.d("serverResponse", response.toString());

                // Read success tag and show feedback
                try {
                    if(response.getInt(TAG_SUCCESS) == SUCCESS)
                    {
                        Log.d("createGroupResponse", "Success");
                        showFeedback(GROUP_CREATION_OK);
                        GlobalValuesManager.getInstance(getContext()).saveIsUserCreatingGroup(false);
                        int groupID = response.getInt("groupID");
                        GlobalValuesManager.getInstance(getContext()).saveLoggedUserGroup(new Group(groupID, "", new ArrayList<User>()));
                        sendGetGroupDetailsRequest();
                        sendGetGroupProductsRequest();
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
        recyclerView = (RecyclerView) loadedFragment.findViewById(R.id.recyclerViewUsers);

        // use this setting to improve performance if you know that changes
        // in content do not change the layout size of the RecyclerView
        recyclerView.setHasFixedSize(true);

        // use a linear layout manager
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity().getApplicationContext()));

        // create an Object for Adapter
        adapter = new UserCardViewDataAdapter(userModelList, UserCardViewDataAdapter.CREATION_MODE);

        // set the adapter object to the Recyclerview
        recyclerView.setAdapter(adapter);
    }

    private void setupToolbar(View loadedFragment)
    {
        Toolbar toolbar = (Toolbar) loadedFragment.findViewById(R.id.createGroupToolbar);
        toolbar.setTitle(getString(R.string.create_group_toolbar));
        toolbar.setTitleTextColor(0xFFFFFFFF);
    }

    private void showFeedback(int feedbackCode)
    {
        Snackbar snackShowStatus;
        switch (feedbackCode)
        {
            case GROUP_CREATION_OK:            snackShowStatus = Snackbar.make(getActivity().findViewById(R.id.activity_home), R.string.group_creation_OK, Snackbar.LENGTH_LONG); break;
            case GROUP_CREATION_KO_NO_NAME:    snackShowStatus = Snackbar.make(getActivity().findViewById(R.id.activity_home), R.string.group_creation_KO_no_name, Snackbar.LENGTH_LONG); break;
            case GROUP_CREATION_KO_NO_USERS:   snackShowStatus = Snackbar.make(getActivity().findViewById(R.id.activity_home), R.string.group_creation_KO_no_users, Snackbar.LENGTH_LONG); break;
            case CONNECTION_ERROR:             snackShowStatus = Snackbar.make(getActivity().findViewById(R.id.activity_home), R.string.connection_error, Snackbar.LENGTH_LONG); break;
            case REMOTE_ERROR:                 snackShowStatus = Snackbar.make(getActivity().findViewById(R.id.activity_home), R.string.remote_group_creation_error, Snackbar.LENGTH_LONG); break;
            default:                           snackShowStatus = Snackbar.make(getActivity().findViewById(R.id.activity_home), R.string.generic_error, Snackbar.LENGTH_LONG); break;
        }
        snackShowStatus.show();
    }

    private void showManageGroupFragment()
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
                        // Cache group
                        GlobalValuesManager.getInstance(getContext()).saveIsUserPartOfAGroup(true);
                        Group group = new Group(response.getInt("groupID"), response.getString("groupName"), response.getJSONArray("members"));
                        GlobalValuesManager.getInstance(getContext()).saveLoggedUserGroup(group);

                        // Get default supermarkets
                        GlobalValuesManager.getInstance(getContext()).saveSupermarkets(response.getJSONArray("supermarkets"));
                        if(GlobalValuesManager.getInstance(getContext()).getSupermarkets().size() > 0)
                        {
                            GlobalValuesManager.getInstance(getContext()).saveHasUserSupermarkets(true);
                        }

                        showManageGroupFragment();
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onError(VolleyError error) {
                error.printStackTrace();
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
        // params.put("id", ((Integer) (loggedUser.getID())).toString());
        params.put("id", String.valueOf(GlobalValuesManager.getInstance(getContext()).getLoggedUserGroup().getID()));

        // Encapsulate in JSON.
        JSONObject jsonPostParameters = new JSONObject(params);

        // Print parameters to console for debug purposes.
        Log.d("JSON_GET_GROUP_DETAILS", jsonPostParameters.toString());

        GroupsDatabaseHelper.sendGetGroupDetailsRequest(jsonPostParameters, getContext(), groupDetailsResponseHandler);
    }

    private void sendGetGroupProductsRequest()
    {
        setupGroupProductsResponseHandler();

        JSONObject jsonPost = new JSONObject();
        String groupID = String.valueOf(GlobalValuesManager.getInstance(getContext()).getLoggedUserGroup().getID());

        try {
            jsonPost.put("id",groupID);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        GroupsDatabaseHelper.sendGetGroupProductsRequest(jsonPost, getContext(), groupProductsResponseHandler);
    }

    private void setupGroupProductsResponseHandler()
    {
        this.groupProductsResponseHandler = new NetworkResponseHandler() {
            @Override
            public void onSuccess(JSONObject response) {
                Log.d("GET_PRODUCTS_RESP", response.toString());
                try {
                    if(response.getInt("success") == 1)
                    {
                        // Determine if the group has templates and update the SharedPreferences accordingly
                        JSONArray products = response.getJSONArray("products");
                        GlobalValuesManager.getInstance(getContext()).saveGroupProducts(products);
                    }
                    else
                    {

                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onError(VolleyError error) {
                error.printStackTrace();
            }
        };
    }

    private final class AddressResolutionTask extends AsyncTask<Void, Void, Object> {

        private final Geocoder mGeocoder;
        private LatLng loggedUserCoordinates;

        private List<Address> userAddresses = new ArrayList<>();

        private AddressResolutionTask() {
            mGeocoder = new Geocoder(getContext(), Locale.getDefault());
        }

        @Override
        protected Object doInBackground(final Void... params) {
            try {
                for(User user : userModelList)
                {
                    // Get absolute location from string address
                    userAddresses.add(mGeocoder.getFromLocationName(user.getAddress(), 1, true).get(0));
                }
                Address loggedUserAddress = mGeocoder.getFromLocationName(GlobalValuesManager.getInstance(getContext()).getLoggedUser().getAddress(), 1, true).get(0);
                loggedUserCoordinates = new LatLng(loggedUserAddress.getLocation().latitude, loggedUserAddress.getLocation().longitude);

                return userAddresses;
            } catch (GeocoderException e) {
                return e;
            }
        }

        @Override
        protected void onPostExecute(final Object result) {
            if (result instanceof GeocoderException)
            {
                Toast.makeText(getContext(), result.toString(), Toast.LENGTH_LONG).show();
                return;
            }

            List<Address> resultAddressList = (List<Address>) result;
            if(resultAddressList.size() > 0)
            {
                for(int i = 0; i < resultAddressList.size(); i++)
                {
                    // Set absolute location to each user in the list
                    double latitude = resultAddressList.get(i).getLocation().latitude;
                    double longitude = resultAddressList.get(i).getLocation().longitude;
                    userModelList.get(i).setCachedAbsolutePosition(new LatLng(latitude, longitude));
                }

                Comparator<User> geographicalComparator = new Comparator<User>() {
                    @Override
                    public int compare(User u1, User u2) {
                        LatLng firstUserCoordinates = u1.getCachedAbsolutePosition();
                        LatLng secondUserCoordinates = u2.getCachedAbsolutePosition();

                        Location loggedUserLocation = new Location("");
                        loggedUserLocation.setLatitude(loggedUserCoordinates.latitude);
                        loggedUserLocation.setLongitude(loggedUserCoordinates.longitude);

                        Location l1 = new Location("");
                        l1.setLatitude(firstUserCoordinates.latitude);
                        l1.setLongitude(firstUserCoordinates.longitude);

                        Location l2 = new Location("");
                        l2.setLatitude(secondUserCoordinates.latitude);
                        l2.setLongitude(secondUserCoordinates.longitude);

                        float d1 = l1.distanceTo(loggedUserLocation);
                        float d2 = l2.distanceTo(loggedUserLocation);

                        return Float.compare(d1, d2);
                    }
                };

                Collections.sort(userModelList, geographicalComparator);

                // Hide progress bar
                progressBar.setVisibility(View.GONE);

                // Tell the RecyclerView to reload elements
                adapter.notifyDataSetChanged();
            }
            else
            {
                Toast.makeText(getContext(), getString(R.string.location_resolution_error), Toast.LENGTH_LONG).show();
            }

        }
    }


}

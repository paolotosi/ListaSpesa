package com.mobile.paolo.listaspesa;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MenuItem;

import com.android.volley.VolleyError;
import com.ittianyu.bottomnavigationviewex.BottomNavigationViewEx;
import com.mobile.paolo.listaspesa.database.GroupsDatabaseHelper;
import com.mobile.paolo.listaspesa.database.TemplatesDatabaseHelper;
import com.mobile.paolo.listaspesa.model.Group;
import com.mobile.paolo.listaspesa.model.User;
import com.mobile.paolo.listaspesa.network.NetworkResponseHandler;
import com.mobile.paolo.listaspesa.utility.GlobalValuesManager;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class HomeActivity extends AppCompatActivity {

    // The fragments
    private CreateGroupFragment createGroupFragment;
    private ManageGroupFragment manageGroupFragment;
    private EmptyTemplateFragment emptyTemplateFragment;
    private CreateTemplateFragment createTemplateFragment;

    // The networkResponseHandler
    private NetworkResponseHandler groupResponseHandler;
    private NetworkResponseHandler templateResponseHandler;

    // Response codes
    private static final int NETWORK_ERROR = 0;
    private static final int USER_HAS_GROUP = 1;
    private static final int USER_DOESNT_HAVE_GROUP = 2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        // Determine if logged user is already part of a group
        // NOTE: it also contains the request to verify if the group has already defined some templates
        sendGetGroupDetailsRequest();

        BottomNavigationViewEx bottomNavigationView = (BottomNavigationViewEx) findViewById(R.id.home_bottom_navigation);
        bottomNavigationView.enableShiftingMode(false);
        bottomNavigationView.enableItemShiftingMode(false);

        bottomNavigationView.setOnNavigationItemSelectedListener
                (new BottomNavigationView.OnNavigationItemSelectedListener() {
                    @Override
                    public boolean onNavigationItemSelected(@NonNull MenuItem selectedTab) {
                        Fragment selectedFragment = selectCorrectFragment(selectedTab);
                        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
                        transaction.replace(R.id.home_main_content, selectedFragment);
                        transaction.commit();
                        return true;
                    }
                });

        // Manually displaying the first fragment - one time only
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.home_main_content, ItemOneFragment.newInstance());
        transaction.commit();


        //Used to select an item programmatically
        //bottomNavigationView.getMenu().getItem(2).setChecked(true);
    }

    private void setupGroupResponseHandler()
    {
        this.groupResponseHandler = new NetworkResponseHandler() {
            @Override
            public void onSuccess(JSONObject response) {
                // Debug
                Log.d("GROUP_DETAILS", response.toString());

                try {
                    int responseCode = response.getInt("success");
                    switch (responseCode)
                    {
                        case NETWORK_ERROR: break;
                        case USER_HAS_GROUP:
                            // Create group from response
                            GlobalValuesManager.getInstance(getApplicationContext()).saveIsUserPartOfAGroup(true);
                            Group group = new Group(response.getInt("groupID"), response.getString("groupName"), response.getJSONArray("members"));
                            GlobalValuesManager.getInstance(getApplicationContext()).saveLoggedUserGroup(group);
                            break;
                        case USER_DOESNT_HAVE_GROUP:
                            GlobalValuesManager.getInstance(getApplicationContext()).saveIsUserPartOfAGroup(false);
                            break;
                    }
                    sendGetTemplatesRequest();

                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }

            @Override
            public void onError(VolleyError error) {

            }
        };
    }

    private void sendGetGroupDetailsRequest()
    {
        setupGroupResponseHandler();

        User loggedUser = GlobalValuesManager.getInstance(getApplicationContext()).getLoggedUser();

        // The POST parameters.
        Map<String, String> params = new HashMap<>();
        params.put("id", ((Integer) (loggedUser.getId())).toString());

        // Encapsulate in JSON.
        JSONObject jsonPostParameters = new JSONObject(params);

        // Print parameters to console for debug purposes.
        Log.d("JSON_GET_GROUP_DETAILS", jsonPostParameters.toString());

        GroupsDatabaseHelper.sendGetGroupDetailsRequest(jsonPostParameters, getApplicationContext(), groupResponseHandler);
    }

    private boolean isUserPartOfAGroup()
    {
        return GlobalValuesManager.getInstance(getApplicationContext()).isUserPartOfAGroup();
    }

    private void setupTemplateResponseHandler()
    {
        this.templateResponseHandler = new NetworkResponseHandler() {
            @Override
            public void onSuccess(JSONObject response) {
                try {
                    Log.d("TEMPLATE_RESPONSE", response.toString());
                    if(response.getInt("success") == 1)
                    {
                        JSONArray templates = response.getJSONArray("templates");
                        if(templates.length() == 0)
                        {
                            GlobalValuesManager.getInstance(getApplicationContext()).saveHasUserTemplates(false);
                        }
                        else
                        {
                            GlobalValuesManager.getInstance(getApplicationContext()).saveHasUserTemplates(true);
                        }
                    }
                    else
                    {
                        GlobalValuesManager.getInstance(getApplicationContext()).saveHasUserTemplates(false);
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

    private void sendGetTemplatesRequest()
    {
        setupTemplateResponseHandler();

        Integer groupID = -1;

        if(GlobalValuesManager.getInstance(getApplicationContext()).getLoggedUserGroup() != null)
        {
            groupID = GlobalValuesManager.getInstance(getApplicationContext()).getLoggedUserGroup().getID();
        }

        Map<String, String> params = new HashMap<>();
        params.put("groupID", groupID.toString());

        JSONObject jsonPostParameters = new JSONObject(params);

        Log.d("JSON_TEMPLATES", jsonPostParameters.toString());

        TemplatesDatabaseHelper.sendGetGroupTemplatesRequest(jsonPostParameters, getApplicationContext(), templateResponseHandler);

    }

    private boolean hasUserTemplates()
    {
        return GlobalValuesManager.getInstance(getApplicationContext()).hasUserTemplates();
    }

    private Fragment selectCorrectFragment(MenuItem selectedTab)
    {
        Fragment selectedFragment = null;
        switch (selectedTab.getItemId()) {
            case R.id.tab_supermarket:
                selectedFragment = new ItemOneFragment();
                break;
            case R.id.tab_templates:
                selectedFragment = selectTemplateFragment();
                break;
            case R.id.tab_list:
                selectedFragment = new ItemTwoFragment();
                break;
            case R.id.tab_group:
                selectedFragment = selectGroupFragment();
                break;
        }
        return selectedFragment;
    }

    private Fragment selectTemplateFragment()
    {
        Fragment selectedFragment;
        if(hasUserTemplates())
        {
            if(createTemplateFragment == null)
            {
                createTemplateFragment = new CreateTemplateFragment();
            }
            selectedFragment = createTemplateFragment;
        }
        else
        {
            if(emptyTemplateFragment == null)
            {
                emptyTemplateFragment = new EmptyTemplateFragment();
            }
            selectedFragment = emptyTemplateFragment;
        }
        return selectedFragment;
    }

    private Fragment selectGroupFragment()
    {
        Fragment selectedFragment;
        if(isUserPartOfAGroup())
        {
            if(manageGroupFragment == null)
            {
                manageGroupFragment = new ManageGroupFragment();
            }
            selectedFragment = manageGroupFragment;
        }
        else
        {
            if(createGroupFragment == null)
            {
                createGroupFragment = new CreateGroupFragment();
            }
            selectedFragment = createGroupFragment;
        }
        return selectedFragment;
    }


















}

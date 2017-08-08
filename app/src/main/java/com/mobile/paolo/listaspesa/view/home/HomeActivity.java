package com.mobile.paolo.listaspesa.view.home;

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
import com.mobile.paolo.listaspesa.R;
import com.mobile.paolo.listaspesa.database.GroupsDatabaseHelper;
import com.mobile.paolo.listaspesa.database.TemplatesDatabaseHelper;
import com.mobile.paolo.listaspesa.model.objects.Group;
import com.mobile.paolo.listaspesa.model.objects.User;
import com.mobile.paolo.listaspesa.network.NetworkResponseHandler;
import com.mobile.paolo.listaspesa.utility.GlobalValuesManager;
import com.mobile.paolo.listaspesa.view.home.group.CreateGroupFragment;
import com.mobile.paolo.listaspesa.view.home.group.EmptyGroupFragment;
import com.mobile.paolo.listaspesa.view.home.group.ManageGroupFragment;
import com.mobile.paolo.listaspesa.view.home.template.EmptyTemplateFragment;
import com.mobile.paolo.listaspesa.view.home.template.ManageTemplateFragment;
import com.mobile.paolo.listaspesa.view.home.shoppingList.EmptyShoppingListFragment;
import com.mobile.paolo.listaspesa.view.home.shoppingList.ManageShoppingListFragment;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

/**
 * -- HomeActivity --
 * The central activity in the application.
 * It's based on a bottom bar navigation layout: when the user selects a tab, the
 * view is updated by loading the corresponding fragment.
 * Each tab has different fragments, for example the "Gruppo" tab has a creation and management view.
 * This calls for the necessity of contextualizing the application before setting up the tabs.
 * We need to know:
 * - which user is logged
 * - if he's already part of a group
 * - if his group has already defined some templates
 * This info is gathered from the database as soon as the activity is loaded and saved in
 * SharedPreferences for future uses.
 */

public class HomeActivity extends AppCompatActivity {

    // The fragments
    private EmptyGroupFragment emptyGroupFragment;
    private CreateGroupFragment createGroupFragment;
    private ManageGroupFragment manageGroupFragment;
    private EmptyTemplateFragment emptyTemplateFragment;
    private ManageTemplateFragment manageTemplateFragment;
    private EmptyShoppingListFragment emptyListFragment;
    private ManageShoppingListFragment manageListFragment;

    // The networkResponseHandlers
    private NetworkResponseHandler groupResponseHandler;
    private NetworkResponseHandler templateResponseHandler;

    // Response codes
    private static final int NETWORK_ERROR = 0;
    private static final int USER_HAS_GROUP = 1;
    private static final int USER_DOESNT_HAVE_GROUP = 2;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_home);

        overridePendingTransition(R.anim.slide_in, R.anim.slide_out);

        // Determine the context: logged user, group, templates...
        contextualize();

        // From here on out, everything is handled by the bottom navigation listener
        setupBottomNavigationView();
    }

    private void contextualize()
    {
        // Determine if logged user is already part of a group
        // NOTE: it also contains the request to verify if the group has already defined some templates
        // The second request has to be sent only after the first is finished.
        sendGetGroupDetailsRequest();
    }

    private void setupBottomNavigationView()
    {
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
                            // Create group from response and updated SharedPreferences
                            GlobalValuesManager.getInstance(getApplicationContext()).saveIsUserPartOfAGroup(true);
                            Group group = new Group(response.getInt("groupID"), response.getString("groupName"), response.getJSONArray("members"));
                            GlobalValuesManager.getInstance(getApplicationContext()).saveLoggedUserGroup(group);
                            // Query for templates only if the user has a group.
                            // The request needs to be sent now, otherwise we don't know the group ID!
                            sendGetTemplatesRequest();
                            break;
                        case USER_DOESNT_HAVE_GROUP:
                            GlobalValuesManager.getInstance(getApplicationContext()).saveIsUserPartOfAGroup(false);
                            break;
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
                        // Determine if the group has templates and update the SharedPreferences accordingly
                        JSONArray templates = response.getJSONArray("templates");
                        if(templates.length() == 0)
                        {
                            GlobalValuesManager.getInstance(getApplicationContext()).saveHasUserTemplates(false);
                        }
                        else
                        {
                            GlobalValuesManager.getInstance(getApplicationContext()).saveHasUserTemplates(true);
                            GlobalValuesManager.getInstance(getApplicationContext()).saveUserTemplates(templates);
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
                error.printStackTrace();
            }
        };
    }

    private void sendGetTemplatesRequest()
    {
        // Define what to do on response
        setupTemplateResponseHandler();

        Integer groupID = -1;

        if(GlobalValuesManager.getInstance(getApplicationContext()).getLoggedUserGroup() != null)
        {
            groupID = GlobalValuesManager.getInstance(getApplicationContext()).getLoggedUserGroup().getID();
        }

        // The POST parameters
        Map<String, String> params = new HashMap<>();
        params.put("groupID", groupID.toString());

        // Encapsulate in JSON
        JSONObject jsonPostParameters = new JSONObject(params);

        // Debug
        Log.d("JSON_TEMPLATES", jsonPostParameters.toString());

        // Send request
        TemplatesDatabaseHelper.sendGetGroupTemplatesRequest(jsonPostParameters, getApplicationContext(), templateResponseHandler);

    }

    private boolean hasUserTemplates()
    {
        return GlobalValuesManager.getInstance(getApplicationContext()).hasUserTemplates();
    }

    private boolean hasUserList()
    {
        return false;
        //return GlobalValuesManager.getInstance(getApplicationContext()).hasUserList();
    }

    // Define which fragment to load based on context
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
                selectedFragment = selectListFragment();
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
            if(manageTemplateFragment == null)
            {
                manageTemplateFragment = new ManageTemplateFragment();
            }
            selectedFragment = manageTemplateFragment;
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
//            if(createGroupFragment == null)
//            {
//                createGroupFragment = new CreateGroupFragment();
//            }
//            selectedFragment = createGroupFragment;
            if(emptyGroupFragment == null)
            {
                emptyGroupFragment = new EmptyGroupFragment();
            }
            selectedFragment = emptyGroupFragment;
        }
        return selectedFragment;
    }

    private Fragment selectListFragment()
    {
        Fragment selectedFragment;
        if(hasUserList())
        {
            if(manageListFragment == null)
            {
                manageListFragment = new ManageShoppingListFragment();
            }
            selectedFragment = manageListFragment;
        }
        else
        {
            if(emptyListFragment == null)
            {
                emptyListFragment = new EmptyShoppingListFragment();
            }
            selectedFragment = emptyListFragment;
        }
        return selectedFragment;
    }




















}

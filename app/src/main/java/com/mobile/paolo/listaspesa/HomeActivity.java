package com.mobile.paolo.listaspesa;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MenuItem;

import com.android.volley.VolleyError;
import com.mobile.paolo.listaspesa.database.GroupsDatabaseHelper;
import com.mobile.paolo.listaspesa.model.Group;
import com.mobile.paolo.listaspesa.model.User;
import com.mobile.paolo.listaspesa.network.NetworkResponseHandler;
import com.mobile.paolo.listaspesa.utility.GlobalValuesManager;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class HomeActivity extends AppCompatActivity {

    // The networkResponseHandler
    private NetworkResponseHandler networkResponseHandler;

    // Response codes
    private static final int NETWORK_ERROR = 0;
    private static final int USER_HAS_GROUP = 1;
    private static final int USER_DOESNT_HAVE_GROUP = 2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        // Determine if logged user is already part of a group
        sendGetGroupDetailsRequest();

        BottomNavigationView bottomNavigationView = (BottomNavigationView) findViewById(R.id.navigation);

        bottomNavigationView.setOnNavigationItemSelectedListener
                (new BottomNavigationView.OnNavigationItemSelectedListener() {
                    @Override
                    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                        Fragment selectedFragment = null;
                        switch (item.getItemId()) {
                            case R.id.action_item1:
                                selectedFragment = ItemOneFragment.newInstance();
                                break;
                            case R.id.action_item2:
                                selectedFragment = ItemTwoFragment.newInstance();
                                break;
                            case R.id.action_item3:
                                if(isUserPartOfAGroup())
                                {
                                    selectedFragment = ManageGroupFragment.newInstance();
                                }
                                else
                                {
                                    selectedFragment = CreateGroupFragment.newInstance();
                                }
                                break;
                        }
                        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
                        transaction.replace(R.id.frame_layout, selectedFragment);
                        transaction.commit();
                        return true;
                    }
                });

        //Manually displaying the first fragment - one time only
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.frame_layout, ItemOneFragment.newInstance());
        transaction.commit();


        //Used to select an item programmatically
        //bottomNavigationView.getMenu().getItem(2).setChecked(true);
    }

    private void setupNetworkResponseHandler()
    {
        this.networkResponseHandler = new NetworkResponseHandler() {
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
        setupNetworkResponseHandler();

        User loggedUser = GlobalValuesManager.getInstance(getApplicationContext()).getLoggedUser();

        // The POST parameters.
        Map<String, String> params = new HashMap<>();
        params.put("id", ((Integer) (loggedUser.getId())).toString());

        // Encapsulate in JSON.
        JSONObject jsonPostParameters = new JSONObject(params);

        // Print parameters to console for debug purposes.
        Log.d("JSON_LOGIN_PARAM", jsonPostParameters.toString());

        GroupsDatabaseHelper.sendGetGroupDetailsRequest(jsonPostParameters, getApplicationContext(), networkResponseHandler);
    }

    private boolean isUserPartOfAGroup()
    {
        return GlobalValuesManager.getInstance(getApplicationContext()).isUserPartOfAGroup();
    }

}

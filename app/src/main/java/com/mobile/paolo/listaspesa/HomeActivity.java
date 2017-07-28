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
import com.mobile.paolo.listaspesa.model.User;
import com.mobile.paolo.listaspesa.network.NetworkResponseHandler;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class HomeActivity extends AppCompatActivity {

    // The networkResponseHandler
    NetworkResponseHandler networkResponseHandler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
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
                                selectedFragment = CreateGroupFragment.newInstance();
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

        sendGetGroupDetailsRequest();
        //Used to select an item programmatically
        //bottomNavigationView.getMenu().getItem(2).setChecked(true);
    }

    private void setupNetworkResponseHandler()
    {
        this.networkResponseHandler = new NetworkResponseHandler() {
            @Override
            public void onSuccess(JSONObject response) {
                Log.d("GROUP_DETAILS", response.toString());
            }

            @Override
            public void onError(VolleyError error) {

            }
        };
    }

    private User getLoggedUser()
    {
        User user = null;
        SharedPreferences sharedPref = getSharedPreferences("SHARED_PREF", Context.MODE_PRIVATE);
        String jsonLoggedUser = sharedPref.getString(getResources().getString(R.string.LOGGED_USER), "No user logged");
        try {
            user = new User(new JSONObject(jsonLoggedUser));
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return user;
    }

    private void sendGetGroupDetailsRequest()
    {
        setupNetworkResponseHandler();

        User loggedUser = getLoggedUser();

        // The POST parameters.
        Map<String, String> params = new HashMap<>();
        params.put("id", ((Integer) (loggedUser.getId())).toString());

        // Encapsulate in JSON.
        JSONObject jsonPostParameters = new JSONObject(params);

        // Print parameters to console for debug purposes.
        Log.d("JSON_LOGIN_PARAM", jsonPostParameters.toString());

        GroupsDatabaseHelper.sendGetGroupDetailsRequest(jsonPostParameters, getApplicationContext(), networkResponseHandler);
    }

}

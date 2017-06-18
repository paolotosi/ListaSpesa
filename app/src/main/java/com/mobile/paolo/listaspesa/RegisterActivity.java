package com.mobile.paolo.listaspesa;

import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.mobile.paolo.listaspesa.network.NetworkManager;


import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

/**
 * This class handles the registration of a new user.
 * After the user input is validated, it creates a HTTP post request with the inserted data
 * and sends it (asynchronously) to the PHP page on the server that manages the insertion on the database.
 * Upon receiving the JSON response from the server, the activity shows feedback to the user.
 */

public class RegisterActivity extends AppCompatActivity {

    // Widgets
    private Button btnRegister;
    private EditText usernameField, passwordField, addressField;
    private TextInputLayout usernameInputLayout, passwordInputLayout, addressInputLayout;

    // URL to create new user
    private static String url_add_user = "http://10.0.2.2/listaspesa/android_connect/users/add_user.php";

    // A boolean to show if there were problems during the insertion
    private boolean insertionOK;

    // JSON Node names
    private static final String TAG_SUCCESS = "success";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        initializeWidgets();

        btnRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(isInsertionValid())
                {
                    sendHTTPRequest();
                }
            }
        });
    }

    private void initializeWidgets()
    {
        btnRegister = (Button) findViewById(R.id.btnRegister);

        usernameField = (EditText) findViewById(R.id.usernameField);
        usernameInputLayout = (TextInputLayout) findViewById(R.id.usernameInputLayout);

        passwordField = (EditText) findViewById(R.id.passwordField);
        passwordInputLayout = (TextInputLayout) findViewById(R.id.passwordInputLayout);

        addressField = (EditText) findViewById(R.id.addressField);
        addressInputLayout = (TextInputLayout) findViewById(R.id.addressInputLayout);
    }

    private boolean isInsertionValid()
    {
        boolean isValid = true;

        // Check that username field is not empty
        if(usernameField.getText().toString().isEmpty())
        {
            isValid = false;
            // Set the error message
            usernameInputLayout.setError(getResources().getString(R.string.empty_username_error));
        }
        else
            usernameInputLayout.setErrorEnabled(false);

        // Check that password field is not empty
        if(passwordField.getText().toString().isEmpty())
        {
            isValid = false;
            // Set the error message
            passwordInputLayout.setError(getResources().getString(R.string.empty_password_error));
        }
        else
            passwordInputLayout.setErrorEnabled(false);

        // Check that address field is not empty
        if(addressField.getText().toString().isEmpty())
        {
            isValid = false;
            // Set the error message
            addressInputLayout.setError(getResources().getString(R.string.empty_address_error));
        }
        else
            addressInputLayout.setErrorEnabled(false);

        return isValid;
    }

    private void sendHTTPRequest()
    {
        // Get the RequestQueue from NetworkManager
        RequestQueue queue = NetworkManager.getInstance(this.getApplicationContext()).getRequestQueue();

        // Request a string response from the provided URL
        StringRequest stringRequest = new StringRequest(Request.Method.POST, url_add_user,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        // Parse JSON response and check if the insertion was successful
                        Log.d("RESPONSE_MSG", response);
                        try {
                            JSONObject json = new JSONObject(response);
                            // TODO: check feedback logic
                            if(json.getInt(TAG_SUCCESS) == 1)
                            {
                                insertionOK = true;
                                showFeedback();
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        insertionOK = false;
                        showFeedback();
                    }

                }) {
            @Override
            protected Map<String, String> getParams() {
                // The POST parameters
                Map<String, String> params = new HashMap<>();
                params.put(getResources().getString(R.string.USER_KEY), usernameField.getText().toString());
                params.put(getResources().getString(R.string.PASS_KEY), passwordField.getText().toString());
                params.put(getResources().getString(R.string.ADDR_KEY), addressField.getText().toString());
                return params;
            }
        };
        // Add the request to the RequestQueue
        queue.add(stringRequest);
    }

    private void showFeedback()
    {
        Snackbar snackShowStatus;
        if(insertionOK)
        {
            snackShowStatus = Snackbar.make(findViewById(R.id.registerLayout), R.string.insertion_OK, Snackbar.LENGTH_LONG);
        }
        else
        {
            snackShowStatus = Snackbar.make(findViewById(R.id.registerLayout), R.string.insertion_KO, Snackbar.LENGTH_LONG);
        }
        snackShowStatus.show();
    }


}

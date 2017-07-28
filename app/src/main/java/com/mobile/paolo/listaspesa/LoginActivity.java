package com.mobile.paolo.listaspesa;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.mobile.paolo.listaspesa.database.UsersDatabaseHelper;
import com.mobile.paolo.listaspesa.model.User;
import com.mobile.paolo.listaspesa.network.NetworkQueueManager;
import com.mobile.paolo.listaspesa.network.NetworkResponseHandler;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

/**
 * This class handles the user login.
 * After the user input is validated, it creates a HTTP post request with the inserted data
 * and sends it (asynchronously) to the PHP page on the server that manages the login.
 * Upon receiving the JSON response from the server, the activity shows feedback to the user.
 */

public class LoginActivity extends AppCompatActivity {

    // Widgets
    private Button btnLogin;
    private EditText usernameField, passwordField;
    private TextInputLayout usernameInputLayout, passwordInputLayout;

    // Login URL
    private static String url_login = "http://10.0.2.2/listaspesa/android_connect/users/login.php";

    // NetworkResponseHandler & UsersDatabaseHelper
    private NetworkResponseHandler networkResponseHandler;
    private UsersDatabaseHelper usersDatabaseHelper;

    // JSON Node names
    private static final String TAG_SUCCESS = "success";
    private static final String TAG_MESSAGE = "message";
    private static final String USER_ERROR = "USER_ERROR";
    private static final String PASSWORD_ERROR = "PASSWORD_ERROR";

    // Feedback codes
    private static final int LOGIN_OK =                1;
    private static final int LOGIN_KO_NO_USER =        2;
    private static final int LOGIN_KO_WRONG_PASSWORD = 3;
    private static final int CONNECTION_ERROR =        4;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        initializeWidgets();

        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(isInsertionValid())
                {
                    setupNetworkResponseHandler();
                    sendLoginRequest();
                }
            }
        });

    }

    private void initializeWidgets()
    {
        btnLogin = (Button) findViewById(R.id.btnLogin);

        usernameField = (EditText) findViewById(R.id.usernameField);
        passwordField = (EditText) findViewById(R.id.passwordField);

        usernameInputLayout = (TextInputLayout) findViewById(R.id.usernameInputLayout);
        passwordInputLayout = (TextInputLayout) findViewById(R.id.passwordInputLayout);
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

        return isValid;
    }


    private void verifyLogin(JSONObject json, User user)
    {
        try
        {
            if(json.getInt(TAG_SUCCESS) == 1)
            {
                showFeedback(LOGIN_OK);
                SharedPreferences sharedPref = getSharedPreferences("prefs", Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = sharedPref.edit();
                editor.putString(getResources().getString(R.string.LOGGED_USER), user.toJSON().toString());
                Log.d("SHARED_PREF", user.toJSON().toString());
                editor.commit();

                goHome();
            }
            else
            {
                if(json.getString(TAG_MESSAGE).equals(USER_ERROR))
                {
                    showFeedback(LOGIN_KO_NO_USER);
                }
                if(json.getString(TAG_MESSAGE).equals(PASSWORD_ERROR))
                {
                    showFeedback(LOGIN_KO_WRONG_PASSWORD);
                }

            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void showFeedback(int feedbackCode)
    {
        Snackbar snackShowStatus;
        switch (feedbackCode)
        {
            case LOGIN_OK:                  snackShowStatus = Snackbar.make(findViewById(R.id.loginLayout), R.string.login_OK, Snackbar.LENGTH_LONG); break;
            case LOGIN_KO_NO_USER:          snackShowStatus = Snackbar.make(findViewById(R.id.loginLayout), R.string.login_KO_no_user, Snackbar.LENGTH_LONG); break;
            case LOGIN_KO_WRONG_PASSWORD:   snackShowStatus = Snackbar.make(findViewById(R.id.loginLayout), R.string.login_KO_wrong_password, Snackbar.LENGTH_LONG); break;
            case CONNECTION_ERROR:          snackShowStatus = Snackbar.make(findViewById(R.id.loginLayout), R.string.connection_error, Snackbar.LENGTH_LONG); break;
            default:                        snackShowStatus = Snackbar.make(findViewById(R.id.loginLayout), R.string.generic_error, Snackbar.LENGTH_LONG); break;
        }
        snackShowStatus.show();
    }

    private void goHome()
    {
        Intent intent = new Intent(LoginActivity.this, HomeActivity.class);
        startActivity(intent);
    }

    private void setupNetworkResponseHandler()
    {
        this.networkResponseHandler = new NetworkResponseHandler() {

            @Override
            public void onSuccess(JSONObject response) {
                Log.d("RESPONSE_MSG", response.toString());
                User user = null;
                try {
                    user = new User(response.getInt("userId"), response.getString("userName"), null, response.getString("userAddress"));
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                verifyLogin(response, user);
            }

            @Override
            public void onError(VolleyError error) {
                showFeedback(CONNECTION_ERROR);
            }
        };
    }

    private void sendLoginRequest()
    {
        // The POST parameters.
        Map<String, String> params = new HashMap<>();
        params.put(getResources().getString(R.string.USER_KEY), usernameField.getText().toString());
        params.put(getResources().getString(R.string.PASS_KEY), passwordField.getText().toString());

        // Encapsulate in JSON.
        JSONObject jsonPostParameters = new JSONObject(params);

        // Print parameters to console for debug purposes.
        Log.d("JSON_LOGIN_PARAM", jsonPostParameters.toString());

        // Send request.
        UsersDatabaseHelper.sendLoginRequest(jsonPostParameters, getApplicationContext(), networkResponseHandler);
    }

}

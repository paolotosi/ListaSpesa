package com.mobile.paolo.listaspesa.view.auth;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.content.Intent;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;

import com.android.volley.VolleyError;
import com.mobile.paolo.listaspesa.R;
import com.mobile.paolo.listaspesa.database.UsersDatabaseHelper;
import com.mobile.paolo.listaspesa.model.objects.User;
import com.mobile.paolo.listaspesa.network.NetworkResponseHandler;
import com.mobile.paolo.listaspesa.utility.GlobalValuesManager;
import com.mobile.paolo.listaspesa.view.home.HomeActivity;

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
    private ImageView logo;

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

        enteringAnimation();

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

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(R.anim.back_slide_in, R.anim.back_slide_out);
    }

    private void enteringAnimation()
    {
        overridePendingTransition(R.anim.slide_in, R.anim.slide_out);

        logo = (ImageView) findViewById(R.id.logo);

        float startPositionX = -750;
        float endPositionX = 0;
        int duration = 500;

        ValueAnimator motionAnimator = ValueAnimator.ofFloat(startPositionX, endPositionX);
        motionAnimator.setDuration(duration);

        motionAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                logo.setTranslationX((float) animation.getAnimatedValue());
            }
        });

        motionAnimator.start();
    }

    private void transitionToNextActivity()
    {
        logo = (ImageView) findViewById(R.id.logo);

        float startPositionX = 0;
        float endPositionX = 750;
        int duration = 500;

        ValueAnimator motionAnimator = ValueAnimator.ofFloat(startPositionX, endPositionX);
        motionAnimator.setDuration(duration);

        motionAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                logo.setTranslationX((float) animation.getAnimatedValue());
            }
        });

        motionAnimator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                goHome();
            }
        });

        motionAnimator.start();
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


    private void verifyLogin(JSONObject jsonServerResponse, User user)
    {
        try
        {
            if(jsonServerResponse.getInt(TAG_SUCCESS) == 1)
            {
                showFeedback(LOGIN_OK);
                GlobalValuesManager.getInstance(getApplicationContext()).saveLoggedUser(user);
                transitionToNextActivity();
            }
            else
            {
                if(jsonServerResponse.getString(TAG_MESSAGE).equals(USER_ERROR))
                {
                    showFeedback(LOGIN_KO_NO_USER);
                }
                if(jsonServerResponse.getString(TAG_MESSAGE).equals(PASSWORD_ERROR))
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
        // Remove this activity from stack after loading the new one
        // This way we can avoid returning to the login page after the login with the back button
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
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
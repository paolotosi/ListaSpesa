package com.mobile.paolo.listaspesa.view.auth;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.content.Intent;
import android.media.Image;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
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
    private ImageView logo;

    // The NetworkResponseHandler
    NetworkResponseHandler networkResponseHandler;

    // A boolean to show if there were problems during the insertion
    private boolean insertionOK;

    // JSON Node names
    private static final String TAG_SUCCESS = "success";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        enteringAnimation();

        overridePendingTransition(R.anim.slide_in, R.anim.slide_out);

        initializeWidgets();

        setupNetworkResponseHandler();

        btnRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(isInsertionValid())
                {
                    sendRegistrationRequest();
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

    private void goHome()
    {
        Intent intent = new Intent(RegisterActivity.this, HomeActivity.class);
        // Remove this activity from stack after loading the new one
        // This way we can avoid returning to this page after the registration with the back button
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
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

    private void setupNetworkResponseHandler()
    {
        this.networkResponseHandler = new NetworkResponseHandler() {
            @Override
            public void onSuccess(JSONObject response)
            {
                try {
                    if (response.getInt(TAG_SUCCESS) == 1) {
                        insertionOK = true;
                        showFeedback();
                        User user = new User(response.getInt("userId"), response.getString("userName"), null, response.getString("userAddress"));
                        //saveLoggedUserInSharedPreferences(user);
                        GlobalValuesManager.getInstance(getApplicationContext()).saveLoggedUser(user);
                        transitionToNextActivity();
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onError(VolleyError error)
            {
                insertionOK = false;
                showFeedback();
            }
        };
    }

    private void sendRegistrationRequest()
    {
        // The POST parameters.
        Map<String, String> params = new HashMap<>();
        params.put(getResources().getString(R.string.USER_KEY), usernameField.getText().toString());
        params.put(getResources().getString(R.string.PASS_KEY), passwordField.getText().toString());
        params.put(getResources().getString(R.string.ADDR_KEY), addressField.getText().toString());

        // Encapsulate in JSON.
        JSONObject jsonPostParameters = new JSONObject(params);

        // Print parameters to console for debug purposes.
        Log.d("JSON_REGISTER_PARAM", jsonPostParameters.toString());

        // Send request.
        UsersDatabaseHelper.sendRegistrationRequest(jsonPostParameters, getApplicationContext(), networkResponseHandler);
    }

//    private void saveLoggedUserInSharedPreferences(User loggedUser)
//    {
//        // Get shared preferences file
//        SharedPreferences sharedPref = getSharedPreferences("SHARED_PREF", Context.MODE_PRIVATE);
//
//        // Get the editor
//        SharedPreferences.Editor editor = sharedPref.edit();
//
//        // Save logged user as JSON String
//        editor.putString(getResources().getString(R.string.LOGGED_USER), loggedUser.toJSON().toString());
//
//        // Debug
//        Log.d("SHARED_PREF", loggedUser.toJSON().toString());
//
//        // Commit changes
//        editor.commit();
//    }

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
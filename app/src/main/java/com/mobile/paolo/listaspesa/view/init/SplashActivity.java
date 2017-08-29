package com.mobile.paolo.listaspesa.view.init;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import com.mobile.paolo.listaspesa.R;
import com.mobile.paolo.listaspesa.utility.GlobalValuesManager;
import com.mobile.paolo.listaspesa.utility.SharedPreferencesManager;
import com.mobile.paolo.listaspesa.view.home.HomeActivity;

/**
 * -- SplashActivity --
 * Flushes SharedPreferences.
 * Animates a logo making it spinning on itself and coloring the background at the same time.
 * Fades away once the animations are over.
 */


public class SplashActivity extends Activity {

    private ImageView logoBlue, logoWhite;
    private boolean animationStarted = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        flushSharedPreferences();

        if(checkLoggedUser())
        {
            String username = GlobalValuesManager.getInstance(getApplicationContext()).getLoggedUser().getUsername();
            Toast.makeText(getApplicationContext(), getString(R.string.welcome_back_user) + " " + username + "!", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(SplashActivity.this, HomeActivity.class);
            // Remove this activity from stack after loading the new one
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
        }
        else
        {
            // Delete SharedPreferences content
            flushSharedPreferences();
        }

        // Retrieve components by id
        logoBlue = (ImageView) findViewById(R.id.logo_blue);
        logoWhite = (ImageView) findViewById(R.id.logo_white);

        logoBlue.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!animationStarted)
                {
                    animationStarted = true;
                    rotateLogoAndColorBackground();
                }
            }
        });
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }

    private void rotateLogoAndColorBackground()
    {
        final int aSecond = 1000;
        final float fullCircle = 360f;

        // rotationAnimator rotates the logo 360 degrees clockwise in a second
        ValueAnimator rotationAnimator = ValueAnimator.ofFloat(0, fullCircle);
        rotationAnimator.setDuration(aSecond);
        rotationAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener()
        {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                float value = Float.parseFloat(animation.getAnimatedValue().toString());
                logoBlue.setRotation(value);
                logoWhite.setRotation(value);
            }
        });

        // colorAnimator colors the background from white to colorPrimary in a second
        ValueAnimator colorAnimator = ValueAnimator.ofArgb(Color.parseColor("#FFFFFF"), Color.parseColor("#303F9F"));
        colorAnimator.setDuration(aSecond);
        colorAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                findViewById(R.id.splashView).setBackgroundColor((Integer) animation.getAnimatedValue());
            }
        });

        ValueAnimator opacityAnimator = ValueAnimator.ofFloat(1f, 0f);
        opacityAnimator.setDuration(aSecond);
        opacityAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                logoBlue.setAlpha((Float) animation.getAnimatedValue());
            }
        });

        colorAnimator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                transitionToNextActivity();
            }
        });

        // Start the animations
        rotationAnimator.start();
        colorAnimator.start();
        opacityAnimator.start();
    }


    private void transitionToNextActivity()
    {
        Intent intent = new Intent(SplashActivity.this, WelcomeActivity.class);
        startActivity(intent);
        finish();
    }

    private void flushSharedPreferences()
    {
        SharedPreferencesManager.getInstance(getApplicationContext()).flush();
    }

    private boolean checkLoggedUser()
    {
        return GlobalValuesManager.getInstance(getApplicationContext()).isUserLogged();
    }
}
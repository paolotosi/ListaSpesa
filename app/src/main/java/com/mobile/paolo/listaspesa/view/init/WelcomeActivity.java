package com.mobile.paolo.listaspesa.view.init;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Gravity;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.mobile.paolo.listaspesa.R;
import com.mobile.paolo.listaspesa.view.auth.LoginActivity;
import com.mobile.paolo.listaspesa.view.auth.RegisterActivity;

/**
 *  -- WelcomeActivity --
 *  Welcomes the user.
 *  Simply dispatches either to LoginActivity or RegisterActivity.
 */

public class WelcomeActivity extends AppCompatActivity {

    ImageView logo;
    LinearLayout welcomeLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome);

        enteringAnimation();

        // Login
        findViewById(R.id.btnLogin).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(WelcomeActivity.this, LoginActivity.class);
                animateLogoAndStartNextActivity(intent);
            }
        });

        // Register
        findViewById(R.id.btnRegister).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(WelcomeActivity.this, RegisterActivity.class);
                animateLogoAndStartNextActivity(intent);
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        // If the user returns to this view, make the x position of the logo zero
        findViewById(R.id.logo).setTranslationX(0);
    }

    private void enteringAnimation()
    {
        // Disable default android animation
        overridePendingTransition(0, 0);

        logo = (ImageView) findViewById(R.id.logo);
        welcomeLayout = (LinearLayout) findViewById(R.id.mainWelcomeLayout);

        float startOpacity = 0;
        float endOpacity = 1;
        int duration = 500;

        // Fade the main layout in
        ValueAnimator opacityAnimator = ValueAnimator.ofFloat(startOpacity, endOpacity);
        opacityAnimator.setDuration(duration);

        opacityAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                welcomeLayout.setAlpha((float) animation.getAnimatedValue());
            }
        });

        float startPositionY = 0;
        float endPositionY = 775;

        // Move the logo to the bottom
        ValueAnimator motionAnimator = ValueAnimator.ofFloat(startPositionY, endPositionY);
        motionAnimator.setDuration(duration);

        motionAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                logo.setTranslationY((Float) animation.getAnimatedValue());
            }
        });

        opacityAnimator.start();
        motionAnimator.start();
    }

    private void animateLogoAndStartNextActivity(final Intent intent)
    {
        logo = (ImageView) findViewById(R.id.logo);

        int startPositionX = 0;
        int endPositionX = 750;
        long duration = 250;

        ValueAnimator motionAnimator = ValueAnimator.ofInt(startPositionX, endPositionX);
        motionAnimator.setDuration(duration);

        // Update x position of logo on animator update
        motionAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                logo.setTranslationX(Float.parseFloat(animation.getAnimatedValue().toString()));
            }
        });

        // When the animation is over, start the next activity
        motionAnimator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                startActivity(intent);
            }
        });

        motionAnimator.start();
    }




}

package com.mobile.paolo.listaspesa;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.app.Activity;
import android.app.ActivityOptions;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.transition.Fade;
import android.transition.Transition;
import android.util.Pair;
import android.view.View;
import android.view.Window;
import android.view.animation.TranslateAnimation;
import android.widget.ImageView;

import java.util.ArrayList;
import java.util.List;

import static android.R.attr.animation;

/*
 * -- SplashActivity --
 *  Animates a logo making it spinning on itself and coloring the background at the same time.
 *  Fades away once the animations are over.
 */


public class SplashActivity extends Activity {

    ImageView logoBlue, logoWhite;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        // Retrieve components by id
        logoBlue = (ImageView) findViewById(R.id.logo_blue);
        logoWhite = (ImageView) findViewById(R.id.logo_white);

        logoBlue.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                rotateLogoAndColorBackground();
            }
        });
    }

    public void rotateLogoAndColorBackground()
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
        ValueAnimator colorAnimator = ValueAnimator.ofArgb(Color.parseColor("#FFFFFF"), Color.parseColor("#3F51B5"));
        colorAnimator.setDuration(aSecond);
        colorAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                findViewById(R.id.splashView).setBackgroundColor((Integer) animation.getAnimatedValue());
            }
        });

        // When the second animation finishes, the transition is started
        colorAnimator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                TranslateAnimation animationCleaner = new TranslateAnimation(0.0f, 0.0f, 0.0f, 0.0f);
                animationCleaner.setDuration(aSecond);
                findViewById(R.id.splashView).startAnimation(animationCleaner);
                findViewById(R.id.splashView).startAnimation(animationCleaner);
                transitionToNextActivity();
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

        // Start the animations
        rotationAnimator.start();
        colorAnimator.start();
        opacityAnimator.start();
    }

    public void transitionToNextActivity()
    {
        // Shared element transition
        View statusBar = findViewById(android.R.id.statusBarBackground);
        View navigationBar = findViewById(android.R.id.navigationBarBackground);
        View background = findViewById(android.R.id.background);
        Pair sharedElements = new Pair<View, String>(logoWhite, "logo_shared");
        List<Pair<View, String>> pairs = new ArrayList<>();
        pairs.add(Pair.create(statusBar, Window.STATUS_BAR_BACKGROUND_TRANSITION_NAME));
        pairs.add(Pair.create(navigationBar, Window.NAVIGATION_BAR_BACKGROUND_TRANSITION_NAME));
        //pairs.add(Pair.create(background, "Background"));
        pairs.add(sharedElements);

        Bundle options = ActivityOptions.makeSceneTransitionAnimation(this, pairs.toArray(new Pair[pairs.size()])).toBundle();
        Intent intent = new Intent(SplashActivity.this, WelcomeActivity.class);
        startActivity(intent, options);
    }
}
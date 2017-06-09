package com.mobile.paolo.listaspesa;

import android.animation.ArgbEvaluator;
import android.animation.ValueAnimator;
import android.app.Activity;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class SplashActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        // Retrieve components by id
        ImageView logo = (ImageView) findViewById(R.id.logo);

        logo.setOnClickListener(new View.OnClickListener() {
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
                findViewById(R.id.logo).setRotation(value);
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

        rotationAnimator.start();
        colorAnimator.start();
    }
}
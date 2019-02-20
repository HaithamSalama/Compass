package com.compass.ham.compass;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Build;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;

public class MainActivity extends AppCompatActivity implements SensorEventListener {

    //Ads
    private static final String TAG = "MainActivity";
    private AdView mAdView;

    private ImageView imageView;
    private float[] mGravity = new float[3];
    private float[] mGeomagnetic = new float[3];
    private float muth = 0f;
    private float correctMuth = 0f;
    private SensorManager mSensorManager;

    @Override
    protected void onPostResume() {
        super.onPostResume();
        mSensorManager.registerListener(this, mSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD),
                SensorManager.SENSOR_DELAY_GAME);
        mSensorManager.registerListener(this, mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER),
                SensorManager.SENSOR_DELAY_GAME);
    }

    @Override
    protected void onPause() {
        super.onPause();
        mSensorManager.unregisterListener(this);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //Ads
        mAdView = findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder().build();
        mAdView.loadAd(adRequest);


        // Hide status and navigation bar
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            this.getWindow().getDecorView().setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_LAYOUT_STABLE |
                            View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION |
                            View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN |
                            View.SYSTEM_UI_FLAG_HIDE_NAVIGATION |
                            View.SYSTEM_UI_FLAG_FULLSCREEN |
                            View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
        }

        imageView = findViewById(R.id.compass_img);
        mSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);

    }

    @Override
    protected void onResume() {
        super.onResume();

        // Hide status and navigation bar
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            this.getWindow().getDecorView().setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_LAYOUT_STABLE |
                            View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION |
                            View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN |
                            View.SYSTEM_UI_FLAG_HIDE_NAVIGATION |
                            View.SYSTEM_UI_FLAG_FULLSCREEN |
                            View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
        }
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        TextView degree = findViewById(R.id.deg);
        final float alpha = 0.97f;
        synchronized (this) {
            if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
                mGravity[0] = alpha * mGravity[0] + (1 - alpha) * event.values[0];
                mGravity[1] = alpha * mGravity[1] + (1 - alpha) * event.values[1];
                mGravity[2] = alpha * mGravity[2] + (1 - alpha) * event.values[2];
            }
            if (event.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD) {
                mGeomagnetic[0] = alpha * mGeomagnetic[0] + (1 - alpha) * event.values[0];
                mGeomagnetic[1] = alpha * mGeomagnetic[1] + (1 - alpha) * event.values[1];
                mGeomagnetic[2] = alpha * mGeomagnetic[2] + (1 - alpha) * event.values[2];
            }
            float R[] = new float[9];
            float I[] = new float[9];
            boolean success = SensorManager.getRotationMatrix(R, I, mGravity, mGeomagnetic);
            if (success) {
                float orientation[] = new float[3];
                SensorManager.getOrientation(R, orientation);
                muth = (float) Math.toDegrees(orientation[0]);
                muth = (muth + 360) % 360;

                //Change text with your degree
                float rMuth = Math.round(muth * 10);
                rMuth = rMuth / 10;
                degree.setText(String.format("%s%s", rMuth, getString(com.compass.ham.compass.R.string.degreeSymbol)));

                Animation anime = new RotateAnimation(-correctMuth, -muth, Animation.RELATIVE_TO_SELF, .5f
                        , Animation.RELATIVE_TO_SELF, .5f);
                correctMuth = muth;
                anime.setDuration(500);
                anime.setRepeatCount(0);
                anime.setFillAfter(true);

                imageView.startAnimation(anime);
            }
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }
}
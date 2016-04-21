package justin_lelouedec.project;

import android.app.Activity;
import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.util.Log;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;
import android.widget.ImageView;

import java.util.List;

/**
 * Created by 40214897 on 26/02/2016.
 */
public class Compas extends Activity implements SensorEventListener {

    private SensorManager mSensorManager;
    Context mContext;
    private Sensor compas;
    private Sensor acce;
    private boolean acceset,compasset;
    private float[] mLastAccelerometer = new float[3];
    private float[] mLastMagnetometer = new float[3];
    private float[] mR = new float[9];
    private float[] mOrientation = new float[3];
    private float mCurrentDegree = 0f;

    private ImageView mPointer;


    public void onCreate(Bundle savedInstanceState) {


        super.onCreate(savedInstanceState);
        setContentView(R.layout.compas_activity);

        mContext = getApplicationContext();
        mSensorManager = (SensorManager) mContext.getSystemService(Context.SENSOR_SERVICE);
        if (mSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD) != null) {
            compas = mSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
        }
        else {
            Log.i("Error", "no magnetic field sensor");
        }
        if (mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER) != null) {
            acce = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        }
        else {
            Log.i("Error", "no accelerometer  sensor");
        }
        mPointer = (ImageView) findViewById(R.id.pointer);

     }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor == acce) {
            System.arraycopy(event.values, 0, mLastAccelerometer, 0, event.values.length);
            acceset = true;
        } else if (event.sensor == compas) {
            System.arraycopy(event.values, 0, mLastMagnetometer, 0, event.values.length);
            compasset = true;
        }
        if (acceset && compasset) {
            SensorManager.getRotationMatrix(mR, null, mLastAccelerometer, mLastMagnetometer);
            SensorManager.getOrientation(mR, mOrientation);
            float azimuthInRadians = mOrientation[0];
            float azimuthInDegress = (float) (Math.toDegrees(azimuthInRadians) + 360) % 360;
            RotateAnimation ra = new RotateAnimation(
                    mCurrentDegree,
                    -azimuthInDegress,
                    Animation.RELATIVE_TO_SELF, 0.5f,
                    Animation.RELATIVE_TO_SELF,
                    0.5f);

            ra.setDuration(250);

            ra.setFillAfter(true);

            mPointer.startAnimation(ra);
            mCurrentDegree = -azimuthInDegress;

        }
    }

    @Override
    public final void onAccuracyChanged(Sensor sensor, int accuracy) {
        // Do something here if sensor accuracy changes.
    }


    @Override
    protected void onResume() {
        super.onResume();
        mSensorManager.registerListener(this, compas, SensorManager.SENSOR_DELAY_NORMAL);
        mSensorManager.registerListener(this, acce, SensorManager.SENSOR_DELAY_NORMAL);

    }

    @Override
    protected void onPause() {
        super.onPause();
        mSensorManager.unregisterListener(this,compas);
        mSensorManager.unregisterListener(this,acce);

    }


}

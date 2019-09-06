package com.xw.vrlibrary.filters.vr;

import android.app.Activity;
import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;

import com.xw.vrlibrary.util.SensorUtils;
import com.xw.vrlibrary.util.StatusHelper;
import com.xw.vrlibrary.util.constant.Constants;


/**
 * Project: Pano360
 * Package: com.martin.ads.pano360
 */
public class SensorEventHandler implements SensorEventListener {

    public static String TAG = "SensorEventHandler";

    private float[] rotationMatrix = new float[16];

    private StatusHelper statusHelper;
    private SensorHandlerCallback sensorHandlerCallback;

    private boolean sensorRegistered;
    private SensorManager sensorManager;

    private int mDeviceRotation;

    public void init(){
        sensorRegistered=false;
        sensorManager = (SensorManager) statusHelper.getContext()
                .getSystemService(Context.SENSOR_SERVICE);
        Sensor sensorRot = sensorManager.getDefaultSensor(Constants.SENSOR_ROT);
        if (sensorRot==null) return;
        sensorManager.registerListener(this, sensorRot, SensorManager.SENSOR_DELAY_GAME);
        sensorRegistered=true;
    }

    public void releaseResources(){
        if (!sensorRegistered || sensorManager==null) return;
        sensorManager.unregisterListener(this);
        sensorRegistered = false;
    }


    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.accuracy != 0){
            int type = event.sensor.getType();
            switch (type){
                case Constants.SENSOR_ROT:
                    //FIXME
                    mDeviceRotation= ((Activity)statusHelper.getContext()).getWindowManager().getDefaultDisplay().getRotation();
                    SensorUtils.sensorRotationVectorToMatrix(event,mDeviceRotation,rotationMatrix);
                    sensorHandlerCallback.updateSensorMatrix(rotationMatrix);
                break;
            }
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    public void setSensorHandlerCallback(SensorHandlerCallback sensorHandlerCallback){
        this.sensorHandlerCallback=sensorHandlerCallback;
    }

    public void setStatusHelper(StatusHelper statusHelper){
        this.statusHelper=statusHelper;
    }

    public interface SensorHandlerCallback{
        void updateSensorMatrix(float[] sensorMatrix);
    }
}

package com.example.myapplication;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Handler;
import android.os.IBinder;
import android.widget.Toast;

import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;

public class RespiratoryRate extends Service implements SensorEventListener {

    private SensorManager sensorManager;
    private Sensor accelerometerSensor;
    private int noofSamples = 128;
    List<Float> listAccelerometerValuesZ = new ArrayList<Float>();

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate(){
        Toast.makeText(this,"Respiratory Monitoring Started",Toast.LENGTH_LONG).show();
        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        accelerometerSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        sensorManager.registerListener(this, accelerometerSensor, 100000);

        final SensorEventListener currentListener = this;
        new Handler().postDelayed(new Runnable(){
            @Override
            public void run() {
                sensorManager.unregisterListener(currentListener);
                measureRespiratoryRate();
            }
        },45000);
    }

    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {
        Sensor currentSensor = sensorEvent.sensor;
//        Toast.makeText(this,String.valueOf(index),Toast.LENGTH_LONG).show();

        if (currentSensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            listAccelerometerValuesZ.add(sensorEvent.values[2]);
        }
    }

    public void measureRespiratoryRate() {
        float zeroCrossingsSum = 0;
        int size = listAccelerometerValuesZ.size();
        for(int k=0;k<size;k+=noofSamples){
            float sum = 0;
            for (int i = 0; i < noofSamples && (i+k) < size; i++) {
                sum = sum + listAccelerometerValuesZ.get(i+k);
            }
            float average = sum / noofSamples;
            int zeroCrossings = 0;
            boolean up = true;
            if (listAccelerometerValuesZ.get(k) < average) up = false;
            for (int i = 0; i < noofSamples && (i+k) < size; i += 8) {
                float value = listAccelerometerValuesZ.get(i+k);
                if (value >= average && !up) {
                    zeroCrossings++;
                    up = true;
                } else if (value < average && up) {
                    zeroCrossings++;
                    up = false;
                }
            }
            zeroCrossingsSum = zeroCrossingsSum+zeroCrossings;
        }
        sendMessageToActivity((float) Math.ceil(zeroCrossingsSum/2));
        Toast.makeText(getApplicationContext(),"Respiratory Monitoring ended", Toast.LENGTH_LONG).show();
        stopSelf();
    }

    private void sendMessageToActivity(float reading){
        Intent intent = new Intent("RespiratoryRateReading");
        intent.putExtra("reading",reading);
        sendBroadcast(intent);
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {
    }
}

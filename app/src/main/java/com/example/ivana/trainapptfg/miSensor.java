package com.example.ivana.trainapptfg;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;

import java.util.Date;

public class miSensor implements SensorEventListener{

    private float[] values;
    private long timestamp;
    private Sensor sensor;
    private SensorManager mSensorManager;
    private int frecuencia;

    public miSensor(Sensor sensor, SensorManager sm, int freq) {
        this.sensor = sensor;
        this.mSensorManager = sm;
        this.frecuencia = freq;
    }


    @Override
    public void onSensorChanged(SensorEvent event) {
        this.values = event.values;

        long timeInMillis = (new Date()).getTime()
                + (event.timestamp - System.nanoTime()) / 1000000L;

        this.timestamp = timeInMillis;
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    private void registerMiSensorListener(){
        this.mSensorManager.registerListener(this, this.sensor, this.frecuencia);
    }

    private void desregistrarMiSensorListener(){
        this.mSensorManager.unregisterListener(this);
    }

    public void activarSensor(){
        this.registerMiSensorListener();
    }

    public void desactivarSensor(){
        this.desregistrarMiSensorListener();
    }

    public DataTAD obtenerDatosSensor(){
        return new DataTAD(this.timestamp, this.values);
    }
}

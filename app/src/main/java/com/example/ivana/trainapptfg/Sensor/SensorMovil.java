package com.example.ivana.trainapptfg.Sensor;

import android.app.Service;
import android.hardware.SensorManager;

import com.example.ivana.trainapptfg.Utilidades.DataTAD;
import com.example.ivana.trainapptfg.Utilidades.MiSensorEventListener;

public class SensorMovil implements Sensor{

    private SensorManager mSensorManager;
    private MiSensorEventListener miSensorEventListenerAcelerometro;
    private MiSensorEventListener miSensorEventListenerGiroscopio;
    private android.hardware.Sensor mAccelerometer;
    private android.hardware.Sensor mGyroscope;

    private Service service;

    public SensorMovil(Service ser){
        this.service = ser;
    }

    @Override
    public void encenderSensor() {
        configurarSensor();

        this.miSensorEventListenerAcelerometro.activarSensor();
        this.miSensorEventListenerGiroscopio.activarSensor();
    }

    @Override
    public void apagarSensor() {
        this.miSensorEventListenerAcelerometro.desactivarSensor();
        this.miSensorEventListenerGiroscopio.desactivarSensor();
    }

    private void configurarSensor() {
        this.mSensorManager = (SensorManager) service.getSystemService(service.SENSOR_SERVICE);

        this.mAccelerometer = mSensorManager.getDefaultSensor(android.hardware.Sensor.TYPE_ACCELEROMETER);
        this.mGyroscope = mSensorManager.getDefaultSensor(android.hardware.Sensor.TYPE_GYROSCOPE);

        this.miSensorEventListenerAcelerometro = new MiSensorEventListener(this.mAccelerometer, this.mSensorManager, SensorManager.SENSOR_DELAY_FASTEST);
        this.miSensorEventListenerGiroscopio = new MiSensorEventListener(this.mGyroscope, this.mSensorManager, SensorManager.SENSOR_DELAY_FASTEST);
    }

    @Override
    public DataTAD obtenerDatosAcel() {
        return this.miSensorEventListenerAcelerometro.obtenerDatosSensor();
    }

    @Override
    public DataTAD obtenerDatosGyro() {
        return this.miSensorEventListenerGiroscopio.obtenerDatosSensor();
    }
}
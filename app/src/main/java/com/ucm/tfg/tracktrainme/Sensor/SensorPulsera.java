package com.ucm.tfg.tracktrainme.Sensor;

import android.os.Bundle;
import android.os.Handler;
import android.os.ResultReceiver;

import com.ucm.tfg.tracktrainme.Services.BluetoothLeService;
import com.ucm.tfg.tracktrainme.Utilidades.DataTAD;

public class SensorPulsera implements Sensor{

    private BluetoothLeService mService;
    private double[] datosAcelGyro = null;
    private long timestamp;

    private SensorPulsera.MyResultReceiverBluetooth datosRecibidos;

    public class MyResultReceiverBluetooth extends ResultReceiver {
        public MyResultReceiverBluetooth(Handler handler){
            super(handler);
        }

        @Override
        protected void onReceiveResult(int resultCode, Bundle resultData){
            //100 -> datos recibidos de la pulsera

            if(resultCode == 100) {
                datosAcelGyro = resultData.getDoubleArray("datos");
                timestamp = System.currentTimeMillis();
            }
        }
    }

    public SensorPulsera(BluetoothLeService ser){
        this.mService = ser;
        this.datosRecibidos = new MyResultReceiverBluetooth(null);
        this.mService.inicializarResultReceiverEnvioDatos(this.datosRecibidos);
    }

    @Override
    public void encenderSensor() {
        mService.mensaje_encenderSensorCC2650();
    }

    @Override
    public void apagarSensor() {
        mService.mensaje_apagarSensorCC2650();
    }

    @Override
    public DataTAD obtenerDatosAcel() {
        float[] aux = new float[3];
        DataTAD dAcel = null;

        if(this.datosAcelGyro != null){
            aux[0] = (float)this.datosAcelGyro[0];
            aux[1] = (float)this.datosAcelGyro[1];
            aux[2] = (float)this.datosAcelGyro[2];

            dAcel = new DataTAD(this.timestamp, aux);
        }

        return dAcel;
    }

    @Override
    public DataTAD obtenerDatosGyro() {
        float[] aux = new float[3];
        DataTAD dAcel = null;

        if(this.datosAcelGyro != null){
            aux[0] = (float)this.datosAcelGyro[3];
            aux[1] = (float)this.datosAcelGyro[4];
            aux[2] = (float)this.datosAcelGyro[5];

            dAcel = new DataTAD(this.timestamp, aux);
        }

        return dAcel;
    }

}

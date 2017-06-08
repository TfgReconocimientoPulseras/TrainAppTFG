package com.example.ivana.trainapptfg.Sensor;

import android.app.Application;

public class ModoSensor extends Application {

    public int modo ;

    public static int MODO_MOVIL = 1;
    public static  int MODO_PULSERA = 2;

    public void onCreate(){
        super.onCreate();

        //Por defecto, al arrancar se inicializa con el modo movil
        this.modo = 1;
    }

    public int getModo(){
        return this.modo;
    }

    public void setModo(int m){
        this.modo = m;
    }
}

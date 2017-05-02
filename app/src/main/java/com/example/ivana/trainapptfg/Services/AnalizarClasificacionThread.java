package com.example.ivana.trainapptfg.Services;

import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.example.ivana.trainapptfg.EstadosFSM.Estado;
import com.example.ivana.trainapptfg.EstadosFSM.EstadoInicial;

import java.util.concurrent.BlockingQueue;

import static java.lang.Thread.sleep;

public class AnalizarClasificacionThread implements Runnable {
    private BlockingQueue<Integer> queueConsume;
    private Estado estado;
    private LocalBroadcastManager broadcaster;


    public AnalizarClasificacionThread(BlockingQueue<Integer> bqConsume, LocalBroadcastManager bm){
        this.queueConsume = bqConsume;
        this.estado = new EstadoInicial();
        this.broadcaster = bm;

        Log.d("AnalClasif","Resultado: " + estado.getActividad());

    }

    @Override
    public void run() {

        int ultimoEstado = estado.getActividad();

        while(true){
            try {
                int actividadPredicha = queueConsume.take();
                Log.d("AnalClasif","actividad: " + actividadPredicha);

                estado = estado.procesarActividad(actividadPredicha);

                if(estado.getActividad() != ultimoEstado){
                    ultimoEstado = estado.getActividad();
                    if(estado.getActividad() >= 0){
                        updateIconClassifUI(estado.getActividad());
                    }
                }
                Log.d("AnalClasif","Resultado: " + estado.getActividad());

                sleep(500);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

        }
    }

    private void updateIconClassifUI(int estado){
        Intent intent = new Intent("estado_actualizado");
        intent.putExtra("estado", estado);
        broadcaster.sendBroadcast(intent);
    }
}

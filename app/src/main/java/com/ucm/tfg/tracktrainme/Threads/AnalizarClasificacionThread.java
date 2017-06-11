package com.ucm.tfg.tracktrainme.Threads;

import android.content.Context;
import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.ucm.tfg.tracktrainme.EstadosFSM.Estado;
import com.ucm.tfg.tracktrainme.EstadosFSM.EstadoInicial;

import java.util.concurrent.BlockingQueue;

public class AnalizarClasificacionThread implements Runnable {
    private BlockingQueue<Integer> queueConsume;
    private Estado estado;
    private LocalBroadcastManager broadcaster;
    private Context ctx;

    public AnalizarClasificacionThread(BlockingQueue<Integer> bqConsume, LocalBroadcastManager bm, Context ctx){
        this.queueConsume = bqConsume;
        this.estado = new EstadoInicial(ctx);
        this.broadcaster = bm;
        this.ctx = ctx;

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
            } catch (InterruptedException e) {
                Log.d("Thread - AnalClasifi", "Interrupted");
                return;
            }

        }
    }

    private void updateIconClassifUI(int estado){
        Intent intent = new Intent("estado_actualizado");
        intent.putExtra("estado", estado);
        broadcaster.sendBroadcast(intent);
    }
}

package com.ucm.tfg.tracktrainme.EstadosFSM;

import android.content.Context;

public class EstadoInicial extends Estado {
    private int contGlobal;
    private int contUltAct;
    private int ultAct;

    public EstadoInicial(Context ctx){
        super();
        setContext(ctx);
        this.contGlobal = 0;
        this.contUltAct = 0;
        this.ultAct = 0;
    }


    @Override
    public Estado procesarActividad(int numeroActividad) {
        Estado retEstado = this;


        if(contGlobal < Estado.MAX_SIZE){
            if(contUltAct == 3){
                retEstado = new EstadoActividad(this.ultAct);
            }
            else if(numeroActividad == ultAct){
                contUltAct++;
            }
            else{
                contUltAct = 1;
                ultAct = numeroActividad;
            }

        }
        else if(contGlobal >= Estado.MAX_SIZE){
            retEstado = new EstadoNoTeEntiendo(this.contUltAct, this.ultAct);

        }
        else if(contGlobal == 0){
            ultAct = numeroActividad;
            contUltAct++;
        }

        if(contUltAct == 3){
            retEstado = new EstadoActividad(this.ultAct);
        }

        contGlobal++;

        return retEstado;
    }

    @Override
    public int getActividad() {
        return -1;
    }
}

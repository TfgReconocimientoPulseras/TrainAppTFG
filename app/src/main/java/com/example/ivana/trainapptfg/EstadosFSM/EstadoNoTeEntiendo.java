package com.example.ivana.trainapptfg.EstadosFSM;

public class EstadoNoTeEntiendo extends Estado {
    private int contUltAct;
    private int ultAct;


    public EstadoNoTeEntiendo(int contUltAct, int ultAct) {
        this.contUltAct = contUltAct;
        this.ultAct = ultAct;
    }

    @Override
    public Estado procesarActividad(int numeroActividad) {
        Estado retEstado = this;


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
        if(contUltAct != MIN_ACT_SEGUIDAS_IGUALES){
            retEstado = new EstadoNoTeEntiendo(this.contUltAct, this.ultAct);
        }
        else if(contUltAct == MIN_ACT_SEGUIDAS_IGUALES){
            retEstado = new EstadoActividad(this.ultAct);
        }

        return retEstado;
    }

    @Override
    public int getActividad() {
        return 0;
    }
}

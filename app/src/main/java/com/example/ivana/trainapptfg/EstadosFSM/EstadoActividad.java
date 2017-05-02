package com.example.ivana.trainapptfg.EstadosFSM;

public class EstadoActividad extends Estado {
    private int actividad;

    public EstadoActividad(int actividad){
        super();
        this.actividad = actividad;
    }

    @Override
    public Estado procesarActividad(int numeroActividad) {
        Estado estadoSiguiente;

        if(numeroActividad != this.actividad){
            estadoSiguiente = new EstadoDudoso(this, numeroActividad);
        }
        else{
            estadoSiguiente = this;
        }

        return estadoSiguiente;
    }

    public int getActividad() {
        return actividad;
    }
}

package com.example.ivana.trainapptfg.EstadosFSM;

public abstract class Estado {
    protected static int MAX_SIZE = 5;

    public abstract Estado procesarActividad(int numeroActividad);
    public abstract int getActividad();

}

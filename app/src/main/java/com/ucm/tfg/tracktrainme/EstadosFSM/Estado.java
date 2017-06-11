package com.ucm.tfg.tracktrainme.EstadosFSM;

import android.content.Context;

import com.ucm.tfg.tracktrainme.DataBase.HistoryDataTransfer;

public abstract class Estado {
    protected static int MAX_SIZE = 5;
    protected static int MIN_ACT_SEGUIDAS_IGUALES = 3;
    protected static HistoryDataTransfer historyDataTransfer;
    protected static Context context;

    public Estado(){
        super();
    }

    public void setContext(Context ctx){
        context = ctx;
    }

    public Context getContext(){
        return context;
    }

    public abstract Estado procesarActividad(int numeroActividad);
    public abstract int getActividad();


}

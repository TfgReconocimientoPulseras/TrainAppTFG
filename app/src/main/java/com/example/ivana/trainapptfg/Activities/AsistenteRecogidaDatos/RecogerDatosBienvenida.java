package com.example.ivana.trainapptfg.Activities.AsistenteRecogidaDatos;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.example.ivana.trainapptfg.R;
import com.example.ivana.trainapptfg.Services.BluetoothLeService;

public class RecogerDatosBienvenida extends Activity {

    //ELEMENTOS GRÁFICOS/////////////////////////////////////////////////////////////////////////////////////////////
    private Button botonNext;

    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recoger_datos_bienvenida);
        this.botonNext = (Button)findViewById(R.id.buttonNextBienvenida);


    }

    protected void onResume() {

        super.onResume();
    }

    public void onClickButtonNext(View view) {

        if(!mBound) {
            Intent intent = new Intent(this, BluetoothLeService.class);
            bindService(intent, mConnetion, Context.BIND_AUTO_CREATE);
        }
        else{
            mService.mensaje_apagarSensorCC2650();
        }

        //mService.mensaje_encenderSensorCC2650();

        //Intent formulario = new Intent (RecogerDatosBienvenida.this, RecogerDatosFormulario.class);
        //startActivity(formulario);

    }

    private BluetoothLeService mService;
    private boolean mBound = false;
    private ServiceConnection mConnetion = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            BluetoothLeService.LocalBinder binder = (BluetoothLeService.LocalBinder) service;
            mService = binder.getService();
            Log.d("BIND", "mBound(true)");
            mBound = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            Log.d("BIND", "mBound(false)");
            mBound = false;
        }
    };

    public void desconexionService(){
        if(mBound){
            unbindService(mConnetion);
            mBound = false;
        }
    }
}

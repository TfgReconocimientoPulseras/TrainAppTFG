package com.ucm.tfg.tracktrainme.Activities.AsistenteRecogidaDatos;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.ucm.tfg.tracktrainme.MainActivity;
import com.ucm.tfg.tracktrainme.R;

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

        Intent formulario = new Intent (RecogerDatosBienvenida.this, RecogerDatosFormulario.class);
        startActivity(formulario);
    }
}

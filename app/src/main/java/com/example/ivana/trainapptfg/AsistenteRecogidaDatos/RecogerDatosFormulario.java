package com.example.ivana.trainapptfg.AsistenteRecogidaDatos;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.ivana.trainapptfg.R;


public class RecogerDatosFormulario extends Activity {

    //ELEMENTOS GRÁFICOS//////////////////////////////////////////////////////////////////////////////////////////////
    private Button botonNext;
    private EditText textNombre;
    private EditText textActividad;

    //DATOS FORMULARIO////////////////////////////////////////////////////////////////////////////////////////////////
    private String nameUser;
    private String nameActivity;

    //////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recoger_datos_formulario);

        this.botonNext = (Button)findViewById(R.id.buttonNextForumulario);
        this.textNombre = (EditText)findViewById(R.id.nombreUsuFormulario);
        this.textActividad = (EditText)findViewById(R.id.nombreActFormulario);

    }

    protected void onResume() {

        super.onResume();
    }

    public void onClickButtonNext(View view) {

        this.nameUser = this.textNombre.getText().toString();
        this.nameActivity = this.textActividad.getText().toString();

        if(this.nameUser.equals("") || this.nameActivity.equals("")){
            Toast.makeText(this, "¡Rellene todos los campos!", Toast.LENGTH_LONG);
        }
        else{
            Intent recogida = new Intent (RecogerDatosFormulario.this, RecogerDatosRecogida.class);
            recogida.putExtra("nombreUsu", this.nameUser);
            recogida.putExtra("nombreAct", this.nameActivity);
            startActivity(recogida);
        }
    }
}

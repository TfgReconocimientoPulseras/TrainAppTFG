package com.example.ivana.trainapptfg.Historial;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;

import com.example.ivana.trainapptfg.R;

public class HistorialDiaConcreto extends Activity {

    private TextView textoPruebas;

    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_historial_dia_concreto);
        this.textoPruebas = (TextView)findViewById(R.id.textoPrueba);
        Intent i = getIntent();
        this.textoPruebas.setText("Aquí aparecerá una lista con las actividades que has realizado en el día " + Integer.toString(i.getIntExtra("dayOfMonth", 1)) + " del " + Integer.toString(i.getIntExtra("month", 1) + 1) + " del " + Integer.toString(i.getIntExtra("year", 1970)) + " ;)");
    }

    protected void onResume() {

        super.onResume();
    }
}

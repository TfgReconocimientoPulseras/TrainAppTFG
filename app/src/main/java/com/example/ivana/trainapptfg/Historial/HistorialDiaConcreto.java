package com.example.ivana.trainapptfg.Historial;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.example.ivana.trainapptfg.DataBase.DatabaseAdapter;
import com.example.ivana.trainapptfg.R;

import java.util.ArrayList;
import java.util.List;

public class HistorialDiaConcreto extends Activity {

    private ListView listView;
    private ArrayAdapter<String> adapter;
    private ArrayList<String> list;

    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_historial_dia_concreto);
        this.listView = (ListView)findViewById(R.id.list);

        //INICIALIZAMOS MODELO LISTA//////////////////////////////////////////////////////////////////////
        //this.list = new ArrayList<String>();

        //INICIALIZAMOS ADAPTADOR/////////////////////////////////////////////////////////////////////////


        Intent i = getIntent();
        String dateToShow = i.getStringExtra("dayToShow");
        DatabaseAdapter db = new DatabaseAdapter(this);

        db.open();
        List list = db.dameActividadesFecha(dateToShow);
        db.close();

        this.adapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_list_item_1, android.R.id.text1, list);

        listView.setAdapter(adapter);
        //this.textoPruebas.setText("Aquí aparecerá una lista con las actividades que has realizado en el día " + Integer.toString(i.getIntExtra("dayOfMonth", 1)) + " del " + Integer.toString(i.getIntExtra("month", 1) + 1) + " del " + Integer.toString(i.getIntExtra("year", 1970)) + " ;)");
    }

    protected void onResume() {

        super.onResume();
    }
}

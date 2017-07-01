package com.ucm.tfg.tracktrainme.Historial;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import com.ucm.tfg.tracktrainme.DataBase.DatabaseAdapter;
import com.ucm.tfg.tracktrainme.DataBase.HistoryDataTransfer;
import com.ucm.tfg.tracktrainme.R;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class HistorialDiaConcreto extends AppCompatActivity {

    private ListView listView;
    private HistorialListAdapter adapter;

    protected void onCreate(Bundle savedInstanceState){
        List<HistoryDataTransfer> list;
        List<String> nombreActividad = new ArrayList<>();
        List<Date> horaInicio = new ArrayList<>();
        List<Date> horaFin = new ArrayList<>();
        Intent i;
        String dateToShow;
        String dateToShow2;
        DatabaseAdapter db;

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_historial_dia_concreto);
        this.listView = (ListView)findViewById(R.id.list);

        i = getIntent();
        dateToShow = i.getStringExtra("dayToShow");
        dateToShow2 = i.getStringExtra("dayToShow2");
        db = new DatabaseAdapter(this);

        db.open();
        list = db.dameActividadesFecha(dateToShow);
        db.close();

        for(HistoryDataTransfer aux: list){
            nombreActividad.add(aux.getNombreActividad());
            horaInicio.add(aux.getfIni());
            horaFin.add(aux.getfFin());
        }

        this.adapter = new HistorialListAdapter(this, nombreActividad, horaInicio, horaFin);

        listView.setAdapter(adapter);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbarHistorial);
        toolbar.setTitle("Historial del " + dateToShow2);
        setSupportActionBar(toolbar);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_historial, menu);
        return true;
    }

    protected void onResume() {

        super.onResume();
    }
}

package com.ucm.tfg.tracktrainme.Activities;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.text.InputType;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import com.ucm.tfg.tracktrainme.DataBase.ActivityDataTransfer;
import com.ucm.tfg.tracktrainme.DataBase.DatabaseAdapter;
import com.ucm.tfg.tracktrainme.DataBase.HistoryDataTransfer;
import com.ucm.tfg.tracktrainme.Historial.HistorialListAdapter;
import com.ucm.tfg.tracktrainme.R;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class ListarActividades extends AppCompatActivity {
    private ListView listView;
    private FloatingActionButton floatingActionButton;

    private ActividadesListAdapter adapter;
    private List<ActivityDataTransfer> list;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        List<String> nombreActividad = new ArrayList<>();
        List<Date> fechaCreacion = new ArrayList<>();

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_listar_actividades);
        this.listView = (ListView)findViewById(R.id.list);
        this.listView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                ActivityDataTransfer activityDataTransfer = (ActivityDataTransfer) list.get(position);
                DatabaseAdapter db = new DatabaseAdapter(getActivity());
                db.open();
                if(db.deleteActivty(activityDataTransfer.getId())){
                    list.remove(position);
                    adapter.notifyDataSetChanged();
                    Toast toast = Toast.makeText(getActivity(),"Actividad eliminada", Toast.LENGTH_LONG);
                    toast.show();

                }
                else{
                    Toast toast = Toast.makeText(getActivity(),"Fallo al eliminar la actividad", Toast.LENGTH_LONG);
                    toast.show();
                }

                db.close();
                return true;
            }
        });

        DatabaseAdapter db = new DatabaseAdapter(this);
        db.open();
        list = db.listarActividadesSistema();
        db.close();

        for(ActivityDataTransfer aux: list){
            nombreActividad.add(aux.getName());
            fechaCreacion.add(aux.getFechaCreacion());
        }

        this.adapter = new ActividadesListAdapter(this, nombreActividad, fechaCreacion);

        listView.setAdapter(adapter);
    }

    private Activity getActivity(){
        return this;
    }
}

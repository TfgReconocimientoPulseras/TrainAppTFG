package com.example.ivana.trainapptfg.Activities;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.text.InputType;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import com.example.ivana.trainapptfg.DataBase.ActivityDataTransfer;
import com.example.ivana.trainapptfg.DataBase.DatabaseAdapter;
import com.example.ivana.trainapptfg.R;

import java.util.List;

public class ListarActividades extends AppCompatActivity {
    private ListView listView;
    private FloatingActionButton floatingActionButton;

    private ArrayAdapter<String> adapter;
    private List list;


    //TODO HACER LIMPIEZA DE ESTA CLASE
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_listar_actividades);
        this.listView = (ListView)findViewById(R.id.list);
        this.listView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                ActivityDataTransfer activityDataTransfer = (ActivityDataTransfer) list.get(position);
                DatabaseAdapter db = new DatabaseAdapter(getActivity());
                //TODO COMPROBAR SI LA ACTIVIDAD YA EXISTÍA CAMPO UNIQUE
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

        this.floatingActionButton = (FloatingActionButton) findViewById(R.id.floatingActionButton);

        this.floatingActionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                //TODO CREAR MÉTODO PARA ENCAPSULAR LA CREACION DEL DIALOGO
                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                builder.setTitle("Añadir nueva actividad");

                final EditText input = new EditText(getActivity());
                input.setInputType(InputType.TYPE_CLASS_TEXT);
                builder.setView(input);
                builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                       String activity = input.getText().toString();
                        DatabaseAdapter db = new DatabaseAdapter(getActivity());
                        //TODO COMPROBAR SI LA ACTIVIDAD YA EXISTÍA CAMPO UNIQUE
                        db.open();
                        long id = db.insertActivity(activity);
                        if( id != -1){
                            list.add(db.getDataTransfer(id));
                            adapter.notifyDataSetChanged();
                        }
                        else{
                            Toast toast = Toast.makeText(getActivity(),"La actividad ya se encuentra registrada", Toast.LENGTH_LONG);
                            toast.show();
                        }
                        
                        db.close();
                    }
                });
                builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });
                builder.create().show();
            }
        });


        DatabaseAdapter db = new DatabaseAdapter(this);
        db.open();
        list = db.listarActividadesSistema();
        db.close();

        this.adapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_list_item_1, android.R.id.text1, list);

        listView.setAdapter(adapter);
    }

    private Activity getActivity(){
        return this;
    }
}

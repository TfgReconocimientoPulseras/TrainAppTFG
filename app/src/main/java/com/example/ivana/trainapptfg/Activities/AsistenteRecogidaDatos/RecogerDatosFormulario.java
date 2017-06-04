package com.example.ivana.trainapptfg.Activities.AsistenteRecogidaDatos;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.ivana.trainapptfg.DataBase.ActivityDataTransfer;
import com.example.ivana.trainapptfg.DataBase.DatabaseAdapter;
import com.example.ivana.trainapptfg.R;
import com.example.ivana.trainapptfg.Utilidades.Utils;

import java.io.File;


public class RecogerDatosFormulario extends Activity {

    //ELEMENTOS GRÁFICOS//////////////////////////////////////////////////////////////////////////////////////////////
    private Button botonNext;
    private EditText textNombre;
    private EditText textActividad;

    //DATOS FORMULARIO////////////////////////////////////////////////////////////////////////////////////////////////
    private String nameUser;
    private String nameActivity;

    //CONSTANTES//////////////////////////////////////////////////////////////////////////////////////////////////////
    private static final int REQUEST_WRITE_EXTERNAL_STORAGE = 123;

    //////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recoger_datos_formulario);

        this.botonNext = (Button)findViewById(R.id.buttonNextForumulario);
        this.textNombre = (EditText)findViewById(R.id.nombreUsuFormulario);
        this.textActividad = (EditText)findViewById(R.id.nombreActFormulario);

        askForStoragePermission();

    }

    protected void onResume() {

        super.onResume();
    }

    public void onClickButtonNext(View view) {

        boolean aplicacionNoexiste = false;
        ActivityDataTransfer aux = null;

        this.nameUser = this.textNombre.getText().toString();
        this.nameActivity = this.textActividad.getText().toString();

        if(this.nameUser.equals("") || this.nameActivity.equals("")){
            Toast.makeText(this, "¡Rellene todos los campos!", Toast.LENGTH_LONG);
        }
        else{
            //1º Crear carpeta donde se alojarán los datos
            File directory = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/MyFiles/" + this.nameActivity);
            directory.mkdirs();

            //2º Registrar la actividad en la base de datos
            DatabaseAdapter db = new DatabaseAdapter(this);
            db.open();

            ActivityDataTransfer nuevaActividad = new ActivityDataTransfer(this.nameActivity, Environment.getExternalStorageDirectory().getAbsolutePath() + "/MyFiles/Imagenes/" + "ico_act_new.jpg");

            long id = db.insertActivity(nuevaActividad);
            if( id != -1){
                aux = db.getActivityDataTransfer(id);
                aplicacionNoexiste = true;
            }
            else{
                Toast toast = Toast.makeText(this,"La actividad ya se encuentra registrada", Toast.LENGTH_LONG);
                toast.show();
            }

            db.close();

            //3º Arrancar la tercera parte del asistente de recogida de datos
            if(aplicacionNoexiste){
                Intent recogida = new Intent (RecogerDatosFormulario.this, RecogerDatosRecogida.class);
                recogida.putExtra("nombreUsu", this.nameUser);
                recogida.putExtra("nombreAct", this.nameActivity);
                recogida.putExtra("numActividad", (int)aux.getId());
                startActivity(recogida);
            }
        }
    }

    private void askForStoragePermission(){
        if(ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_WRITE_EXTERNAL_STORAGE);
        }

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults){
        if(grantResults.length > 0){
            switch (requestCode){
                case REQUEST_WRITE_EXTERNAL_STORAGE:
                    if(!Utils.checkPermissionsResult(this, permissions, grantResults)){
                        System.exit(0);
                    };
                    break;
                default:
                    super.onRequestPermissionsResult(requestCode, permissions, grantResults);
            }
        }
    }
}

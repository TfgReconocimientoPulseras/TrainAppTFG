package com.ucm.tfg.tracktrainme.Activities.AsistenteRecogidaDatos;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.ucm.tfg.tracktrainme.DataBase.ActivityDataTransfer;
import com.ucm.tfg.tracktrainme.DataBase.DatabaseAdapter;
import com.ucm.tfg.tracktrainme.R;
import com.ucm.tfg.tracktrainme.Utilidades.Utils;

import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.UUID;


public class RecogerDatosFormulario extends Activity {

    //ELEMENTOS GRÁFICOS//////////////////////////////////////////////////////////////////////////////////////////////
    private Button botonNext;
    private EditText textActividad;
    private ImageView image;

    //DATOS FORMULARIO////////////////////////////////////////////////////////////////////////////////////////////////
    private String nameUser;
    private String nameActivity;
    private Bitmap bitmap;

    //CONSTANTES//////////////////////////////////////////////////////////////////////////////////////////////////////
    private static final int REQUEST_WRITE_EXTERNAL_STORAGE = 123;
    private static final int PICK_CODE = 321;
    private String dirImage;


    //////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recoger_datos_formulario);

        this.botonNext = (Button)findViewById(R.id.buttonNextForumulario);
        this.textActividad = (EditText)findViewById(R.id.nombreActFormulario);
        this.image = (ImageView) findViewById(R.id.imagen_actividad);
        this.image.setImageDrawable(ContextCompat.getDrawable(getApplicationContext(), R.drawable.ico_activ));

        askForStoragePermission();

    }

    protected void onResume() {

        super.onResume();
    }

    public void onClickButtonImage(View view){
        Intent getIntent = new Intent(Intent.ACTION_GET_CONTENT);
        getIntent.setType("image/*");

        Intent pickIntent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        pickIntent.setType("image/*");

        Intent chooserIntent = Intent.createChooser(getIntent, "Select Image");
        chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, new Intent[] {pickIntent});

        startActivityForResult(chooserIntent, PICK_CODE);
    }

    public void onClickButtonNext(View view) {

        int id = -1;
        boolean actividadNoexiste = false;

        this.nameUser = "Pepe";
        this.nameActivity = this.textActividad.getText().toString();

        if(this.nameUser.equals("") || this.nameActivity.equals("")){
            Toast.makeText(this, "¡Rellene todos los campos!", Toast.LENGTH_LONG).show();
        }
        else{
            //1º Crear carpeta donde se alojarán los datos

            //2º Registrar la actividad en la base de datos
            DatabaseAdapter db = new DatabaseAdapter(this);
            db.open();

            if(!db.existeActividad(this.nameActivity)){
                actividadNoexiste = true;
                id = db.getNextIndexValueActivityTable();
            }
            else{
                Toast.makeText(this,"La actividad ya se encuentra registrada", Toast.LENGTH_LONG).show();
            }

            db.close();

            //3º Arrancar la tercera parte del asistente de recogida de datos
            if(actividadNoexiste){
                Intent recogida = new Intent (RecogerDatosFormulario.this, RecogerDatosRecogida.class);
                recogida.putExtra("nombreUsu", this.nameUser);
                recogida.putExtra("nombreAct", this.nameActivity);
                recogida.putExtra("dirImage", this.dirImage);
                recogida.putExtra("numActividad", id);
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


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == PICK_CODE) {
            if (resultCode == RESULT_OK) {
                InputStream stream = null;

                try {
                    stream = getContentResolver().openInputStream(data.getData());
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }

                bitmap = BitmapFactory.decodeStream(stream);
                image.setImageBitmap(bitmap);

                this.dirImage = data.getDataString();
            }
        }
    }
}

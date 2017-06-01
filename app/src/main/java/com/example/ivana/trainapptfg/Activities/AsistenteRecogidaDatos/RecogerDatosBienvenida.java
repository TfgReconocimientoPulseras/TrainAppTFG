package com.example.ivana.trainapptfg.Activities.AsistenteRecogidaDatos;

import android.app.Activity;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.example.ivana.trainapptfg.R;
import com.example.ivana.trainapptfg.Utilidades.Utils;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.List;

import cz.msebera.android.httpclient.Header;

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

        //Intent formulario = new Intent (RecogerDatosBienvenida.this, RecogerDatosFormulario.class);
        //startActivity(formulario);

        do_post();

    }

    public void do_post(){
        String url = "http://192.168.1.33:8081/subeDatos";
        AsyncHttpClient asyncHttpClient = new AsyncHttpClient();
        RequestParams requestParams = new RequestParams();
        File directory = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/MyFiles/");

        try {
            requestParams.put("upload", Utils.convertListToArray(Utils.obtenerArchivosDatosRecursivo(directory)));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        //AÑADIR FILES AL PARAM
        asyncHttpClient.post(url, requestParams, new AsyncHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                Log.d("REQUEST AL SERVIDOR", "PETICION EXITOSA");
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                Log.d("REQUEST AL SERVIDOR", "PETICION DESASTRE");
            }
        });

    }
}

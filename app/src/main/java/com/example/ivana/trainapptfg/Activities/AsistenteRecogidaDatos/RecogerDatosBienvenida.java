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


import org.codehaus.commons.compiler.CompileException;
import org.codehaus.janino.ScriptEvaluator;

import java.io.File;
import java.io.FileNotFoundException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;

import bsh.EvalError;
import bsh.Interpreter;
import bsh.UtilEvalError;
import bsh.util.BeanShellBSFEngine;
import cz.msebera.android.httpclient.Header;
import joinery.DataFrame;

public class RecogerDatosBienvenida extends Activity {

    //ELEMENTOS GRÁFICOS/////////////////////////////////////////////////////////////////////////////////////////////
    private Button botonNext;
    private String daniString;

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

        //if(daniString == null) {
            //do_post();
        //}
        //else {
            compilaArbol("");
        //}

    }

    public void do_post(){
        String url = "http://192.168.1.34:8081/subeDatos";
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
                daniString = new String(responseBody);
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                Log.d("REQUEST AL SERVIDOR", "PETICION DESASTRE");
            }
        });
    }


    public void compilaArbol(String responseBody) {
/**
        SimpleCompiler sc = new SimpleCompiler();
        String className = "PruebasServer";

        try {
            sc.cook(responseBody);
            Class<?> daniClass;
            daniClass = sc.getClassLoader().loadClass(className);

            Object daniInstancia = daniClass.newInstance();
            Method printNumber = daniClass.getDeclaredMethod("printNumber", null);

            Object result = printNumber.invoke(daniInstancia);
        } catch (CompileException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }

        Log.d("REQUEST AL SERVIDOR", "PETICION EXITOSA: " + new String(responseBody));

 **/
        Interpreter interpreter = new Interpreter();
        String path = null;

        /*
        HashMap hashMap = new HashMap<>();
        hashMap.put("hola", 2);
        System.out.println(hashMap.get("hola"));
        if(hashMap.get("hola") >= 2 ){
            System.out.println(hashMap.get("hola")
        }*/

        String s = "HashMap hashMap = new HashMap();\n" +
                "hashMap.put(\"hola\", 1);\n" +
                "if(hashMap.get(\"hola\") >= 2 ){\n" +
                "   System.out.println(hashMap.get(\"hola\"));\n" +
                "}" +
                "System.out.println(hashMap.get(\"hola\"));";
        try {
            interpreter.eval(s);

        } catch (EvalError evalError) {
            evalError.printStackTrace();
        }

    }
}

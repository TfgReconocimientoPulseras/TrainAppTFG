package com.example.ivana.trainapptfg.Activities.AsistenteRecogidaDatos;

import android.app.Activity;
import android.content.Context;
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

import org.cakedev.internal.janino.SimpleCompiler;
import org.cakedev.internal.janino.compiler.CompileException;

import java.io.File;
import java.io.FileNotFoundException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import cz.msebera.android.httpclient.Header;

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

        String sClassName = "RForest";
        String sClassImport = "import java.util.ArrayList; \n import java.util.Iterator; \n import java.util.ListIterator; \n\n";
        String sClassHead = " public class RForest { \n";
        String sClassVariables = "   public int a = 2; \n   public String ab = \"MiTexto\"; \n ";
        String sClassMethod1 = "  public float classifica(String ab, int a) {\n if (a > 1){\n System.out.println(ab + \" - IF 1\"); \n} ";
        sClassMethod1 += " else { \n System.out.println(ab + \" - ELSE 1 \"); \n } \n return 1.1f; \n } \n\n";
        String sClassMethod2 = "   public float printArray(ArrayList al) { for (String a: al) { if (a == \"C\" || a == \"D\" || a == \"E\"){ System.out.println(\"--> LETRAS \" + a); \n" +
                " }else { System.out.println(\"--> NUMERO \" + a); } } return 5.3f; }\n";

        String sClassMethod3 = "   public float classifica2 (ArrayList al) { \n Iterator itr = al.iterator(); \n while(itr.hasNext()) { \n Object element = itr.next(); \n  System.out.print(element + \" \"); \n}\n return 0.1f;}\n";


        String sClass = sClassImport.concat(sClassHead).concat(sClassVariables).concat(sClassMethod1).concat(sClassMethod3).concat("\n}\n");
        System.out.println(sClass);

        try {

            SimpleCompiler sc = new SimpleCompiler();
            sc.cook(sClass);
            Class<?> arneClass;
            arneClass = sc.getClassLoader().loadClass(sClassName);
            Class[] paramString = new Class[2];
            paramString[0] = String.class;
            paramString[1] = int.class;

            Class[] param = new Class[1];
            param[0] = ArrayList.class;

            ArrayList<String> al = new ArrayList<String>();

            al.add("C");
            al.add("1");
            al.add("E");
            al.add("3");
            al.add("D");
            al.add("5");
            Object arne = arneClass.newInstance();
            Method classifica = arneClass.getDeclaredMethod("classifica", paramString);
            Object result1 = classifica.invoke(arne, "MyTest", 0);
            System.out.println(result1);
            Method printArray = arneClass.getDeclaredMethod("classifica2", param);
            Object result2 = printArray.invoke(arne, al);
            System.out.println(result2);

        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (SecurityException e) {
            e.printStackTrace();
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        } catch (CompileException e) {
            e.printStackTrace();
        }
    }
}

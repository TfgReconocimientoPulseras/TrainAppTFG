package com.example.ivana.trainapptfg.Fragments;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.ivana.trainapptfg.DataBase.ActivityDataTransfer;
import com.example.ivana.trainapptfg.DataBase.DatabaseAdapter;
import com.example.ivana.trainapptfg.MainActivity;
import com.example.ivana.trainapptfg.R;
import com.example.ivana.trainapptfg.Services.BluetoothLeService;
import com.example.ivana.trainapptfg.Services.RecogidaDeDatosService;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import joinery.DataFrame;

public class ReconocerActividadFragment extends Fragment {

    private BluetoothLeService mServiceBluetooth;
    private HashMap<Integer, HashMap<String, Object>> actividadesSistema;
    private static final String KEY_NOMBRE_ACTIVIDAD = "text";
    private static final String KEY_IMAGEN = "imagen";


    private Handler modificadorActividad = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            /*if(idActividad == 0){
                texto = "No tengo muy claro lo que estás haciendo.";
                iconoActividad.setBackgroundResource(R.drawable.ico_act_0);
            }
            else if(idActividad == 1){
                texto = "Estas caminando.";
                iconoActividad.setBackgroundResource(R.drawable.ico_act_1);
            }
            else if(idActividad == 2){
                texto = "Estás aplaudiendo.";
                iconoActividad.setBackgroundResource(R.drawable.ico_act_8);
            }
            else if(idActividad == 3){
                texto = "Estás quieto.";
                iconoActividad.setBackgroundResource(R.drawable.ico_act_3);
            }
            else if(idActividad == 4){
                texto = "Estás barriendo.";
                iconoActividad.setBackgroundResource(R.drawable.ico_act_2);
            }
            else if(idActividad == -20){
                texto = "Empiezo a notar algo....";
                iconoActividad.setBackgroundResource(R.drawable.ico_pensando);
            }
            else if(idActividad == -40){
                texto = "Debes estar haciendo algo como...";
                iconoActividad.setBackgroundResource(R.drawable.ico_idea);
            }
            nombreActividad.setText(texto);*/

            int idActividad = (Integer)msg.obj;

            Log.d("MECCCCCCCCc ¬¬", String.valueOf(idActividad));
            if(idActividad != 0){
                nombreActividad.setText((String)((actividadesSistema.get(idActividad)).get(KEY_NOMBRE_ACTIVIDAD)));
                iconoActividad.setImageBitmap((Bitmap)((actividadesSistema.get(idActividad)).get(KEY_IMAGEN)));
            }
            else if(idActividad == 0){
                iconoActividad.setImageBitmap(BitmapFactory.decodeFile(Environment.getExternalStorageDirectory().getAbsolutePath() + "/MyFiles/Imagenes/" + "ico_act_0.png"));
                nombreActividad.setText("No tengo muy claro lo que estás haciendo.");
            }

        }
    };

    //GESTIÓN DE DATAFRAMES//////////////////////////////////////////////////////////////////////////////////////////////////
    private DataFrame featuresSegmentado1;
    private DataFrame featuresSegmentado2;

    // {"Andar", "Barrer", "De pie", "Subir escaleras", "Bajar escaleras"};

    private int reconocedorEncendido = 0;


    //GESTIÓN ELEMENTOS GRÁFICOS
    private Button button;
    private TextView nombreActividad;
    private ImageView iconoActividad;

    //BROADCAST RECEIVER
    private BroadcastReceiver receiver;

    public ReconocerActividadFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        this.actividadesSistema = new HashMap<Integer, HashMap<String, Object>>();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_reconocer_actividad, container, false);

        nombreActividad = (TextView) view.findViewById(R.id.nombre_actividad);
        iconoActividad = (ImageView) view.findViewById(R.id.icono_actividad);
        //iconoActividad.setBackgroundResource(R.drawable.ico_pausa);
        iconoActividad.setImageBitmap(BitmapFactory.decodeFile(Environment.getExternalStorageDirectory().getAbsolutePath() + "/MyFiles/Imagenes/" + "ico_pausa.png"));

        receiver = new BroadcastReceiver(){
            @Override
            public void onReceive(Context context, Intent intent) {
                int estado = intent.getIntExtra("estado", 0);
                Message msg = new Message();
                msg.obj = estado;
                modificadorActividad.sendMessage(msg);
            }
        };
        button = (Button) view.findViewById(R.id.boton_reconocer);
        button.setOnClickListener(new View.OnClickListener() {
            private Intent intent;
            @Override
            public void onClick(View v) {

                if(reconocedorEncendido == 0) {
                    reconocedorEncendido = 1;
                    nombreActividad.setText("Comenzando a reconocer...");
                    button.setText("Parar el reconocimiento");

                    DatabaseAdapter db = new DatabaseAdapter(getContext());
                    ActivityDataTransfer activityDataTransfer = null;
                    db.open();
                    List<ActivityDataTransfer> listaAct = db.listarActividadesSistema();
                    db.close();

                    for(final ActivityDataTransfer act: listaAct){
                        //actividadesSistema.put((int)act.getId(), new ArrayList(){{add(0, act.getName()); add(1,act.getUrlImage());}});
                        Log.d("ACTIVIDADES", String.valueOf((int)act.getId()) + " " + act.getName() + " " + act.getUrlImage());
                        HashMap<String, Object> aux = new HashMap<String, Object>();
                        aux.put(KEY_NOMBRE_ACTIVIDAD, act.getName());
                        aux.put(KEY_IMAGEN, BitmapFactory.decodeFile(act.getUrlImage()));

                        actividadesSistema.put((int)act.getId(), aux);

                    }

                    if(mBound){
                        mService.mensaje_encenderSensorCC2650();
                    }

                    intent = new Intent(getContext(), RecogidaDeDatosService.class);
                    intent.putExtra("modo", ((MainActivity)getActivity()).getModo());
                    getActivity().startService(intent);
                }
                else if(reconocedorEncendido == 1){
                    getActivity().stopService(intent);
                    nombreActividad.setText("Para volver a reconocer pulse el botón de abajo.");
                    button.setText("Comenzar a reconocer");
                    reconocedorEncendido = 0;
                    //iconoActividad.setBackgroundResource(R.drawable.ico_pausa);
                    iconoActividad.setImageBitmap(BitmapFactory.decodeFile(Environment.getExternalStorageDirectory().getAbsolutePath() + "/MyFiles/Imagenes/" + "ico_pausa.png"));
                    actividadesSistema.clear();
                }
            }
        });

        return view;
    }

    private void formatDataToCsvExternalStorage(String fName, DataFrame df) {
        String fileName = new Date().getTime() + "_" + fName + ".csv";
        File directory = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/MyFiles");
        directory.mkdirs();
        File file = new File(directory, fileName);

        try {
            OutputStream oS = new FileOutputStream(file);
            df.writeCsv(oS);
            oS.flush();
            oS.close();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        LocalBroadcastManager.getInstance(getContext()).registerReceiver((receiver),
                new IntentFilter("estado_actualizado")
        );


        if(((MainActivity)getActivity()).getModo().equals("PULSERA")) {
            if (!mBound) {
                Intent intent = new Intent(getContext(), BluetoothLeService.class);
                getActivity().bindService(intent, mConnetion, Context.BIND_AUTO_CREATE);
            }
        }
    }

    @Override
    public void onStop() {
        LocalBroadcastManager.getInstance(getContext()).unregisterReceiver(receiver);
        super.onStop();
    }

    private BluetoothLeService mService;
    private boolean mBound = false;
    private ServiceConnection mConnetion = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            BluetoothLeService.LocalBinder binder = (BluetoothLeService.LocalBinder) service;
            mService = binder.getService();
            Log.d("BIND", "mBound(true)");
            mBound = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            Log.d("BIND", "mBound(false)");
            mBound = false;
        }
    };

    public void desconexionService(){
        if(mBound){
            getActivity().unbindService(mConnetion);
            mBound = false;
        }
    }

}

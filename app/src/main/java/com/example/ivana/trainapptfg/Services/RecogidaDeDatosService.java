package com.example.ivana.trainapptfg.Services;

import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.example.ivana.trainapptfg.Sensor.SensorMovil;
import com.example.ivana.trainapptfg.Sensor.SensorPulsera;
import com.example.ivana.trainapptfg.Threads.AnalizarClasificacionThread;
import com.example.ivana.trainapptfg.Threads.ClasificacionDeDatosThread;
import com.example.ivana.trainapptfg.Threads.SegmentacionDeDatosThread;
import com.example.ivana.trainapptfg.Utilidades.DataTAD;
import com.example.ivana.trainapptfg.Utilidades.MiSensorEventListener;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

import joinery.DataFrame;

public class RecogidaDeDatosService extends Service{
    //NOMBRES PARA LOS DATAFRAMES////////////////////////////////////////////////////////////////////////////////////////////
    private Collection colsNames = new ArrayList<String>(){{
        add("timestamp");
        add("gyro-alpha");
        add("gyro-beta");
        add("gyro-gamma");
        add("accel-x");
        add("accel-y");
        add("accel-z");
    }};

    //BROADCASTER PARA ACTUALIZAR LA VISTA////////////////////////////////////////////////////////////////////////////////////
    private LocalBroadcastManager broadcaster;

    //GESTIÓN DE DATAFRAMES//////////////////////////////////////////////////////////////////////////////////////////////////
    private DataFrame df;

    //GESTIÓN DE SENSORES///////////////////////////////////////////////////////////////////////////////////////////////////
    private SensorManager mSensorManager;
    private MiSensorEventListener miSensorEventListenerAcelerometro;
    private MiSensorEventListener miSensorEventListenerGiroscopio;
    private Sensor mAccelerometer;
    private Sensor mGyroscope;
    private long timestampIni = 0;


    //GESTIÓN DE TIMER//////////////////////////////////////////////////////////////////////////////////////////////////////
    private Timer timer;
    private TimerTask timerTask;
    private int timeAcumulated;
    private boolean comenzando = true;

    //COLAS BLOQUEANTES (CONCURRENCIA)//////////////////////////////////////////////////////////////////////////////////////////////////////
    private BlockingQueue<DataFrame> bqRec_Segment;
    private ArrayBlockingQueue bqSegment_Recog;
    private BlockingQueue<DataFrame> bqSegment_Clasif;
    private BlockingQueue<Integer> bqResultados;
    private Thread segmentacionDeDatosThread;
    private Thread clasificacionDeDatosThread;
    private Thread anlisisClasificacionDeDatosThread;

    //CONSTANTES/////////////////////////////////////////////////////////////////////////////////////////////////////////////
    private static final int FREQUENCY_DEF = 100; //100ms
    private static final int DELAY_TIMER_TASK = 1500; //1000ms
    private static final int COLLECTION_TIME = 1000; //5000ms - 5 s para recoger datos y segmentarlos...
    private static final int TAM_COLA_RECOGIDA = 5;
    private static final int TAM_COLA_CLASIFICACION = 5;
    private static final int TAM_COLA_RESULTADOS = 30;

    //SENSOR//////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    private com.example.ivana.trainapptfg.Sensor.Sensor mSensor;

    private String modo;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();

        Log.d("Servicio - Recogida","Iniciado");

        df = new DataFrame(colsNames);
        this.timer = new Timer();
        /*this.mSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        this.mAccelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        this.mGyroscope = mSensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);
        this.miSensorEventListenerAcelerometro = new MiSensorEventListener(this.mAccelerometer, this.mSensorManager, SensorManager.SENSOR_DELAY_FASTEST);
        this.miSensorEventListenerGiroscopio = new MiSensorEventListener(this.mGyroscope, this.mSensorManager, SensorManager.SENSOR_DELAY_FASTEST);*/
        this.timeAcumulated = 0;

        this.broadcaster = LocalBroadcastManager.getInstance(this);


        //TODO ADAPTAR A LA NUEVA FORMA QUE TIENE EL HANDLER QUE MANEJA NOMBRE Y FOTO
        /*Intent intent = new Intent("estado_actualizado");
        intent.putExtra("estado", -20);
        broadcaster.sendBroadcast(intent);*/

        //TODO FAIR POLICY????
        this.bqRec_Segment = new ArrayBlockingQueue(TAM_COLA_RECOGIDA, true);
        this.bqSegment_Recog = new ArrayBlockingQueue(TAM_COLA_RECOGIDA, true);
        this.bqSegment_Clasif = new ArrayBlockingQueue(TAM_COLA_CLASIFICACION, true);
        this.bqResultados = new ArrayBlockingQueue<Integer>(TAM_COLA_RESULTADOS, true);


        this.segmentacionDeDatosThread = new Thread(new SegmentacionDeDatosThread(bqRec_Segment, bqSegment_Clasif, bqSegment_Recog));
        this.clasificacionDeDatosThread = new Thread(new ClasificacionDeDatosThread(bqSegment_Clasif, bqResultados, getApplicationContext()));
        this.anlisisClasificacionDeDatosThread = new Thread(new AnalizarClasificacionThread(bqResultados, broadcaster, getApplicationContext()));

        this.timerTask = new TimerTask() {

            @Override
            public void run() {
                //timeAcumulated += FREQUENCY_DEF;

                /*DataTAD dataAccel = miSensorEventListenerAcelerometro.obtenerDatosSensor();
                DataTAD dataGyro = miSensorEventListenerGiroscopio.obtenerDatosSensor();*/

                DataTAD dataAccel = mSensor.obtenerDatosAcel();
                DataTAD dataGyro = mSensor.obtenerDatosGyro();


                if(dataAccel != null && dataGyro != null){
                    //unificamos los valores del acelerómetro y del giroscopio...
                    float[] floatUnificada = DataTAD.concatenateValues(dataGyro.getValues(), dataAccel.getValues());
                    DataTAD dataUnificada = new DataTAD(System.currentTimeMillis(), floatUnificada);

                    df.append(dataUnificada.getDataTADasArrayList());

                    int actividadPredicha = -1;
                    int filaActual = df.length();

                    //lo añadimos al dataframe
                    if(timestampIni == 0){
                        timestampIni = (Long) df.get(0, 0);
                        filaActual = 0;
                    }
                    else{
                        filaActual = df.length() - 1;
                    }

                    //TODO CUANDO SE HA COMPLETADO LA PRIMERA VENTANA EL TIMESTAMP ACTUAL ES EL DE LA PRIMERA,NO EL DE LA ULTIMA
                    long timestampActual = (Long) df.get(filaActual, 0);

                    if( (timestampActual - timestampIni) >= COLLECTION_TIME + 10 || (timestampActual - timestampIni) >= COLLECTION_TIME - 10){
                        Log.d("Servicio - Recogida", "Ya han pasado " + COLLECTION_TIME + " segundos\n");
                        try {
                            Log.d("Servicio - Recogida", "Produzco dataframe LONGITUD: " + df.length() + "\n");
                            bqRec_Segment.put(df);
                            df = (DataFrame) bqSegment_Recog.take();
                            df = df.resetIndex();
                            Log.d("Servicio - Recogida", "Consumo dataframe del servicio de procesado\n");

                            timestampIni = 0;

                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }

                    /**
                     if (timeAcumulated >= COLLECTION_TIME) { // tras 5 segundos para pruebas
                     Log.d("Servicio - Recogida", "Ya han pasado 5 segundos\n");

                     try {
                     Log.d("Servicio - Recogida", "Produzco dataframe\n");
                     bqRec_Segment.put(df);
                     } catch (InterruptedException e) {
                     e.printStackTrace();
                     }

                     df = new DataFrame(colsNames);
                     timeAcumulated = 0;

                     }
                     else{
                        if(comenzando){
                             Log.d("Servicio - Recogida", "Iniciando...\n");
                             if(timeAcumulated >= 250){
                             Intent intent = new Intent("estado_actualizado");
                             intent.putExtra("estado", -40);
                             broadcaster.sendBroadcast(intent);
                             comenzando = false;
                        }
                     }
                     }
                     */
                }
            }


        };
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
            //TODO CONTROLAR CUANDO ESTEMOS CONECTADOS CON PULSERA O CON MÓVIL
            if(modo.equals("PULSERA")){
                mSensor = new SensorPulsera(mService);
            }
            else if(modo.equals("MOVIL")){
                mSensor = new SensorMovil(RecogidaDeDatosService.this);
            }


             activarSensores();
            timer.scheduleAtFixedRate(timerTask, DELAY_TIMER_TASK, FREQUENCY_DEF);
            segmentacionDeDatosThread.start();
            clasificacionDeDatosThread.start();
            anlisisClasificacionDeDatosThread.start();
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            Log.d("BIND", "mBound(false)");
            mBound = false;
        }
    };

    public void desconexionService(){
        if(mBound){
            unbindService(mConnetion);
            mBound = false;
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags,
                              int startId) {

        this.modo = intent.getStringExtra("modo");

        //TODO CAMBIAR A CONSTANTE
        if (this.modo.equals("PULSERA") && !mBound) {
            Intent inte = new Intent(getBaseContext(), BluetoothLeService.class);
            bindService(inte, mConnetion, Context.BIND_AUTO_CREATE);
        }
        else if(modo.equals("MOVIL")){
            //TODO ARREGLAR ESTO
            mSensor = new SensorMovil(RecogidaDeDatosService.this);
            activarSensores();
            timer.scheduleAtFixedRate(timerTask, DELAY_TIMER_TASK, FREQUENCY_DEF);
            segmentacionDeDatosThread.start();
            clasificacionDeDatosThread.start();
            anlisisClasificacionDeDatosThread.start();
        }

        return Service.START_STICKY;
    }

    @Override
    public void onDestroy(){
        Log.d("Servicio - Recogida","Terminado");

        timer.cancel();
        desactivarSensores();


        segmentacionDeDatosThread.interrupt();
        clasificacionDeDatosThread.interrupt();
        anlisisClasificacionDeDatosThread.interrupt();

        super.onDestroy();
    }

    private void activarSensores() {
        /*this.miSensorEventListenerAcelerometro.activarSensor();
        this.miSensorEventListenerGiroscopio.activarSensor();*/
        this.mSensor.encenderSensor();
    }

    private void desactivarSensores() {
        /*this.miSensorEventListenerAcelerometro.desactivarSensor();
        this.miSensorEventListenerGiroscopio.desactivarSensor();*/
        this.mSensor.apagarSensor();
    }


}

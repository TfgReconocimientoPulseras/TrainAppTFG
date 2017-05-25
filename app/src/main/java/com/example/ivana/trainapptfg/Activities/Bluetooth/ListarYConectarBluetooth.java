package com.example.ivana.trainapptfg.Activities.Bluetooth;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.ResultReceiver;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import com.example.ivana.trainapptfg.MainActivity;
import com.example.ivana.trainapptfg.R;
import com.example.ivana.trainapptfg.Services.BluetoothLeService;
import com.example.ivana.trainapptfg.Utilidades.Utils;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.concurrent.ArrayBlockingQueue;

//https://developer.android.com/guide/topics/connectivity/bluetooth-le.html
//DOCUMENTACION PARA REALIZAR ESCANEO -> http://www.londatiga.net/it/programming/android/how-to-programmatically-scan-or-discover-android-bluetooth-device/
public class ListarYConectarBluetooth extends AppCompatActivity {
    //Comprobacion de permisos dados
    private static final int REQUEST_ACCESS_COARSE_LOCATION = 222;
    //Comprobacion si bluetooth esta activado
    private static final int REQUEST_ENABLE_BT = 1;

    private boolean bluetoothConfigurado = false;

    //Gestion de la lista de dispositivos
    private ListView listView;
    private ArrayAdapter<String> adapter;
    private ArrayList<String> list;


    private IntentFilter filter;

    private MyResultReceiverBluetooth dispositivosEncontrados;

    public class MyResultReceiverBluetooth extends ResultReceiver {
        public MyResultReceiverBluetooth(Handler handler){
            super(handler);
        }

        @Override
        protected void onReceiveResult(int resultCode, Bundle resultData){
            //100 -> dispositivo bluetooth encontrado, lo mostramos en la lista
            if(resultCode == 100){
                list.add(resultData.getString("address"));
                adapter.notifyDataSetChanged();
            }
            else if(resultCode == 200){
                Intent i = new Intent(ListarYConectarBluetooth.this, MainActivity.class);
                i.putExtra("sensor", "PULSERA");
                startActivity(i);
            }
        }
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////
    //Metodos y atributos para poder conectarse con el service//////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////////////////
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
            unbindService(mConnetion);
            mBound = false;
        }
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////

    @Override
    protected void onStart(){
        super.onStart();

        //TODO salta excepcion cuando se sale de la ventana. arreglar
        if(!mBound) {
            Intent intent = new Intent(this, BluetoothLeService.class);
            startService(intent);
            bindService(intent, mConnetion, Context.BIND_AUTO_CREATE);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_listar_yconectar_bluetooth);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        this.dispositivosEncontrados = new MyResultReceiverBluetooth(null);

        //Solicitud de permisos
        askForLocationPermission();

        //COMPROBACION PARA VER SI EL DISPOSITIVO ES COMPATIBLE CON BLE, EN OTRO CASO MOSTRAR QUE NO LO ES
        if (!getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
            Toast.makeText(this, R.string.ble_not_supported, Toast.LENGTH_SHORT).show();
            finish();
        }

        //INICIALIZAR LISTA (MODELO Y GRAFICA)
        inicializarListaGraficaYModelo();
    }


    @Override
    public void onDestroy(){

        //mService.unbindService(this.mConnetion);
        super.onDestroy();
    }


    ////////////////////////////////////////////////////////////////////////////////////////////////
    //Metodos encargados de los permisos////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////////////////
    //CUANDO SE SOLICITA LOS PERMISOS(requestPermissions()), SE LLAMA A LA SIGUIENTE
    //FUNCION PARA SABER QUE PERMISOS HAN SIDO CONCEDIDOS
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults){
        if(grantResults.length > 0){
            switch (requestCode){
                case REQUEST_ACCESS_COARSE_LOCATION:
                    if(!Utils.checkPermissionsResult(this, permissions, grantResults)){
                        System.exit(0);
                    }
                    break;
                default:
                    super.onRequestPermissionsResult(requestCode, permissions, grantResults);
            }
        }
    }

    private void askForLocationPermission(){
        if(ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, REQUEST_ACCESS_COARSE_LOCATION);
        }
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////
    //Metodos que trabajan con la lista de dispositivos de bluetooth
    ////////////////////////////////////////////////////////////////////////////////////////////////

    private void inicializarListaGraficaYModelo(){

        //INICIALIZAMOS ELEMENTO GRAFICO////////////////////////////////////////////////
        this.listView = (ListView) findViewById(R.id.list);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {

                // ListView Clicked item index
                int itemPosition = position;

                // ListView Clicked item value
                String itemValue = (String) listView.getItemAtPosition(position);

                // Show Alert
                /*Toast.makeText(getApplicationContext(),
                        "Position :" + itemPosition + "  ListItem : " + itemValue, Toast.LENGTH_LONG)
                        .show();*/

                //conectarseDispositivo(itemValue);
                if(!mService.mensaje_conectarDispositivo(itemValue)){
                    Toast.makeText(getApplicationContext(), "No ha sido posible conectarse a " + itemValue, Toast.LENGTH_LONG).show();
                }
                else {
                    Toast.makeText(getApplicationContext(), "Configurando dispositivo  " + itemValue, Toast.LENGTH_LONG).show();
                }
            }

        });

        //INICIALIZAMOS MODELO LISTA//////////////////////////////////////////////////////////////////////
        this.list = new ArrayList<String>();

        //INICIALIZAMOS ADAPTADOR/////////////////////////////////////////////////////////////////////////
        this.adapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_list_item_1, android.R.id.text1, list);

        listView.setAdapter(adapter);
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////
    //Metodos que trabajan con el menu superior
    ////////////////////////////////////////////////////////////////////////////////////////////////


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_bluetooth, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        if(id == R.id.action_refresh){
            if(mBound == true){
                if(!bluetoothConfigurado){
                    this.bluetoothConfigurado = mService.mensaje_configurarBluetooth();
                }


                if(!bluetoothConfigurado){
                    Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                    startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);

                    this.bluetoothConfigurado = mService.mensaje_configurarBluetooth();

                    if(!this.bluetoothConfigurado){
                        Toast.makeText(this, "Parece ser que el Bluetooth no se activado correctamente. " +
                                "Actívelo manuealmanete para poder continuar.", Toast.LENGTH_LONG).show();
                    }
                }

                if(this.bluetoothConfigurado){
                    //mService.inicializarColaDispositivosEncontrados(this.queueDispositivosEncontrados);
                    mService.inicializarResultReceiver(this.dispositivosEncontrados);
                    mService.mensaje_startDiscovery();
                }

                /*if(mBound){
                    unbindService(mConnetion);
                    mBound = false;
                }*/
            }

            this.list.clear();
            this.adapter.notifyDataSetChanged();
            mService.mensaje_startDiscovery();
        }

        return super.onOptionsItemSelected(item);
    }

}

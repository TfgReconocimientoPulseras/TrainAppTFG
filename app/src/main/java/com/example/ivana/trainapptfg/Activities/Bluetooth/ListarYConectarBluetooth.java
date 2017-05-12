package com.example.ivana.trainapptfg.Activities.Bluetooth;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
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

import com.example.ivana.trainapptfg.R;
import com.example.ivana.trainapptfg.Utilidades.Utils;

import java.util.ArrayList;

//https://developer.android.com/guide/topics/connectivity/bluetooth-le.html
//DOCUMENTACION PARA REALIZAR ESCANEO -> http://www.londatiga.net/it/programming/android/how-to-programmatically-scan-or-discover-android-bluetooth-device/
public class ListarYConectarBluetooth extends AppCompatActivity {
    //Comprobacion de permisos dados
    private static final int REQUEST_ACCESS_COARSE_LOCATION = 222;
    //Comprobacion si bluetooth esta activado
    private static final int REQUEST_ENABLE_BT = 1;

    private BluetoothAdapter mBluetoothAdapter;

    //Gestion de la lista de dispositivos
    private ListView listView;
    private ArrayAdapter<String> adapter;
    private ArrayList<String> list;

    private IntentFilter filter;

    //Atiende a los eventos del bluetooth
    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (BluetoothAdapter.ACTION_DISCOVERY_STARTED.equals(action)) {
                Log.d("BLUETOOTH", "DISCOVERY STARTED"); //lo que se ejecuta cuando el bluetooth comienza a escanear
            } else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) { //lo que se ejecuta cuando el bluetooth finaliza el escaneo
                Log.d("BLUETOOTH", "DISCOVERY FINISHED");

            } else if (BluetoothDevice.ACTION_FOUND.equals(action)) { //lo que se ejecuta cuando el bluetooth encuentra un dispositivo
                final BluetoothDevice device = (BluetoothDevice) intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                Log.d("BLUETOOTH", "DEVICE DISCOVERED");

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        list.add(device.getAddress());
                        adapter.notifyDataSetChanged();
                    }
                });
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_listar_yconectar_bluetooth);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);


        //Solicitud de permisos
        askForLocationPermission();

        //COMPROBACION PARA VER SI EL DISPOSITIVO ES COMPATIBLE CON BLE, EN OTRO CASO MOSTRAR QUE NO LO ES
        if (!getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
            Toast.makeText(this, R.string.ble_not_supported, Toast.LENGTH_SHORT).show();
            finish();
        }

        //OBRTENEMOS EL ADAPTADOR DEL BLUETOOTH
        this.mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        filter = new IntentFilter();
        filter.addAction(BluetoothDevice.ACTION_FOUND);
        filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_STARTED);
        filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);


        //COMPROBAMOS QUE ESTÉ ACTIVADO EL BLUETOOTH
        if (mBluetoothAdapter == null || !mBluetoothAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
        }

        //REGISTRAR BROADCAST BLUETOOTH
        registerReceiver(mReceiver, filter);

        //INICIALIZAR LISTA (MODELO Y GRAFICA)
        inicializarListaGraficaYModelo();
    }


    @Override
    public void onDestroy(){
        mBluetoothAdapter.cancelDiscovery();
        unregisterReceiver(mReceiver);
        super.onDestroy();
    }

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
            list.clear();
            adapter.notifyDataSetChanged();
            mBluetoothAdapter.startDiscovery();
        }

        return super.onOptionsItemSelected(item);
    }

    private void inicializarListaGraficaYModelo(){

        //INICIALIZAMOS ELEMENTO GRAFICO/////////////////////////////////////////////////////////////////
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
                Toast.makeText(getApplicationContext(),
                        "Position :" + itemPosition + "  ListItem : " + itemValue, Toast.LENGTH_LONG)
                        .show();

            }

        });

        //INICIALIZAMOS MODELO LISTA//////////////////////////////////////////////////////////////////////
        this.list = new ArrayList<String>();

        //INICIALIZAMOS ADAPTADOR/////////////////////////////////////////////////////////////////////////
        this.adapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_list_item_1, android.R.id.text1, list);

        listView.setAdapter(adapter);
    }

    private void askForLocationPermission(){
        if(ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, REQUEST_ACCESS_COARSE_LOCATION);
        }
    }

    //CUANDO SE SOLICITA LOS PERMISOS(requestPermissions()), SE LLAMA A LA SIGUIENTE FUNCION PARA SABER QUE PERMISOS HAN SIDO CONCEDIDOS
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
}

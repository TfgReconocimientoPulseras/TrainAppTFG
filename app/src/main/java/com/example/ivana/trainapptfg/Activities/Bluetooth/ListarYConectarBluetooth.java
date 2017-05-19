package com.example.ivana.trainapptfg.Activities.Bluetooth;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothProfile;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Bundle;
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
import java.util.HashMap;
import java.util.List;
import java.util.Queue;
import java.util.UUID;
import java.util.concurrent.ConcurrentLinkedQueue;

//https://developer.android.com/guide/topics/connectivity/bluetooth-le.html
//DOCUMENTACION PARA REALIZAR ESCANEO -> http://www.londatiga.net/it/programming/android/how-to-programmatically-scan-or-discover-android-bluetooth-device/
public class ListarYConectarBluetooth extends AppCompatActivity {
    //Comprobacion de permisos dados
    private static final int REQUEST_ACCESS_COARSE_LOCATION = 222;
    //Comprobacion si bluetooth esta activado
    private static final int REQUEST_ENABLE_BT = 1;
    private static final double GRAVITIY = 9.81;


    // Accelerometer ranges
    private static final int ACC_RANGE_2G = 0;
    private static final int ACC_RANGE_4G = 1;
    private static final int ACC_RANGE_8G = 2;
    private static final int ACC_RANGE_16G = 3;

    private static final int ACC_RANGE_ACTUAL_CONF = 0x03;

    private BluetoothAdapter mBluetoothAdapter;
    private BluetoothGatt mBluetoothGatt;

    //Gestion de la lista de dispositivos
    private ListView listView;
    private ArrayAdapter<String> adapter;
    private ArrayList<String> list;
    private HashMap<String, BluetoothDevice> listaDevices;

    //UUID Pulsera CCC
    private static final UUID UUID_CCC = UUID.fromString("00002902-0000-1000-8000-00805f9b34fb");

    //UUID Pulsera Humedad
    private static final UUID UUID_HUMIDITY_SERVICE =     UUID.fromString("f000aa20-0451-4000-b000-000000000000");
    private static final UUID UUID_HUMIDITY_DATA =        UUID.fromString("f000aa21-0451-4000-b000-000000000000");
    private static final UUID UUID_HUMIDITY_CONF =        UUID.fromString("f000aa22-0451-4000-b000-000000000000");

    //UUID Pulsera Acelerometro
    private static final UUID UUID_ACELEROMETRO_SERVICE = UUID.fromString("f000aa80-0451-4000-b000-000000000000");
    private static final UUID UUID_ACELEROMETRO_DATA =    UUID.fromString("f000aa81-0451-4000-b000-000000000000");
    private static final UUID UUID_ACELEROMETRO_CONF =    UUID.fromString("f000aa82-0451-4000-b000-000000000000");

    //ENCENDER/APAGAR SENSOR HUMEDAD
    private static final byte[] ENCENDER_SENSOR_HUMEDAD = {0x01};
    private static final byte[] APAGAR_SENSOR_HUMEDAD = {0x00};

    //ENCENDER/APAGAR SENSOR ACELERÓMETRO
    //0x3f para encender con rango 2g
    //0x
    private static final byte[] ENCENDER_SENSOR_ACELEROMETRO = {0x3f, 0x02}; //array de bytes, bytes[0] es el byte menos significativo
    private static final byte[] APAGAR_SENSOR_ACELEROMETRO = {0x00, 0x00};

    //COLAS DE ESCRITURA PARA SENSOR
    private static final Queue<Object> sWriteQueue = new ConcurrentLinkedQueue<Object>();
    private static boolean sIsWritting = false;

    //Elementos (servicios/caracteristicas) de la pulsera
    private List<BluetoothGattService> mBleServices;

    private final BroadcastReceiver mGattUpdateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            final int status = intent.getIntExtra("EXTRA_STATUS", BluetoothGatt.GATT_SUCCESS);
        }
    };

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
                        listaDevices.put(device.getAddress(), device);
                        adapter.notifyDataSetChanged();
                    }
                });
            }
        }
    };

    private BluetoothGattCallback mBtleCallback = new BluetoothGattCallback() {
        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            super.onConnectionStateChange(gatt, status, newState);

            Log.d("BLUETOOTH", "Estado de conexión bluetooth: " + (newState == BluetoothProfile.STATE_CONNECTED ? "Connected" : "Disconnected"));

            if(newState == BluetoothProfile.STATE_CONNECTED){
                //setState(State.CONNECTED);
                mBluetoothGatt.discoverServices();
            }
            else{
                //setState(State.IDDLE);
            }
        }

        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            super.onServicesDiscovered(gatt, status);

            if(status == BluetoothGatt.GATT_SUCCESS){
                Log.d("BLUETOOTH", "Servicios descubiertos :)");
                //obtenerCaracteristicasDescriptoresHumedad(mBluetoothGatt);
                obtenerCaracteristicasDescriptoresAcelerometro(mBluetoothGatt);
            }
        }

        @Override
        public void onCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            super.onCharacteristicWrite(gatt, characteristic, status);
            Log.d("BLUETOOTH", "onCharacteristicWrite: " + status);
            sIsWritting = false;
            nextWrite(); //Si la cola de escriuras y no se está escribiendo -> escribeme
        }

        @Override
        public void onDescriptorWrite(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status) {
            super.onDescriptorWrite(gatt, descriptor, status);
            Log.d("BLUETOOTH", "onDescriptorWrite: " + status);
            sIsWritting = false;
            nextWrite(); //Si la cola de escriuras y no se está escribiendo -> escribeme
        }

        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
            super.onCharacteristicChanged(gatt, characteristic);
            Log.d("BLUETOOTH", "onCharacteristicChanged");

            //SI SON DATOS DE TIPO HUMEDAD
            /*if(characteristic.getUuid().equals(UUID_HUMIDITY_DATA)){
                int t = shortUnsignedAtOffset(characteristic, 0);
                int h = shortUnsignedAtOffset(characteristic, 2);
                t = t - (t % 4);
                h = h - (h % 4);

                float humidity = (-6f) + 125f * (h / 65535f);
                float temperature = -46.85f +
                        175.72f/65536f * (float)t;
                Log.d("HUMEDAD-TEMPERATURA", "Value: " + humidity + " : " + temperature);
            }*/

            if(characteristic.getUuid().equals(UUID_ACELEROMETRO_DATA)){
                /*Integer x = characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_SINT8, 0);
                Integer y = characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_SINT8, 1);
                Integer z = characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_SINT8, 2) * -1;
                characteristic.

                double scaledX = x / 64.0;
                double scaledY = x / 64.0;
                double scaledZ = x / 64.0;*/

                byte[] valores = characteristic.getValue();

                double acc_x = (valores[7] << 8) +  valores[6];
                double acc_y = (valores[9] << 8) +  valores[8];
                double acc_z = (valores[11] << 8) +  valores[10];


                //TODO REVISAR DIRECCIONES (-1) Z(-1) X(-1) Y(1)
                double acc_scaledX = sensorMpu9250AccConvert(acc_x) * GRAVITIY;
                double acc_scaledY = sensorMpu9250AccConvert(acc_y) * GRAVITIY;
                double acc_scaledZ = sensorMpu9250AccConvert(acc_z) * GRAVITIY;

                Log.d("ACELEROMETRO", "Value: " + acc_scaledX + " : " + acc_scaledY + " : " + acc_scaledZ);

                double gyro_x = (valores[1] << 8) +  valores[0];
                double gyro_y = (valores[3] << 8) +  valores[2];
                double gyro_z = (valores[5] << 8) +  valores[4];


                //TODO CONVERTIR A RADIANES/SEGUNDO
                double gyro_scaledX = sensorMpu9250GyroConvert(gyro_x);
                double gyro_scaledY = sensorMpu9250AccConvert(gyro_y);
                double gyro_scaledZ = sensorMpu9250AccConvert(gyro_z);

                Log.d("GIROSCOPIO  ", "Value: " + gyro_scaledX + " : " + gyro_scaledY + " : " + gyro_scaledZ);

            }
        }
    };

    //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

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
            this.list.clear();
            this.listaDevices.clear();
            this.adapter.notifyDataSetChanged();
            this.mBluetoothAdapter.startDiscovery();
        }

        return super.onOptionsItemSelected(item);
    }

    private void inicializarListaGraficaYModelo(){

        //INICIALIZAMOS ELEMENTO GRAFICO/////////////////////////////////////////////////////////////////
        this.listView = (ListView) findViewById(R.id.list);
        this.listaDevices = new HashMap<String, BluetoothDevice>();
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

                conectarseDispositivo(itemValue);

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

    public void conectarseDispositivo(String mac){
        BluetoothDevice device = this.listaDevices.get(mac);
        if(device != null){
            this.mBluetoothGatt = device.connectGatt(this, true, this.mBtleCallback);
        }
    }

    private void obtenerCaracteristicasDescriptoresHumedad(BluetoothGatt gatt){
        BluetoothGattService humedadService = gatt.getService(UUID_HUMIDITY_SERVICE);
        if(humedadService != null){
            BluetoothGattCharacteristic humedadCharacteristic = humedadService.getCharacteristic(UUID_HUMIDITY_DATA);
            BluetoothGattCharacteristic humedadConf = humedadService.getCharacteristic(UUID_HUMIDITY_CONF);

            if(humedadCharacteristic != null && humedadConf != null){
                BluetoothGattDescriptor config = humedadCharacteristic.getDescriptor(UUID_CCC);

                if(config != null){
                    mBluetoothGatt.setCharacteristicNotification(humedadCharacteristic, true);

                    humedadConf.setValue(ENCENDER_SENSOR_HUMEDAD);
                    write(humedadConf);

                    config.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
                    write(config);
                }
            }
        }
    }

    private void obtenerCaracteristicasDescriptoresAcelerometro(BluetoothGatt gatt){
        BluetoothGattService acelerometroService = gatt.getService(UUID_ACELEROMETRO_SERVICE);
        if(acelerometroService != null){
            BluetoothGattCharacteristic acelerometroCharacteristic = acelerometroService.getCharacteristic(UUID_ACELEROMETRO_DATA);
            BluetoothGattCharacteristic acelerometroConf = acelerometroService.getCharacteristic(UUID_ACELEROMETRO_CONF);

            if(acelerometroCharacteristic != null && acelerometroConf != null){
                BluetoothGattDescriptor config = acelerometroCharacteristic.getDescriptor(UUID_CCC);

                if(config != null){
                    mBluetoothGatt.setCharacteristicNotification(acelerometroCharacteristic, true);

                    acelerometroConf.setValue(ENCENDER_SENSOR_ACELEROMETRO);

                    write(acelerometroConf);

                    config.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
                    write(config);
                }
            }
        }
    }

    //Si no hay nadie escrbiendo y la lista no está vacia, que escriba el siguiente que tiene de escribir
    private synchronized void nextWrite(){
        if(!sIsWritting && !sWriteQueue.isEmpty()){
            doWrite(sWriteQueue.poll()); //escribirá el primero de la lista
        }
    }

    //Realiza el proceso de escritura diferenciando entre descriptor/característica
    private synchronized void doWrite(Object o){
        if(o instanceof BluetoothGattCharacteristic){
            sIsWritting = true;
            mBluetoothGatt.writeCharacteristic((BluetoothGattCharacteristic)o);
        }
        else if(o instanceof BluetoothGattDescriptor){
            sIsWritting = true;
            mBluetoothGatt.writeDescriptor((BluetoothGattDescriptor)o);
        }
        else{
            nextWrite();
        }
    }

    private synchronized void write(Object o){
        if(sWriteQueue.isEmpty() && !sIsWritting){
            doWrite(o);
        }else{
            sWriteQueue.add(o);
        }
    }

    private static Integer shortUnsignedAtOffset(BluetoothGattCharacteristic characteristic, int offset) {

        Integer lowerByte = characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_UINT8, offset);
        Integer upperByte = characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_UINT8, offset + 1);

        return (upperByte << 8) + lowerByte;
    }

    private double sensorMpu9250GyroConvert(double data){
        //-- calculate rotation, unit deg/s, range -250, +250
        return (data * 1.0) / (65536 / 500);
    }




    private double sensorMpu9250AccConvert(double rawData) {
        double v = -1;

        int accRange = ENCENDER_SENSOR_ACELEROMETRO[1] & ACC_RANGE_ACTUAL_CONF;
        switch (accRange)
        {
            case ACC_RANGE_2G:
                //-- calculate acceleration, unit G, range -2, +2
                v = (rawData * 1.0) / (32768/2);
                break;

            case ACC_RANGE_4G:
                //-- calculate acceleration, unit G, range -4, +4
                v = (rawData * 1.0) / (32768/4);
                break;

            case ACC_RANGE_8G:
                //-- calculate acceleration, unit G, range -8, +8
                v = (rawData * 1.0) / (32768/8);
                break;

            case ACC_RANGE_16G:
                //-- calculate acceleration, unit G, range -16, +16
                v = (rawData * 1.0) / (32768/16);
                break;
        }

        return v;
    }

}

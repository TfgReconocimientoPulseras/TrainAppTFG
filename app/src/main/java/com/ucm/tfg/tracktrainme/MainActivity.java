package com.ucm.tfg.tracktrainme;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.PorterDuff;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.ucm.tfg.tracktrainme.Activities.AsistenteRecogidaDatos.RecogerDatosBienvenida;
import com.ucm.tfg.tracktrainme.Activities.Bluetooth.ListarYConectarBluetooth;
import com.ucm.tfg.tracktrainme.Activities.ListarActividades;
import com.ucm.tfg.tracktrainme.DataBase.ActivityDataTransfer;
import com.ucm.tfg.tracktrainme.DataBase.DatabaseAdapter;
import com.ucm.tfg.tracktrainme.Fragments.HistorialFragment;
import com.ucm.tfg.tracktrainme.Fragments.ReconocerActividadFragment;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;


public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {
    private TabLayout tabLayout;
    private ViewPager viewPager;

    private String modo;

    private int[] icons = {
            R.drawable.ic_directions_run_black_24dp,
            R.drawable.ic_history_black_24dp,
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        comprobarExistenciaSensores();
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        //TODO crear constantes para "sensor" o modo "PULSERA" o "MOVIL"
        //TODO modificar esto. DESFASADO
        //MODO PULSERA -> CC2650
        //MODO MOVIL   -> sensor del telefono
        Intent i = getIntent();
        this.modo = i.getStringExtra("sensor");
        if(this.modo == null){
            this.modo = "MOVIL";
        }

        //TODO adaptar esto, pues this.modo va a dejar de existir
        if(i.getStringExtra("sensor") != null)
            if(i.getStringExtra("sensor").equals("PULSERA")){
                Toast.makeText(getApplicationContext(), "La conexión por Bluetooth ha finalizado correctamente. Conectado!", Toast.LENGTH_LONG).show();
            }


        viewPager = (ViewPager) findViewById(R.id.viewpager);
        initViewPager(viewPager);

        tabLayout = (TabLayout) findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(viewPager);
        tabLayout.setSelectedTabIndicatorColor(getResources().getColor(R.color.colorWhite));
        initTabsWithIcons();

        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                tab.getIcon().setColorFilter(getResources().getColor(R.color.colorWhite), PorterDuff.Mode.SRC_IN);
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
                tab.getIcon().setColorFilter(getResources().getColor(R.color.colorWhiteDark), PorterDuff.Mode.SRC_IN);
            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });


        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        if(!comprobarSiExisteDirImagenes())
            crearImagenesPorDefecto();

    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        /*if (id == R.id.action_settings) {
            return true;
        }*/
        if(id == R.id.action_refresh){
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_register_activity) {
            Intent recogida = new Intent(this, RecogerDatosBienvenida.class);
            startActivity(recogida);
        } else if (id == R.id.synchBluetooth) {
            Intent intent = new Intent(this, ListarYConectarBluetooth.class);
            startActivity(intent);
        }
        else if (id == R.id.listActivity) {
            Intent intent = new Intent(this, ListarActividades.class);
            startActivity(intent);
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }
    private void initViewPager(ViewPager viewPager) {
        ViewPagerAdapter adapter = new ViewPagerAdapter(getSupportFragmentManager());
        adapter.addFragment(new ReconocerActividadFragment(), "ACTIVIDADES");
        adapter.addFragment(new HistorialFragment(), "HISTORIAL");

        viewPager.setAdapter(adapter);
    }

    private void initTabsWithIcons(){
        tabLayout.getTabAt(0).setIcon(icons[0]);
        tabLayout.getTabAt(0).getIcon().setColorFilter(getResources().getColor(R.color.colorWhite), PorterDuff.Mode.SRC_IN);

        tabLayout.getTabAt(1).setIcon(icons[1]);
        tabLayout.getTabAt(1).getIcon().setColorFilter(getResources().getColor(R.color.colorWhiteDark), PorterDuff.Mode.SRC_IN);

    }

    class ViewPagerAdapter extends FragmentPagerAdapter {
        private final List<Fragment> mFragmentList = new ArrayList<>();
        private final List<String> mFragmentTitleList = new ArrayList<>();

        public ViewPagerAdapter(FragmentManager manager) {
            super(manager);
        }

        @Override
        public Fragment getItem(int position) {
            return mFragmentList.get(position);
        }

        @Override
        public int getCount() {
            return mFragmentList.size();
        }

        public void addFragment(Fragment fragment, String title) {
            mFragmentList.add(fragment);
            mFragmentTitleList.add(title);
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return null;
        }
    }

    private void comprobarExistenciaSensores(){

        SensorManager sm = (SensorManager) getSystemService(SENSOR_SERVICE);
        Sensor acc = sm.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        Sensor gyr = sm.getDefaultSensor(Sensor.TYPE_GYROSCOPE);

        if(acc == null || gyr == null){
            Intent salir = new Intent (MainActivity.this, NoSensores.class);
            startActivity(salir);
        }
    }

    private void guardarImagenes(String nombre, Bitmap bitmap, boolean isActivity){
        String dataDir = getApplicationContext().getApplicationInfo().dataDir;
        File f = null;
        File directory = new File(dataDir + "/images/");
        directory.mkdirs();

        if(isActivity)
            f = new File(directory, UUID.randomUUID().toString() + ".png");
        else
            f = new File(directory, nombre + ".png");

        OutputStream os;

        try {
            os = new FileOutputStream(f);
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, os);
            os.flush();
            os.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }


        if(isActivity){
            ActivityDataTransfer activity = new ActivityDataTransfer(nombre, f.getAbsolutePath());
            DatabaseAdapter db = new DatabaseAdapter(this);
            db.open();

            long id = db.insertActivity(activity);

            db.close();
        }

    }

    private void crearImagenesPorDefecto(){
        Bitmap bitMap = BitmapFactory.decodeResource(getResources(), R.drawable.ico_act_1);
        guardarImagenes("Caminar", bitMap, true);

        bitMap = BitmapFactory.decodeResource(getResources(), R.drawable.ico_act_8);
        guardarImagenes("Aplaudir", bitMap, true);

        bitMap = BitmapFactory.decodeResource(getResources(), R.drawable.ico_act_3);
        guardarImagenes("Quieto", bitMap, true);

        bitMap = BitmapFactory.decodeResource(getResources(), R.drawable.ico_act_2);
        guardarImagenes("Barrer", bitMap, true);

    }

    private boolean comprobarSiExisteDirImagenes(){
        String dataDir = getApplicationContext().getApplicationInfo().dataDir;
        File directory = new File(dataDir + "/images/");

        return directory.exists();
    }

    public String getModo(){
        return this.modo;
    }
}

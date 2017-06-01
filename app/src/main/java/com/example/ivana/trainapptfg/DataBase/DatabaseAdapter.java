package com.example.ivana.trainapptfg.DataBase;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by Ivan on 14/05/2017.
 */

public class DatabaseAdapter {

    //TODO QUIZÁS SERÍA ÓPTIMO DIVIDIR ESTA CLASE EN TRANSFER Y DAO

    //TAG
    private static final String TAG = "DataBaseAdapter";

    //VERSION BD
    private static final int DATABASE_VERSION = 26;

    //Nombre de la BD
    private static final String DB_NAME = "AppDB";

    //Nombres de tablas
    private static final String TABLA_ACTIVIDADES = "actividades";
    private static final String TABLA_HISTORIAL = "historial";

    //Columnas comunes
    private static final String KEY_ID = "id";

    //Columnas TABLA_ACTIVIDADES
    private static final String KEY_FECHACREACION = "fechaCreacion";
    private static final String KEY_ACTIVIDAD = "actividad";


    //Columnas TABLA_HISTORIAL
    private static final String KEY_ACTIVIDAD_FK = "actividad";
    private static final String KEY_FECHAINI = "fechaIni";
    private static final String KEY_FECHAFIN = "fechaFin";
    private static final String KEY_DURACION = "duracion";


    private static final String CREAR_TABLA_ACTIVIDADES = "CREATE TABLE "
            + TABLA_ACTIVIDADES + "(" + KEY_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
            + KEY_ACTIVIDAD + " TEXT NOT NULL UNIQUE,"
            + KEY_FECHACREACION + " DATETIME DEFAULT (datetime('now', 'localtime')))";

    private static final String BORRAR_TABLA_ACTIVIDADES = "DROP TABLE IF EXISTS " + TABLA_ACTIVIDADES;

    private static final String CREAR_TABLA_HISTORIAL = "CREATE TABLE " + TABLA_HISTORIAL
            + "(" + KEY_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
            + KEY_ACTIVIDAD_FK + " INTEGER NOT NULL,"
            + KEY_FECHAINI + " DATETIME NOT NULL,"
            + KEY_FECHAFIN + " DATETIME,"
            + " FOREIGN KEY ("+KEY_ACTIVIDAD_FK+") REFERENCES "+TABLA_ACTIVIDADES+"("+KEY_ID+") ON DELETE CASCADE"
            + ")";

    private static final String BORRAR_TABLA_HISTORIAL = "DROP TABLE IF EXISTS " + TABLA_HISTORIAL;

    private final Context context;
    private DataBaseHelper dbHelper;
    private SQLiteDatabase db;

    public DatabaseAdapter(Context ctx){
        this.context = ctx;
        this.dbHelper = new DataBaseHelper(context);
    }

    private static class DataBaseHelper extends SQLiteOpenHelper {

        public DataBaseHelper (Context ctx){
            super(ctx, DB_NAME, null, DATABASE_VERSION);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            db.execSQL(CREAR_TABLA_ACTIVIDADES);
            db.execSQL(CREAR_TABLA_HISTORIAL);
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            Log.w(TAG, "Actualizando BD de la version " + oldVersion + " a " + newVersion + " lo cual destruirá todos los datos");
            db.execSQL(BORRAR_TABLA_ACTIVIDADES);
            db.execSQL(BORRAR_TABLA_HISTORIAL);
            onCreate(db);
        }

        @Override
        public void onConfigure(SQLiteDatabase db){
            db.setForeignKeyConstraintsEnabled(true);
        }
    }

    public DatabaseAdapter open(){
        this.db = this.dbHelper.getWritableDatabase();

        return this;
    }

    public void close(){
        this.dbHelper.close();
    }

    //TODO CREAR CLASE ACTIVIDAD PARA REPRESENTAR EL MODELO (IGUAL NO ES NECESARIO)
    public long insertActivity(String activity){
        ContentValues values = new ContentValues();
        values.put(KEY_ACTIVIDAD, activity);

        long id = db.insert(TABLA_ACTIVIDADES, null, values);

        return id;
    }

    public ActivityDataTransfer getActivityDataTransfer(long id){
        ActivityDataTransfer activityDataTransfer = null;
        String query = "SELECT * FROM " + TABLA_ACTIVIDADES + " WHERE " + KEY_ID + "=" + id;
        Cursor c = db.rawQuery(query, null);

        while(c.getCount() == 1 && c.moveToNext()){
            String actividad = c.getString(c.getColumnIndex(KEY_ACTIVIDAD));
            Date fCreacion = getDateFromSqlite(c.getString(c.getColumnIndex(KEY_FECHACREACION)));
            activityDataTransfer = new ActivityDataTransfer(id, actividad, fCreacion);
        }

        return activityDataTransfer;

    }


    public long insertarNuevoRegistroAlHistorial(HistoryDataTransfer historyDataTransfer){
        String s = "";
        String s1 = "";

        if(historyDataTransfer.getfIni() == null){
            s = getDateTimeToSqlite(0);
        }
        else{
            s = getDateTimeToSqlite(historyDataTransfer.getfIni().getTime());
        }

        if(historyDataTransfer.getfFin() == null){
            s1 = getDateTimeToSqlite(0);
        }
        else{
            s1 = getDateTimeToSqlite(historyDataTransfer.getfFin().getTime());
        }

        ContentValues values = new ContentValues();
        values.put(KEY_ACTIVIDAD_FK, historyDataTransfer.getActividad());
        values.put(KEY_FECHAINI, s);
        values.put(KEY_FECHAFIN, s1);


        long id = db.insert(TABLA_HISTORIAL, null, values);
        historyDataTransfer.setId(id);

        return id;
    }

    public boolean deleteActivty(long rowId){
        long ret = db.delete(TABLA_ACTIVIDADES, KEY_ID + "=" + rowId, null);
        return ret > 0;
    }

    public List<HistoryDataTransfer> dameActividadesFecha(String fecha){
        //SELECT * FROM historial WHERE date(fechaIni)=date('2017-05-14')
        List<HistoryDataTransfer> retList = new ArrayList<>();
        String query = "SELECT * FROM " + TABLA_HISTORIAL  + " JOIN " + TABLA_ACTIVIDADES + " ON " + TABLA_HISTORIAL  + "." + KEY_ACTIVIDAD_FK + "=" + TABLA_ACTIVIDADES + "." + KEY_ID + " WHERE date(" + KEY_FECHAINI + ")=date(?)";
        Cursor c = db.rawQuery(query, new String[]{fecha});

        while(c.moveToNext()){
            int id = c.getInt(c.getColumnIndex(KEY_ID));
            int actividad = c.getInt(c.getColumnIndex(KEY_ACTIVIDAD_FK));
            String actividadString = c.getString(c.getColumnIndex(KEY_ACTIVIDAD));
            Date fIni = getDateFromSqlite(c.getString(c.getColumnIndex(KEY_FECHAINI)));
            Date fFin = getDateFromSqlite(c.getString(c.getColumnIndex(KEY_FECHAFIN)));

            HistoryDataTransfer historyDataTransfer = new HistoryDataTransfer(id, actividad, fIni, fFin, actividadString);


            retList.add(historyDataTransfer);
        }

        return retList;
    }


    public List<ActivityDataTransfer> listarActividadesSistema(){
        List<ActivityDataTransfer> retList = new ArrayList<>();

        String query = "SELECT * FROM " + TABLA_ACTIVIDADES;
        Cursor c = db.rawQuery(query, null);

        while(c.moveToNext()){
            int id = c.getInt(c.getColumnIndex(KEY_ID));
            String actividad = c.getString(c.getColumnIndex(KEY_ACTIVIDAD));
            Date fCreacion = getDateFromSqlite(c.getString(c.getColumnIndex(KEY_FECHACREACION)));

            ActivityDataTransfer activityDataTransfer = new ActivityDataTransfer(id, actividad, fCreacion);
            retList.add(activityDataTransfer);
        }

        return retList;
    }

    private static String getDateTimeToSqlite(long timeInMillis){
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        return dateFormat.format(new java.util.Date(timeInMillis));
    }

    private static Date getDateFromSqlite(String date){
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date d = new Date();
        try {
            d = dateFormat.parse(date);
        } catch (ParseException e) {
            e.printStackTrace();
        }

        return d;
    }


    //C:\Users\Ivan\AppData\Local\Android\sdk\platform-tools
    //adb shell
    //run-as com.example.ivana.trainapptfg
    //chmod 666 db
    //adb pull /sdcard/MyFiles/AppDB C:\Users\Ivan\Desktop
    //exit
    //exit
    //ABRIR DB CON BROWSER FOR SQLITE
    //

}

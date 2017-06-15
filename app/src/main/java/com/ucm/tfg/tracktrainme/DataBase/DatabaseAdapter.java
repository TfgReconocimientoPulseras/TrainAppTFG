package com.ucm.tfg.tracktrainme.DataBase;

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

public class DatabaseAdapter {

    //TODO QUIZÁS SERÍA ÓPTIMO DIVIDIR ESTA CLASE EN TRANSFER Y DAO

    //TAG
    private static final String TAG = "DataBaseAdapter";

    //VERSION BD
    private static final int DATABASE_VERSION = 1;

    //Nombre de la BD
    private static final String DB_NAME = "AppDB";

    //Nombres de tablas
    private static final String TABLA_ACTIVIDADES = "actividades";
    private static final String TABLA_HISTORIAL = "historial";
    private static final String TABLA_ARBOLES = "arboles";

    //Columnas comunes
    private static final String KEY_ID = "id";

    //Columnas TABLA_ACTIVIDADES
    private static final String KEY_FECHACREACION = "fechaCreacion";
    private static final String KEY_ACTIVIDAD = "actividad";
    private static final String KEY_URL_IMAGE = "urlImage";


    //Columnas TABLA_HISTORIAL
    private static final String KEY_ACTIVIDAD_FK = "actividad";
    private static final String KEY_FECHAINI = "fechaIni";
    private static final String KEY_FECHAFIN = "fechaFin";
    private static final String KEY_DURACION = "duracion";

    //Columnas TABLA _ARBOLES
    private static final String KEY_TREE_TEXT = "arbol";


    private static final String CREAR_TABLA_ACTIVIDADES = "CREATE TABLE "
            + TABLA_ACTIVIDADES + "(" + KEY_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
            + KEY_ACTIVIDAD + " TEXT NOT NULL UNIQUE,"
            + KEY_FECHACREACION + " DATETIME DEFAULT (datetime('now', 'localtime')),"
            + KEY_URL_IMAGE + " TEXT NOT NULL " + ")";

    private static final String BORRAR_TABLA_ACTIVIDADES = "DROP TABLE IF EXISTS " + TABLA_ACTIVIDADES;

    private static final String CREAR_TABLA_HISTORIAL = "CREATE TABLE " + TABLA_HISTORIAL
            + "(" + KEY_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
            + KEY_ACTIVIDAD_FK + " INTEGER NOT NULL,"
            + KEY_FECHAINI + " DATETIME NOT NULL,"
            + KEY_FECHAFIN + " DATETIME,"
            + " FOREIGN KEY ("+KEY_ACTIVIDAD_FK+") REFERENCES "+TABLA_ACTIVIDADES+"("+KEY_ID+") ON DELETE CASCADE"
            + ")";

    private static final String BORRAR_TABLA_HISTORIAL = "DROP TABLE IF EXISTS " + TABLA_HISTORIAL;

    private static final String CREAR_TABLA_ARBOLES = "CREATE TABLE " + TABLA_ARBOLES
            + "(" + KEY_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
            + KEY_TREE_TEXT + " TEXT NOT NULL"
            + ")";

    private static final String BORRAR_TABLA_ARBOLES = "DROP TABLE IF EXISTS " + TABLA_ARBOLES;

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
            db.execSQL(CREAR_TABLA_ARBOLES);
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            Log.w(TAG, "Actualizando BD de la version " + oldVersion + " a " + newVersion + " lo cual destruirá todos los datos");
            db.execSQL(BORRAR_TABLA_ACTIVIDADES);
            db.execSQL(BORRAR_TABLA_HISTORIAL);
            db.execSQL(BORRAR_TABLA_ARBOLES);
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

    public long insertActivity(ActivityDataTransfer act){
        ContentValues values = new ContentValues();
        values.put(KEY_ACTIVIDAD, act.getName());
        values.put(KEY_URL_IMAGE, act.getUrlImage());

        long id = db.insert(TABLA_ACTIVIDADES, null, values);

        act.setId(id);

        return id;
    }

    public ActivityDataTransfer getActivityDataTransfer(long id){
        ActivityDataTransfer activityDataTransfer = null;
        String query = "SELECT * FROM " + TABLA_ACTIVIDADES + " WHERE " + KEY_ID + "=" + id;
        Cursor c = db.rawQuery(query, null);

        while(c.getCount() == 1 && c.moveToNext()){
            String actividad = c.getString(c.getColumnIndex(KEY_ACTIVIDAD));
            String url = c.getString(c.getColumnIndex(KEY_URL_IMAGE));
            Date fCreacion = getDateFromSqlite(c.getString(c.getColumnIndex(KEY_FECHACREACION)));

            activityDataTransfer = new ActivityDataTransfer(id, actividad, fCreacion, url);
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
        //TODO CONTROLAR QUE NO HA HABIDO ERROR (ID>0)
        historyDataTransfer.setId(id);

        return id;
    }

    public long insertarNuevoArbol(TreeDataTransfer treeDataTransfer){
        ContentValues values = new ContentValues();
        values.put(KEY_TREE_TEXT, treeDataTransfer.getTree());

        long id = db.insert(TABLA_ARBOLES, null, values);
        if(id > 0){
            treeDataTransfer.setId(id);
        }

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
            String url = c.getString(c.getColumnIndex(KEY_URL_IMAGE));

            ActivityDataTransfer activityDataTransfer = new ActivityDataTransfer(id, actividad, fCreacion, url);
            retList.add(activityDataTransfer);
        }

        return retList;
    }

    public boolean existeActividad(String actividad){
        String query = "SELECT * FROM " + TABLA_ACTIVIDADES + " WHERE " + KEY_ACTIVIDAD + "=" + "'" + actividad + "'";
        Cursor c = db.rawQuery(query, null);

        int i = c.getCount();
        return (c.getCount() > 0);
    }

    private static String getDateTimeToSqlite(long timeInMillis){
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        return dateFormat.format(new java.util.Date(timeInMillis));
    }

    public TreeDataTransfer getLastTree(){
        TreeDataTransfer treeDataTransfer = null;
        String query = "SELECT * FROM " + TABLA_ARBOLES  + " WHERE " + KEY_ID + " = (SELECT MAX(" + KEY_ID + ") FROM " + TABLA_ARBOLES + ")";
        Cursor c = db.rawQuery(query, null);

        if(c.getCount() == 0){
            String arbolGeneral = getGeneralTree();
            treeDataTransfer = new TreeDataTransfer(arbolGeneral);
            insertarNuevoArbol(treeDataTransfer);
        }
        while(c.getCount() == 1 && c.moveToNext()){
            long id = c.getLong(c.getColumnIndex(KEY_ID));
            String tree_text = c.getString(c.getColumnIndex(KEY_TREE_TEXT));
            treeDataTransfer = new TreeDataTransfer(id, tree_text);
        }


        return treeDataTransfer;
    }

    public int getNextIndexValueActivityTable(){
        int i = -1;

        String query = "SELECT seq FROM sqlite_sequence WHERE name='" + TABLA_ACTIVIDADES + "'";
        Cursor c = db.rawQuery(query, null);


        while(c.moveToNext()){
            i = c.getInt(0);
        }

        if(i != -1){
            i = i + 1; //next
        }

        return i;
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

    private String getGeneralTree(){
        return "public int miMetodo(HashMap hashMap){\n" +
                "if(hashMap.get(\"gyro_gamma_max\") <= 0.209456175566)\n" +
                "if(hashMap.get(\"accel_y_std\") <= 0.242084830999)\n" +
                "if(hashMap.get(\"gyro_alpha_min\") <= -0.308585643768)\n" +
                "if(hashMap.get(\"accel_x_max\") <= 9.31309700012)\n" +
                "if(hashMap.get(\"gyro_beta_std\") <= 0.167273283005)\n" +
                "return 3;\n" +
                "else //if(gyro_beta_std > 0.167273283005)\n" +
                "if(hashMap.get(\"gyro_alpha_min\") <= -0.643096804619)\n" +
                "return 3;\n" +
                "else //if(gyro_alpha_min > -0.643096804619)\n" +
                "return 1;\n" +
                "else //if(accel_x_max > 9.31309700012)\n" +
                "if(hashMap.get(\"gyro_gamma_med\") <= -0.250750094652)\n" +
                "return 4;\n" +
                "else //if(gyro_gamma_med > -0.250750094652)\n" +
                "return 2;\n" +
                "else //if(gyro_alpha_min > -0.308585643768)\n" +
                "if(hashMap.get(\"gyro_beta_min\") <= -1.05088806152)\n" +
                "return 1;\n" +
                "else //if(gyro_beta_min > -1.05088806152)\n" +
                "return 3;\n" +
                "else //if(accel_y_std > 0.242084830999)\n" +
                "if(hashMap.get(\"accel_y_max\") <= 2.62441110611)\n" +
                "if(hashMap.get(\"accel_x_med\") <= 2.50304412842)\n" +
                "if(hashMap.get(\"gyro_gamma_min\") <= -0.18482208252)\n" +
                "return 1;\n" +
                "else //if(gyro_gamma_min > -0.18482208252)\n" +
                "if(hashMap.get(\"accel_y_med\") <= -3.60224342346)\n" +
                "return 4;\n" +
                "else //if(accel_y_med > -3.60224342346)\n" +
                "return 1;\n" +
                "else //if(accel_x_med > 2.50304412842)\n" +
                "if(hashMap.get(\"accel_y_std\") <= 0.506831645966)\n" +
                "if(hashMap.get(\"gyro_gamma_med\") <= -0.263118743896)\n" +
                "return 1;\n" +
                "else //if(gyro_gamma_med > -0.263118743896)\n" +
                "return 3;\n" +
                "else //if(accel_y_std > 0.506831645966)\n" +
                "if(hashMap.get(\"gyro_beta_max\") <= 1.56587266922)\n" +
                "return 4;\n" +
                "else //if(gyro_beta_max > 1.56587266922)\n" +
                "return 1;\n" +
                "else //if(accel_y_max > 2.62441110611)\n" +
                "return 2;\n" +
                "else //if(gyro_gamma_max > 0.209456175566)\n" +
                "if(hashMap.get(\"accel_y_avg\") <= -8.48324775696)\n" +
                "if(hashMap.get(\"accel_x_max\") <= 8.04502105713)\n" +
                "if(hashMap.get(\"accel_y_min\") <= -10.8130435944)\n" +
                "if(hashMap.get(\"accel_x_med\") <= 4.60125732422)\n" +
                "if(hashMap.get(\"gyro_gamma_min\") <= -0.466049194336)\n" +
                "return 1;\n" +
                "else //if(gyro_gamma_min > -0.466049194336)\n" +
                "return 1;\n" +
                "else //if(accel_x_med > 4.60125732422)\n" +
                "if(hashMap.get(\"gyro_beta_min\") <= -1.39317393303)\n" +
                "return 1;\n" +
                "else //if(gyro_beta_min > -1.39317393303)\n" +
                "return 4;\n" +
                "else //if(accel_y_min > -10.8130435944)\n" +
                "if(hashMap.get(\"gyro_gamma_min\") <= -0.682512402534)\n" +
                "if(hashMap.get(\"accel_z_avg\") <= 3.95836639404)\n" +
                "return 1;\n" +
                "else //if(accel_z_avg > 3.95836639404)\n" +
                "return 4;\n" +
                "else //if(gyro_gamma_min > -0.682512402534)\n" +
                "if(hashMap.get(\"accel_x_max\") <= 3.54990386963)\n" +
                "return 3;\n" +
                "else //if(accel_x_max > 3.54990386963)\n" +
                "return 1;\n" +
                "else //if(accel_x_max > 8.04502105713)\n" +
                "if(hashMap.get(\"accel_x_med\") <= 4.50099945068)\n" +
                "if(hashMap.get(\"yz_cor\") <= 0.531381249428)\n" +
                "if(hashMap.get(\"accel_x_min\") <= 0.725691199303)\n" +
                "return 4;\n" +
                "else //if(accel_x_min > 0.725691199303)\n" +
                "return 1;\n" +
                "else //if(yz_cor > 0.531381249428)\n" +
                "return 4;\n" +
                "else //if(accel_x_med > 4.50099945068)\n" +
                "if(hashMap.get(\"accel_x_max\") <= 8.52625274658)\n" +
                "if(hashMap.get(\"xz_cor\") <= -0.193183273077)\n" +
                "return 4;\n" +
                "else //if(xz_cor > -0.193183273077)\n" +
                "return 1;\n" +
                "else //if(accel_x_max > 8.52625274658)\n" +
                "return 4;\n" +
                "else //if(accel_y_avg > -8.48324775696)\n" +
                "if(hashMap.get(\"accel_y_max\") <= 4.24551773071)\n" +
                "if(hashMap.get(\"accel_x_max\") <= 8.47423171997)\n" +
                "if(hashMap.get(\"gyro_gamma_min\") <= -1.3512878418)\n" +
                "if(hashMap.get(\"gyro_beta_max\") <= 2.33505249023)\n" +
                "return 4;\n" +
                "else //if(gyro_beta_max > 2.33505249023)\n" +
                "return 1;\n" +
                "else //if(gyro_gamma_min > -1.3512878418)\n" +
                "if(hashMap.get(\"accel_x_med\") <= 5.54896402359)\n" +
                "return 1;\n" +
                "else //if(accel_x_med > 5.54896402359)\n" +
                "return 4;\n" +
                "else //if(accel_x_max > 8.47423171997)\n" +
                "if(hashMap.get(\"accel_y_med\") <= 0.981155395508)\n" +
                "if(hashMap.get(\"gyro_gamma_max\") <= 1.2589943409)\n" +
                "return 4;\n" +
                "else //if(gyro_gamma_max > 1.2589943409)\n" +
                "return 4;\n" +
                "else //if(accel_y_med > 0.981155395508)\n" +
                "if(hashMap.get(\"z_fft\") <= 161.493133545)\n" +
                "return 3;\n" +
                "else //if(z_fft > 161.493133545)\n" +
                "return 1;\n" +
                "else //if(accel_y_max > 4.24551773071)\n" +
                "if(hashMap.get(\"accel_y_med\") <= 0.325078427792)\n" +
                "if(hashMap.get(\"xy_cor\") <= -0.523706614971)\n" +
                "return 4;\n" +
                "else //if(xy_cor > -0.523706614971)\n" +
                "return 2;\n" +
                "else //if(accel_y_med > 0.325078427792)\n" +
                "return 2;\n" +
                "\n" +
                "}";
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

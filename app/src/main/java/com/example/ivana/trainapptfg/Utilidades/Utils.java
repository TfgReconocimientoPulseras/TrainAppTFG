package com.example.ivana.trainapptfg.Utilidades;

import android.content.Context;
import android.content.pm.PackageManager;
import android.widget.Toast;

/**
 * Created by Iván on 19/04/2017.
 */

public class Utils {
    /**
     * Esta función devuelve si un número double es un entero
     * @param n double
     * @return true if n is an integer (without decimals)
     */
    public static boolean isInteger(double n){
        return Math.floor(n) == Math.ceil(n) ;
    }

    public static Boolean checkPermissionsResult(Context context, String[] permissions, int[] grantResults){
        if(grantResults[0] == PackageManager.PERMISSION_GRANTED){
            Toast.makeText(context, "Permission granted" + permissions, Toast.LENGTH_LONG).show();
            return true;
        }
        else{
            Toast.makeText(context, "Permission denied", Toast.LENGTH_SHORT).show();
            return false;
        }
    }
}

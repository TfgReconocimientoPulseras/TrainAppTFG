package com.ucm.tfg.tracktrainme.Utilidades;

import android.content.Context;
import android.content.pm.PackageManager;
import android.widget.Toast;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

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

    public static List<File> obtenerArchivosDatosRecursivo(File directorioRaiz){
        File[] files = directorioRaiz.listFiles();
        ArrayList<File> retFiles = new ArrayList<File>();

        for(File file : files){
            if (file.isDirectory()) {
                retFiles.addAll(obtenerArchivosDatosRecursivo(file));
            } else {
                if(file.getName().endsWith(".csv")){
                    retFiles.add(file);
                }
            }
        }

        return retFiles;
    }

    public static File[] convertListToArray(List<File> filesList){
        File[] fileArray = new File[filesList.size()];

        for (int i = 0; i < filesList.size(); i++){
            fileArray[i] = filesList.get(i);
        }

        return fileArray;
    }
}

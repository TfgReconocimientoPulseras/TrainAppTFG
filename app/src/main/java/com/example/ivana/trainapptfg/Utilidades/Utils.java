package com.example.ivana.trainapptfg.Utilidades;

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
}

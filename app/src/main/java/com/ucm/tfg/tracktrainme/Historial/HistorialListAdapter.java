package com.ucm.tfg.tracktrainme.Historial;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.ucm.tfg.tracktrainme.R;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class HistorialListAdapter extends ArrayAdapter {

    private final Activity context;
    private final List<String> nombreActividad;
    private final List<Date> horaInicio;
    private final List<Date> horaFin;

    public HistorialListAdapter(Activity context, List<String> nombreActividad, List<Date> horaInicio, List<Date> horaFin){


        super(context, R.layout.lista_historial, nombreActividad);

        this.context = context;
        this.nombreActividad = nombreActividad;
        this.horaInicio = horaInicio;
        this.horaFin = horaFin;
    }

    //@Override
    public View getView(int position, View view, ViewGroup parent){
        LayoutInflater inflater = context.getLayoutInflater();
        View rowView = inflater.inflate(R.layout.lista_historial, null);

        TextView textNombreActividad = (TextView) rowView.findViewById(R.id.textNombreAct);
        TextView textSiglasActividad = (TextView) rowView.findViewById(R.id.textSiglasAct);
        TextView textHoraIni = (TextView) rowView.findViewById(R.id.textHoraIni);
        TextView textHoraFin = (TextView) rowView.findViewById(R.id.textHoraFin);

        SimpleDateFormat fecha = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

        textNombreActividad.setText(this.nombreActividad.get(position));

        if(nombreActividad.get(position).length() < 2)
            textSiglasActividad.setText((this.nombreActividad.get(position)).toUpperCase());
        else
            textSiglasActividad.setText(((this.nombreActividad.get(position)).substring(0, 2)).toUpperCase());

        textHoraIni.setText((fecha.format(this.horaInicio.get(position))).substring(11));
        textHoraFin.setText((fecha.format(this.horaFin.get(position))).substring(11));


        return rowView;
    }
}

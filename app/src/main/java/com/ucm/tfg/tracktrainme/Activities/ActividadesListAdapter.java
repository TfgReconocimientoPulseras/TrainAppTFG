package com.ucm.tfg.tracktrainme.Activities;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.ucm.tfg.tracktrainme.R;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class ActividadesListAdapter extends BaseAdapter {

    private Activity context;
    private List<String> nombreActividad;
    private List<Date> fechaCreacion;

    public ActividadesListAdapter(Activity context, List<String> nombreActividad, List<Date> fecha){
        this.context = context;
        this.nombreActividad = nombreActividad;
        this.fechaCreacion = fecha;
    }

    @Override
    public int getCount() {
        return nombreActividad.size();
    }

    @Override
    public Object getItem(int position) {
        return nombreActividad.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    public View getView(int position, View view, ViewGroup parent){
        LayoutInflater inflater = context.getLayoutInflater();
        View rowView = inflater.inflate(R.layout.lista_actividades, null);

        TextView textNombreActividad = (TextView) rowView.findViewById(R.id.textNombreActSys);
        TextView textSiglasActividad = (TextView) rowView.findViewById(R.id.textSiglasActSys);
        TextView textHoraFechaCreacion = (TextView) rowView.findViewById(R.id.textFechaCreaActSys);
        TextView textHoraCreacion = (TextView) rowView.findViewById(R.id.textHoraCreaActSys);

        SimpleDateFormat fecha = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

        textNombreActividad.setText(this.nombreActividad.get(position));

        if(nombreActividad.get(position).length() < 2)
            textSiglasActividad.setText((this.nombreActividad.get(position)).toUpperCase());
        else
            textSiglasActividad.setText(((this.nombreActividad.get(position)).substring(0, 2)).toUpperCase());

        textHoraFechaCreacion.setText((fecha.format(this.fechaCreacion.get(position))).substring(8, 10) + "/" + (fecha.format(this.fechaCreacion.get(position))).substring(5, 7) + "/" + (fecha.format(this.fechaCreacion.get(position))).substring(0, 4));
        textHoraCreacion.setText((fecha.format(this.fechaCreacion.get(position))).substring(11));


        return rowView;
    }
}

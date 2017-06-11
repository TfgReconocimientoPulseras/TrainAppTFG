package com.ucm.tfg.tracktrainme.Fragments;


import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CalendarView;

import com.ucm.tfg.tracktrainme.Historial.HistorialDiaConcreto;
import com.ucm.tfg.tracktrainme.R;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;


public class HistorialFragment extends Fragment {

    private CalendarView calendario;


    public HistorialFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_historial, container, false);

        this.calendario = (CalendarView) view.findViewById(R.id.calendario_historial);
        configuracionCalendario();

        return view;
    }

    private void configuracionCalendario(){
        this.calendario.setOnDateChangeListener(new CalendarView.OnDateChangeListener() {
            @Override
            public void onSelectedDayChange(@NonNull CalendarView view, int year, int month, int dayOfMonth) {
                SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");

                Calendar cal = Calendar.getInstance();
                cal.set(Calendar.YEAR, year);
                cal.set(Calendar.MONTH, month);
                cal.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                Date dateRepresentation = cal.getTime();

                String dateString = dateFormat.format(dateRepresentation);
                Intent intent = new Intent(getContext(), HistorialDiaConcreto.class);
                intent.putExtra("dayToShow", dateString);

                startActivity(intent);
            }
        });
    }

}

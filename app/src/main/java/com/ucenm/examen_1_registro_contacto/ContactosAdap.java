package com.ucenm.examen_1_registro_contacto;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

public class ContactosAdap extends BaseAdapter {

    private final Context context;
    private final List<Contactos> datos = new ArrayList<>();
    private int seleccionadoId = -1;

    public ContactosAdap(Context context) {
        this.context = context;
    }

    public void setDatos(List<Contactos> nuevos) {
        datos.clear();
        datos.addAll(nuevos);
        notifyDataSetChanged();
    }

    public void setSeleccionadoId(int id) {
        seleccionadoId = id;
        notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        return datos.size();
    }

    @Override
    public Contactos getItem(int position) {
        return datos.get(position);
    }

    @Override
    public long getItemId(int position) {
        return datos.get(position).getId();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.item_contacto, parent, false);
        }
        Contactos c = getItem(position);
        TextView tv = convertView.findViewById(R.id.tvContacto);
        tv.setText(c.getNombre() + " | " + c.getTelefonoCompleto());

        if (c.getId() == seleccionadoId) {
            convertView.setBackgroundColor(context.getResources().getColor(R.color.seleccion));
        } else {
            convertView.setBackgroundColor(position % 2 == 0 ? Color.WHITE : 0xFFF1F5FB);
        }
        return convertView;
    }
}

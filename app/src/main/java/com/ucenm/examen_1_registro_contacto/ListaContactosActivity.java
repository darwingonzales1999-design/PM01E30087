package com.ucenm.examen_1_registro_contacto;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class ListaContactosActivity extends AppCompatActivity {

    private ContactosDb db;
    private ContactosAdap adapter;
    private EditText etBuscar;
    private final List<Contactos> todos = new ArrayList<>();
    private Contactos seleccionado;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lista);

        db = new ContactosDb(this);
        etBuscar = findViewById(R.id.etBuscar);
        ListView lista = findViewById(R.id.listaContactos);
        Button btnAtras = findViewById(R.id.btnAtras);
        Button btnCompartir = findViewById(R.id.btnCompartir);
        Button btnVerImagen = findViewById(R.id.btnVerImagen);
        Button btnEliminar = findViewById(R.id.btnEliminar);
        Button btnActualizar = findViewById(R.id.btnActualizar);

        adapter = new ContactosAdap(this);
        lista.setAdapter(adapter);

        btnAtras.setOnClickListener(v -> finish());

        lista.setOnItemClickListener((parent, view, position, id) -> {
            seleccionado = adapter.getItem(position);
            adapter.setSeleccionadoId(seleccionado.getId());
            confirmarLlamada(seleccionado);
        });

        etBuscar.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filtrar();
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        btnCompartir.setOnClickListener(v -> compartir());
        btnVerImagen.setOnClickListener(v -> verImagen());
        btnEliminar.setOnClickListener(v -> eliminar());
        btnActualizar.setOnClickListener(v -> actualizar());
    }

    @Override
    protected void onResume() {
        super.onResume();
        cargar();
    }

    private void cargar() {
        todos.clear();
        todos.addAll(db.obtenerTodos());

        if (seleccionado != null) {
            boolean existe = false;
            for (Contactos c : todos) {
                if (c.getId() == seleccionado.getId()) {
                    seleccionado = c;
                    existe = true;
                    break;
                }
            }
            if (!existe) seleccionado = null;
        }
        filtrar();
        adapter.setSeleccionadoId(seleccionado == null ? -1 : seleccionado.getId());
    }

    private void filtrar() {
        String q = etBuscar.getText().toString().trim().toLowerCase(Locale.getDefault());
        List<Contactos> visibles = new ArrayList<>();
        for (Contactos c : todos) {
            if (q.isEmpty()
                    || c.getNombre().toLowerCase(Locale.getDefault()).contains(q)
                    || c.getTelefonoCompleto().contains(q)) {
                visibles.add(c);
            }
        }
        adapter.setDatos(visibles);
    }

    private boolean haySeleccion() {
        if (seleccionado == null) {
            Toast.makeText(this, "Seleccione un contacto de la lista", Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }

    private void confirmarLlamada(Contactos c) {
        new AlertDialog.Builder(this)
                .setTitle("Acción")
                .setMessage("Desea llamar a " + c.getNombre())
                .setNegativeButton("No", null)
                .setPositiveButton("Yes", (d, w) -> {
                    Intent i = new Intent(this, LlamarActivity.class);
                    i.putExtra(LlamarActivity.EXTRA_NOMBRE, c.getNombre());
                    i.putExtra(LlamarActivity.EXTRA_NUMERO, "+" + c.getTelefonoCompleto());
                    startActivity(i);
                })
                .show();
    }

    // ---------- Botones ----------

    private void compartir() {
        if (!haySeleccion()) return;
        Intent i = new Intent(Intent.ACTION_SEND);
        i.setType("text/plain");
        i.putExtra(Intent.EXTRA_SUBJECT, "Contacto: " + seleccionado.getNombre());
        i.putExtra(Intent.EXTRA_TEXT, "Nombre: " + seleccionado.getNombre()
                + "\nTeléfono: +" + seleccionado.getTelefonoCompleto());
        startActivity(Intent.createChooser(i, "Compartir contacto"));
    }

    private void verImagen() {
        if (!haySeleccion()) return;
        byte[] datos = db.obtenerImagen(seleccionado.getId());
        Bitmap bmp = ImagenUtil.aBitmap(datos);
        if (bmp == null) {
            Toast.makeText(this, "Este contacto no tiene imagen guardada", Toast.LENGTH_SHORT).show();
            return;
        }
        ImageView iv = new ImageView(this);
        iv.setLayoutParams(new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        iv.setAdjustViewBounds(true);
        iv.setImageBitmap(bmp);

        new AlertDialog.Builder(this)
                .setTitle(seleccionado.getNombre())
                .setView(iv)
                .setPositiveButton("Cerrar", null)
                .show();
    }

    private void eliminar() {
        if (!haySeleccion()) return;
        final Contactos c = seleccionado;
        new AlertDialog.Builder(this)
                .setTitle("Eliminar contacto")
                .setMessage("¿Desea eliminar a " + c.getNombre() + "?")
                .setNegativeButton("No", null)
                .setPositiveButton("Sí", (d, w) -> {
                    db.eliminar(c.getId());
                    seleccionado = null;
                    cargar();
                    Toast.makeText(this, "Contacto eliminado", Toast.LENGTH_SHORT).show();
                })
                .show();
    }

    private void actualizar() {
        if (!haySeleccion()) return;
        Intent i = new Intent(this, MainActivity.class);
        i.putExtra(MainActivity.EXTRA_ID, seleccionado.getId());
        startActivity(i);
    }

    @Override
    protected void onDestroy() {
        db.close();
        super.onDestroy();
    }
}

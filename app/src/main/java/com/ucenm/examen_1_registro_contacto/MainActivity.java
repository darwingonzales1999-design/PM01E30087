package com.ucenm.examen_1_registro_contacto;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.text.InputFilter;
import android.text.TextUtils;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

public class MainActivity extends AppCompatActivity {

    public static final String EXTRA_ID = "contacto_id";

    private static final String[] PAISES = {
            "Honduras (504)", "Costa Rica (506)", "Guatemala (502)", "El Salvador (503)"
    };

    private static final Pattern PATRON_NOMBRE = Pattern.compile("^[\\p{L} ]+$");
    private static final Pattern PATRON_TELEFONO = Pattern.compile("^[0-9]+$");

    private ContactosDb db;
    private ImageView imgContacto;
    private Spinner spPais;
    private EditText etNombre, etTelefono, etNota;
    private Button btnSalvar, btnContactos;

    private byte[] imagenBytes;
    private int contactoId = -1;
    private Uri camaraUri;

    private final ActivityResultLauncher<Uri> camaraLauncher = registerForActivityResult(
            new ActivityResultContracts.TakePicture(), ok -> {
                if (ok && camaraUri != null) cargarImagen(camaraUri);
            });

    private final ActivityResultLauncher<String> galeriaLauncher = registerForActivityResult(
            new ActivityResultContracts.GetContent(), uri -> {
                if (uri != null) cargarImagen(uri);
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        db = new ContactosDb(this);

        imgContacto = findViewById(R.id.imgContacto);
        spPais = findViewById(R.id.spPais);
        etNombre = findViewById(R.id.etNombre);
        etTelefono = findViewById(R.id.etTelefono);
        etNota = findViewById(R.id.etNota);
        btnSalvar = findViewById(R.id.btnSalvar);
        btnContactos = findViewById(R.id.btnContactos);
        View btnAgregarFoto = findViewById(R.id.btnAgregarFoto);

        spPais.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, PAISES));

        etNombre.setFilters(new InputFilter[]{filtroRegex("[\\p{L} ]*"), new InputFilter.LengthFilter(50)});
        etTelefono.setFilters(new InputFilter[]{filtroRegex("[0-9]*"), new InputFilter.LengthFilter(15)});

        btnAgregarFoto.setOnClickListener(v -> elegirOrigenImagen());
        btnSalvar.setOnClickListener(v -> guardar());
        btnContactos.setOnClickListener(v ->
                startActivity(new Intent(this, ListaContactosActivity.class)));

        if (savedInstanceState != null) {
            camaraUri = savedInstanceState.getParcelable("camaraUri");
            byte[] guardada = savedInstanceState.getByteArray("imagen");
            if (guardada != null) mostrarImagen(guardada);
        }

        int id = getIntent().getIntExtra(EXTRA_ID, -1);
        if (id != -1 && savedInstanceState == null) {
            cargarParaEditar(id);
        } else if (id != -1) {
            contactoId = id;
            btnSalvar.setText("Actualizar Contacto");
            btnContactos.setVisibility(View.GONE);
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelable("camaraUri", camaraUri);
        if (imagenBytes != null) outState.putByteArray("imagen", imagenBytes);
    }

    private InputFilter filtroRegex(String permitido) {
        final Pattern p = Pattern.compile(permitido);
        return (source, start, end, dest, dstart, dend) ->
                p.matcher(source.subSequence(start, end)).matches() ? null : "";
    }

    private void elegirOrigenImagen() {
        new AlertDialog.Builder(this)
                .setTitle("Imagen del contacto")
                .setItems(new String[]{"Tomar foto", "Elegir de la galería"}, (d, which) -> {
                    if (which == 0) tomarFoto();
                    else galeriaLauncher.launch("image/*");
                })
                .show();
    }

    private void tomarFoto() {
        try {
            File dir = new File(getCacheDir(), "images");
            dir.mkdirs();
            File archivo = File.createTempFile("foto_", ".jpg", dir);
            camaraUri = FileProvider.getUriForFile(this, getPackageName() + ".fileprovider", archivo);
            camaraLauncher.launch(camaraUri);
        } catch (IOException | ActivityNotFoundException e) {
            Toast.makeText(this, "No se pudo abrir la cámara", Toast.LENGTH_SHORT).show();
        }
    }

    private void cargarImagen(Uri uri) {
        try {
            Bitmap bmp = ImagenUtil.decodificar(this, uri, 800);
            mostrarImagen(ImagenUtil.aBytes(bmp));
        } catch (IOException e) {
            Toast.makeText(this, "No se pudo cargar la imagen", Toast.LENGTH_SHORT).show();
        }
    }

    private void mostrarImagen(byte[] datos) {
        Bitmap bmp = ImagenUtil.aBitmap(datos);
        if (bmp == null) return;
        imagenBytes = datos;
        imgContacto.setPadding(0, 0, 0, 0);
        imgContacto.setScaleType(ImageView.ScaleType.CENTER_CROP);
        imgContacto.setImageBitmap(bmp);
    }

    private void guardar() {
        String nombre = etNombre.getText().toString().trim();
        String telefono = etTelefono.getText().toString().trim();
        String nota = etNota.getText().toString().trim();
        String pais = (String) spPais.getSelectedItem();

        etNombre.setError(null);
        etTelefono.setError(null);
        etNota.setError(null);

        List<String> errores = new ArrayList<>();

        if (nombre.isEmpty()) {
            errores.add("Debe escribir un nombre");
            etNombre.setError("Campo obligatorio");
        } else if (!PATRON_NOMBRE.matcher(nombre).matches()) {
            errores.add("El nombre solo puede contener letras");
            etNombre.setError("Solo letras");
        }

        if (telefono.isEmpty()) {
            errores.add("Debe escribir un teléfono");
            etTelefono.setError("Campo obligatorio");
        } else if (!PATRON_TELEFONO.matcher(telefono).matches()) {
            errores.add("El teléfono solo puede contener dígitos");
            etTelefono.setError("Solo dígitos");
        }

        if (nota.isEmpty()) {
            errores.add("Debe escribir una nota");
            etNota.setError("Campo obligatorio");
        }

        if (!errores.isEmpty()) {
            new AlertDialog.Builder(this)
                    .setTitle("Alerta")
                    .setMessage(TextUtils.join("\n", errores))
                    .setPositiveButton("Aceptar", null)
                    .show();
            return;
        }

        Contactos c = new Contactos(pais, nombre, telefono, nota, imagenBytes);

        if (contactoId == -1) {
            long resultado = db.insertar(c);
            if (resultado == -1) {
                Toast.makeText(this, "Error al guardar el contacto", Toast.LENGTH_SHORT).show();
                return;
            }
            Toast.makeText(this, "Contacto salvado", Toast.LENGTH_SHORT).show();
            limpiarFormulario();
        } else {
            c.setId(contactoId);
            db.actualizar(c);
            Toast.makeText(this, "Contacto actualizado", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    private void cargarParaEditar(int id) {
        Contactos c = db.obtenerPorId(id);
        if (c == null) {
            Toast.makeText(this, "El contacto ya no existe", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        contactoId = id;
        btnSalvar.setText("Actualizar Contacto");
        btnContactos.setVisibility(View.GONE);

        for (int i = 0; i < PAISES.length; i++) {
            if (PAISES[i].equals(c.getPais())) {
                spPais.setSelection(i);
                break;
            }
        }
        etNombre.setText(c.getNombre());
        etTelefono.setText(c.getTelefono());
        etNota.setText(c.getNota());
        if (c.getImagen() != null) mostrarImagen(c.getImagen());
    }

    private void limpiarFormulario() {
        etNombre.setText("");
        etTelefono.setText("");
        etNota.setText("");
        spPais.setSelection(0);
        imagenBytes = null;
        imgContacto.setScaleType(ImageView.ScaleType.FIT_CENTER);
        int p = (int) (24 * getResources().getDisplayMetrics().density);
        imgContacto.setPadding(p, p, p, p);
        imgContacto.setImageResource(R.drawable.ic_person);
        etNombre.requestFocus();
    }

    @Override
    protected void onDestroy() {
        db.close();
        super.onDestroy();
    }
}
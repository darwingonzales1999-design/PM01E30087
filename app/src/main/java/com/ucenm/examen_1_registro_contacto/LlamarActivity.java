package com.ucenm.examen_1_registro_contacto;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

public class LlamarActivity extends AppCompatActivity {

    public static final String EXTRA_NOMBRE = "nombre";
    public static final String EXTRA_NUMERO = "numero";

    private String numero;

    private final ActivityResultLauncher<String> permisoLauncher = registerForActivityResult(
            new ActivityResultContracts.RequestPermission(), concedido -> {
                if (concedido) {
                    realizarLlamada();
                } else {
                    Toast.makeText(this, "Se necesita el permiso para realizar llamadas",
                            Toast.LENGTH_LONG).show();
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_llamar);

        numero = getIntent().getStringExtra(EXTRA_NUMERO);

        TextView tvNumero = findViewById(R.id.tvNumero);
        Button btnAtras = findViewById(R.id.btnAtras);
        ImageButton btnLlamar = findViewById(R.id.btnLlamar);

        tvNumero.setText(numero);
        btnAtras.setOnClickListener(v -> finish());
        btnLlamar.setOnClickListener(v -> llamar());
    }

    private void llamar() {
        if (numero == null || numero.isEmpty()) return;

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CALL_PHONE)
                == PackageManager.PERMISSION_GRANTED) {
            realizarLlamada();
        } else {
            permisoLauncher.launch(Manifest.permission.CALL_PHONE);
        }
    }

    private void realizarLlamada() {
        try {
            Intent callIntent = new Intent(Intent.ACTION_CALL);
            callIntent.setData(Uri.parse("tel:" + Uri.encode(numero)));
            startActivity(callIntent);
        } catch (SecurityException e) {
            Toast.makeText(this, "No hay permiso para llamar", Toast.LENGTH_SHORT).show();
        }
    }
}

package com.ucenm.examen_1_registro_contacto;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;
import java.util.List;

public class ContactosDb extends SQLiteOpenHelper {

    private static final String DB_NAME = "contactos.db";
    private static final int DB_VERSION = 1;

    public static final String TABLA = "contactos";
    public static final String COL_ID = "id";
    public static final String COL_PAIS = "pais";
    public static final String COL_NOMBRE = "nombre";
    public static final String COL_TELEFONO = "telefono";
    public static final String COL_NOTA = "nota";
    public static final String COL_IMAGEN = "imagen";

    public ContactosDb(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE " + TABLA + " ("
                + COL_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + COL_PAIS + " TEXT NOT NULL, "
                + COL_NOMBRE + " TEXT NOT NULL, "
                + COL_TELEFONO + " TEXT NOT NULL, "
                + COL_NOTA + " TEXT NOT NULL, "
                + COL_IMAGEN + " BLOB)");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLA);
        onCreate(db);
    }

    private ContentValues valores(Contactos c) {
        ContentValues v = new ContentValues();
        v.put(COL_PAIS, c.getPais());
        v.put(COL_NOMBRE, c.getNombre());
        v.put(COL_TELEFONO, c.getTelefono());
        v.put(COL_NOTA, c.getNota());
        v.put(COL_IMAGEN, c.getImagen());
        return v;
    }

    public long insertar(Contactos c) {
        return getWritableDatabase().insert(TABLA, null, valores(c));
    }

    public int actualizar(Contactos c) {
        return getWritableDatabase().update(TABLA, valores(c), COL_ID + "=?",
                new String[]{String.valueOf(c.getId())});
    }

    public int eliminar(int id) {
        return getWritableDatabase().delete(TABLA, COL_ID + "=?",
                new String[]{String.valueOf(id)});
    }

    /** Lista todos los contactos (sin la imagen, para no cargar BLOBs en la lista). */
    public List<Contactos> obtenerTodos() {
        List<Contactos> lista = new ArrayList<>();
        Cursor cur = getReadableDatabase().query(TABLA,
                new String[]{COL_ID, COL_PAIS, COL_NOMBRE, COL_TELEFONO, COL_NOTA},
                null, null, null, null, COL_NOMBRE + " COLLATE NOCASE ASC");
        try {
            while (cur.moveToNext()) {
                lista.add(new Contactos(cur.getInt(0), cur.getString(1), cur.getString(2),
                        cur.getString(3), cur.getString(4), null));
            }
        } finally {
            cur.close();
        }
        return lista;
    }

    /** Devuelve un contacto con su imagen, o null si no existe. */
    public Contactos obtenerPorId(int id) {
        Cursor cur = getReadableDatabase().query(TABLA, null, COL_ID + "=?",
                new String[]{String.valueOf(id)}, null, null, null);
        try {
            if (cur.moveToFirst()) {
                return new Contactos(
                        cur.getInt(cur.getColumnIndexOrThrow(COL_ID)),
                        cur.getString(cur.getColumnIndexOrThrow(COL_PAIS)),
                        cur.getString(cur.getColumnIndexOrThrow(COL_NOMBRE)),
                        cur.getString(cur.getColumnIndexOrThrow(COL_TELEFONO)),
                        cur.getString(cur.getColumnIndexOrThrow(COL_NOTA)),
                        cur.getBlob(cur.getColumnIndexOrThrow(COL_IMAGEN)));
            }
            return null;
        } finally {
            cur.close();
        }
    }

    public byte[] obtenerImagen(int id) {
        Contactos c = obtenerPorId(id);
        return c == null ? null : c.getImagen();
    }
}
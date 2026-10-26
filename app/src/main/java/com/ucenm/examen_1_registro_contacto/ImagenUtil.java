package com.ucenm.examen_1_registro_contacto;

import android.content.ContentResolver;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.net.Uri;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
public final class ImagenUtil {

    private ImagenUtil(){
    }

    public static Bitmap decodificar(Context ctx, Uri uri, int maxLado) throws IOException {
        ContentResolver cr = ctx.getContentResolver();

        BitmapFactory.Options op = new BitmapFactory.Options();
        op.inJustDecodeBounds = true;
        try (InputStream is = cr.openInputStream(uri)) {
            BitmapFactory.decodeStream(is, null, op);
        }
        int sample = 1;
        while (op.outWidth / sample > maxLado * 2 || op.outHeight / sample > maxLado * 2) {
            sample *= 2;
        }
        op.inJustDecodeBounds = false;
        op.inSampleSize = sample;

        Bitmap bmp;
        try (InputStream is = cr.openInputStream(uri)) {
            bmp = BitmapFactory.decodeStream(is, null, op);
        }
        if (bmp == null) {
            throw new IOException("No se pudo leer la imagen");
        }

        int rotacion = 0;
        try (InputStream is = cr.openInputStream(uri)) {
            ExifInterface exif = new ExifInterface(is);
            int o = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);
            if (o == ExifInterface.ORIENTATION_ROTATE_90) rotacion = 90;
            else if (o == ExifInterface.ORIENTATION_ROTATE_180) rotacion = 180;
            else if (o == ExifInterface.ORIENTATION_ROTATE_270) rotacion = 270;
        } catch (IOException ignorado) {
        }

        float escala = Math.min(1f, (float) maxLado / Math.max(bmp.getWidth(), bmp.getHeight()));
        Matrix m = new Matrix();
        if (escala < 1f) m.postScale(escala, escala);
        if (rotacion != 0) m.postRotate(rotacion);
        if (!m.isIdentity()) {
            bmp = Bitmap.createBitmap(bmp, 0, 0, bmp.getWidth(), bmp.getHeight(), m, true);
        }
        return bmp;
    }

    public static byte[] aBytes(Bitmap bmp) {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        bmp.compress(Bitmap.CompressFormat.JPEG, 85, out);
        return out.toByteArray();
    }

    public static Bitmap aBitmap(byte[] datos) {
        if (datos == null || datos.length == 0) return null;
        return BitmapFactory.decodeByteArray(datos, 0, datos.length);
    }
}
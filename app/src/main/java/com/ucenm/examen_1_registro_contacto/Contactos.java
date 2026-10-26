package com.ucenm.examen_1_registro_contacto;

public class Contactos {

    private int id;
    private String pais;
    private String nombre;
    private String telefono;
    private String nota;
    private byte[] imagen;
        public Contactos(String pais, String nombre, String telefono, String nota, byte[] imagen) {
            this.pais = pais;
            this.nombre = nombre;
            this.telefono = telefono;
            this.nota = nota;
            this.imagen = imagen;
        }


    public Contactos(int id, String pais, String nombre, String telefono, String nota, byte[] imagen) {
        this(pais, nombre, telefono, nota, imagen);
        this.id = id;
    }

    public int getId() {return id;}
    public void setId(int id) {this.id = id;}

    public String getPais() {return pais;}
    public void setPais(String pais) {this.pais = pais;}
    public String getNombre() {return nombre;}
    public void setNombre(String nombre) {this.nombre = nombre;}
    public String getTelefono() {return telefono;}
    public void setTelefono(String telefono) {this.telefono = telefono;}
    public String getNota() {return nota;}
    public void setNota(String nota) {this.nota = nota;}

    public String getCodigoPais(){
        if (pais == null) return "";
        int a = pais.indexOf('(');
        int b = pais.indexOf(')');

        if (a >= 0 && b > a){
            return pais.substring(a + 1, b).trim();
        }
        return "";
    }

    public String getTelefonoCompleto(){
        return getCodigoPais() + (telefono == null ? "" : telefono);
    }

    public byte[] getImagen() {
        return imagen;
    }

    public void setImagen(byte[] imagen) {
        this.imagen = imagen;
    }

}

package com.chrisventura.apps.noteline.Model;

/**
 * Created by ventu on 15/5/2017.
 */

public class Category {
    String nombre;
    String idCategory;
    int notesCount;
    long createdAt;

    public Category(String idCategory, String nombre) {
        this.nombre = nombre;
        this.idCategory = idCategory;
    }

    public long getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(long createdAt) {
        this.createdAt = createdAt;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getIdCategory() {
        return idCategory;
    }

    public void setIdCategory(String idCategory) {
        this.idCategory = idCategory;
    }

    public int getNotesCount() {
        return notesCount;
    }

    public void setNotesCount(int notesCount) {
        this.notesCount = notesCount;
    }
}

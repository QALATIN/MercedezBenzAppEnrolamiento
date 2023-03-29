package com.latinid.mercedes.ui.nuevosolicitante.capturaid;

public class TablaPDFModel {

    private String titulo;
    private String valor;


    public TablaPDFModel() {
    }

    public TablaPDFModel(String titulo, String valor) {
        this.titulo = titulo;
        this.valor = valor;
    }

    public String getTitulo() {
        return titulo;
    }

    public void setTitulo(String titulo) {
        this.titulo = titulo;
    }

    public String getValor() {
        return valor;
    }

    public void setValor(String valor) {
        this.valor = valor;
    }
}


package com.latinid.mercedes.model.local;

public class BinnacleModel {
    private int OrigenId;
    private int TipoLogId;
    private int UsuarioId;
    private String Mensaje;
    private String Referencia;

    public BinnacleModel() {
    }

    public BinnacleModel(int origenId, int tipoLogId, int usuarioId, String mensaje, String referencia) {
        OrigenId = origenId;
        TipoLogId = tipoLogId;
        UsuarioId = usuarioId;
        Mensaje = mensaje;
        Referencia = referencia;
    }

    public int getOrigenId() {
        return OrigenId;
    }

    public void setOrigenId(int origenId) {
        OrigenId = origenId;
    }

    public int getTipoLogId() {
        return TipoLogId;
    }

    public void setTipoLogId(int tipoLogId) {
        TipoLogId = tipoLogId;
    }

    public int getUsuarioId() {
        return UsuarioId;
    }

    public void setUsuarioId(int usuarioId) {
        UsuarioId = usuarioId;
    }

    public String getMensaje() {
        return Mensaje;
    }

    public void setMensaje(String mensaje) {
        Mensaje = mensaje;
    }

    public String getReferencia() {
        return Referencia;
    }

    public void setReferencia(String referencia) {
        Referencia = referencia;
    }

    @Override
    public String toString() {
        return "BinnacleModel{" +
                "OrigenId=" + OrigenId +
                ", TipoLogId=" + TipoLogId +
                ", UsuarioId=" + UsuarioId +
                ", Mensaje='" + Mensaje + '\'' +
                ", Referencia='" + Referencia + '\'' +
                '}';
    }
}

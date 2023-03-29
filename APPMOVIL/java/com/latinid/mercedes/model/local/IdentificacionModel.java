package com.latinid.mercedes.model.local;


import com.acuant.acuantdocumentprocessing.resultmodel.IDResult;

public class IdentificacionModel {

    private String TipoDeIdentificacion;
    private String Serie;
    private int Resultado;
    private String FechaDeRegistro;
    private String FechaDeVigencia;
    private String FechaDeEmision;
    private String NumeroDeEmision;
    private String NumeroDeDocumento;
    private int ResultadoAssure;
    private String CapturaIdentificacionFrente;
    private String CapturaIdentificacionReverso;
    private String fotoRecorteB64;
    private String OCR;
    private String CIC;
    private IDResult result;
    private String ElectorNumber;
    private String MRZ;
    private String Nacionalidad;
    private String DocumentoPdfBase64;

    public IdentificacionModel() {
    }


    public IdentificacionModel(String tipoDeIdentificacion, String serie, int resultado, String fechaDeRegistro, String fechaDeVigencia, String fechaDeEmision, String numeroDeEmision, String numeroDeDocumento, int resultadoAssure, String capturaIdentificacionFrente, String capturaIdentificacionReverso, String fotoRecorteB64, String OCR, String CIC, IDResult result, String electorNumber, String MRZ, String nacionalidad, String documentoPdfBase64) {
        TipoDeIdentificacion = tipoDeIdentificacion;
        Serie = serie;
        Resultado = resultado;
        FechaDeRegistro = fechaDeRegistro;
        FechaDeVigencia = fechaDeVigencia;
        FechaDeEmision = fechaDeEmision;
        NumeroDeEmision = numeroDeEmision;
        NumeroDeDocumento = numeroDeDocumento;
        ResultadoAssure = resultadoAssure;
        CapturaIdentificacionFrente = capturaIdentificacionFrente;
        CapturaIdentificacionReverso = capturaIdentificacionReverso;
        this.fotoRecorteB64 = fotoRecorteB64;
        this.OCR = OCR;
        this.CIC = CIC;
        this.result = result;
        ElectorNumber = electorNumber;
        this.MRZ = MRZ;
        Nacionalidad = nacionalidad;
        DocumentoPdfBase64 = documentoPdfBase64;
    }

    public String getTipoDeIdentificacion() {
        return TipoDeIdentificacion;
    }

    public void setTipoDeIdentificacion(String tipoDeIdentificacion) {
        TipoDeIdentificacion = tipoDeIdentificacion;
    }

    public String getSerie() {
        return Serie;
    }

    public void setSerie(String serie) {
        Serie = serie;
    }

    public int getResultado() {
        return Resultado;
    }

    public void setResultado(int resultado) {
        Resultado = resultado;
    }

    public String getFechaDeRegistro() {
        return FechaDeRegistro;
    }

    public void setFechaDeRegistro(String fechaDeRegistro) {
        FechaDeRegistro = fechaDeRegistro;
    }

    public String getFechaDeVigencia() {
        return FechaDeVigencia;
    }

    public void setFechaDeVigencia(String fechaDeVigencia) {
        FechaDeVigencia = fechaDeVigencia;
    }

    public String getFechaDeEmision() {
        return FechaDeEmision;
    }

    public void setFechaDeEmision(String fechaDeEmision) {
        FechaDeEmision = fechaDeEmision;
    }

    public String getNumeroDeEmision() {
        return NumeroDeEmision;
    }

    public void setNumeroDeEmision(String numeroDeEmision) {
        NumeroDeEmision = numeroDeEmision;
    }

    public String getNumeroDeDocumento() {
        return NumeroDeDocumento;
    }

    public void setNumeroDeDocumento(String numeroDeDocumento) {
        NumeroDeDocumento = numeroDeDocumento;
    }

    public int getResultadoAssure() {
        return ResultadoAssure;
    }

    public void setResultadoAssure(int resultadoAssure) {
        ResultadoAssure = resultadoAssure;
    }

    public String getCapturaIdentificacionFrente() {
        return CapturaIdentificacionFrente;
    }

    public void setCapturaIdentificacionFrente(String capturaIdentificacionFrente) {
        CapturaIdentificacionFrente = capturaIdentificacionFrente;
    }

    public String getCapturaIdentificacionReverso() {
        return CapturaIdentificacionReverso;
    }

    public void setCapturaIdentificacionReverso(String capturaIdentificacionReverso) {
        CapturaIdentificacionReverso = capturaIdentificacionReverso;
    }

    public String getFotoRecorteB64() {
        return fotoRecorteB64;
    }

    public void setFotoRecorteB64(String fotoRecorteB64) {
        this.fotoRecorteB64 = fotoRecorteB64;
    }

    public String getOCR() {
        return OCR;
    }

    public void setOCR(String OCR) {
        this.OCR = OCR;
    }

    public String getCIC() {
        return CIC;
    }

    public void setCIC(String CIC) {
        this.CIC = CIC;
    }

    public IDResult getResult() {
        return result;
    }

    public void setResult(IDResult result) {
        this.result = result;
    }


    public String getElectorNumber() {
        return ElectorNumber;
    }

    public void setElectorNumber(String electorNumber) {
        ElectorNumber = electorNumber;
    }

    public String getMRZ() {
        return MRZ;
    }

    public void setMRZ(String MRZ) {
        this.MRZ = MRZ;
    }

    public String getNacionalidad() {
        return Nacionalidad;
    }

    public void setNacionalidad(String nacionalidad) {
        Nacionalidad = nacionalidad;
    }

    public String getDocumentoPdfBase64() {
        return DocumentoPdfBase64;
    }

    public void setDocumentoPdfBase64(String documentoPdfBase64) {
        DocumentoPdfBase64 = documentoPdfBase64;
    }

    @Override
    public String toString() {
        return "InfoIdentificacionModel{" +
                "TipoDeIdentificacion='" + TipoDeIdentificacion + '\'' +
                ", Serie='" + Serie + '\'' +
                ", Resultado=" + Resultado +
                ", FechaDeRegistro='" + FechaDeRegistro + '\'' +
                ", FechaDeVigencia='" + FechaDeVigencia + '\'' +
                ", FechaDeEmision='" + FechaDeEmision + '\'' +
                ", NumeroDeEmision='" + NumeroDeEmision + '\'' +
                ", NumeroDeDocumento='" + NumeroDeDocumento + '\'' +
                ", ResultadoAssure=" + ResultadoAssure +
                ", OCR='" + OCR + '\'' +
                ", CIC='" + CIC + '\'' +
                ", result=" + result +
                '}';
    }
}

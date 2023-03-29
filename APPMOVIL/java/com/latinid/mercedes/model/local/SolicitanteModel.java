package com.latinid.mercedes.model.local;

public class SolicitanteModel {

    private String FechaDeRegistro;
    private String CapturaFrenteEnBase64;
    private String CapturaReversoEnBase64;
    private String SelfieBase64;
    private String FotoEnBase64;
    private String FotoDeIdentificacionEnBase64;
    private String ScoreDeLaComparacionFacial;
    private int Id;
    private String Nombre;
    private String Paterno;
    private String Materno;
    private String FechaDeNacimiento;
    private String LugarDeNacimiento;
    private String Curp;
    private String CorreoElectronico;
    private String TipoDeDocumento;
    private String NumeroDeDocumento;
    private String AnioRegistro;
    private int UsuarioId;
    private String Telefono;
    private String NombreCompletoDelSolicitante;
    private String Sexo;
    private String ResultadoGeneral;
    private String Guid;
    private String ResultadoDeLaComparacionFacial;
    private String CoordenadasGps;
    private String Serie;
    private String Vigencia;
    private String FechaDeEmision;
    private String Emision;
    private String OCR;
    private String CIC;
    private String ClaveElector;
    private String Mrz;
    private String Nacionalidad;
    private String CodigoPostal;
    private String CalleNumero;
    private String Colonia;
    private String Municipio;
    private String Estado;
    private String PruebaDeVida;
    private String DireccionCompleta;
    private String Edad;
    private String TipoCliente;
    private String Estatus;
    private String Folio;
    private String DocumentoPdfBase64;
    private int SolicitanteId;
    private String IdentificadorCiudadano;
    private String IneNumeroDeEmision;

    public SolicitanteModel() {
    }

    public SolicitanteModel(String fechaDeRegistro, String capturaFrenteEnBase64, String capturaReversoEnBase64, String selfieBase64, String fotoEnBase64, String fotoDeIdentificacionEnBase64, String scoreDeLaComparacionFacial, int id, String nombre, String paterno, String materno, String fechaDeNacimiento, String lugarDeNacimiento, String curp, String correoElectronico, String tipoDeDocumento, String numeroDeDocumento, String anioRegistro, int usuarioId, String telefono, String nombreCompletoDelSolicitante, String sexo, String resultadoGeneral, String guid, String resultadoDeLaComparacionFacial, String coordenadasGps, String serie, String vigencia, String fechaDeEmision, String emision, String OCR, String CIC, String claveElector, String mrz, String nacionalidad, String codigoPostal, String calleNumero, String colonia, String municipio, String estado, String pruebaDeVida, String direccionCompleta, String edad, String tipoCliente, String estatus, String folio, String documentoPdfBase64, int solicitanteId, String identificadorCiudadano, String ineNumeroDeEmision) {
        FechaDeRegistro = fechaDeRegistro;
        CapturaFrenteEnBase64 = capturaFrenteEnBase64;
        CapturaReversoEnBase64 = capturaReversoEnBase64;
        SelfieBase64 = selfieBase64;
        FotoEnBase64 = fotoEnBase64;
        FotoDeIdentificacionEnBase64 = fotoDeIdentificacionEnBase64;
        ScoreDeLaComparacionFacial = scoreDeLaComparacionFacial;
        Id = id;
        Nombre = nombre;
        Paterno = paterno;
        Materno = materno;
        FechaDeNacimiento = fechaDeNacimiento;
        LugarDeNacimiento = lugarDeNacimiento;
        Curp = curp;
        CorreoElectronico = correoElectronico;
        TipoDeDocumento = tipoDeDocumento;
        NumeroDeDocumento = numeroDeDocumento;
        AnioRegistro = anioRegistro;
        UsuarioId = usuarioId;
        Telefono = telefono;
        NombreCompletoDelSolicitante = nombreCompletoDelSolicitante;
        Sexo = sexo;
        ResultadoGeneral = resultadoGeneral;
        Guid = guid;
        ResultadoDeLaComparacionFacial = resultadoDeLaComparacionFacial;
        CoordenadasGps = coordenadasGps;
        Serie = serie;
        Vigencia = vigencia;
        FechaDeEmision = fechaDeEmision;
        Emision = emision;
        this.OCR = OCR;
        this.CIC = CIC;
        ClaveElector = claveElector;
        Mrz = mrz;
        Nacionalidad = nacionalidad;
        CodigoPostal = codigoPostal;
        CalleNumero = calleNumero;
        Colonia = colonia;
        Municipio = municipio;
        Estado = estado;
        PruebaDeVida = pruebaDeVida;
        DireccionCompleta = direccionCompleta;
        Edad = edad;
        TipoCliente = tipoCliente;
        Estatus = estatus;
        Folio = folio;
        DocumentoPdfBase64 = documentoPdfBase64;
        SolicitanteId = solicitanteId;
        IdentificadorCiudadano = identificadorCiudadano;
        IneNumeroDeEmision = ineNumeroDeEmision;
    }

    public String getCapturaFrenteEnBase64() {
        return CapturaFrenteEnBase64;
    }

    public void setCapturaFrenteEnBase64(String capturaFrenteEnBase64) {
        CapturaFrenteEnBase64 = capturaFrenteEnBase64;
    }

    public String getCapturaReversoEnBase64() {
        return CapturaReversoEnBase64;
    }

    public void setCapturaReversoEnBase64(String capturaReversoEnBase64) {
        CapturaReversoEnBase64 = capturaReversoEnBase64;
    }

    public String getSelfieBase64() {
        return SelfieBase64;
    }

    public void setSelfieBase64(String selfieBase64) {
        SelfieBase64 = selfieBase64;
    }

    public String getFotoEnBase64() {
        return FotoEnBase64;
    }

    public void setFotoEnBase64(String fotoEnBase64) {
        FotoEnBase64 = fotoEnBase64;
    }

    public String getFotoDeIdentificacionEnBase64() {
        return FotoDeIdentificacionEnBase64;
    }

    public void setFotoDeIdentificacionEnBase64(String fotoDeIdentificacionEnBase64) {
        FotoDeIdentificacionEnBase64 = fotoDeIdentificacionEnBase64;
    }

    public String getScoreDeLaComparacionFacial() {
        return ScoreDeLaComparacionFacial;
    }

    public void setScoreDeLaComparacionFacial(String scoreDeLaComparacionFacial) {
        ScoreDeLaComparacionFacial = scoreDeLaComparacionFacial;
    }

    public int getId() {
        return Id;
    }

    public void setId(int id) {
        Id = id;
    }

    public String getNombre() {
        return Nombre;
    }

    public void setNombre(String nombre) {
        Nombre = nombre;
    }

    public String getPaterno() {
        return Paterno;
    }

    public void setPaterno(String paterno) {
        Paterno = paterno;
    }

    public String getMaterno() {
        return Materno;
    }

    public void setMaterno(String materno) {
        Materno = materno;
    }

    public String getFechaDeNacimiento() {
        return FechaDeNacimiento;
    }

    public void setFechaDeNacimiento(String fechaDeNacimiento) {
        FechaDeNacimiento = fechaDeNacimiento;
    }

    public String getLugarDeNacimiento() {
        return LugarDeNacimiento;
    }

    public void setLugarDeNacimiento(String lugarDeNacimiento) {
        LugarDeNacimiento = lugarDeNacimiento;
    }

    public String getCurp() {
        return Curp;
    }

    public void setCurp(String curp) {
        Curp = curp;
    }

    public String getCorreoElectronico() {
        return CorreoElectronico;
    }

    public void setCorreoElectronico(String correoElectronico) {
        CorreoElectronico = correoElectronico;
    }

    public String getTipoDeDocumento() {
        return TipoDeDocumento;
    }

    public void setTipoDeDocumento(String tipoDeDocumento) {
        TipoDeDocumento = tipoDeDocumento;
    }

    public String getNumeroDeDocumento() {
        return NumeroDeDocumento;
    }

    public void setNumeroDeDocumento(String numeroDeDocumento) {
        NumeroDeDocumento = numeroDeDocumento;
    }

    public String getAnioRegistro() {
        return AnioRegistro;
    }

    public void setAnioRegistro(String anioRegistro) {
        AnioRegistro = anioRegistro;
    }

    public int getUsuarioId() {
        return UsuarioId;
    }

    public void setUsuarioId(int usuarioId) {
        UsuarioId = usuarioId;
    }

    public String getTelefono() {
        return Telefono;
    }

    public void setTelefono(String telefono) {
        Telefono = telefono;
    }

    public String getNombreCompletoDelSolicitante() {
        return NombreCompletoDelSolicitante;
    }

    public void setNombreCompletoDelSolicitante(String nombreCompletoDelSolicitante) {
        NombreCompletoDelSolicitante = nombreCompletoDelSolicitante;
    }

    public String getSexo() {
        return Sexo;
    }

    public void setSexo(String sexo) {
        Sexo = sexo;
    }

    public String getResultadoGeneral() {
        return ResultadoGeneral;
    }

    public void setResultadoGeneral(String resultadoGeneral) {
        ResultadoGeneral = resultadoGeneral;
    }

    public String getGuid() {
        return Guid;
    }

    public void setGuid(String guid) {
        Guid = guid;
    }

    public String getResultadoDeLaComparacionFacial() {
        return ResultadoDeLaComparacionFacial;
    }

    public void setResultadoDeLaComparacionFacial(String resultadoDeLaComparacionFacial) {
        ResultadoDeLaComparacionFacial = resultadoDeLaComparacionFacial;
    }

    public String getCoordenadasGps() {
        return CoordenadasGps;
    }

    public void setCoordenadasGps(String coordenadasGps) {
        CoordenadasGps = coordenadasGps;
    }

    public String getSerie() {
        return Serie;
    }

    public void setSerie(String serie) {
        Serie = serie;
    }

    public String getVigencia() {
        return Vigencia;
    }

    public void setVigencia(String vigencia) {
        Vigencia = vigencia;
    }

    public String getFechaDeEmision() {
        return FechaDeEmision;
    }

    public void setFechaDeEmision(String fechaDeEmision) {
        FechaDeEmision = fechaDeEmision;
    }

    public String getEmision() {
        return Emision;
    }

    public void setEmision(String emision) {
        Emision = emision;
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

    public String getClaveElector() {
        return ClaveElector;
    }

    public void setClaveElector(String claveElector) {
        ClaveElector = claveElector;
    }

    public String getMrz() {
        return Mrz;
    }

    public void setMrz(String mrz) {
        Mrz = mrz;
    }

    public String getNacionalidad() {
        return Nacionalidad;
    }

    public void setNacionalidad(String nacionalidad) {
        Nacionalidad = nacionalidad;
    }

    public String getCodigoPostal() {
        return CodigoPostal;
    }

    public void setCodigoPostal(String codigoPostal) {
        CodigoPostal = codigoPostal;
    }

    public String getCalleNumero() {
        return CalleNumero;
    }

    public void setCalleNumero(String calleNumero) {
        CalleNumero = calleNumero;
    }

    public String getColonia() {
        return Colonia;
    }

    public void setColonia(String colonia) {
        Colonia = colonia;
    }

    public String getMunicipio() {
        return Municipio;
    }

    public void setMunicipio(String municipio) {
        Municipio = municipio;
    }

    public String getEstado() {
        return Estado;
    }

    public void setEstado(String estado) {
        Estado = estado;
    }

    public String getPruebaDeVida() {
        return PruebaDeVida;
    }

    public void setPruebaDeVida(String pruebaDeVida) {
        PruebaDeVida = pruebaDeVida;
    }

    public String getDireccionCompleta() {
        return DireccionCompleta;
    }

    public void setDireccionCompleta(String direccionCompleta) {
        DireccionCompleta = direccionCompleta;
    }

    public String getEdad() {
        return Edad;
    }

    public void setEdad(String edad) {
        Edad = edad;
    }

    public String getFechaDeRegistro() {
        return FechaDeRegistro;
    }

    public void setFechaDeRegistro(String fechaDeRegistro) {
        FechaDeRegistro = fechaDeRegistro;
    }

    public String getTipoCliente() {
        return TipoCliente;
    }

    public void setTipoCliente(String tipoCliente) {
        TipoCliente = tipoCliente;
    }

    public String getEstatus() {
        return Estatus;
    }

    public void setEstatus(String estatus) {
        Estatus = estatus;
    }

    public String getFolio() {
        return Folio;
    }

    public void setFolio(String folio) {
        Folio = folio;
    }

    public String getDocumentoPdfBase64() {
        return DocumentoPdfBase64;
    }

    public void setDocumentoPdfBase64(String documentoPdfBase64) {
        DocumentoPdfBase64 = documentoPdfBase64;
    }

    public int getSolicitanteId() {
        return SolicitanteId;
    }

    public void setSolicitanteId(int solicitanteId) {
        SolicitanteId = solicitanteId;
    }

    public String getIdentificadorCiudadano() {
        return IdentificadorCiudadano;
    }

    public void setIdentificadorCiudadano(String identificadorCiudadano) {
        IdentificadorCiudadano = identificadorCiudadano;
    }

    public String getIneNumeroDeEmision() {
        return IneNumeroDeEmision;
    }

    public void setIneNumeroDeEmision(String ineNumeroDeEmision) {
        IneNumeroDeEmision = ineNumeroDeEmision;
    }

    @Override
    public String toString() {
        return "SolicitanteModel{" +
                "FechaDeRegistro='" + FechaDeRegistro + '\'' +
                ", ScoreDeLaComparacionFacial='" + ScoreDeLaComparacionFacial + '\'' +
                ", Id=" + Id +
                ", Nombre='" + Nombre + '\'' +
                ", Paterno='" + Paterno + '\'' +
                ", Materno='" + Materno + '\'' +
                ", FechaDeNacimiento='" + FechaDeNacimiento + '\'' +
                ", LugarDeNacimiento='" + LugarDeNacimiento + '\'' +
                ", Curp='" + Curp + '\'' +
                ", CorreoElectronico='" + CorreoElectronico + '\'' +
                ", TipoDeDocumento='" + TipoDeDocumento + '\'' +
                ", NumeroDeDocumento='" + NumeroDeDocumento + '\'' +
                ", AnioRegistro='" + AnioRegistro + '\'' +
                ", UsuarioId=" + UsuarioId +
                ", Telefono='" + Telefono + '\'' +
                ", NombreCompletoDelSolicitante='" + NombreCompletoDelSolicitante + '\'' +
                ", Sexo='" + Sexo + '\'' +
                ", ResultadoGeneral='" + ResultadoGeneral + '\'' +
                ", Guid='" + Guid + '\'' +
                ", ResultadoDeLaComparacionFacial='" + ResultadoDeLaComparacionFacial + '\'' +
                ", CoordenadasGps='" + CoordenadasGps + '\'' +
                ", Serie='" + Serie + '\'' +
                ", Vigencia='" + Vigencia + '\'' +
                ", FechaDeEmision='" + FechaDeEmision + '\'' +
                ", Emision='" + Emision + '\'' +
                ", OCR='" + OCR + '\'' +
                ", CIC='" + CIC + '\'' +
                ", ClaveElector='" + ClaveElector + '\'' +
                ", Mrz='" + Mrz + '\'' +
                ", Nacionalidad='" + Nacionalidad + '\'' +
                ", CodigoPostal='" + CodigoPostal + '\'' +
                ", CalleNumero='" + CalleNumero + '\'' +
                ", Colonia='" + Colonia + '\'' +
                ", Municipio='" + Municipio + '\'' +
                ", Estado='" + Estado + '\'' +
                ", PruebaDeVida='" + PruebaDeVida + '\'' +
                ", DireccionCompleta='" + DireccionCompleta + '\'' +
                ", Edad='" + Edad + '\'' +
                ", TipoCliente='" + TipoCliente + '\'' +
                ", Estatus='" + Estatus + '\'' +
                ", Folio='" + Folio + '\'' +
                ", SolicitanteId=" + SolicitanteId +
                ", IdentificadorCiudadano='" + IdentificadorCiudadano + '\'' +
                ", IneNumeroDeEmision='" + IneNumeroDeEmision + '\'' +
                '}';
    }
}

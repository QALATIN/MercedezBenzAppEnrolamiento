package com.latinid.mercedes.model.local;

public class PersonaModel {

    private String Nombre;
    private String Paterno;
    private String Materno;
    private String FechaDeNacimiento;
    private String Curp;
    private String Sexo;
    private String CodigoPostal;
    private String Calle_Numero;
    private String Colonia;
    private String Municipio;
    private String Estado;
    private String correo;
    private String telefono;
    private String pruebaDeVida;
    private String comparacionFacial;
    private String fotoSelfieB64;
    private String domicilioCompleto;
    private String edad;


    public PersonaModel() {
    }

    public PersonaModel(String nombre, String paterno, String materno, String fechaDeNacimiento, String curp, String sexo, String codigoPostal, String calle_Numero, String colonia, String municipio, String estado, String correo, String telefono, String pruebaDeVida, String comparacionFacial, String fotoSelfieB64, String domicilioCompleto, String edad) {
        Nombre = nombre;
        Paterno = paterno;
        Materno = materno;
        FechaDeNacimiento = fechaDeNacimiento;
        Curp = curp;
        Sexo = sexo;
        CodigoPostal = codigoPostal;
        Calle_Numero = calle_Numero;
        Colonia = colonia;
        Municipio = municipio;
        Estado = estado;
        this.correo = correo;
        this.telefono = telefono;
        this.pruebaDeVida = pruebaDeVida;
        this.comparacionFacial = comparacionFacial;
        this.fotoSelfieB64 = fotoSelfieB64;
        this.domicilioCompleto = domicilioCompleto;
        this.edad = edad;
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

    public String getCurp() {
        return Curp;
    }

    public void setCurp(String curp) {
        Curp = curp;
    }

    public String getSexo() {
        return Sexo;
    }

    public void setSexo(String sexo) {
        Sexo = sexo;
    }

    public String getCodigoPostal() {
        return CodigoPostal;
    }

    public void setCodigoPostal(String codigoPostal) {
        CodigoPostal = codigoPostal;
    }

    public String getCalle_Numero() {
        return Calle_Numero;
    }

    public void setCalle_Numero(String calle_Numero) {
        Calle_Numero = calle_Numero;
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

    public String getCorreo() {
        return correo;
    }

    public void setCorreo(String correo) {
        this.correo = correo;
    }

    public String getTelefono() {
        return telefono;
    }

    public void setTelefono(String telefono) {
        this.telefono = telefono;
    }

    public String getPruebaDeVida() {
        return pruebaDeVida;
    }

    public void setPruebaDeVida(String pruebaDeVida) {
        this.pruebaDeVida = pruebaDeVida;
    }

    public String getComparacionFacial() {
        return comparacionFacial;
    }

    public void setComparacionFacial(String comparacionFacial) {
        this.comparacionFacial = comparacionFacial;
    }

    public String getFotoSelfieB64() {
        return fotoSelfieB64;
    }

    public void setFotoSelfieB64(String fotoSelfieB64) {
        this.fotoSelfieB64 = fotoSelfieB64;
    }

    public String getDomicilioCompleto() {
        return domicilioCompleto;
    }

    public void setDomicilioCompleto(String domicilioCompleto) {
        this.domicilioCompleto = domicilioCompleto;
    }

    public String getEdad() {
        return edad;
    }

    public void setEdad(String edad) {
        this.edad = edad;
    }



    @Override
    public String toString() {
        return "PersonaModel{" +
                "Nombre='" + Nombre + '\'' +
                ", Paterno='" + Paterno + '\'' +
                ", Materno='" + Materno + '\'' +
                ", FechaDeNacimiento='" + FechaDeNacimiento + '\'' +
                ", Curp='" + Curp + '\'' +
                ", Sexo='" + Sexo + '\'' +
                ", CodigoPostal='" + CodigoPostal + '\'' +
                ", Calle_Numero='" + Calle_Numero + '\'' +
                ", Colonia='" + Colonia + '\'' +
                ", Municipio='" + Municipio + '\'' +
                ", Estado='" + Estado + '\'' +
                ", correo='" + correo + '\'' +
                ", telefono='" + telefono + '\'' +
                ", pruebaDeVida='" + pruebaDeVida + '\'' +
                ", comparacionFacial='" + comparacionFacial + '\'' +
                '}';
    }
}

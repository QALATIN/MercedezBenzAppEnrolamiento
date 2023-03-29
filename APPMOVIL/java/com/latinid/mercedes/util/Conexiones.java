package com.latinid.mercedes.util;

public class Conexiones {

    /**
     * WEB SERVICE GENERAL
     * Produccion = https://iddrivers.latinid.com.mx/ServiceGs/api/
     * Local =
     */
    public static String webServiceGeneral = "https://mbfs.latinid.com.mx:9582";
    //public static String webServiceGeneralDevLocal = "http://10.10.8.240";
    //public static String webServiceGeneralDevPublico = "https://mbfs.latinid.com.mx:9582";
    //--
    //public static String webServiceGeneralQALocal = "http://10.10.8.241";
    //public static String webServiceGeneralQAPublico = "http://mbfs.latinid.com.mx:9583";

    /**
     * URL GENERADORA DE TOKENS EN EL SERVICIO DE ALONSO
     */
    public static String webServiceTokenBearer = "http://10.10.8.211/API/GetToken";

    /**
     WEB SERVICE SEGURIDATA - SERVICIO ADMINISTRADO POR GABRIELA Y ALONSO
     */
    public static String webServiceSeguriData = "https://mbfs.latinid.com.mx:9582/Gateway/ApiFirma";
    //public static String webServiceGSeguriDataLocalDEV = "http://10.10.8.240:8080/ApiFirma";
    //public static String webServiceGSeguriDataPublicoDEV = "https://mbfs.latinid.com.mx:9582/Gateway/ApiFirma";
    //----
    //public static String webServiceGSeguriDataLocalQA = "http://10.10.8.241:8080/ApiFirma";
    //public static String webServiceGSeguriDataPublicoQA = "https://mbfs.latinid.com.mx:9583/Gateway/ApiFirma";

}
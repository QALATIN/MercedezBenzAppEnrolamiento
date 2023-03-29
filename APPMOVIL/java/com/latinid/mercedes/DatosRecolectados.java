package com.latinid.mercedes;

import android.content.Intent;

import com.acuant.acuantechipreader.model.NfcData;
import com.latinid.mercedes.db.model.ActiveEnrollment;
import com.latinid.mercedes.model.local.IdentificacionModel;
import com.latinid.mercedes.model.local.PersonaModel;

public class DatosRecolectados {

    public static String versionApp = "1.0.16";//DEV=1.0.15//QA=1.0.0
    public static int usuarioId = 2;//for default to help log server
    public static boolean inSesion = false;
    public static String token = "";
    public static String typeEnrollActivate=null;
    public static String idEnrollTemp=null;
    public static String coors = null;
    public static String nameCompleteUSER = "";
    public static String pdfToSignTemp="";

    public static ActiveEnrollment activeEnrollment;
    public static ActiveEnrollment activeEnrollmentTempAvalesCoacredit;

    public static String recorte = "";

    //Datos para lectura de chip pasaporte
    public static String dob = null;
    public static String doe = null;
    public static String docNumber = null;
    public static String country = null;
    public static NfcData cardDetails = null;
    public static boolean capNfc = false;
    public static Intent tempIntent = null;
    public static boolean nfcFinish = false;

    /**
     * <p><b>Variables Selfie</b></p>
     */
    public static boolean selfieFinish = false;
    public static String selfieB64 = "";
    public static boolean proofLifeSelfie = false;

    /**
     * <p><b>Vars Signature</b></p>
     */
    public static String docSignatureFinish = "";

    /**
     * <p><b>Vars FingerPrint</b></p>
     */
    public static String thumbRight = "";
    public static String indexRight = "";
    public static String middleRight = "";
    public static String ringRight = "";
    public static String littleRight = "";
    public static String thumbLeft = "";
    public static String indexLeft = "";
    public static String middleLeft = "";
    public static String ringLeft = "";
    public static String littleLeft = "";

    /**
     * <p><b>Vars Docs</b></p>
     */
    public static String docB64Address = "";
    public static String docB64Banking = "";
    public static String docB64Income = "";
    public static String fileTypeAddress = "";
    public static String fileTypeBanking = "";
    public static String fileTypeIncome = "";

}

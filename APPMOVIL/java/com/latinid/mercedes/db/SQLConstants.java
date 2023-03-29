package com.latinid.mercedes.db;

public class SQLConstants {

    /**
     * DB
     */
    public static final String DB = "dbmeche.db";
    /**
     * TABLES
     */
    public static final String tableUser = "user";
    public static final String tableEnrrollment = "enrollment";

    /**
     * TABLE USER
     */
    public static final String USER_ID = "user_id";
    public static final String USER_NAME = "name";
    public static final String USER_PASS = "pass";
    public static final String USER_REGISTRATION_DATE = "registration_date";
    public static final String USER_ACTIVE = "active";
    public static final String USER_FINGER_ID = "finger_id";

    /**
     * TABLE ENROLLMENT
     */
    public static final String ENROLL_ID = "enroll_id";
    public static final String STATE_ID = "state_id";
    public static final String DATE = "date";
    public static final String SIGNATURE_ID = "signature_id";
    public static final String IDENT_ID = "ident_id";
    public static final String FINGER_ID = "finger_id";
    public static final String DOCS_ID = "docs_id";
    public static final String JSON_SIGNATURE = "json_signature";
    public static final String JSON_IDENT = "json_ident";
    public static final String JSON_PERS = "json_pers";
    public static final String JSON_FINGER = "json_finger";
    public static final String JSON_DOCS = "json_docs";
    public static final String FOLIO = "folio";
    public static final String SOLICITANTE_ID = "solicitante_id";
    public static final String TIPO_ENROLL = "tipo_enroll";
    public static final String ENROLL_SOLICITANTE = "enroll_solicitante";

    /**
     * QUERYS
     */
    public static final String SQL_CREATE_TABLE_USER =
            "CREATE TABLE " + tableUser + " (" +
                    USER_ID + " TEXT PRIMARY KEY," +
                    USER_NAME + " TEXT," +
                    USER_PASS + " TEXT," +
                    USER_REGISTRATION_DATE + " TEXT," +
                    USER_ACTIVE + " TEXT," +
                    USER_FINGER_ID + " TEXT" + ");";

    public static final String SQL_CREATE_TABLE_ENROLLMENT =
            "CREATE TABLE " + tableEnrrollment + " (" +
                    ENROLL_ID + " TEXT PRIMARY KEY," +
                    STATE_ID + " TEXT," +
                    DATE + " TEXT," +
                    SIGNATURE_ID + " TEXT," +
                    IDENT_ID + " TEXT," +
                    FINGER_ID + " TEXT," +
                    DOCS_ID + " TEXT," +
                    JSON_SIGNATURE + " TEXT," +
                    JSON_IDENT + " TEXT," +
                    JSON_PERS + " TEXT," +
                    JSON_FINGER + " TEXT," +
                    JSON_DOCS + " TEXT," +
                    FOLIO + " TEXT," +
                    SOLICITANTE_ID + " TEXT," +
                    TIPO_ENROLL + " TEXT," +
                    ENROLL_SOLICITANTE + " TEXT" + ");";


    public static final String[] ALL_COLUMNS_USER = {
            USER_ID,
            USER_NAME,
            USER_PASS,
            USER_REGISTRATION_DATE,
            USER_ACTIVE,
            USER_FINGER_ID};

    public static final String[] ALL_COLUMNS_ENROLL = {
            ENROLL_ID,
            STATE_ID,
            DATE,
            SIGNATURE_ID,
            IDENT_ID,
            FINGER_ID,
            DOCS_ID,
            JSON_SIGNATURE,
            JSON_IDENT,
            JSON_PERS,
            JSON_FINGER,
            JSON_DOCS,
            FOLIO,
            SOLICITANTE_ID,
            TIPO_ENROLL,
            ENROLL_SOLICITANTE};


}

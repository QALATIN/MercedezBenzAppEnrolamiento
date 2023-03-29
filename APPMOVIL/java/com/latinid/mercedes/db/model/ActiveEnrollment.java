package com.latinid.mercedes.db.model;

import android.content.ContentValues;

import com.latinid.mercedes.db.SQLConstants;

public class ActiveEnrollment {

    private String enroll_id;
    private String state_id;
    private String date;
    private String signature_id;
    private String ident_id;
    private String finger_id;
    private String docs_id;
    private String json_signature;
    private String json_pers;
    private String json_ident;
    private String json_finger;
    private String json_docs;
    private String folio;
    private String solicitante_id;
    private String tipo_enroll;
    private String enroll_solicitante;

    public ActiveEnrollment() {
    }

    public ActiveEnrollment(String enroll_id, String state_id, String date, String signature_id, String ident_id, String finger_id, String docs_id, String json_signature, String json_pers, String json_ident, String json_finger, String json_docs, String folio, String solicitante_id, String tipo_enroll, String enroll_solicitante) {
        this.enroll_id = enroll_id;
        this.state_id = state_id;
        this.date = date;
        this.signature_id = signature_id;
        this.ident_id = ident_id;
        this.finger_id = finger_id;
        this.docs_id = docs_id;
        this.json_signature = json_signature;
        this.json_pers = json_pers;
        this.json_ident = json_ident;
        this.json_finger = json_finger;
        this.json_docs = json_docs;
        this.folio = folio;
        this.solicitante_id = solicitante_id;
        this.tipo_enroll = tipo_enroll;
        this.enroll_solicitante = enroll_solicitante;
    }

    public String getEnroll_id() {
        return enroll_id;
    }

    public void setEnroll_id(String enroll_id) {
        this.enroll_id = enroll_id;
    }

    public String getState_id() {
        return state_id;
    }

    public void setState_id(String state_id) {
        this.state_id = state_id;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getSignature_id() {
        return signature_id;
    }

    public void setSignature_id(String signature_id) {
        this.signature_id = signature_id;
    }

    public String getIdent_id() {
        return ident_id;
    }

    public void setIdent_id(String ident_id) {
        this.ident_id = ident_id;
    }

    public String getFinger_id() {
        return finger_id;
    }

    public void setFinger_id(String finger_id) {
        this.finger_id = finger_id;
    }

    public String getDocs_id() {
        return docs_id;
    }

    public void setDocs_id(String docs_id) {
        this.docs_id = docs_id;
    }

    public String getJson_signature() {
        return json_signature;
    }

    public void setJson_signature(String json_signature) {
        this.json_signature = json_signature;
    }

    public String getJson_pers() {
        return json_pers;
    }

    public void setJson_pers(String json_pers) {
        this.json_pers = json_pers;
    }

    public String getJson_ident() {
        return json_ident;
    }

    public void setJson_ident(String json_ident) {
        this.json_ident = json_ident;
    }

    public String getJson_finger() {
        return json_finger;
    }

    public void setJson_finger(String json_finger) {
        this.json_finger = json_finger;
    }

    public String getJson_docs() {
        return json_docs;
    }

    public void setJson_docs(String json_docs) {
        this.json_docs = json_docs;
    }

    public String getFolio() {
        return folio;
    }

    public void setFolio(String folio) {
        this.folio = folio;
    }

    public String getSolicitante_id() {
        return solicitante_id;
    }

    public void setSolicitante_id(String solicitante_id) {
        this.solicitante_id = solicitante_id;
    }

    public String getTipo_enroll() {
        return tipo_enroll;
    }

    public void setTipo_enroll(String tipo_enroll) {
        this.tipo_enroll = tipo_enroll;
    }

    public String getEnroll_solicitante() {
        return enroll_solicitante;
    }

    public void setEnroll_solicitante(String enroll_solicitante) {
        this.enroll_solicitante = enroll_solicitante;
    }

    public ContentValues toValues() {
        ContentValues contentValues = new ContentValues(17);
        contentValues.put(SQLConstants.ENROLL_ID, enroll_id);
        contentValues.put(SQLConstants.STATE_ID, state_id);
        contentValues.put(SQLConstants.DATE, date);
        contentValues.put(SQLConstants.SIGNATURE_ID, signature_id);
        contentValues.put(SQLConstants.IDENT_ID, ident_id);
        contentValues.put(SQLConstants.FINGER_ID, finger_id);
        contentValues.put(SQLConstants.DOCS_ID, docs_id);
        contentValues.put(SQLConstants.JSON_SIGNATURE, json_signature);
        contentValues.put(SQLConstants.JSON_IDENT, json_ident);
        contentValues.put(SQLConstants.JSON_PERS, json_pers);
        contentValues.put(SQLConstants.JSON_FINGER, json_finger);
        contentValues.put(SQLConstants.JSON_DOCS, json_docs);
        contentValues.put(SQLConstants.FOLIO, folio);
        contentValues.put(SQLConstants.SOLICITANTE_ID, solicitante_id);
        contentValues.put(SQLConstants.TIPO_ENROLL, tipo_enroll);
        contentValues.put(SQLConstants.ENROLL_SOLICITANTE, enroll_solicitante);
        return contentValues;
    }

    @Override
    public String toString() {
        return "ActiveEnrollment{" +
                "enroll_id='" + enroll_id + '\'' +
                ", state_id='" + state_id + '\'' +
                ", date='" + date + '\'' +
                ", signature_id='" + signature_id + '\'' +
                ", ident_id='" + ident_id + '\'' +
                ", finger_id='" + finger_id + '\'' +
                ", docs_id='" + docs_id + '\'' +
                ", json_signature='" + json_signature + '\'' +
                ", json_pers='" + json_pers + '\'' +
                ", json_ident='" + json_ident + '\'' +
                ", json_finger='" + json_finger + '\'' +
                ", json_docs='" + json_docs + '\'' +
                ", folio='" + folio + '\'' +
                ", solicitante_id='" + solicitante_id + '\'' +
                ", tipo_enroll='" + tipo_enroll + '\'' +
                ", enroll_solicitante='" + enroll_solicitante + '\'' +
                '}';
    }
}

package com.latinid.mercedes.model.local;

public class SignatureModel {
        private String base64Signature;
        private String base64DocSignature;
        private String reference;

    public SignatureModel() {
    }

    public SignatureModel(String base64Signature, String base64DocSignature, String reference) {
        this.base64Signature = base64Signature;
        this.base64DocSignature = base64DocSignature;
        this.reference = reference;
    }

    public String getBase64Signature() {
        return base64Signature;
    }

    public void setBase64Signature(String base64Signature) {
        this.base64Signature = base64Signature;
    }

    public String getBase64DocSignature() {
        return base64DocSignature;
    }

    public void setBase64DocSignature(String base64DocSignature) {
        this.base64DocSignature = base64DocSignature;
    }

    public String getReference() {
        return reference;
    }

    public void setReference(String reference) {
        this.reference = reference;
    }

    @Override
    public String toString() {
        return "SignatureModel{" +
                "base64Signature='" + base64Signature + '\'' +
                ", base64DocSignature='" + base64DocSignature + '\'' +
                ", reference='" + reference + '\'' +
                '}';
    }
}

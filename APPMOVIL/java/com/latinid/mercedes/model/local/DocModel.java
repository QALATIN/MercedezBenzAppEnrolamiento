package com.latinid.mercedes.model.local;

public class DocModel {
    private String docAndress;
    private String typeAndress;
    private String docBanking;
    private String typeBanking;
    private String docIncome;
    private String typeIncome;

    public DocModel() {
    }

    public DocModel(String docAndress, String typeAndress, String docBanking, String typeBanking, String docIncome, String typeIncome) {
        this.docAndress = docAndress;
        this.typeAndress = typeAndress;
        this.docBanking = docBanking;
        this.typeBanking = typeBanking;
        this.docIncome = docIncome;
        this.typeIncome = typeIncome;
    }

    public String getDocAndress() {
        return docAndress;
    }

    public void setDocAndress(String docAndress) {
        this.docAndress = docAndress;
    }

    public String getTypeAndress() {
        return typeAndress;
    }

    public void setTypeAndress(String typeAndress) {
        this.typeAndress = typeAndress;
    }

    public String getDocBanking() {
        return docBanking;
    }

    public void setDocBanking(String docBanking) {
        this.docBanking = docBanking;
    }

    public String getTypeBanking() {
        return typeBanking;
    }

    public void setTypeBanking(String typeBanking) {
        this.typeBanking = typeBanking;
    }

    public String getDocIncome() {
        return docIncome;
    }

    public void setDocIncome(String docIncome) {
        this.docIncome = docIncome;
    }

    public String getTypeIncome() {
        return typeIncome;
    }

    public void setTypeIncome(String typeIncome) {
        this.typeIncome = typeIncome;
    }
}

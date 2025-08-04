package com.adroit.hotlistmicroservice.dto;

public class DocumentAddedResponse {

    private String consultantId;

    private String message;

    public String getConsultantId() {
        return consultantId;
    }

    public void setConsultantId(String consultantId) {
        this.consultantId = consultantId;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}

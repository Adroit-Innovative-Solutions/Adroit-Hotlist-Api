package com.adroit.hotlistmicroservice.dto;

import com.adroit.hotlistmicroservice.model.Consultant;

public class DocumentDetailsDTO {

    private long documentId;

    private String fileName;

    private String documentType;


    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getDocumentType() {
        return documentType;
    }

    public void setDocumentType(String documentType) {
        this.documentType = documentType;
    }

    public long getDocumentId() {
        return documentId;
    }

    public void setDocumentId(long documentId) {
        this.documentId = documentId;
    }
}

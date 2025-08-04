package com.adroit.hotlistmicroservice.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;

@Entity
public class ConsultantDocument {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long documentId;

    private String fileName;

    @Lob
    @Column(name = "fileData",columnDefinition = "LONGBLOB")
    private byte[] fileData;

    private String documentType;

    private String fileType;

    @JsonIgnore
    @ManyToOne
    @JoinColumn(name = "consultant_id")
    private Consultant consultant;

    public String getFileType() {
        return fileType;
    }

    public void setFileType(String fileType) {
        this.fileType = fileType;
    }

    public long getDocumentId() {
        return documentId;
    }

    public void setDocumentId(long documentId) {
        this.documentId = documentId;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public byte[] getFileData() {
        return fileData;
    }

    public void setFileData(byte[] fileData) {
        this.fileData = fileData;
    }

    public String getDocumentType() {
        return documentType;
    }

    public void setDocumentType(String documentType) {
        this.documentType = documentType;
    }

    public Consultant getConsultant() {
        return consultant;
    }

    public void setConsultant(Consultant consultant) {
        this.consultant = consultant;
    }
}

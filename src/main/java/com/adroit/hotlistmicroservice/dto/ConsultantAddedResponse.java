package com.adroit.hotlistmicroservice.dto;

import java.time.LocalDateTime;

public class ConsultantAddedResponse {

    private String consultantId;

    private String name;

    private String recruiter;

    private LocalDateTime addedTimeStamp;

    public String getConsultantId() {
        return consultantId;
    }

    public void setConsultantId(String consultantId) {
        this.consultantId = consultantId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getRecruiter() {
        return recruiter;
    }

    public void setRecruiter(String recruiter) {
        this.recruiter = recruiter;
    }

    public LocalDateTime getAddedTimeStamp() {
        return addedTimeStamp;
    }

    public void setAddedTimeStamp(LocalDateTime addedTimeStamp) {
        this.addedTimeStamp = addedTimeStamp;
    }
}

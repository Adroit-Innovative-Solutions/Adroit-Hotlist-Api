package com.adroit.hotlistmicroservice.dto;

import java.time.LocalDateTime;

public class DeleteConsultantResponse {

    private String consultantId;
    private LocalDateTime consultantAddedTimeStamp;

    public String getConsultantId() {
        return consultantId;
    }

    public void setConsultantId(String consultantId) {
        this.consultantId = consultantId;
    }

    public LocalDateTime getConsultantAddedTimeStamp() {
        return consultantAddedTimeStamp;
    }

    public void setConsultantAddedTimeStamp(LocalDateTime consultantAddedTimeStamp) {
        this.consultantAddedTimeStamp = consultantAddedTimeStamp;
    }
}

package com.adroit.hotlistmicroservice.dto;

import java.time.LocalDateTime;

public class DeleteConsultantResponse {

    private String consultantId;
    private LocalDateTime addedTimeStamp;

    public String getConsultantId() {
        return consultantId;
    }

    public void setConsultantId(String consultantId) {
        this.consultantId = consultantId;
    }

    public LocalDateTime getAddedTimeStamp() {
        return addedTimeStamp;
    }

    public void setAddedTimeStamp(LocalDateTime addedTimeStamp) {
        this.addedTimeStamp = addedTimeStamp;
    }
}

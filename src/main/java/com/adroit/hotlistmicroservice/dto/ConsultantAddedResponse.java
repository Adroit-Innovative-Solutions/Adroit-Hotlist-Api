package com.adroit.hotlistmicroservice.dto;

import java.time.LocalDateTime;

public class ConsultantAddedResponse {

    private String consultantId;

    private String name;

    private String recruiterName;

    private LocalDateTime consultantAddedTimeStamp;

    private boolean movedToHotlist;

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


    public String getRecruiterName() {
        return recruiterName;
    }

    public void setRecruiterName(String recruiterName) {
        this.recruiterName = recruiterName;
    }

    public LocalDateTime getConsultantAddedTimeStamp() {
        return consultantAddedTimeStamp;
    }

    public void setConsultantAddedTimeStamp(LocalDateTime consultantAddedTimeStamp) {
        this.consultantAddedTimeStamp = consultantAddedTimeStamp;
    }
    public boolean getMovedToHotlist() {
        return movedToHotlist;
    }

    public void setMovedToHotlist(boolean movedToHotlist) {
        this.movedToHotlist = movedToHotlist;
    }
}

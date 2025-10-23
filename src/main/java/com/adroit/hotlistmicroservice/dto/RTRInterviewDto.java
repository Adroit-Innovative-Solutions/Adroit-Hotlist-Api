package com.adroit.hotlistmicroservice.dto;

import lombok.*;

import java.time.LocalDateTime;
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class RTRInterviewDto {

    private String interviewId;
    private String rtrId;
    private String consultantId;
    private String consultantName;
    private String consultantEmailId;
    private String technology;
    private String clientId;
    private String clientName;
    private String salesExecutiveId;
    private String salesExecutive;
    private String interviewLevel;
    private String interviewStatus;
    private LocalDateTime interviewDateTime;
    private String interviewerEmailId;
    private String zoomLink;
    private int duration;
    private Boolean isPlaced;

}

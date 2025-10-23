package com.adroit.hotlistmicroservice.dto;

import lombok.*;

import java.time.LocalDateTime;
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UpdateInterviewDto {

    private String interviewId;
    private String interviewStatus;
    private String interviewLevel;
    private LocalDateTime interviewDateTime;
    private String interviewerEmailId;
    private String zoomLink;
    private int duration;


}

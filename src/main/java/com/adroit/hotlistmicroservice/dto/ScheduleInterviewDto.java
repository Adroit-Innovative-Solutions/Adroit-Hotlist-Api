package com.adroit.hotlistmicroservice.dto;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ScheduleInterviewDto {

    private String rtrId;
    private String interviewLevel;
    private LocalDateTime interviewDateTime;
    private String interviewerEmailId;
    private String zoomLink;

}

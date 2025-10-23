package com.adroit.hotlistmicroservice.dto;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class InterviewAddedDto {

    private String interviewId;
    private String rtrId;
    private String consultantId;
    private String consultantName;

}

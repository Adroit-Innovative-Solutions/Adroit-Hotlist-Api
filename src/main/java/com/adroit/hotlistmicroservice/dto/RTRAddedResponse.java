package com.adroit.hotlistmicroservice.dto;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class RTRAddedResponse {

    private String rtrId;
    private String consultantId;
    private String consultantName;
    private String clientName;
}

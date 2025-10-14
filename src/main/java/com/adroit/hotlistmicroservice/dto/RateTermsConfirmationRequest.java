package com.adroit.hotlistmicroservice.dto;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class RateTermsConfirmationRequest {

    private String consultantId;
    private String clientId;
    private String clientName;
    private String ratePart;
    private String vendorName;
    private String vendorEmailId;
    private String vendorMobileNumber;
    private String vendorCompany;
    private String vendorLinkedIn;
    private String implementationPartner;
    private String comments;

}

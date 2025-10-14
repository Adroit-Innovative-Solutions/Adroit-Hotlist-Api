package com.adroit.hotlistmicroservice.dto;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class RTRUpdateDTO {

    private String rtrId;
    private String clientId;
    private String clientName;
    private String ratePart;
    private String rtrStatus;
    private String salesExecutiveId;
    private String salesExecutive;
    private String vendorName;
    private String vendorEmailId;
    private String vendorMobileNumber;
    private String vendorCompany;
    private String vendorLinkedIn;
    private String implementationPartner;


}

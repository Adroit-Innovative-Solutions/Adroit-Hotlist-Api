package com.adroit.hotlistmicroservice.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Table(name = "rtr_us")
@Entity
public class RateTermsConfirmation extends BaseEntity{

    @Id
    private String rtrId;
    private String consultantId;
    private String consultantName;
    private String technology;
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
    private String comments;


    @PrePersist
    protected void onCreate(){
        super.onCreate();
        this.rtrStatus="SUBMITTED";
    }
}

package com.adroit.hotlistmicroservice.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "interviews_us")
public class RTRInterview extends BaseEntity {

    @Id
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

    @PrePersist
    protected void onCreate(){
        this.interviewStatus="SCHEDULED";
    }
}

package com.adroit.hotlistmicroservice.model;

import jakarta.persistence.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
public class Consultant {

    @Id
    private String consultantId;
    private String name;
    private String emailId;
    private String grade;
    private String marketingContact;
    private String personalContact;
    private String reference;
    private String recruiter;
    private String teamLead;
    private String status;
    private String passport;
    private String salesExecutive;
    private String remoteOnsite;
    private String technology;

    @OneToMany(mappedBy = "consultant", cascade = CascadeType.ALL,orphanRemoval = true)
    private List<ConsultantDocument> documents=new ArrayList<>();

    private String marketingVisa;
    private String actualVisa;
    private String experience;
    private String location;
    private LocalDate originalDOB;
    private LocalDate editedDOB;
    private String linkedInUrl;
    private String relocation;
    private String billRate;
    private String payroll;
    private LocalDate marketingStartDate;
    private String remarks;
    private LocalDateTime consultantAddedTimeStamp;
    private LocalDateTime updatedTimeStamp;


    public LocalDateTime getConsultantAddedTimeStamp() {
        return consultantAddedTimeStamp;
    }

    public void setConsultantAddedTimeStamp(LocalDateTime consultantAddedTimeStamp) {
        this.consultantAddedTimeStamp = consultantAddedTimeStamp;
    }

    public LocalDateTime getUpdatedTimeStamp() {
        return updatedTimeStamp;
    }

    public void setUpdatedTimeStamp(LocalDateTime updatedTimeStamp) {
        this.updatedTimeStamp = updatedTimeStamp;
    }

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

    public String getEmailId() {
        return emailId;
    }

    public void setEmailId(String emailId) {
        this.emailId = emailId;
    }

    public String getGrade() {
        return grade;
    }

    public void setGrade(String grade) {
        this.grade = grade;
    }

    public String getMarketingContact() {
        return marketingContact;
    }

    public void setMarketingContact(String marketingContact) {
        this.marketingContact = marketingContact;
    }

    public String getPersonalContact() {
        return personalContact;
    }

    public void setPersonalContact(String personalContact) {
        this.personalContact = personalContact;
    }

    public String getReference() {
        return reference;
    }

    public void setReference(String reference) {
        this.reference = reference;
    }

    public String getRecruiter() {
        return recruiter;
    }

    public void setRecruiter(String recruiter) {
        this.recruiter = recruiter;
    }

    public String getTeamLead() {
        return teamLead;
    }

    public void setTeamLead(String teamLead) {
        this.teamLead = teamLead;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getPassport() {
        return passport;
    }

    public void setPassport(String passport) {
        this.passport = passport;
    }

    public String getSalesExecutive() {
        return salesExecutive;
    }

    public void setSalesExecutive(String salesExecutive) {
        this.salesExecutive = salesExecutive;
    }

    public String getRemoteOnsite() {
        return remoteOnsite;
    }

    public void setRemoteOnsite(String remoteOnsite) {
        this.remoteOnsite = remoteOnsite;
    }

    public String getTechnology() {
        return technology;
    }

    public void setTechnology(String technology) {
        this.technology = technology;
    }

    public String getMarketingVisa() {
        return marketingVisa;
    }

    public void setMarketingVisa(String marketingVisa) {
        this.marketingVisa = marketingVisa;
    }

    public String getActualVisa() {
        return actualVisa;
    }

    public void setActualVisa(String actualVisa) {
        this.actualVisa = actualVisa;
    }

    public String getExperience() {
        return experience;
    }

    public void setExperience(String experience) {
        this.experience = experience;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public LocalDate getOriginalDOB() {
        return originalDOB;
    }

    public void setOriginalDOB(LocalDate originalDOB) {
        this.originalDOB = originalDOB;
    }

    public LocalDate getEditedDOB() {
        return editedDOB;
    }

    public void setEditedDOB(LocalDate editedDOB) {
        this.editedDOB = editedDOB;
    }

    public String getLinkedInUrl() {
        return linkedInUrl;
    }

    public void setLinkedInUrl(String linkedInUrl) {
        this.linkedInUrl = linkedInUrl;
    }

    public String getRelocation() {
        return relocation;
    }

    public void setRelocation(String relocation) {
        this.relocation = relocation;
    }

    public String getBillRate() {
        return billRate;
    }

    public void setBillRate(String billRate) {
        this.billRate = billRate;
    }

    public String getPayroll() {
        return payroll;
    }

    public void setPayroll(String payroll) {
        this.payroll = payroll;
    }

    public LocalDate getMarketingStartDate() {
        return marketingStartDate;
    }

    public void setMarketingStartDate(LocalDate marketingStartDate) {
        this.marketingStartDate = marketingStartDate;
    }

    public String getRemarks() {
        return remarks;
    }

    public void setRemarks(String remarks) {
        this.remarks = remarks;
    }

    public List<ConsultantDocument> getDocuments() {
        return documents;
    }

    public void setDocuments(List<ConsultantDocument> documents) {
        this.documents = documents;
    }

}

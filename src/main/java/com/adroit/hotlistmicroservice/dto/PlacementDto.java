package com.adroit.hotlistmicroservice.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor

public class PlacementDto {
    private String placementId;
    private String consultantFullName;
    private String consultantContactNo;
    private String consultantId;
    private String consultantEmailId;
    private String clientName;
    private String technology;
    private String vendorName;
    private String startDate;
    private String endDate;
    private String sales;
    private String salesExecutiveId;
    private String salesExecutiveName;
    private String salesTeamLeadId;
    private String salesTeamLeadName;
    private String recruiterId;
    private String recruiterName;
    private String recruiterTeamLead;
    private BigDecimal billRateFromClient;
    private BigDecimal billRateToConsultant;
    private BigDecimal payRate;
    private BigDecimal referals;
    private BigDecimal margin;
    private String po_with;
    private String projectBy;
    private String branch;
    private String closedMonth;
    private String employmentType;
    private String visaSubmitted;
    private String originalVisa;
    private String c2cEmployerDetails;
    private BigDecimal cashTerms;
    private BigDecimal netPay;
    private String projectStatus;
    private String remarks;
    private String statusMessage;
    private String interviewId;
    private String jobId;
    private LocalDate createdAt;
    private boolean isRegister;
    private String employeeWorkingType;
    private BigDecimal hourlyRate;

    public String getPlacementId() {
        return placementId;
    }

    public void setPlacementId(String placementId) {
        this.placementId = placementId;
    }

    public String getConsultantFullName() {
        return consultantFullName;
    }

    public void setConsultantFullName(String consultantFullName) {
        this.consultantFullName = consultantFullName;
    }

    public String getConsultantContactNo() {
        return consultantContactNo;
    }

    public void setConsultantContactNo(String consultantContactNo) {
        this.consultantContactNo = consultantContactNo;
    }

    public String getConsultantId() {
        return consultantId;
    }

    public void setConsultantId(String consultantId) {
        this.consultantId = consultantId;
    }

    public String getConsultantEmailId() {
        return consultantEmailId;
    }

    public void setConsultantEmailId(String consultantEmailId) {
        this.consultantEmailId = consultantEmailId;
    }

    public String getClientName() {
        return clientName;
    }

    public void setClientName(String clientName) {
        this.clientName = clientName;
    }

    public String getTechnology() {
        return technology;
    }

    public void setTechnology(String technology) {
        this.technology = technology;
    }

    public String getVendorName() {
        return vendorName;
    }

    public void setVendorName(String vendorName) {
        this.vendorName = vendorName;
    }

    public String getStartDate() {
        return startDate;
    }

    public void setStartDate(String startDate) {
        this.startDate = startDate;
    }

    public String getEndDate() {
        return endDate;
    }

    public void setEndDate(String endDate) {
        this.endDate = endDate;
    }

    public String getSales() {
        return sales;
    }

    public void setSales(String sales) {
        this.sales = sales;
    }

    public String getSalesExecutiveId() {
        return salesExecutiveId;
    }

    public void setSalesExecutiveId(String salesExecutiveId) {
        this.salesExecutiveId = salesExecutiveId;
    }

    public String getSalesExecutiveName() {
        return salesExecutiveName;
    }

    public void setSalesExecutiveName(String salesExecutiveName) {
        this.salesExecutiveName = salesExecutiveName;
    }

    public String getSalesTeamLeadId() {
        return salesTeamLeadId;
    }

    public void setSalesTeamLeadId(String salesTeamLeadId) {
        this.salesTeamLeadId = salesTeamLeadId;
    }

    public String getSalesTeamLeadName() {
        return salesTeamLeadName;
    }

    public void setSalesTeamLeadName(String salesTeamLeadName) {
        this.salesTeamLeadName = salesTeamLeadName;
    }

    public String getRecruiterId() {
        return recruiterId;
    }

    public void setRecruiterId(String recruiterId) {
        this.recruiterId = recruiterId;
    }

    public String getRecruiterName() {
        return recruiterName;
    }

    public void setRecruiterName(String recruiterName) {
        this.recruiterName = recruiterName;
    }

    public String getRecruiterTeamLead() {
        return recruiterTeamLead;
    }

    public void setRecruiterTeamLead(String recruiterTeamLead) {
        this.recruiterTeamLead = recruiterTeamLead;
    }

    public BigDecimal getBillRateFromClient() {
        return billRateFromClient;
    }

    public void setBillRateFromClient(BigDecimal billRateFromClient) {
        this.billRateFromClient = billRateFromClient;
    }

    public BigDecimal getBillRateToConsultant() {
        return billRateToConsultant;
    }

    public void setBillRateToConsultant(BigDecimal billRateToConsultant) {
        this.billRateToConsultant = billRateToConsultant;
    }

    public BigDecimal getPayRate() {
        return payRate;
    }

    public void setPayRate(BigDecimal payRate) {
        this.payRate = payRate;
    }

    public BigDecimal getReferals() {
        return referals;
    }

    public void setReferals(BigDecimal referals) {
        this.referals = referals;
    }

    public BigDecimal getMargin() {
        return margin;
    }

    public void setMargin(BigDecimal margin) {
        this.margin = margin;
    }

    public String getPo_with() {
        return po_with;
    }

    public void setPo_with(String po_with) {
        this.po_with = po_with;
    }

    public String getProjectBy() {
        return projectBy;
    }

    public void setProjectBy(String projectBy) {
        this.projectBy = projectBy;
    }

    public String getBranch() {
        return branch;
    }

    public void setBranch(String branch) {
        this.branch = branch;
    }

    public String getClosedMonth() {
        return closedMonth;
    }

    public void setClosedMonth(String closedMonth) {
        this.closedMonth = closedMonth;
    }

    public String getEmploymentType() {
        return employmentType;
    }

    public void setEmploymentType(String employmentType) {
        this.employmentType = employmentType;
    }

    public String getVisaSubmitted() {
        return visaSubmitted;
    }

    public void setVisaSubmitted(String visaSubmitted) {
        this.visaSubmitted = visaSubmitted;
    }

    public String getOriginalVisa() {
        return originalVisa;
    }

    public void setOriginalVisa(String originalVisa) {
        this.originalVisa = originalVisa;
    }

    public String getC2cEmployerDetails() {
        return c2cEmployerDetails;
    }

    public void setC2cEmployerDetails(String c2cEmployerDetails) {
        this.c2cEmployerDetails = c2cEmployerDetails;
    }

    public BigDecimal getCashTerms() {
        return cashTerms;
    }

    public void setCashTerms(BigDecimal cashTerms) {
        this.cashTerms = cashTerms;
    }

    public BigDecimal getNetPay() {
        return netPay;
    }

    public void setNetPay(BigDecimal netPay) {
        this.netPay = netPay;
    }

    public String getProjectStatus() {
        return projectStatus;
    }

    public void setProjectStatus(String projectStatus) {
        this.projectStatus = projectStatus;
    }

    public String getRemarks() {
        return remarks;
    }

    public void setRemarks(String remarks) {
        this.remarks = remarks;
    }

    public String getStatusMessage() {
        return statusMessage;
    }

    public void setStatusMessage(String statusMessage) {
        this.statusMessage = statusMessage;
    }

    public String getInterviewId() {
        return interviewId;
    }

    public void setInterviewId(String interviewId) {
        this.interviewId = interviewId;
    }

    public String getJobId() {
        return jobId;
    }

    public void setJobId(String jobId) {
        this.jobId = jobId;
    }

    public LocalDate getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDate createdAt) {
        this.createdAt = createdAt;
    }

    public boolean isRegister() {
        return isRegister;
    }

    public void setRegister(boolean register) {
        isRegister = register;
    }

    public String getEmployeeWorkingType() {
        return employeeWorkingType;
    }

    public void setEmployeeWorkingType(String employeeWorkingType) {
        this.employeeWorkingType = employeeWorkingType;
    }

    public BigDecimal getHourlyRate() {
        return hourlyRate;
    }

    public void setHourlyRate(BigDecimal hourlyRate) {
        this.hourlyRate = hourlyRate;
    }
}

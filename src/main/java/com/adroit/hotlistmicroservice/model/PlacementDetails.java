package com.adroit.hotlistmicroservice.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

@Setter
@Getter
@Entity
@Table(name = "placements_us")
@NoArgsConstructor
@AllArgsConstructor
public class PlacementDetails {

    @Id
    @Column(name = "placementId", updatable = false, nullable = false)
    private String placementId;

    @Column(name = "consultantFullName")
    private String consultantFullName;

    @Pattern(regexp = "^\\d{10}$", message = "contactNumber must be 10 digits")
    @NotBlank(message = "contact number is required")
    @Column(name = "consultantContactNo")
    private String consultantContactNo;

    @Column(name = "technology")
    private String technology;

    @Column(name = "client_name")
    private String clientName;

    @Column(name = "consultantId")
    private String consultantId;

    @Column(name = "consultantEmailId")
    private String consultantEmailId;

    @Column(name = "vendor_name")
    private String vendorName;

    @Column(name = "start_date")
    private LocalDate startDate;

    @Column(name = "end_date")
    private LocalDate endDate;

    @Column(name = "sales")
    private String sales;

    @Column(name = "sales_executive_id")
    private String salesExecutiveId;

    @Column(name = "sales_executive_name")
    private String salesExecutiveName;

    @Column(name = "sales_team_lead_id")
    private String salesTeamLeadId;

    @Column(name = "sales_team_lead_name")
    private String salesTeamLeadName;

    @Column(name = "recruiter_id")
    private String recruiterId;

    @Column(name = "recruiter_name")
    private String recruiterName;

    @Column(name = "recruiter_team_lead")
    private String recruiterTeamLead;

    @Column(name = "bill_rate_from_client")
    private BigDecimal billRateFromClient;

    @Column(name = "bill_rate_to_consultant")
    private BigDecimal billRateToConsultant;

    @Column(name = "pay_rate")
    private BigDecimal payRate;

    @Column(name = "referals")
    private BigDecimal referals;

    @Column(name = "margin")
    private BigDecimal margin;

    @Column(name = "po_with")
    private String po_with;

    @Column(name = "project_by")
    private String projectBy;

    @Column(name = "branch")
    private String branch;

    @Column(name = "closed_month")
    private String closedMonth;

    @Column(name = "employment_type")
    private String employmentType;

    @Column(name = "visa_submitted")
    private String visaSubmitted;

    @Column(name = "original_visa")
    private String originalVisa;

    @Column(name = "c2c_employer_details")
    private String c2cEmployerDetails;

    @Column(name = "cash_terms")
    @DecimalMin(value = "0.0", inclusive = true, message = "Cash Terms must be non-negative")
    @Digits(integer = 5, fraction = 2, message = "Invalid format for Cash Terms")
    private BigDecimal cashTerms; // store as percentage value (e.g., 10 for 10%)

    @Column(name = "net_pay", precision = 12, scale = 2)
    private BigDecimal netPay;

    @Column(name = "remarks")
    private String remarks;

    @Column(name = "project_status")
    private String projectStatus = "";

    @Column(name = "status_message")
    private String statusMessage;

    @Column(name = "created_At")
    private LocalDate createdAt;

    @Column(name = "interview_id")
    private String interviewId;

    @Column(name = "isRegister")
    private Boolean isRegister = false;

    @DecimalMin(value = "0.0", inclusive = false, message = "Pay Rate must be a positive number")
    @Digits(integer = 10, fraction = 5, message = "Invalid format for Pay Rate")
    @Column(name = "hourly_rate")
    private BigDecimal hourlyRate;

    @PrePersist
    @PreUpdate
    public void updateFields() {
        if (createdAt == null) {
            createdAt = LocalDate.now();
        }

        if (endDate != null) {
            // Format closedMonth as "MMMM-yyyy", e.g., "October-2025"
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMMM-yyyy");
            closedMonth = endDate.format(formatter);
        } else {
            closedMonth = null;
        }
    }

    @Transient
    private boolean isLogin;

    @Column(name = "employee_working_type")
    private String employeeWorkingType = "MONTHLY"; // Default value

    // getter and setter for isLogin

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

    public String getTechnology() {
        return technology;
    }

    public void setTechnology(String technology) {
        this.technology = technology;
    }

    public String getClientName() {
        return clientName;
    }

    public void setClientName(String clientName) {
        this.clientName = clientName;
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

    public String getVendorName() {
        return vendorName;
    }

    public void setVendorName(String vendorName) {
        this.vendorName = vendorName;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public void setStartDate(LocalDate startDate) {
        this.startDate = startDate;
    }

    public LocalDate getEndDate() {
        return endDate;
    }

    public void setEndDate(LocalDate endDate) {
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

    public String getRemarks() {
        return remarks;
    }

    public void setRemarks(String remarks) {
        this.remarks = remarks;
    }

    public String getProjectStatus() {
        return projectStatus;
    }

    public void setProjectStatus(String projectStatus) {
        this.projectStatus = projectStatus;
    }

    public String getStatusMessage() {
        return statusMessage;
    }

    public void setStatusMessage(String statusMessage) {
        this.statusMessage = statusMessage;
    }

    public LocalDate getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDate createdAt) {
        this.createdAt = createdAt;
    }

    public String getInterviewId() {
        return interviewId;
    }

    public void setInterviewId(String interviewId) {
        this.interviewId = interviewId;
    }

    public Boolean getRegister() {
        return isRegister;
    }

    public void setRegister(Boolean register) {
        isRegister = register;
    }

    public BigDecimal getHourlyRate() {
        return hourlyRate;
    }

    public void setHourlyRate(BigDecimal hourlyRate) {
        this.hourlyRate = hourlyRate;
    }

    public boolean isLogin() {
        return isLogin;
    }

    public void setLogin(boolean login) {
        isLogin = login;
    }

    public String getEmployeeWorkingType() {
        return employeeWorkingType;
    }

    public void setEmployeeWorkingType(String employeeWorkingType) {
        this.employeeWorkingType = employeeWorkingType;
    }
}


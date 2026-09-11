package com.adroit.hotlistmicroservice.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class EmailNotificationUtil {

    private static final Logger logger = LoggerFactory.getLogger(EmailNotificationUtil.class);

    private final EmailService emailService;

    public EmailNotificationUtil(EmailService emailService) {
        this.emailService = emailService;
    }

    public void send(String recipients, String subject, String emailBody) {
        if (recipients == null || recipients.isBlank()) {
            logger.warn("Skipping email '{}': empty recipient", subject);
            return;
        }
        emailService.sendHtmlEmail(recipients, subject, emailBody);
    }
    public String safeValue(String value){
        return (value==null || value.trim().isEmpty()) ? "NA":value;
    }

    public void sendConsultantApprovalRequestEmail(
            Map<String,String> recipientEmails, String consultantId, String consultantName,
            String technology,String teamLead,String salesExecutive,String recruiter, String requestedUserName) {

        String subject = "Action Required: Consultant Approval Pending";
        String messageContent = "A new consultant approval request has been raised by <strong>" + requestedUserName + "</strong>.<br>"
                + "<strong>Consultant Details:</strong><br>"
                + "<ul style='list-style-type:none; padding:0;'>"
                + "<li><strong>ID:</strong> " + consultantId + "</li>"
                + "<li><strong>Name:</strong> " + safeValue(consultantName) + "</li>"
                + "<li><strong>Technology:</strong> " + safeValue(technology) + "</li>"
                + "<li><strong>TEAM LEAD:</strong> " + safeValue(teamLead) + "</li>"
                + "<li><strong>SALES EXECUTIVE:</strong> " + safeValue(salesExecutive) + "</li>"
                + "<li><strong>RECRUITER:</strong> " + safeValue(recruiter) + "</li>"
                + "</ul>"
                + "Please review and take the necessary action.";

        sendToRecipients(recipientEmails, subject, messageContent);
    }

    public void sendConsultantApprovedEmail(
            Map<String,String> recipientEmails, String consultantId, String consultantName,
            String technology,String teamLead,String salesExecutive,String recruiter,String approvedBy) {
        String subject = "Consultant Approval Notification";
        String messageContent = "<p>Your consultant profile has been <strong>APPROVED</strong> in the Mulya Portal.</p>"
                + "<p><strong>Consultant Details:</strong></p>"
                + "<ul style='list-style-type:none; padding:0;'>"
                + "<li><strong>ID:</strong> " + consultantId + "</li>"
                + "<li><strong>Name:</strong> " + safeValue(consultantName) + "</li>"
                + "<li><strong>Technology:</strong> " + safeValue(technology) + "</li>"
                + "<li><strong>TEAM LEAD:</strong> " + safeValue(teamLead) + "</li>"
                + "<li><strong>SALES EXECUTIVE:</strong> " + safeValue(salesExecutive) + "</li>"
                + "<li><strong>RECRUITER:</strong> " + safeValue(recruiter) + "</li>"
                + "</ul>"
                + "<p>This approval was completed by <strong>" + approvedBy + "</strong>.</p>"
                + "<p>Please log in to the portal for further details.</p>";

        sendToRecipients(recipientEmails, subject, messageContent);
    }
    public void sendConsultantRejectedEmail(
            Map<String,String> recipientEmails, String consultantId, String consultantName,
            String technology,String teamLead,String salesExecutive,String recruiter,String rejectedBy) {
        String subject = "Consultant Approval Notification";
        String messageContent = "<p>Your consultant profile has been <strong>REJECTED</strong> in the Mulya Portal.</p>"
                + "<p><strong>Consultant Details:</strong></p>"
                + "<ul style='list-style-type:none; padding:0;'>"
                + "<li><strong>ID:</strong> " + consultantId + "</li>"
                + "<li><strong>Name:</strong> " + safeValue(consultantName) + "</li>"
                + "<li><strong>Technology:</strong> " + safeValue(technology) + "</li>"
                + "<li><strong>TEAM LEAD:</strong> " + safeValue(teamLead) + "</li>"
                + "<li><strong>SALES EXECUTIVE:</strong> " + safeValue(salesExecutive) + "</li>"
                + "<li><strong>RECRUITER:</strong> " + safeValue(recruiter) + "</li>"
                + "</ul>"
                + "<p>This approval was rejected by <strong>" + rejectedBy + "</strong>.</p>"
                + "<p>Please log in to the portal for further details.</p>";

        sendToRecipients(recipientEmails, subject, messageContent);
    }
    public void notifyTeamForApprovedConsultant(
            Map<String, String> recipientEmails, String consultantId, String consultantName,
            String technology, String teamLead, String salesExecutive, String recruiter, String approvedBy) {

        String subject = "Consultant Approved – Start Marketing Candidate";

        String messageContent = "<p>The consultant profile has been <strong>APPROVED</strong> by <strong>"
                + safeValue(approvedBy) + "</strong> in the MyMulya Portal.</p>"
                + "<p><strong>Consultant Details:</strong></p>"
                + "<ul style='list-style-type:none; padding:0;'>"
                + "<li><strong>ID:</strong> " + safeValue(consultantId) + "</li>"
                + "<li><strong>Name:</strong> " + safeValue(consultantName) + "</li>"
                + "<li><strong>Technology:</strong> " + safeValue(technology) + "</li>"
                + "<li><strong>Team Lead:</strong> " + safeValue(teamLead) + "</li>"
                + "<li><strong>Sales Executive:</strong> " + safeValue(salesExecutive) + "</li>"
                + "<li><strong>Recruiter:</strong> " + safeValue(recruiter) + "</li>"
                + "</ul>"
                + "<p><strong>Next Step:</strong> Please start marketing this candidate profile to clients immediately.</p>"
                + "<p>Login to the portal for full details.</p>";

        sendToRecipients(recipientEmails, subject, messageContent);
    }




    private void sendToRecipients(Map<String, String> recipientEmails, String subject, String messageContent) {
        if (recipientEmails == null || recipientEmails.isEmpty()) {
            logger.warn("No recipients for email '{}'", subject);
            return;
        }
        recipientEmails.forEach((name, email) -> {
            if (email == null || email.isBlank()) {
                logger.warn("Skipping email '{}' for {}: empty address", subject, name);
                return;
            }
            try {
                send(email, subject, buildMulyaTemplate(name, messageContent));
                logger.info("Sent '{}' to {}", subject, email);
            } catch (Exception e) {
                logger.error("Failed to send '{}' to {}: {}", subject, email, e.getMessage(), e);
            }
        });
    }

    // Building Template
    private String buildMulyaTemplate(String username, String messageContent) {
        return "<!DOCTYPE html>"
                + "<html><head><style>"
                + "body {font-family: 'Helvetica Neue', Arial, sans-serif; background-color: #f6f8fa; margin:0; padding:0;}"
                + ".email-wrapper {width:100%; background-color:#f6f8fa; padding:40px 0;}"
                + ".email-container {max-width:600px; background:#ffffff; margin:auto; border-radius:12px; "
                + "box-shadow:0 2px 12px rgba(0,0,0,0.08); border:1px solid #eaeaea; overflow:hidden;}"
                + ".email-header {background:#6a1b9a; color:#ffffff; text-align:center; padding:18px; font-size:1.4rem; "
                + "font-weight:bold; letter-spacing:1px;}"
                + ".email-body {padding:24px; color:#2a3357; font-size:1rem; line-height:1.6;}"
                + ".email-body h2 {color:#0056b3; font-size:1.2rem; margin-bottom:12px;}"
                + ".action-btn {background:#6a1b9a; color:white !important; font-size:1rem; padding:12px 30px; "
                + "border-radius:6px; text-decoration:none; font-weight:600; display:inline-block; margin-top:20px;}"
                + ".action-btn:hover {background:#4a148c;}"
                + ".email-footer {background:#f1f3f6; text-align:center; padding:16px; font-size:0.9rem; color:#5e6b8b; "
                + "border-top:1px solid #ddd;}"
                + "</style></head><body>"
                + "<div class='email-wrapper'><div class='email-container'>"
                + "<div class='email-header'>MYMULYA PORTAL</div>"
                + "<div class='email-body'>"
                + "<h2>Hi " + username + ",</h2>"
                + "<p>" + messageContent + "</p>"
                + "<a href='https://mymulya.com' class='action-btn'>Go to Dashboard</a>"
                + "</div>"
                + "<div class='email-footer'>This is an automated message from Mulya Portal. Please do not reply.<br>"
                + "&copy; 2025 Mulya. All rights reserved.</div>"
                + "</div></div>"
                + "</body></html>";
    }



}

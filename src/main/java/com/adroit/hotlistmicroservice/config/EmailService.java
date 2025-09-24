package com.adroit.hotlistmicroservice.config;

import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class EmailService {

    @Autowired
    JavaMailSender javaMailSender;
    @Value("${spring.mail.username}")
    private String senderEmail;

    public void sendHtmlEmail(/*List<String> toList */ String email, String subject, String htmlBody){

        try {
            MimeMessage mimeMessage = javaMailSender.createMimeMessage();
            MimeMessageHelper mimeMessageHelper = new MimeMessageHelper(mimeMessage, true, "UTF-8");

            mimeMessageHelper.setFrom(senderEmail);
            //mimeMessageHelper.setTo(toList.toArray(new String[0]));
            mimeMessageHelper.setTo(email);
            mimeMessageHelper.setSubject(subject);
            mimeMessageHelper.setText(htmlBody,true);

            javaMailSender.send(mimeMessage);

        }catch (Exception e){
            throw new RuntimeException("Failed to send email to " + email, e);
        }
    }



}

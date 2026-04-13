package com.security.security.service;


import lombok.AllArgsConstructor;

import jakarta.mail.internet.MimeMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ClassPathResource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import com.security.security.models.Users;
import com.security.security.repositories.UserRepo;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;

@Service
@Slf4j
@AllArgsConstructor
public class EmailService {

    private static final String WELCOME_TEMPLATE_PATH = "templates/email/welcome.html";
    private static final String WELCOME_IMAGE_PNG = "static/images/email/welcome-banner.png";
    private static final String WELCOME_IMAGE_CID = "welcomeImage";

    private final JavaMailSender mailSender;
    private UserRepo userRepo;

    @Async("mailExecutor")
    public void sendHtmlEmail(String to, String userName) {

        try {
            log.info("config email start");
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, StandardCharsets.UTF_8.name());

            helper.setTo(to);
            helper.setSubject("Welcome to Our SpringBoot Application Platform");

            String htmlContent = loadWelcomeTemplate(userName);
            helper.setText(htmlContent, true);

            // Embed Spring Boot logo as PNG (Gmail and most clients don't display inline SVG)
            ClassPathResource imageResource = new ClassPathResource(WELCOME_IMAGE_PNG);
            if (imageResource.exists()) {
                helper.addInline(WELCOME_IMAGE_CID, imageResource, "image/png");
            }

            mailSender.send(message);


            log.info("Email sent successfully to {}", to);

        } catch (Exception e) {
            log.error("Failed to send email to {}", to, e);
        }
    }

    private String loadWelcomeTemplate(String userName) throws IOException {
        ClassPathResource resource = new ClassPathResource(WELCOME_TEMPLATE_PATH);
        String content = new String(resource.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
        return content.replace("{{userName}}", userName != null ? userName : "User");
    }

    @Scheduled(cron = "0 */3 * * * ?") // Every 3minute for testing, change to "0 0 9 * * ?" for daily at 9 AM
    public void sendGreetingEmail() {
        List<Users> users = userRepo.findAll();
        try {
            for (Users user : users) {
                sendHtmlEmail(user.getEmail(), user.getName());
                log.info("Scheduled email sent to {}->{}", user.getName(), user.getEmail());
            }
        } catch (Exception e) {
            log.error("Failed to send email to users", e);
        }
    }
}

package com.skillbridge.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import jakarta.mail.internet.MimeMessage;

/**
 * Wraps JavaMailSender so the app never crashes if SMTP isn't configured -
 * common in local/demo environments. Falls back to logging the email instead.
 */
@Service
@Slf4j
public class EmailService {

    private final JavaMailSender mailSender;

    @Value("${app.mail.from}")
    private String from;

    @Value("${spring.mail.host:}")
    private String mailHost;

    public EmailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    public void send(String to, String subject, String htmlBody) {
        if (mailHost == null || mailHost.isBlank()) {
            log.info("[mail:stub] To: {} | Subject: {}\n{}", to, subject, htmlBody);
            return;
        }
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true);
            helper.setFrom(from);
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(htmlBody, true);
            mailSender.send(message);
        } catch (Exception ex) {
            log.warn("Failed to send email to {} - logging instead. Reason: {}", to, ex.getMessage());
            log.info("[mail:fallback] To: {} | Subject: {}\n{}", to, subject, htmlBody);
        }
    }

    public String interviewScheduledEmail(String studentName, String internshipTitle, String company, String when, String mode) {
        return """
            <div style="font-family:sans-serif;max-width:480px;">
              <h2>Interview Scheduled</h2>
              <p>Hi %s,</p>
              <p>Your interview for <strong>%s</strong> at <strong>%s</strong> has been scheduled.</p>
              <ul>
                <li><strong>When:</strong> %s</li>
                <li><strong>Mode:</strong> %s</li>
              </ul>
              <p>Good luck!</p>
              <p style="color:#888;font-size:12px;">— SkillBridge</p>
            </div>
            """.formatted(studentName, internshipTitle, company, when, mode);
    }
}

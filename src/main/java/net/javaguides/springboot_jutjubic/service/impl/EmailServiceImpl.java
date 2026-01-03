package net.javaguides.springboot_jutjubic.service.impl;

import net.javaguides.springboot_jutjubic.service.EmailService;
import net.javaguides.springboot_jutjubic.service.RoleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
public class EmailServiceImpl implements EmailService {

    @Autowired
    private JavaMailSender javaMailSender;

    @Autowired
    private Environment env;

    @Async
    public void sendVerificationEmail(String email, String verificationToken, String userName)
    {
        String subject = "Welcome " + userName + "! Verify your email for Jutjubic";

        sendEmail(email, userName, verificationToken, subject);
    }

    private void sendEmail(String email, String username, String token, String subject)
    {
        try {
            String verificationLink = "http://localhost:8082/auth/activate?token=" + token;

            String text = """
                Hello %s,

                Thank you for registering with Jutjubic!

                Please verify your email address by clicking the link below:

                %s

                If you did not create an account, you can safely ignore this email.

                Regards,
                Jutjubic Team

                ----------------------------------
                This is an automated message. Please do not reply.
                """.formatted(username, verificationLink);

            SimpleMailMessage mail = new SimpleMailMessage();
            mail.setFrom(env.getProperty("spring.mail.username"));
            mail.setTo(email);
            mail.setSubject(subject);
            mail.setText(text);

            javaMailSender.send(mail);

        } catch (Exception e) {
            System.err.println("Error sending email: " + e.getMessage());
        }
    }

}

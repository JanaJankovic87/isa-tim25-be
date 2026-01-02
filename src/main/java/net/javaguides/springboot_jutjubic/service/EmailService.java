package net.javaguides.springboot_jutjubic.service;

import java.util.List;
import net.javaguides.springboot_jutjubic.model.User;

public interface EmailService {
    void sendVerificationEmail(String email, String verificationToken, String userName);
}

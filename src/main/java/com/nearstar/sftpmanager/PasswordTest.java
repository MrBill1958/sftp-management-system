package com.nearstar.sftpmanager;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

public class PasswordTest {
    public static void main(String[] args) {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        String rawPassword = "admin123";
        String encodedPassword = encoder.encode(rawPassword);
        System.out.println("Encoded password for 'admin123': " + encodedPassword);

        // Test the hash we're using
        String existingHash = "$2a$10$otQ68nq7zGM.GSbsj0uCgevlitwiY7FAQqzaed/aQGjJNnZAz0mDG";
        boolean matches = encoder.matches(rawPassword, existingHash);
        System.out.println("Does 'admin123' match the existing hash? " + matches);
    }
}
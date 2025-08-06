package com.nearstar.sftpmanager.util;

import org.jasypt.encryption.StringEncryptor;
import org.jasypt.encryption.pbe.StandardPBEStringEncryptor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;  // Changed from javax.annotation.PostConstruct

@Component
public class EncryptionUtil {

    @Value("${encryption.password:defaultSecretKey}")
    private String encryptionPassword;

    private StringEncryptor encryptor;

    @PostConstruct
    public void init() {
        StandardPBEStringEncryptor encryptor = new StandardPBEStringEncryptor();
        encryptor.setPassword(encryptionPassword);
        encryptor.setAlgorithm("PBEWithMD5AndDES");
        this.encryptor = encryptor;
    }

    public String encrypt(String plainText) {
        return encryptor.encrypt(plainText);
    }

    public String decrypt(String encryptedText) {
        return encryptor.decrypt(encryptedText);
    }
}
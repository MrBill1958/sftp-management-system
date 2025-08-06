package com.nearstar.sftpmanager.util;

import java.io.InputStream;
import java.security.MessageDigest;

public class FileChecksum {

    public static String calculateMD5(InputStream inputStream) throws Exception {
        MessageDigest md = MessageDigest.getInstance("MD5");
        byte[] buffer = new byte[8192];
        int read;

        while ((read = inputStream.read(buffer)) > 0) {
            md.update(buffer, 0, read);
        }

        byte[] md5sum = md.digest();
        StringBuilder result = new StringBuilder();

        for (byte b : md5sum) {
            result.append(String.format("%02x", b));
        }

        return result.toString();
    }

    public static String calculateSHA256(InputStream inputStream) throws Exception {
        MessageDigest md = MessageDigest.getInstance("SHA-256");
        byte[] buffer = new byte[8192];
        int read;

        while ((read = inputStream.read(buffer)) > 0) {
            md.update(buffer, 0, read);
        }

        byte[] sha256sum = md.digest();
        StringBuilder result = new StringBuilder();

        for (byte b : sha256sum) {
            result.append(String.format("%02x", b));
        }

        return result.toString();
    }
}
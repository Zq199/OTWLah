/**
 * The SecurityController class provides security-related functionality within the OTA Lah app.
 * It includes methods for working with encryption and data security.
 */
package com.project.sc2006.controllers;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class SecurityController {
    public static final String salt = "SC2006";

    /**
     * Converts a byte array to a hexadecimal string representation.
     *
     * @param b The byte array to convert to a hexadecimal string.
     * @return A hexadecimal string representing the byte array.
     */
    public static String byteArrayToHexString(byte[] b) {
        String result = "";
        for (int i = 0; i < b.length; i++) {
            result +=
                    Integer.toString((b[i] & 0xff) + 0x100, 16).substring(1);
        }
        return result;
    }

    /**
     * Encrypts a raw password for enhanced security.
     *
     * @param rawPassword The raw password to be encrypted.
     * @return An encrypted version of the raw password.
     */
    static public String encryptPassword(String rawPassword) {
        MessageDigest md = null;
        try {
            md = MessageDigest.getInstance("SHA-1");
        }
        catch(NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return byteArrayToHexString(md.digest((rawPassword + salt).getBytes()));
    }
}

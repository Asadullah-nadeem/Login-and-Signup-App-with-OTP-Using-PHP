//package com.example.myapplication.adapters.utils;
//
//import java.security.MessageDigest;
//import java.security.NoSuchAlgorithmException;
//
//public class PasswordUtils {
//    public static String hashPassword(String password) {
//        try {
//            MessageDigest md = MessageDigest.getInstance("SHA-256");
//            byte[] hashedBytes = md.digest(password.getBytes());
//            return bytesToHex(hashedBytes);
//        } catch (NoSuchAlgorithmException e) {
//            throw new RuntimeException("Password hashing failed", e);
//        }
//    }
//
//    private static String bytesToHex(byte[] bytes) {
//        StringBuilder result = new StringBuilder();
//        for (byte b : bytes) {
//            result.append(Integer.toString((b & 0xff) + 0x100, 16).substring(1);
//        }
//        return result.toString();
//    }
//}
package com.example.myapplication.adapters.utils;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;


public class PasswordUtils {
    public static String hashPassword(String password) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hashedBytes = md.digest(password.getBytes());
            StringBuilder sb = new StringBuilder();
            for (byte b : hashedBytes) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Password hashing failed", e);
        }
    }

    private static String bytesToHex(byte[] bytes) {
        StringBuilder result = new StringBuilder();
        for (byte b : bytes) {
            result.append(Integer.toString((b & 0xff) + 0x100, 16).substring(1));
        }
        return result.toString();
    }
}
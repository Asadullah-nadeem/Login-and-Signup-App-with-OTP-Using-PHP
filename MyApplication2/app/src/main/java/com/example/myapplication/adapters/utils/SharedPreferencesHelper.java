package com.example.myapplication.adapters.utils;

import android.content.Context;
import android.content.SharedPreferences;
import java.util.Date;

public class SharedPreferencesHelper {
    private static final String PREFS_NAME = "AuthAppPrefs";
    private static final String KEY_IS_LOGGED_IN = "isLoggedIn";
    private static final String KEY_EMAIL = "email";
    private static final String KEY_FULL_NAME = "fullName";
    private static final String KEY_LOGIN_ATTEMPTS = "loginAttempts";
    private static final String KEY_ACCOUNT_LOCKED_UNTIL = "accountLockedUntil";
    private static final String KEY_OTP_ATTEMPTS = "otpAttempts";
    private static final String KEY_LAST_OTP_ATTEMPT = "lastOtpAttempt";
    private static final String KEY_RESET_TOKEN = "resetToken";
    private static final String KEY_RESET_TOKEN_EXPIRY = "resetTokenExpiry";

    private final SharedPreferences preferences;

    public SharedPreferencesHelper(Context context) {
        preferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    }

    // User Session Management
    public void saveUserData(String email, String fullName) {
        preferences.edit()
                .putBoolean(KEY_IS_LOGGED_IN, true)
                .putString(KEY_EMAIL, email)
                .putString(KEY_FULL_NAME, fullName)
                .apply();
    }

    public boolean isLoggedIn() {
        return preferences.getBoolean(KEY_IS_LOGGED_IN, false);
    }

    public String getEmail() {
        return preferences.getString(KEY_EMAIL, "");
    }

    public String getFullName() {
        return preferences.getString(KEY_FULL_NAME, "");
    }

    // Security Features
    public void updateLoginAttempts(int attempts) {
        preferences.edit()
                .putInt(KEY_LOGIN_ATTEMPTS, attempts)
                .putLong(KEY_LAST_OTP_ATTEMPT, new Date().getTime())
                .apply();
    }

    public int getLoginAttempts() {
        return preferences.getInt(KEY_LOGIN_ATTEMPTS, 0);
    }

    public void setAccountLock(Date lockUntil) {
        preferences.edit()
                .putLong(KEY_ACCOUNT_LOCKED_UNTIL, lockUntil.getTime())
                .apply();
    }

    public Date getAccountLockTime() {
        return new Date(preferences.getLong(KEY_ACCOUNT_LOCKED_UNTIL, 0));
    }

    public boolean isAccountLocked() {
        return new Date().before(getAccountLockTime());
    }

    public void updateOtpAttempts(int attempts) {
        preferences.edit()
                .putInt(KEY_OTP_ATTEMPTS, attempts)
                .putLong(KEY_LAST_OTP_ATTEMPT, new Date().getTime())
                .apply();
    }

    public int getOtpAttempts() {
        return preferences.getInt(KEY_OTP_ATTEMPTS, 0);
    }

    public Date getLastOtpAttemptTime() {
        return new Date(preferences.getLong(KEY_LAST_OTP_ATTEMPT, 0));
    }

    // Password Reset Management
    public void saveResetToken(String token, Date expiry) {
        preferences.edit()
                .putString(KEY_RESET_TOKEN, token)
                .putLong(KEY_RESET_TOKEN_EXPIRY, expiry.getTime())
                .apply();
    }

    public String getResetToken() {
        return preferences.getString(KEY_RESET_TOKEN, null);
    }

    public boolean isResetTokenValid() {
        return new Date().before(new Date(preferences.getLong(KEY_RESET_TOKEN_EXPIRY, 0)));
    }

    // Clear Data
    public void clearUserData() {
        preferences.edit()
                .clear()
                .apply();
    }
    private static final String KEY_USER_ID = "user_id";

    public void saveUserId(int userId) {
        preferences.edit().putInt(KEY_USER_ID, userId).apply();
    }

    public int getUserId() {
        return preferences.getInt(KEY_USER_ID, -1);
    }

    public void clearSecurityFlags() {
        preferences.edit()
                .remove(KEY_LOGIN_ATTEMPTS)
                .remove(KEY_ACCOUNT_LOCKED_UNTIL)
                .remove(KEY_OTP_ATTEMPTS)
                .remove(KEY_LAST_OTP_ATTEMPT)
                .remove(KEY_RESET_TOKEN)
                .remove(KEY_RESET_TOKEN_EXPIRY)
                .remove(KEY_USER_ID)
                .apply();
    }

    public void partialClearForReauth() {
        preferences.edit()
                .remove(KEY_IS_LOGGED_IN)
                .remove(KEY_EMAIL)
                .remove(KEY_FULL_NAME)
                .apply();
    }

    public void setLoggedIn(boolean isLoggedIn) {
        preferences.edit()
                .putBoolean(KEY_IS_LOGGED_IN, isLoggedIn)
                .apply();
    }
}
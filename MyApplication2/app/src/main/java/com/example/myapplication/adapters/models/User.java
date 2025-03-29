package com.example.myapplication.adapters.models;

import com.google.gson.annotations.SerializedName;
import java.util.Date;

public class User {
    @SerializedName("id")
    private int id;

    @SerializedName("full_name")
    private String fullName;

    @SerializedName("email")
    private String email;

    @SerializedName("otp")
    private String otp;

    @SerializedName("otp_expiry")
    private Date otpExpiry;

    @SerializedName("otp_attempts")
    private int otpAttempts;

    @SerializedName("last_otp_attempt")
    private Date lastOtpAttempt;

    @SerializedName("is_verified")
    private boolean isVerified;

    @SerializedName("login_attempts")
    private int loginAttempts;

    @SerializedName("account_locked_until")
    private Date accountLockedUntil;

    @SerializedName("reset_token")
    private String resetToken;

    @SerializedName("reset_token_expiry")
    private Date resetTokenExpiry;

    // Getters
    public int getId() { return id; }
    public String getFullName() { return fullName; }
    public String getEmail() { return email; }
    public String getOtp() { return otp; }
    public Date getOtpExpiry() { return otpExpiry; }
    public int getOtpAttempts() { return otpAttempts; }
    public Date getLastOtpAttempt() { return lastOtpAttempt; }
    public boolean isVerified() { return isVerified; }
    public int getLoginAttempts() { return loginAttempts; }
    public Date getAccountLockedUntil() { return accountLockedUntil; }
    public String getResetToken() { return resetToken; }
    public Date getResetTokenExpiry() { return resetTokenExpiry; }
}
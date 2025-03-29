package com.example.myapplication.adapters.models;

import com.google.gson.annotations.SerializedName;
import java.util.Date;

public class ApiResponse<T> {
    @SerializedName("status")
    private String status;
//    @SerializedName("status")
//    private boolean success;

    @SerializedName("message")
    private String message;

    @SerializedName("data")
    private T data;

    @SerializedName("attempts_left")
    private int attemptsLeft;

    @SerializedName("locked_until")
    private Date lockedUntil;

    @SerializedName("otp_attempts")
    private int otpAttempts;

    // Getters and Setters
//    public boolean isSuccess() { return success; }
    public boolean isSuccess() {
        return "success".equalsIgnoreCase(status); // Check if status is "success"
    }
    // Add proper null checks in getters
    public String getStatus() {
        return status != null ? status : "error";
    }
    public String getMessage() {
        return message != null ? message : "";
    }
    public T getData() { return data; }
    public int getAttemptsLeft() { return attemptsLeft; }
    public Date getLockedUntil() { return lockedUntil; }
    public int getOtpAttempts() { return otpAttempts; }

//    @Override
//    public String toString() {
//        return "ApiResponse{status='" + status + "', message='" + message + "', data=" + data + "}";
//    }
    @Override
    public String toString() {
        return "ApiResponse{" +
                "status='" + status + '\'' +
                ", message='" + message + '\'' +
                ", data=" + data +
                ", attemptsLeft=" + attemptsLeft +
                ", lockedUntil=" + lockedUntil +
                ", otpAttempts=" + otpAttempts +
                '}';
    }
}
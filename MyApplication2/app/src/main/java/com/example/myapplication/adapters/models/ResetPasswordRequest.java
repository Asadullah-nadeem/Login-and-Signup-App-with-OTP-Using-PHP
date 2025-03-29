package com.example.myapplication.adapters.models;

import com.example.myapplication.adapters.utils.PasswordUtils;
import com.google.gson.annotations.SerializedName;

public class ResetPasswordRequest {
    @SerializedName("token")
    private final String token;

    @SerializedName("password")
    private final String password;

    @SerializedName("confirm_password")
    private final String confirmPassword;

    public ResetPasswordRequest(String token, String password, String confirmPassword) {
        this.token = token;
        this.password = PasswordUtils.hashPassword(password);
        this.confirmPassword = PasswordUtils.hashPassword(confirmPassword);
    }
}

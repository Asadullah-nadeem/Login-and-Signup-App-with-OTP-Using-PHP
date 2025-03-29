package com.example.myapplication.adapters.models;

import com.example.myapplication.adapters.utils.PasswordUtils;
import com.google.gson.annotations.SerializedName;

public class SignupRequest {
    @SerializedName("full_name")
    private final String fullName;

    @SerializedName("email")
    private final String email;

    @SerializedName("password")
    private final String password;

    public SignupRequest(String fullName, String email, String password) {
        this.fullName = fullName;
        this.email = email;
        this.password = PasswordUtils.hashPassword(password);
    }
}





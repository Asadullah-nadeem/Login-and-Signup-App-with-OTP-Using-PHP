package com.example.myapplication.adapters.models;

import com.google.gson.annotations.SerializedName;

public class OtpRequest {
    @SerializedName("email")
    private final String email;

    @SerializedName("otp")
    private final String otp;

    public OtpRequest(String email, String otp) {
        this.email = email;
        this.otp = otp;
    }
}


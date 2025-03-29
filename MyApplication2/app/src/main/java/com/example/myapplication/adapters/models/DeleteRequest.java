package com.example.myapplication.adapters.models;

import com.google.gson.annotations.SerializedName;

public class DeleteRequest {
    @SerializedName("id")
    private final int userId;

    public DeleteRequest(int userId) {
        this.userId = userId;
    }
}

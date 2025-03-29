package com.example.myapplication.adapters.utils;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import com.example.myapplication.adapters.models.ApiResponse;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import retrofit2.HttpException;

public class ApiErrorHandler {
    private static final String TAG = "ApiErrorHandler";
    private static final SimpleDateFormat TIME_FORMAT =
            new SimpleDateFormat("hh:mm a", Locale.getDefault());

    public static void handleError(Context context, Throwable t, ApiResponse<?> response) {
        Log.e(TAG, "API Error: ", t);

        if (context == null) return;

        if (t instanceof IOException) {
            showMessage(context, "Network error. Please check your internet connection.");
        } else if (t instanceof HttpException) {
            handleHttpError(context, (HttpException) t);
        } else if (response != null) {
            handleApiError(context, response);
        } else {
            showMessage(context, "Something went wrong. Please try again.");
        }
    }

    private static void handleHttpError(Context context, HttpException exception) {
        String errorMessage;
        switch (exception.code()) {
            case 401:
                errorMessage = "Session expired. Please login again.";
                break;
            case 403:
                errorMessage = "Access denied. Insufficient permissions.";
                break;
            case 404:
                errorMessage = "Resource not found.";
                break;
            case 500:
                errorMessage = "Server error. Please try again later.";
                break;
            default:
                errorMessage = "HTTP Error: " + exception.code();
        }
        showMessage(context, errorMessage);
    }

    private static void handleApiError(Context context, ApiResponse<?> response) {
        if (response.getMessage() == null) {
            showMessage(context, "Unknown server error");
            return;
        }

        String message = response.getMessage().toLowerCase(Locale.ROOT);
        String userMessage;

        if (message.contains("otp")) {
            userMessage = handleOtpErrors(response);
        } else if (message.contains("account locked")) {
            userMessage = "Account locked. Try again after " + formatLockTime(response.getLockedUntil());
        } else if (message.contains("invalid credentials")) {
            userMessage = "Invalid email or password";
        } else if (message.contains("validation error")) {
            userMessage = "Invalid input: " + extractValidationErrors(response);
        } else {
            userMessage = response.getMessage();
        }

        showMessage(context, userMessage);
    }

    private static String handleOtpErrors(ApiResponse<?> response) {
        StringBuilder message = new StringBuilder();
        if (response.getOtpAttempts() > 0) {
            message.append("Attempts left: ")
                    .append(response.getAttemptsLeft());
        }
        if (response.getLockedUntil() != null) {
            message.append("\nTry again after ")
                    .append(formatLockTime(response.getLockedUntil()));
        }
        return message.length() > 0 ? message.toString() : "OTP verification failed";
    }

    private static String formatLockTime(Date lockedUntil) {
        if (lockedUntil == null) return "some time";
        try {
            return TIME_FORMAT.format(lockedUntil);
        } catch (IllegalArgumentException e) {
            Log.e(TAG, "Error formatting lock time", e);
            return "some time";
        }
    }

    private static String extractValidationErrors(ApiResponse<?> response) {
        if (response.getData() == null) return "";
        // Implement specific validation error extraction based on your API response structure
        return "Check your input fields";
    }

    private static void showMessage(Context context, String message) {
        if (context != null) {
            new android.os.Handler(context.getMainLooper()).post(() ->
                    Toast.makeText(context, message, Toast.LENGTH_LONG).show()
            );
        }
    }
}
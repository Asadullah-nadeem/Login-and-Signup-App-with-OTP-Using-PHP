package com.example.myapplication;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.util.Patterns;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.myapplication.adapters.API.ApiClient;
import com.example.myapplication.adapters.API.ApiService;
import com.example.myapplication.adapters.models.ApiResponse;
import com.example.myapplication.adapters.models.LoginRequest;
import com.example.myapplication.adapters.models.User;
import com.example.myapplication.adapters.utils.SharedPreferencesHelper;

import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LoginActivity extends AppCompatActivity {
    private static final String TAG = "LoginActivity";
    private EditText etEmail, etPassword;
    private SharedPreferencesHelper prefsHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        prefsHelper = new SharedPreferencesHelper(this);
        if (prefsHelper.isLoggedIn()) {
            startMainActivity();
            finish();
            return;
        }

        initializeViews();
    }

    private void initializeViews() {
        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);

        findViewById(R.id.btnLogin).setOnClickListener(v -> attemptLogin());
        findViewById(R.id.tvSignup).setOnClickListener(v ->
                startActivity(new Intent(this, SignupActivity.class)));
        findViewById(R.id.tvForgotPassword).setOnClickListener(v ->
                startActivity(new Intent(this, ForgotPasswordActivity.class)));
    }

    private void attemptLogin() {
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        if (!validateInputs(email, password)) return;

        // Hash the password before sending
        String hashedPassword = hashPassword(password);
        Log.d(TAG, "Hashed password: " + hashedPassword);

        ApiService service = ApiClient.getApiService();
        Call<ApiResponse<User>> call = service.login(new LoginRequest(email, hashedPassword));

        call.enqueue(new Callback<ApiResponse<User>>() {
            @Override
            public void onResponse(Call<ApiResponse<User>> call, Response<ApiResponse<User>> response) {
                if (response.isSuccessful()) {
                    ApiResponse<User> apiResponse = response.body();
                    if (apiResponse != null) {
                        Log.d(TAG, "Response: " + apiResponse);
                        handleLoginResponse(apiResponse);
                    } else {
                        Log.e(TAG, "Null response body");
                        Toast.makeText(LoginActivity.this, "Login failed: empty response", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    try {
                        String errorBody = response.errorBody() != null ?
                                response.errorBody().string() : "No error body";
                        Log.e(TAG, "Login failed - Code: " + response.code() + ", Error: " + errorBody);

                        if (response.code() == 401) {
                            Toast.makeText(LoginActivity.this,
                                    "Invalid email or password", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(LoginActivity.this,
                                    "Login failed. Please try again.", Toast.LENGTH_SHORT).show();
                        }
                    } catch (IOException e) {
                        Log.e(TAG, "Error parsing error response", e);
                        Toast.makeText(LoginActivity.this,
                                "Error processing response", Toast.LENGTH_SHORT).show();
                    }
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<User>> call, Throwable t) {
                Log.e(TAG, "Login error: " + t.getMessage(), t);
                Toast.makeText(LoginActivity.this,
                        t.getMessage().contains("Unable to resolve host") ?
                                "No internet connection" : "Network error occurred",
                        Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void handleLoginResponse(ApiResponse<User> response) {
        if (response == null) {
            Toast.makeText(this, "Invalid server response", Toast.LENGTH_SHORT).show();
            return;
        }

        if (response.isSuccess()) {
            User user = response.getData();
            if (user == null) {
                Toast.makeText(this, "User data missing", Toast.LENGTH_SHORT).show();
                return;
            }

            if (user.isVerified()) {
                // Successful login
                prefsHelper.saveUserData(user.getEmail(), user.getFullName());
                prefsHelper.setLoggedIn(true);
                startMainActivity();
            } else {
                // Needs OTP verification
                startVerifyOTPActivity(
                        user.getEmail(),
                        "login",
                        user.getFullName()
                );
            }
        } else {
            String errorMessage = response.getMessage();
            if (errorMessage.contains("credentials") || errorMessage.contains("Invalid")) {
                Toast.makeText(this, "Invalid email or password", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, errorMessage, Toast.LENGTH_SHORT).show();
            }
        }
    }

    // Password hashing method
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

    private void startMainActivity() {
        startActivity(new Intent(this, MainActivity.class));
        finishAffinity();
    }

    private void startVerifyOTPActivity(String email, String source, String fullName) {
        Intent intent = new Intent(this, VerifyOTPActivity.class)
                .putExtra("email", email)
                .putExtra("source", source);

        if (fullName != null) {
            intent.putExtra("full_name", fullName);
        }

        startActivity(intent);
        finish();
    }

    private boolean validateInputs(String email, String password) {
        boolean isValid = true;

        if (email.isEmpty()) {
            etEmail.setError("Email is required");
            isValid = false;
        } else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            etEmail.setError("Enter a valid email");
            isValid = false;
        }

        if (password.isEmpty()) {
            etPassword.setError("Password is required");
            isValid = false;
        } else if (password.length() < 6) {
            etPassword.setError("Password must be at least 6 characters");
            isValid = false;
        }

        return isValid;
    }
}
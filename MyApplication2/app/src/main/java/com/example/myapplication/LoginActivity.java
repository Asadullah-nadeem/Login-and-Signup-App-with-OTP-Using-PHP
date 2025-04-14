package com.example.myapplication;

import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.util.Patterns;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.myapplication.adapters.API.ApiClient;
import com.example.myapplication.adapters.API.ApiService;
import com.example.myapplication.adapters.models.ApiResponse;
import com.example.myapplication.adapters.models.LoginRequest;
import com.example.myapplication.adapters.models.User;
import com.example.myapplication.adapters.utils.SharedPreferencesHelper;

import java.io.IOException;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LoginActivity extends AppCompatActivity {
    private EditText etEmail, etPassword;
    private SharedPreferencesHelper prefsHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        //        Version Show
        TextView versionText = findViewById(R.id.Version);
        try {
            PackageManager pm = getPackageManager();
            PackageInfo pInfo = pm.getPackageInfo(getPackageName(), 0);
            String versionName = pInfo.versionName;
            versionText.setText(getString(R.string.version) + versionName+ getString(R.string.nadeem));
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
            versionText.setText(getString(R.string.vnotf));
        }


        prefsHelper = new SharedPreferencesHelper(this);
        if (prefsHelper.isLoggedIn()) {
            startMainActivity();
            return;
        }

        initializeViews();
    }

    private void initializeViews() {
        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);

        findViewById(R.id.btnLogin).setOnClickListener(v -> attemptLogin());
        findViewById(R.id.tvSignup).setOnClickListener(v -> startActivity(new Intent(this, SignupActivity.class)));
        findViewById(R.id.tvForgotPassword).setOnClickListener(v -> startActivity(new Intent(this, ForgotPasswordActivity.class)));
    }

    //    private void attemptLogin() {
//        String email = etEmail.getText().toString().trim();
//        String password = etPassword.getText().toString().trim();
//
//        if (!validateInputs(email, password)) return;
//
//        ApiService service = ApiClient.getApiService();
//        Call<ApiResponse<User>> call = service.login(new LoginRequest(email, password));
//
//        call.enqueue(new Callback<ApiResponse<User>>() {
//            @Override
//            public void onResponse(Call<ApiResponse<User>> call, Response<ApiResponse<User>> response) {
//                if (response.isSuccessful() && response.body() != null) {
//                    Log.d("LoginResponse", "Raw: " + response.body().toString());
//                    handleLoginResponse(response.body());
//                } else {
//                    Log.e("LoginResponse", "Failed: " + response.code());
//                    Toast.makeText(LoginActivity.this, "Invalid credentials", Toast.LENGTH_SHORT).show();
//                }
//            }
    private void attemptLogin() {
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        if (!validateInputs(email, password)) return;

        // Add debug log to verify password before sending
        Log.d("LoginDebug", "Attempting login with - Email: " + email + " Password: " + password);

        ApiService service = ApiClient.getApiService();
        Call<ApiResponse<User>> call = service.login(new LoginRequest(email, password));

        call.enqueue(new Callback<ApiResponse<User>>() {
            @Override
            public void onResponse(Call<ApiResponse<User>> call, Response<ApiResponse<User>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Log.d("LoginResponse", "Raw: " + response.body().toString());

                    // Add debug log for the response
                    if (response.body().getStatus().equals("error")) {
                        Log.e("LoginError", "API returned error: " + response.body().getMessage());
                    }

                    handleLoginResponse(response.body());
                } else {
                    try {
                        // Add detailed error logging
                        String errorBody = response.errorBody() != null ? response.errorBody().string() : "null";
                        Log.e("LoginError", "Failed: " + response.code() + " - " + errorBody);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    Toast.makeText(LoginActivity.this, "Invalid credentials", Toast.LENGTH_SHORT).show();
                }
            }
            @Override
            public void onFailure(Call<ApiResponse<User>> call, Throwable t) {
                Log.e("LoginResponse", "Error: " + t.getMessage());
                Toast.makeText(LoginActivity.this, "Network error", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void handleLoginResponse(ApiResponse<User> response) {
        if (response.isSuccess()) {
            User user = response.getData();
            if (user != null && user.isVerified()) {
                prefsHelper.saveUserData(user.getEmail(), user.getFullName());
                startMainActivity();
            } else {
                String email = user != null ? user.getEmail() : etEmail.getText().toString().trim();
                String fullName = user != null ? user.getFullName() : null; // Pass fullName if available
                startVerifyOTPActivity(email, "login", fullName);
                finish();
            }
        } else {
            Toast.makeText(this, response.getMessage(), Toast.LENGTH_SHORT).show();
        }

    }

    private void startMainActivity() {
        startActivity(new Intent(this, MainActivity.class));
        finish();
    }

    private void startVerifyOTPActivity(String email, String source, String fullName) {
        Intent intent = new Intent(this, VerifyOTPActivity.class)
                .putExtra("email", email)
                .putExtra("source", source);
        if (fullName != null) {
            intent.putExtra("full_name", fullName);
        }
        startActivity(intent);
    }

    private boolean validateInputs(String email, String password) {
        if (email.isEmpty() || !Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            etEmail.setError("Valid email required");
            return false;
        }
        if (password.isEmpty() || password.length() < 6) {
            etPassword.setError("Password must be 6+ characters");
            return false;
        }
        return true;
    }
}
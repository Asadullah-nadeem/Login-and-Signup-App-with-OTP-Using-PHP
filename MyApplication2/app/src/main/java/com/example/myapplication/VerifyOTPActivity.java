package com.example.myapplication;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.myapplication.adapters.API.ApiClient;
import com.example.myapplication.adapters.API.ApiService;
import com.example.myapplication.adapters.models.ApiResponse;
import com.example.myapplication.adapters.models.OtpRequest;
import com.example.myapplication.adapters.models.User;
import com.example.myapplication.adapters.utils.SharedPreferencesHelper;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class VerifyOTPActivity extends AppCompatActivity {
    private EditText etOtp;
    private String email, source, fullName;
    private SharedPreferencesHelper prefsHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_verify_otpactivity);
        prefsHelper = new SharedPreferencesHelper(this);
        getIntentData();
        initializeViews();
    }

    private void getIntentData() {
        Intent intent = getIntent();
        if (intent == null) {
            Log.e("VerifyOTP", "Intent is null");
            Toast.makeText(this, "Invalid intent", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        email = intent.getStringExtra("email");
        source = intent.getStringExtra("source");
        fullName = intent.getStringExtra("full_name");

        Log.d("VerifyOTP", "Email: " + email + ", Source: " + source + ", FullName: " + fullName);

        if (email == null || source == null) {
            Log.e("VerifyOTP", "Missing email or source");
            Toast.makeText(this, "Missing verification data", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    private void initializeViews() {
        TextView tvEmail = findViewById(R.id.tvEmail);
        tvEmail.setText("Using: " + email);
        etOtp = findViewById(R.id.etOtp);
        findViewById(R.id.btnVerify).setOnClickListener(v -> verifyOtp());
    }

    private void verifyOtp() {
        String otp = etOtp.getText().toString().trim();
        if (otp.length() != 4) {
            etOtp.setError("Enter 4-digit OTP");
            return;
        }

        Log.d("VerifyOTP", "Verifying OTP for email: " + email + ", OTP: " + otp);
        ApiService service = ApiClient.getApiService();
        Call<ApiResponse<User>> call = service.verifyOtp(new OtpRequest(email, otp));

        call.enqueue(new Callback<ApiResponse<User>>() {
            @Override
            public void onResponse(Call<ApiResponse<User>> call, Response<ApiResponse<User>> response) {
                Log.d("VerifyOTP", "Response Code: " + response.code());
                Log.d("VerifyOTP", "Response Body: " + (response.body() != null ? response.body().toString() : "null"));
                if (response.isSuccessful() && response.body() != null) {
                    handleVerificationResponse(response.body());
                } else {
                    Toast.makeText(VerifyOTPActivity.this, "Invalid response: " + response.code(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<User>> call, Throwable t) {
                Log.e("VerifyOTP", "Verification failed: " + t.getMessage());
                Toast.makeText(VerifyOTPActivity.this, "Verification failed: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void handleVerificationResponse(ApiResponse<User> response) {
        Log.d("VerifyOTP", "Success: " + response.isSuccess() + ", Message: " + response.getMessage());
        if (response.isSuccess()) {
            User user = response.getData();
            if (user != null) {
                // Prefer server-provided data
                prefsHelper.saveUserData(user.getEmail(), user.getFullName());
            } else {
                // Fallback to intent data or leave fullName as is if not available
                prefsHelper.saveUserData(email, fullName != null ? fullName : "User"); // Default to "User" if no fullName
            }
            Toast.makeText(this, "OTP Verified Successfully", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(this, MainActivity.class));
            finish();
        } else {
            Toast.makeText(this, response.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }
}
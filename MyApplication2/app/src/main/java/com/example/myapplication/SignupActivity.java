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
import com.example.myapplication.adapters.models.SignupRequest;
import com.example.myapplication.adapters.models.User;
import com.example.myapplication.adapters.utils.SharedPreferencesHelper;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SignupActivity extends AppCompatActivity {
    private EditText etFullName, etEmail, etPassword;
    private SharedPreferencesHelper prefsHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);
        prefsHelper = new SharedPreferencesHelper(this);


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

        initializeViews();
    }

    private void initializeViews() {
        etFullName = findViewById(R.id.etFullName);
        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);

        findViewById(R.id.btnSignup).setOnClickListener(v -> attemptSignup());
        findViewById(R.id.tvLoging).setOnClickListener(v -> startActivity(new Intent(this, LoginActivity.class)));
        findViewById(R.id.tvForgotPassword).setOnClickListener(v -> startActivity(new Intent(this, ForgotPasswordActivity.class)));

    }

    private void attemptSignup() {
        String fullName = etFullName.getText().toString().trim();
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        if (!validateInputs(fullName, email, password)) return;

        ApiService service = ApiClient.getApiService();
        Call<ApiResponse<User>> call = service.signup(new SignupRequest(fullName, email, password));

        call.enqueue(new Callback<ApiResponse<User>>() {
            @Override
            public void onResponse(Call<ApiResponse<User>> call, Response<ApiResponse<User>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Log.d("SignupResponse", "Raw: " + response.body().toString());
                    handleSignupResponse(response.body(), fullName, email);
                } else {
                    Toast.makeText(SignupActivity.this,
                            "Signup failed: " + response.code(),
                            Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<User>> call, Throwable t) {
                Toast.makeText(SignupActivity.this,
                        "Error: " + t.getMessage(),
                        Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void handleSignupResponse(ApiResponse<User> response, String fullName, String email) {
        Log.d("SignupActivity", "Response Success: " + response.isSuccess());
        if (response.isSuccess()) {
            Toast.makeText(this, "OTP sent successfully", Toast.LENGTH_SHORT).show();
            Log.d("SignupActivity", "Starting VerifyOTPActivity");
            startVerifyOTPActivity(email, "signup", fullName);
        } else {
            Toast.makeText(this, response.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void startVerifyOTPActivity(String email, String source, String fullName) {
        try {
            Intent intent = new Intent(this, VerifyOTPActivity.class)
                    .putExtra("email", email)
                    .putExtra("source", source)
                    .putExtra("full_name", fullName);
            startActivity(intent);
        } catch (Exception e) {
            Log.e("SignupActivity", "Error starting VerifyOTPActivity", e);
            Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    private boolean validateInputs(String fullName, String email, String password) {
        if (fullName.isEmpty()) {
            etFullName.setError("Full name required");
            return false;
        }
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
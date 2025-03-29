package com.example.myapplication;

import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.myapplication.adapters.API.ApiClient;
import com.example.myapplication.adapters.API.ApiService;
import com.example.myapplication.adapters.models.ApiResponse;
import com.example.myapplication.adapters.models.ForgotPasswordRequest;
import com.example.myapplication.adapters.utils.ApiErrorHandler;
import com.example.myapplication.adapters.utils.SharedPreferencesHelper;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ForgotPasswordActivity extends AppCompatActivity {
    private EditText etEmail;
    private SharedPreferencesHelper prefsHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgot_password);

        prefsHelper = new SharedPreferencesHelper(this);
        initializeViews();
    }

    private void initializeViews() {
        etEmail = findViewById(R.id.etEmail);
        Button btnSubmit = findViewById(R.id.btnSubmit);

        btnSubmit.setOnClickListener(v -> attemptPasswordReset());
    }

    private void attemptPasswordReset() {
        String email = etEmail.getText().toString().trim();

        if (!validateEmail(email)) return;

        ApiService service = ApiClient.getApiService();
        Call<ApiResponse<Void>> call = service.forgotPassword(new ForgotPasswordRequest(email));

        call.enqueue(new Callback<ApiResponse<Void>>() {
            @Override
            public void onResponse(Call<ApiResponse<Void>> call, Response<ApiResponse<Void>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    handleResetResponse(response.body());
                } else {
                    ApiErrorHandler.handleError(ForgotPasswordActivity.this,
                            new retrofit2.HttpException(response),
                            response.body()
                    );
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<Void>> call, Throwable t) {
                ApiErrorHandler.handleError(ForgotPasswordActivity.this, t, null);
            }
        });
    }

    private void handleResetResponse(ApiResponse<Void> response) {
        if (response.isSuccess()) {
            Toast.makeText(this, "Reset link sent to your email", Toast.LENGTH_LONG).show();
            finish();
        } else {
            Toast.makeText(this, response.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private boolean validateEmail(String email) {
        if (email.isEmpty() || !Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            etEmail.setError("Valid email required");
            return false;
        }
        return true;
    }
}
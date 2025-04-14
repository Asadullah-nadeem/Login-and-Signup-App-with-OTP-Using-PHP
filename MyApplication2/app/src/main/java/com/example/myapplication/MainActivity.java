package com.example.myapplication;

import static android.app.ProgressDialog.show;

import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextClock;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.os.BuildCompat;


import com.example.myapplication.adapters.API.ApiClient;
import com.example.myapplication.adapters.API.ApiService;
import com.example.myapplication.adapters.models.ApiResponse;
import com.example.myapplication.adapters.models.DeleteRequest;
import com.example.myapplication.adapters.utils.SharedPreferencesHelper;
import java.io.IOException;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";
    private SharedPreferencesHelper prefsHelper;
    private ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


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
        progressBar = findViewById(R.id.progressBar);




        checkAuthentication();
        setupUI();
        debugUserData(); // Temporary debug method
    }

    private void debugUserData() {
        Log.d(TAG, "User Data - ID: " + prefsHelper.getUserId()
                + ", Email: " + prefsHelper.getEmail()
                + ", LoggedIn: " + prefsHelper.isLoggedIn());
    }

    private void checkAuthentication() {
        if (!prefsHelper.isLoggedIn()) {
            startActivity(new Intent(this, LoginActivity.class));
            finish();
        }
    }

    private void setupUI() {
        TextView tvUserInfo = findViewById(R.id.tvUserInfo);
        String userInfo = "Name: " + prefsHelper.getFullName() + "\nEmail: " + prefsHelper.getEmail();
        tvUserInfo.setText(userInfo);

        findViewById(R.id.btnLogout).setOnClickListener(v -> logoutUser());
        findViewById(R.id.Daccount).setOnClickListener(v -> showDeleteConfirmationDialog());
    }

    private void showDeleteConfirmationDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Delete Account")
                .setMessage("All your data will be permanently deleted!")
                .setPositiveButton("Delete", (d, w) -> deleteAccount())
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void deleteAccount() {
        int userId = prefsHelper.getUserId();
        Log.d(TAG, "Attempting to delete account for user ID: " + userId);

        if (userId == -1) {
            handleMissingUserId();
//            Toast.makeText(this, "ERROR show", Toast.LENGTH_SHORT).show();
            return;
        }

        showLoading(true);
        ApiService service = ApiClient.getApiService();
        Call<ApiResponse<Void>> call = service.deleteAccount(new DeleteRequest(userId));

        call.enqueue(new Callback<ApiResponse<Void>>() {
            @Override
            public void onResponse(Call<ApiResponse<Void>> call, Response<ApiResponse<Void>> response) {
                showLoading(false);
                try {
                    if (response.isSuccessful()) {
                        handleSuccessfulResponse(response);
                    } else {
                        handleErrorResponse(response);
                    }
                } catch (IOException e) {
                    Log.e(TAG, "Error handling response", e);
                    showError("Error processing response");
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<Void>> call, Throwable t) {
                showLoading(false);
                Log.e(TAG, "Network failure", t);
                showError("Network error: " + t.getMessage());
            }
        });
    }

    private void handleSuccessfulResponse(Response<ApiResponse<Void>> response) throws IOException {
        ApiResponse<Void> body = response.body();
        if (body != null && body.isSuccess()) {
            handleSuccessfulDeletion();
        } else {
            String error = body != null ? body.getMessage() : "Empty response body";
            Log.e(TAG, "Deletion failed: " + error);
            showError("Deletion failed: " + error);
        }
    }

    private void handleErrorResponse(Response<ApiResponse<Void>> response) throws IOException {
        String errorBody = response.errorBody() != null ?
                response.errorBody().string() : "No error body";
        Log.e(TAG, "Server error: " + response.code() + " - " + errorBody);
        showError("Server error (" + response.code() + "): " + errorBody);
    }

    private void handleMissingUserId() {
        Log.e(TAG, "User ID not found in SharedPreferences");
        new AlertDialog.Builder(this)
                .setTitle("Session Error")
                .setMessage("Please login again")
                .setPositiveButton("OK", (d, w) -> redirectToLogin())
                .show();
    }

    private void handleSuccessfulDeletion() {
        Log.d(TAG, "Account deleted successfully");
        prefsHelper.clearUserData();
        showToast("Account deleted successfully");
        redirectToLogin();
    }

    private void logoutUser() {
        prefsHelper.clearUserData();
        redirectToLogin();
    }

    private void redirectToLogin() {
        startActivity(new Intent(this, LoginActivity.class));
        finishAffinity();
    }

    private void showLoading(boolean show) {
        progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
        findViewById(R.id.btnLogout).setEnabled(!show);
        findViewById(R.id.Daccount).setEnabled(!show);
    }

    private void showError(String message) {
        runOnUiThread(() -> Toast.makeText(this, message, Toast.LENGTH_LONG).show());
    }

    private void showToast(String message) {
        runOnUiThread(() -> Toast.makeText(this, message, Toast.LENGTH_SHORT).show());
    }
}
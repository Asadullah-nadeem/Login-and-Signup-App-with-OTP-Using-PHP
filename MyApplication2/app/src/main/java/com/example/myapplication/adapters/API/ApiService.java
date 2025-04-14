package com.example.myapplication.adapters.API;
import com.example.myapplication.adapters.models.ApiResponse;
import com.example.myapplication.adapters.models.DeleteRequest;
import com.example.myapplication.adapters.models.ForgotPasswordRequest;
import com.example.myapplication.adapters.models.LoginRequest;
import com.example.myapplication.adapters.models.OtpRequest;
import com.example.myapplication.adapters.models.ResetPasswordRequest;
import com.example.myapplication.adapters.models.SignupRequest;
import com.example.myapplication.adapters.models.User;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.HTTP;
import retrofit2.http.Header;
import retrofit2.http.POST;

public interface ApiService {
    @POST("signup.php")
    Call<ApiResponse<User>> signup(@Body SignupRequest request);

    @POST("login.php")
    Call<ApiResponse<User>> login(@Body LoginRequest request);

    @POST("verify-otp.php")
    Call<ApiResponse<User>> verifyOtp(@Body OtpRequest request);

    @POST("forgot-password.php")
    Call<ApiResponse<Void>> forgotPassword(@Body ForgotPasswordRequest request);

    @POST("reset-password.php")
    Call<ApiResponse<Void>> resetPassword(@Body ResetPasswordRequest request);

//    @HTTP(method = "DELETE", path = "delete.php", hasBody = true)
//    Call<ApiResponse<Void>> deleteAccount(@Body DeleteRequest deleteRequest);
    @HTTP(method = "DELETE", path = "delete.php", hasBody = true)
    Call<ApiResponse<Void>> deleteAccount(
            @Body DeleteRequest deleteRequest
    );
}
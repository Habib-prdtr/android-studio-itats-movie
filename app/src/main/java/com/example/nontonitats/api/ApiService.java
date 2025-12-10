package com.example.nontonitats.api;

import com.example.nontonitats.request.LoginRequest;
import com.example.nontonitats.request.RegisterRequest;
import com.example.nontonitats.request.UpdateProfileRequest;
import com.example.nontonitats.response.DeleteAccountResponse;
import com.example.nontonitats.response.LoginResponse;
import com.example.nontonitats.response.MeResponse;
import com.example.nontonitats.response.RegisterResponse;
import com.example.nontonitats.response.UpdateProfileResponse;
import com.example.nontonitats.response.UsersResponse;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.POST;

public interface ApiService {

    // ---------- AUTH ----------
    @POST("api/login")
    Call<LoginResponse> login(@Body LoginRequest request);

    @POST("api/register")
    Call<RegisterResponse> register(@Body RegisterRequest request);

    @GET("api/me")
    Call<MeResponse> me();

    @POST("api/user/update")
    Call<UpdateProfileResponse> updateProfile(@Body UpdateProfileRequest request);

    @DELETE("api/user/delete")
    Call<DeleteAccountResponse> deleteAccount();

    @GET("api/users")
    Call<UsersResponse> getAllUsers();



//    @POST("register")
//    Call<RegisterResponse> register(@Body RegisterRequest request);
//
//    @GET("me")
//    Call<UserResponse> getMe(@Header("Authorization") String token);
//
//    @POST("logout")
//    Call<DefaultResponse> logout(@Header("Authorization") String token);
//
//    // ---------- USER ----------
//    @GET("users")
//    Call<UserListResponse> getUsers(@Header("Authorization") String token);
//
//    @POST("user/update")
//    Call<DefaultResponse> updateUser(
//            @Header("Authorization") String token,
//            @Body UpdateUserRequest request
//    );
//
//    @DELETE("user/delete")
//    Call<DefaultResponse> deleteUser(
//            @Header("Authorization") String token
//    );
}


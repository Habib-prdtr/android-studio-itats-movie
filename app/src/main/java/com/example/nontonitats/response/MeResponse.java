package com.example.nontonitats.response;

import com.google.gson.annotations.SerializedName;

public class MeResponse {

    @SerializedName("massage")
    private String message;

    @SerializedName("data")
    private UserData data;

    public String getMessage() {
        return message;
    }

    public UserData getData() {
        return data;
    }
}

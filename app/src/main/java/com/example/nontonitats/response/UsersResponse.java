package com.example.nontonitats.response;

import com.example.nontonitats.response.UserData;
import java.util.List;

public class UsersResponse {
    private boolean status;
    private List<UserData> data;

    public boolean isStatus() {
        return status;
    }

    public List<UserData> getData() {
        return data;
    }
}

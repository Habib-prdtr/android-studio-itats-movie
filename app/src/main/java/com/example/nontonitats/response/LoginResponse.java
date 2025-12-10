package com.example.nontonitats.response;

public class LoginResponse {
    private String message;
    private Data data;

    public String getMessage() {
        return message;
    }

    public Data getData() {
        return data;
    }

    public class Data {
        private String token;
        private UserData user;

        public String getToken() {
            return token;
        }

        public UserData getUser() {
            return user;
        }
    }
}



package com.example.model;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.PUT;

public class User {

    public static interface IUser{

        @PUT("/api/users/auth")
        Call<Boolean> authenticateUser(@Body User user);
    }

    private String pwd;
    private String email;

    public User() {

    }

    public User(String email,String pwd) {
        this.email = email;
        this.pwd = pwd;
    }

    public String getPwd() {
        return pwd;
    }

    public void setPwd(String pwd) {
        this.pwd = pwd;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }
}
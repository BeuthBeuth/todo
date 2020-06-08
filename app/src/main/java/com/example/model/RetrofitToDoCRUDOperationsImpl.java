package com.example.model;


import android.util.Log;

import java.util.List;

import retrofit2.Call;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;

public class RetrofitToDoCRUDOperationsImpl implements IToDoCRUDOperations {

    public static interface TodoWebAPI {

        @POST("api/todos")
        public Call<ToDo> createItem(@Body ToDo item);

        @GET("api/todos")
        public Call<List<ToDo>> readAllItems();

        @PUT("api/todos/{id}")
        public Call<ToDo> updateToDo(@Path("id") long id, @Body ToDo item);

    }

    private TodoWebAPI webAPI;

    public RetrofitToDoCRUDOperationsImpl() {
        Retrofit apiRoot = new Retrofit.Builder()
                .baseUrl("http://10.0.2.2:8080/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        webAPI = apiRoot.create(TodoWebAPI.class);
    }


    @Override
    public ToDo createToDo(ToDo item) {
        try {
            Log.i("RetrofitCRUD", "createItem(): " + item);
            return webAPI.createItem(item).execute().body();
        }
        catch (Exception e) {
            Log.e("RetrofitCRUD", "got exception: " + e);
            return null;
        }
    }

    @Override
    public List<ToDo> readAllToDos() {
        try {
            return webAPI.readAllItems().execute().body();
        }
        catch (Exception e) {
            Log.e("RetrofitCRUD", "got exception: " + e);
            return null;
        }
    }

    @Override
    public ToDo readToDo() {
        return null;
    }

    @Override
    public boolean updateToDo(ToDo item) {
        try {
            if (webAPI.updateToDo(item.getId(), item).execute().body() != null) {
                return true;
            }
            return false;
        }
        catch (Exception e) {
            Log.e("RetrofitCRUD", "got exception: " + e);
            return false;
        }
    }

    @Override
    public boolean deleteToDo(long id) {
        return false;
    }

    @Override
    public ToDo readToDo(long id) {
        return null;
    }
}

package com.carstenbrauer.model;


import java.io.IOException;
import java.util.List;

import retrofit2.Call;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;

public class RemoteTodoCRUDOperation implements ITodoCRUDOperation {

    public static interface TodoWebAPI{

        @POST("/api/todos")
        Call<Todo> createTodo(@Body Todo todo);

        @GET("/api/todos")
        Call<List<Todo>> readAllTodos();

        @GET("/api/todos/{id}")
        Call<Todo> readTodo(@Path("id") long id);

        @PUT("/api/todos/{id}")
        Call<Todo> updateTodo(@Path("id") long id, @Body Todo todo);

        @DELETE("/api/todos/{id}")
        Call<Boolean> deleteTodo(@Path("id") long id);

        @DELETE("/api/todos")
        Call<Boolean> deleteAll();
    }

    private TodoWebAPI serviceProxy;

    public RemoteTodoCRUDOperation() {

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("http://192.168.178.115:8080")
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        serviceProxy = retrofit.create(TodoWebAPI.class);
    }

    @Override
    public long createTodo(Todo todo) {
        try {
            return serviceProxy.createTodo(todo).execute().body().getId();
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    @Override
    public List<Todo> readAllTodos() {
        try {
            return serviceProxy.readAllTodos().execute().body();
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    @Override
    public Todo readTodo(long id) {
        try {
            return serviceProxy.readTodo(id).execute().body();
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    @Override
    public boolean updateTodo(long id, Todo todo) {
        try {
            return serviceProxy.updateTodo(id, todo).execute().body() != null;
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    @Override
    public boolean deleteTodo(long id) {
        try {
            return serviceProxy.deleteTodo(id).execute().body();
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    public boolean deleteAll() {
        try {
            return serviceProxy.deleteAll().execute().body() != null;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}

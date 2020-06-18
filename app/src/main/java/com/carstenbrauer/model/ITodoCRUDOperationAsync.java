package com.carstenbrauer.model;

import java.util.List;

public interface ITodoCRUDOperationAsync {

    public static interface ResultCallback<T>{
        public void onResult(T result);
    }


    void createTodo(Todo todo, ResultCallback<Long> onresult);

    void readAllTodos(ResultCallback<List<Todo>> onresult);

    void readTodo(long id, ResultCallback<Todo> onresult);

    void updateTodo(long id, Todo todoITodoCRUDOperation, ResultCallback<Boolean> onresult);

    void deleteTodo(long id, ResultCallback<Boolean> onresult);

}

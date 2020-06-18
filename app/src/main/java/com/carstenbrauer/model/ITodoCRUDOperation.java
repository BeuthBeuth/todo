package com.carstenbrauer.model;

import java.util.List;

public interface ITodoCRUDOperation {

    long createTodo (Todo todo);

    List<Todo> readAllTodos();

    Todo readTodo(long id);

    boolean updateTodo(long id, Todo todo);

    boolean deleteTodo(long id);

}

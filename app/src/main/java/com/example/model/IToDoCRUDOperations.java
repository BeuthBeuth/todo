package com.example.model;

import java.util.List;

public interface IToDoCRUDOperations {

    public ToDo createToDo(ToDo item);

    public List<ToDo> readAllToDos();

    public ToDo readToDo();

    public boolean updateToDo(ToDo item);

    public boolean deleteToDo(long id);

    public ToDo readToDo(long id);

}

package com.example.model;


import java.util.List;

public class SyncedTodoCRUDOperation implements ITodoCRUDOperation {

    private LocalTodoCRUDOperation localTodoCRUDOperation;
    private RemoteTodoCRUDOperation remoteTodoCRUDOperation;

    public SyncedTodoCRUDOperation(LocalTodoCRUDOperation localTodoCRUDOperation, RemoteTodoCRUDOperation remoteTodoCRUDOperation){
        this.localTodoCRUDOperation = localTodoCRUDOperation;
        this.remoteTodoCRUDOperation = remoteTodoCRUDOperation;
    }

    @Override
    public long createTodo(Todo todo) {
        long id = localTodoCRUDOperation.createTodo(todo);
        todo.setId(id);
        remoteTodoCRUDOperation.createTodo(todo);
        return id;
    }

    @Override
    public List<Todo> readAllTodos() {
        return localTodoCRUDOperation.readAllTodos();
    }

    @Override
    public Todo readTodo(long id) {
        return localTodoCRUDOperation.readTodo(id);
    }

    @Override
    public boolean updateTodo(long id, Todo todo) {
        if (localTodoCRUDOperation.updateTodo(id, todo)){
            remoteTodoCRUDOperation.updateTodo(id,todo);
            return true;
        }
        return false;
    }

    @Override
    public boolean deleteTodo(long id) {
        if (localTodoCRUDOperation.deleteTodo(id)){
            remoteTodoCRUDOperation.deleteTodo(id);
            return true;
        }
        return false;
    }


    public void doSync(){
        List<Todo> localTodos = localTodoCRUDOperation.readAllTodos();

        if (localTodos.isEmpty()) {
            List<Todo> remoteTodos = remoteTodoCRUDOperation.readAllTodos();

            for (Todo todo : remoteTodos){
                localTodoCRUDOperation.createTodo(todo);
            }
            localTodos = localTodoCRUDOperation.readAllTodos();
        }

        remoteTodoCRUDOperation.deleteAll();
        for (Todo todo : localTodos) {
            remoteTodoCRUDOperation.createTodo(todo);
        }
    }
}

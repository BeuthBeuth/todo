package com.example.tasks;


import android.app.Activity;

import com.example.model.IToDoCRUDOperations;
import com.example.model.ToDo;

import java.util.concurrent.CompletableFuture;

public class UpdateToDoTaskWithFuture {

    private IToDoCRUDOperations crudOperations;
    private Activity owner;

    public UpdateToDoTaskWithFuture(Activity owner, IToDoCRUDOperations crudOperations) {
        this.crudOperations = crudOperations;
        this.owner = owner;
    }

    public CompletableFuture<Boolean> execute(ToDo item) {
        CompletableFuture<Boolean> resultFuture = new CompletableFuture<>();

        new Thread(new Runnable() {
            @Override
            public void run() {
                boolean updated = crudOperations.updateToDo(item);
                owner.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        resultFuture.complete(updated);
                    }
                });
            }
        }).start();

        return resultFuture;
    }

}

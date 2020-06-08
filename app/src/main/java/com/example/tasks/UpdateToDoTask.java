package com.example.tasks;


import android.os.AsyncTask;

import com.example.model.IToDoCRUDOperations;
import com.example.model.ToDo;

import java.util.function.Consumer;

public class UpdateToDoTask extends AsyncTask<ToDo, Void, Boolean> {

    private IToDoCRUDOperations crudOperations;
    private Consumer<Boolean> onDoneConsumer;

    public UpdateToDoTask(IToDoCRUDOperations crudOperations, Consumer<Boolean> onDoneConsumer) {
        this.crudOperations = crudOperations;
        this.onDoneConsumer = onDoneConsumer;
    }

    @Override
    protected Boolean doInBackground(ToDo... toDos) {
        return crudOperations.updateToDo(toDos[0]);
    }

    @Override
    protected void onPostExecute(Boolean result) {
        onDoneConsumer.accept(result);
    }

}


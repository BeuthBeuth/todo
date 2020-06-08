package com.example.tasks;


import android.os.AsyncTask;
import android.view.View;
import android.widget.ProgressBar;

import com.example.model.IToDoCRUDOperations;
import com.example.model.ToDo;

import java.util.function.Consumer;

public class CreateToDoTask extends AsyncTask<ToDo, Void, ToDo> {

    private ProgressBar progressBar;
    private IToDoCRUDOperations crudOperations;
    private Consumer<ToDo> onDoneConsumer;

    @Override
    protected void onPreExecute() {
        if (progressBar != null) {
            progressBar.setVisibility(View.VISIBLE);
        }
    }

    public CreateToDoTask(ProgressBar progressBar, IToDoCRUDOperations crudOperations, Consumer<ToDo> onDoneConsumer) {
        this.progressBar = progressBar;
        this.crudOperations = crudOperations;
        this.onDoneConsumer = onDoneConsumer;
    }

    @Override
    protected ToDo doInBackground(ToDo... toDos) {
        return crudOperations.createToDo(toDos[0]);
    }

    @Override
    protected void onPostExecute(ToDo toDo) {
        onDoneConsumer.accept(toDo);
        if (progressBar != null) {
            progressBar.setVisibility(View.INVISIBLE);
            progressBar = null;
        }
    }
}


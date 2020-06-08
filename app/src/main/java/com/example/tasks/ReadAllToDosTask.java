package com.example.tasks;


import android.os.AsyncTask;
import android.view.View;
import android.widget.ProgressBar;

import com.example.model.IToDoCRUDOperations;
import com.example.model.ToDo;

import java.util.List;
import java.util.function.Consumer;

public class ReadAllToDosTask extends AsyncTask<Void,Void, List<ToDo>> {

    private ProgressBar progressBar;
    private IToDoCRUDOperations crudOperations;
    private Consumer<List<ToDo>> onDoneConsumer;

    public ReadAllToDosTask(ProgressBar progressBar, IToDoCRUDOperations crudOperations, Consumer<List<ToDo>> onDoneConsumer) {
        this.progressBar = progressBar;
        this.crudOperations = crudOperations;
        this.onDoneConsumer = onDoneConsumer;
    }

    @Override
    protected void onPreExecute() {
        if (progressBar != null) {
            progressBar.setVisibility(View.VISIBLE);
        }
    }

    @Override
    protected List<ToDo> doInBackground(Void... voids) {
        return crudOperations.readAllToDos();
    }

    @Override
    protected void onPostExecute(List<ToDo> toDos) {
        onDoneConsumer.accept(toDos);
        if (progressBar != null) {
            progressBar.setVisibility(View.INVISIBLE);
            progressBar = null;
        }
    }
}

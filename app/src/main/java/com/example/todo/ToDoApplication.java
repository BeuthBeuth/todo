package com.example.todo;

import android.app.Application;
import android.os.AsyncTask;
import android.widget.Toast;

import com.example.model.IToDoCRUDOperations;
import com.example.model.RetrofitToDoCRUDOperationsImpl;
import com.example.model.RoomToDoCRUDOperationsImpl;

import java.util.function.Consumer;

public class ToDoApplication extends Application {

    private IToDoCRUDOperations crudOperations;

    @Override
    public void onCreate() {
        super.onCreate();
        crudOperations = new RoomToDoCRUDOperationsImpl(this);
    }

    public IToDoCRUDOperations getCrudOperations() {
        return crudOperations;
    }

    public void verifyWebappAvailable(Consumer<Boolean> onDone) {
        new AsyncTask<Void, Void, Boolean>() {

            @Override
            protected Boolean doInBackground(Void... voids) {
                try {
                    Thread.sleep(2000);
                }
                catch (Exception e) {

                }

                return true;
            }

            @Override
            protected void onPostExecute(Boolean available) {
                Toast.makeText(ToDoApplication.this, "Die Webapp läuft.", Toast.LENGTH_SHORT).show();
                onDone.accept(available);
            }

        }.execute();
    }

}

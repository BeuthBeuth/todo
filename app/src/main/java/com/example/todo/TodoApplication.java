package com.example.todo;

import android.app.Application;
import android.os.AsyncTask;

import com.example.model.ITodoCRUDOperation;
import com.example.model.ITodoCRUDOperationAsync;
import com.example.model.LocalTodoCRUDOperation;
import com.example.model.RemoteTodoCRUDOperation;
import com.example.model.SyncedTodoCRUDOperation;
import com.example.model.Todo;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;

public class TodoApplication extends Application implements ITodoCRUDOperationAsync{

    public static enum CRUDStatus{ONLINE, OFFLINE}
    private CRUDStatus crudStatus;

    private ITodoCRUDOperation crudOperation;
    private ITodoCRUDOperation offlineCrudOperation;
    private ITodoCRUDOperation onlineCrudOperation;

    @Override
    public void onCreate() {
        super.onCreate();
//        this.crudOperation = new SimpleTodoCRUDOperation();
        this.offlineCrudOperation = new LocalTodoCRUDOperation(this);
//        this.onlineCrudOperation = new RemoteTodoCRUDOperation();
        this.onlineCrudOperation = new SyncedTodoCRUDOperation((LocalTodoCRUDOperation) offlineCrudOperation, new RemoteTodoCRUDOperation());
    }

    public ITodoCRUDOperationAsync getCRUDOperation() {
        return this;
    }

    @Override
    public void createTodo(Todo todo, final ResultCallback<Long> onresult) {
        new AsyncTask<Todo, Void, Long>(){

            @Override
            protected Long doInBackground(Todo... todos) {
                return crudOperation.createTodo(todos[0]);
            }

            @Override
            protected void onPostExecute(Long aLong) {
                onresult.onResult(aLong);
            }
        }.execute(todo);
    }

    @Override
    public void readAllTodos(final ResultCallback<List<Todo>> onresult) {
        new AsyncTask<Void, Void, List<Todo>>(){

            @Override
            protected List<Todo> doInBackground(Void... voids) {
                return crudOperation.readAllTodos();
            }

            @Override
            protected void onPostExecute(List<Todo> todos) {
                onresult.onResult(todos);
            }
        }.execute();
    }

    @Override
    public void readTodo(long id, final ResultCallback<Todo> onresult) {
        new AsyncTask<Long, Void, Todo>(){

            @Override
            protected Todo doInBackground(Long... longs) {
                return crudOperation.readTodo(longs[0]);
            }

            @Override
            protected void onPostExecute(Todo todo) {
                onresult.onResult(todo);
            }
        }.execute(id);
    }

    @Override
    public void updateTodo(final long id, final Todo todo, final ResultCallback<Boolean> onresult) {
        new AsyncTask<Void, Void, Boolean>() {
            @Override
            protected Boolean doInBackground(Void... voids) {
                return crudOperation.updateTodo(id, todo);
            }

            @Override
            protected void onPostExecute(Boolean result) {
                onresult.onResult(result);
            }
        }.execute();

    }

    @Override
    public void deleteTodo(long id, final ResultCallback<Boolean> onresult) {
        new AsyncTask<Long, Void, Boolean>(){
            @Override
            protected Boolean doInBackground(Long... longs) {
                return crudOperation.deleteTodo(longs[0]);
            }

            @Override
            protected void onPostExecute(Boolean aBoolean) {
                onresult.onResult(aBoolean);
            }
        }.execute(id);
    }

    public CRUDStatus getCrudStatus() {
        return crudStatus;
    }

    public void initialiseCRUDOperations( final ResultCallback<CRUDStatus> oninitialised){


        new AsyncTask<Void, Void, CRUDStatus>(){

            @Override
            protected CRUDStatus doInBackground(Void... voids) {

                try {
                    HttpURLConnection httpURLConnection = (HttpURLConnection) new URL("http://10.0.2.2:8080").openConnection();
                    httpURLConnection.setRequestMethod("GET");
                    httpURLConnection.setDoInput(true);
                    httpURLConnection.setConnectTimeout(1000);
                    httpURLConnection.setReadTimeout(1000);

                    httpURLConnection.connect();

                    crudStatus = CRUDStatus.ONLINE;

                    crudOperation = onlineCrudOperation;
                    ((SyncedTodoCRUDOperation) crudOperation).doSync();

                } catch (IOException e) {
                    crudStatus = CRUDStatus.OFFLINE;
                    crudOperation = offlineCrudOperation;
                    e.printStackTrace();
                }
                return crudStatus;
            }

            @Override
            protected void onPostExecute(CRUDStatus crudStatus) {
//                crudOperation = crudStatus == CRUDStatus.ONLINE ? onlineCrudOperation : offlineCrudOperation;
                oninitialised.onResult(crudStatus);

                //muss wieder raus, nur zum Testen von lokalen DB Operationen
//                crudOperation = offlineCrudOperation;
            }
        }.execute();


    }
}

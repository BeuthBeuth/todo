package com.carstenbrauer.model;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;

public class LocalTodoCRUDOperation implements ITodoCRUDOperation {

    private SQLiteDatabase db;
    private static final String TABLE_TODO = "TODO";
    private static final String[] ALL_COLUMNS = new String[]{"ID", "NAME", "EXPIRY", "DONE", "DESCRIPTION", "FAVOURITE" , "CONTACTS"};
    private static final String CREATION_QUERY = "CREATE TABLE TODO (ID INTEGER PRIMARY KEY, NAME TEXT, EXPIRY INTEGER, DONE INTEGER, DESCRIPTION TEXT, FAVOURITE INTEGER, CONTACTS STRING)";

    public LocalTodoCRUDOperation(Context ctx) {
        this.db = ctx.openOrCreateDatabase("todo.sqlite", Context.MODE_PRIVATE, null);

        if (db.getVersion() == 0){
            db.setVersion(1);
            db.execSQL(CREATION_QUERY);
        }
    }

    @Override
    public long createTodo(Todo todo) {
        ContentValues contentValues = new ContentValues();
        contentValues.put("NAME", todo.getName());
        contentValues.put("DESCRIPTION", todo.getDescription());
        contentValues.put("EXPIRY", todo.getExpiry());
        contentValues.put("DONE", todo.isDone() ? 1 :0);
        contentValues.put("FAVOURITE", todo.isFavourite() ? 1 :0);

        ArrayList<String> tmpArrayList = todo.getContacts();
        contentValues.put("CONTACTS",tmpArrayList == null ? new ArrayList<String>().toString() : tmpArrayList.toString());

        long id = db.insert(TABLE_TODO, null, contentValues);
        todo.setId(id);

        return id;
    }

    @Override
    public List<Todo> readAllTodos() {

        List<Todo> todos = new ArrayList<>();

        Cursor cursor = db.query(TABLE_TODO, ALL_COLUMNS, null, null, null,null, "ID");
        if (cursor.getCount() > 0){
            while ((cursor.moveToNext())){
                todos.add(createTodoFromCursor(cursor));
            }
        }
        return todos;
    }

    public Todo createTodoFromCursor(Cursor cursor){
        Todo todo = new Todo();
        todo.setId(cursor.getLong(cursor.getColumnIndex("ID")));
        todo.setName(cursor.getString(cursor.getColumnIndex("NAME")));
        todo.setDescription(cursor.getString(cursor.getColumnIndex("DESCRIPTION")));
        todo.setExpiry(cursor.getLong(cursor.getColumnIndex("EXPIRY")));
        todo.setDone(cursor.getInt(cursor.getColumnIndex("DONE")) == 1);
        todo.setFavourite(cursor.getInt(cursor.getColumnIndex("FAVOURITE")) == 1);

        String contactString = cursor.getString(cursor.getColumnIndex("CONTACTS"));
        if (contactString != null ) {
            String[] splittedString = contactString.replaceAll(Pattern.quote("["),"").replaceAll("]","").replaceAll(" ","").split(",");
            todo.setContacts(splittedString[0].isEmpty() ? new ArrayList<String>() :  new ArrayList<String>(Arrays.asList(splittedString)));
        }

        return todo;
    }

    @Override
    public Todo readTodo(long id) {

        Cursor cursor = db.query(TABLE_TODO, ALL_COLUMNS, "ID=?", new String[]{String.valueOf(id)}, null, null, null);

        if (cursor.getCount() > 0 ){
            cursor.moveToFirst();
            return createTodoFromCursor(cursor);
        }
        return null;
    }

    @Override
    public boolean updateTodo(long id, Todo todo) {

        ContentValues contentValues = new ContentValues();
        contentValues.put("NAME", todo.getName());
        contentValues.put("DESCRIPTION", todo.getDescription());
        contentValues.put("EXPIRY", todo.getExpiry());
        contentValues.put("DONE", todo.isDone() ? 1 :0);
        contentValues.put("FAVOURITE", todo.isFavourite() ? 1 :0);
        contentValues.put("CONTACTS", todo.getContacts().toString());

        int updateResult = db.update(TABLE_TODO, contentValues, "ID=?", new String[]{String.valueOf(id)});
        return updateResult == 1 ? true : false;
    }

    @Override
    public boolean deleteTodo(long id) {
        int deleteResult = db.delete(TABLE_TODO, "ID=?", new String[]{String.valueOf(id)});
        return deleteResult == 1 ? true : false;
    }
}

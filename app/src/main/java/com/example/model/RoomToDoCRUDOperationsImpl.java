package com.example.model;


import android.content.Context;

import androidx.room.Dao;
import androidx.room.Database;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.Update;

import java.util.List;
import java.util.stream.Collectors;

public class RoomToDoCRUDOperationsImpl implements IToDoCRUDOperations {

    @Dao
    public static interface RoomToDoDao {

        @Query("select * from todo")
        public List<ToDo> readAll();

        @Insert
        public long create(ToDo item);

        @Update
        public int update(ToDo item);

        @Delete
        public void delete(ToDo item);

    }

    @Database(entities = {ToDo.class}, version = 1)
    public abstract static class ToDoDatabase extends RoomDatabase {

        public abstract RoomToDoDao getDao();

    }

    private ToDoDatabase db;

    public RoomToDoCRUDOperationsImpl(Context context) {
        db = Room.databaseBuilder(context, ToDoDatabase.class, "todos.db").build();
    }


    @Override
    public ToDo createToDo(ToDo item) {
//        item.beforePersist();
        long id = db.getDao().create(item);
        item.setId(id);
        return item;
    }

    @Override
    public List<ToDo> readAllToDos() {
        try {
            Thread.sleep(2000);
        }
        catch (Exception e) {

        }
        return db.getDao()
                .readAll();
//                .stream()
//                .map(item -> item.afterLoad())
//                .collect(Collectors.toList());
    }

    @Override
    public ToDo readToDo() {
        return null;
    }

    @Override
    public boolean updateToDo(ToDo item) {
//        item.beforePersist();
        if (db.getDao().update(item) > 0) {
            return true;
        }
        return false;
    }

    @Override
    public boolean deleteToDo(long id) {
        return false;
    }

    @Override
    public ToDo readToDo(long id) {
        return null;
    }
}

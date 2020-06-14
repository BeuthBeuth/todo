package com.example.model;


import android.icu.text.SimpleDateFormat;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;

public class Todo {
    private long id = -1;
    private String name;
    private String description;
    private  ArrayList<String> contacts = new ArrayList<>();
    private long expiry;
    private boolean done;
    private boolean favourite;

    public Todo(){

    }

    public Todo(String name) {
        this.name = name;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public  ArrayList<String> getContacts() {
        return contacts;
    }

    public void setContacts( ArrayList<String> contacts) {
        this.contacts = contacts;
    }

    public long getExpiry() {
        return expiry;
    }

    public void setExpiry(long expiry) {
        this.expiry = expiry;
    }

    public boolean isDone() {
        return done;
    }

    public void setDone(boolean done) {
        this.done = done;
    }

    public boolean isFavourite() {
        return favourite;
    }

    public void setFavourite(boolean favourite) {
        this.favourite = favourite;
    }

    public String getDateString(){
        if (this.expiry == 0){
            return "Datum und Uhrzeit";
        }
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("E  dd.MM.yyyy  -  HH:mm");
        return simpleDateFormat.format(new Date(this.expiry)) + " Uhr";
    }

    public void addContact(String id) {
        this.contacts.add(id);
    }

    public void deleteContact(String id){
        int position = 0;
        for (String currentId : this.contacts){
            if (currentId == id){
                break;
            }
            position++;
        }
        this.contacts.remove(position);
    }

    public static Comparator<Todo> SORT_BY_NAME = new Comparator<Todo>() {
        @Override
        public int compare (Todo todo1, Todo todo2) {
            return String.valueOf(todo1.getName().toLowerCase()).compareTo(String.valueOf(todo2.getName().toLowerCase()));
        }
    };

    public static Comparator<Todo> SORT_BY_ID = new Comparator<Todo>() {
        @Override
        public int compare (Todo todo1, Todo todo2) {
            return (int) (todo1.getId() - todo2.getId());
        }
    };

    public static Comparator<Todo> SORT_BY_DONE = new Comparator<Todo>() {
        @Override
        public int compare (Todo todo1, Todo todo2) {
            return Boolean.compare(todo1.isDone(), todo2.isDone());
        }
    };

    public static Comparator<Todo> SORT_BY_DATE = new Comparator<Todo>() {
        @Override
        public int compare (Todo todo1, Todo todo2) {
            return Long.compare(todo1.getExpiry(), todo2.getExpiry());
        }
    };

    public static Comparator<Todo> SORT_BY_RELEVANCE = new Comparator<Todo>() {
        @Override
        public int compare (Todo todo1, Todo todo2) {
            return Boolean.compare(todo2.isFavourite(), todo1.isFavourite());
        }
    };

}

package com.example.model;


import androidx.room.Entity;

import androidx.room.Ignore;
import androidx.room.PrimaryKey;

import com.google.gson.annotations.Expose;

import java.io.Serializable;


import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Entity
public class ToDo implements Serializable {
//    private static long idcount = 0;

    private static final String CONTACTS_SEPARATOR = "--;;--";

    @PrimaryKey(autoGenerate = true)
    private long id;// = ++idcount;

    private String name;

    private String description;

    //@SerializedName("done")
    private boolean done;

    @Ignore
    private  List<String> contacts = new ArrayList<>();

    @Expose(serialize = false, deserialize = false)
    private String contactsStr;

    public ToDo() {

    }

    public ToDo(String name) {
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

    public boolean isDone() {
        return done;
    }

    public void setDone(boolean done) {
        this.done = done;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ToDo toDo = (ToDo) o;
        return id == toDo.id;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    public List<String> getContacts() {
        return contacts;
    }

    public void setContacts(List<String> contacts) {
        this.contacts = contacts;
    }

    public String getContactsStr() {
        beforePersist();
        return contactsStr;
    }

    public void setContactsStr(String contactsStr) {
        this.contactsStr = contactsStr;
        afterLoad();
    }

    public void beforePersist() {
        if (this.contacts != null) {
            this.contactsStr = this.contacts
                    .stream()
                    .collect(Collectors.joining(CONTACTS_SEPARATOR));
        }
    }

    public ToDo afterLoad() {
        if (this.contactsStr != null) {
            this.contacts = Arrays.asList(this.contactsStr.split(CONTACTS_SEPARATOR))
                    .stream()
                    .map(c -> c.trim())
                    .collect(Collectors.toList());
            this.contactsStr = null;
        }
        return this;
    }
}

package com.capstone.projecthub.Model;

import java.io.Serializable;
import java.util.ArrayList;

public class DataWrapper implements Serializable {
    private ArrayList<User> users;

    public DataWrapper(ArrayList<User> users) {
        this.users = users;
    }

    public ArrayList<User> getUsers() {
        return this.users;
    }
}

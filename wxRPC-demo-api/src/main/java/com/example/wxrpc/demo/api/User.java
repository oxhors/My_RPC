package com.example.wxrpc.demo.api;

public class User {
    private int uid;
    private String name;

    public int getUid() {
        return uid;
    }

    public void setUid(int uid) {
        this.uid = uid;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return "User{" +
                "uid=" + uid +
                ", name='" + name + '\'' +
                '}';
    }

    public User() {
    }

    public User(int uid, String name) {
        this.uid = uid;
        this.name = name;
    }
}

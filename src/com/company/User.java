package com.company;

public class User {
    private String name;

    private Status status;

    public User(String name) {
        this.name = name;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        User user = (User) o;

        return name.equals(user.name);

    }

    @Override
    public int hashCode() {
        return name.hashCode();
    }


    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Status getStatus() {

        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public enum Status {
        STARTED, UNAME_SELECTED, ROOM_SELECTED;
    }

}

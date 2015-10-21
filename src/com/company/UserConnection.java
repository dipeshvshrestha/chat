package com.company;

import java.io.PrintWriter;

public class UserConnection {

    private User user;
    private PrintWriter writer;

    public UserConnection(User users, PrintWriter writer) {
        this.user = users;
        this.writer = writer;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public PrintWriter getWriter() {
        return writer;
    }

    public void setWriter(PrintWriter writer) {
        this.writer = writer;
    }
}

package com.company;

import java.util.HashSet;

public class Main {

    public static void main(String[] args) {
        HashSet<Room> rooms = new HashSet<>();
        rooms.add(new Room("chat"));
        rooms.add(new Room("hottub"));
	    ChatServer server = new ChatServer(9002, rooms);
        try {
            server.start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

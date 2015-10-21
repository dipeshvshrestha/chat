package com.company;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.*;

public class ChatServer {

    private static int port;
    private static Set<User> users = new HashSet<>();
    private static Map<Room, List<UserConnection>> chatRoom = new HashMap<>();

    public ChatServer(int port, Set<Room> rooms) {
        this.port = port;
        for(Room r: rooms) {
            chatRoom.put(r, new ArrayList<>());
        }
        System.out.println("The chat server is started");
    }

    public void start() throws Exception {
        ServerSocket listener = new ServerSocket(port);
        try {
            while (true) {
                new Thread(new ChatHandler(listener.accept())).start();
            }
        } finally {
            listener.close();
        }
    }

    private static class ChatHandler implements Runnable{
        private User user;
        private Room room;
        private Socket socket;
        private BufferedReader in;
        private PrintWriter out;
        private UserConnection userConnection;

        public ChatHandler(Socket s) {
            this.socket = s;
            System.out.println("New client connection...");
        }

        @Override
        public void run() {
            try {
                in = new BufferedReader(new InputStreamReader(
                        socket.getInputStream()));
                out = new PrintWriter(socket.getOutputStream(), true);

                while (true) {
                    out.println("Welcome to the XYZ chat server. Type /help for all commands.");

                    String name = null;
                    while(name == null || name.isEmpty()) {
                        out.println("Login Name?");
                        name = in.readLine();
                    }

                    synchronized (users) {
                        user = new User(name);
                        user.setStatus(User.Status.STARTED);
                        if (!users.contains(user)) {
                            System.out.println("User " + name + " logged in...");
                            user.setStatus(User.Status.UNAME_SELECTED);
                            users.add(user);
                            out.println("Welcome " + name + "!");
                            break;
                        }else {
                            out.println("Login Name " + name + " is already taken. Try another one.");
                        }
                    }
                }

                // accept all commands now
                while (true) {
                    String input = in.readLine();

                    if(input!= null & input.startsWith("/")) {
                        String command = input.substring(1);

                        if ("rooms".equals(command)) {
                            out.println("Active rooms are:");
                            // print list of active rooms
                            for (Map.Entry<Room, List<UserConnection>> e: chatRoom.entrySet()) {
                                out.println("* " + e.getKey().getName() +  " (" + e.getValue().size() + ") ");
                            }
                            out.println("end of list.");
                        }

                        if (command.startsWith("join")) {

                            // TODO: check index
                            String roomName = command.substring(command.indexOf(" ") + 1, command.length());
                            room = new Room(roomName);
                            if (chatRoom.containsKey(room)) {
                                // add user to room
                                userConnection = new UserConnection(user, out);
                                chatRoom.get(room).add(userConnection);
                                user.setStatus(User.Status.ROOM_SELECTED);

                                out.println("entering room: " + roomName);

                                // print list of user in a room and at same time write message to other user console
                                for (Map.Entry<Room, List<UserConnection>> e: chatRoom.entrySet()) {
                                    Iterator<UserConnection> i = e.getValue().iterator();
                                    while(i.hasNext()) {
                                        UserConnection connection = i.next();
                                        if(user.getName() != connection.getUser().getName()) {
                                            connection.getWriter().println("* new user joined chat: " + user.getName());
                                        }
                                        out.println("* " + connection.getUser().getName() +
                                                ((user.getName() == connection.getUser().getName())?" (** this is you)":"") );
                                    }
                                }

                            } else {
                                out.println("Invalid room name!");
                            }
                        }
                        else if(command.equals("leave")) {
                            user.setStatus(User.Status.UNAME_SELECTED);
                            if (chatRoom.containsKey(room)) {
                                // remove user from room
                                chatRoom.get(room).remove(userConnection);

                                // write message to other user console
                                for (Map.Entry<Room, List<UserConnection>> e: chatRoom.entrySet()) {
                                    Iterator<UserConnection> i = e.getValue().iterator();
                                    while(i.hasNext()) {
                                        UserConnection connection = i.next();
                                        if(user.getName() != connection.getUser().getName()) {
                                            connection.getWriter().println("* user has left chat: " + user.getName());
                                        }                           }
                                }
                            }
                        }
                        else if(command.equals("quit")) {
                            if (chatRoom.containsKey(room)) {
                                // remove user from room
                                chatRoom.get(room).remove(userConnection);
                            }

                            out.println("BYE");
                            out.close();
                            out = null;

                            in.close();
                            in = null;
                            socket.close();
                            System.out.println("User " + user.getName() + " has logged out...");
                            break;
                        }
                        else if(command.equals("help")) {
                            out.println("/rooms -> list of rooms");
                            out.println("/join <roomname> -> join a room");
                            out.println("/leave -> leave a room");
                            out.println("/quit -> exit chat");
                        }
                    }
                    // regular message
                    else if(user.getStatus()== User.Status.ROOM_SELECTED) {
                        if (chatRoom.containsKey(room)) {
                            // write message to other user console
                            for (Map.Entry<Room, List<UserConnection>> e: chatRoom.entrySet()) {
                                Iterator<UserConnection> i = e.getValue().iterator();
                                while(i.hasNext()) {
                                    UserConnection connection = i.next();
                                        connection.getWriter().println(user.getName() + ": " + input);}
                            }
                        }
                    }

                }


            } catch (IOException e) {
                e.printStackTrace();
                if(in != null) {
                    try {
                        in.close();
                    } catch (IOException e1) {
                        e1.printStackTrace();
                    }
                }
                if(out != null) {
                    out.close();
                }
                if(socket != null) {
                    try {
                        socket.close();
                    } catch (IOException e1) {
                        e1.printStackTrace();
                    }
                }
            }

        }
    }
}

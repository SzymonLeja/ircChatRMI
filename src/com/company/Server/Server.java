package com.company.Server;

import com.company.Client.clientRMI;

import java.rmi.*;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Scanner;

public class Server extends Thread implements Remote, serverRMI {
    private final ArrayList<ServerUser> usersList = new ArrayList<>();
    private boolean isTesting = false;
    public Server() throws RemoteException {
        UnicastRemoteObject.exportObject(this, 0);
    }

    public void startServer() {
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm");
        Date date = new Date(System.currentTimeMillis());
        Logger.log(formatter.format(date) + " SERVER STARTED");
        Thread t2 = new Thread(() -> {
            while (true) {
                try {
                    Thread.sleep(2000);
                    testConnection();
                } catch (InterruptedException | RemoteException e) {
                    closeServerSocket();
                }
            }
        });
        t2.start();
    }


    @Override
    public String InitialConnection(String input, clientRMI client) throws RemoteException {
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm");
        Date date = new Date(System.currentTimeMillis());
        String clientName = input.split(":")[0];
        if (duplicateNickname(clientName)) {
            Logger.log(formatter.format(date) + " user tried to log in with \"" + clientName + "\" nickname, but it is already taken");
            return formatter.format(date) + " Server: Nickname is taken!";
        } else {
            String clientChannel = input.split(":")[1];
            ServerUser user = new ServerUser(clientName, clientChannel, client);
            usersList.add(user);
            Logger.log(formatter.format(date) + " client \"" + clientName + "\" connected to the \"" + clientChannel + "\" channel");
            sendMessage(clientName, formatter.format(date) + " Server: \"" + clientName + "\" has joined the channel");
            return formatter.format(date) + " Server: Successfully connected";
        }
    }

    private void testConnection() throws RemoteException {
        if(!isTesting){
            isTesting = true;
            for (ServerUser serverUser : usersList) {
                try {
                    serverUser.isConnected();
                } catch (RemoteException e) {
                    disconnectClient(serverUser.getClientName());
                    break;
                }
            }
            isTesting= false;
        }
    }

    @Override
    public void sendMessage(String userName, String clientMessage) throws RemoteException {
        testConnection();
        ServerUser user = usersList.stream().filter(x -> x.getClientName().equals(userName)).findFirst().orElse(null);
        String userChannel = user.getClientChannel();

        if (clientMessage.contains("/join")) {
            joinChannelCommand(user, userName, clientMessage);
        } else {
            String replace = clientMessage.replace(userName, "at \"" + userChannel + "\" channel, \"" + userName + "\"");
            if (clientMessage.contains("/help")) {
                Logger.log(replace);
                user.displayMessage("List of commands: \n /join channel - joins another channel \n /disconnect - disconnects from server \n /list - list of active users on channel");

            } else if (clientMessage.contains("/list")) {
                listOfUsersCommand(user, userName, clientMessage);

            } else {
                if (isNotServerMessage(clientMessage)) {
                    Logger.log(replace);
                }
                for (ServerUser serverUser : usersList) {
                    if (serverUser.getClientChannel().equals(userChannel)) {
                        serverUser.displayMessage(clientMessage);
                    }

                }
            }
        }
    }

    private void joinChannelCommand(ServerUser user, String userName, String clientMessage) throws RemoteException {
        Logger.log(clientMessage);
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm");
        Date date = new Date(System.currentTimeMillis());
        String[] joinCommand = clientMessage.substring(clientMessage.indexOf("/") + 1).replaceAll(" ", "").split("join");
        if (joinCommand.length == 2) {
            sendMessage(userName, formatter.format(date) + " Server: \"" + userName + "\" has left the channel \"" + user.getClientChannel() + "\"");
            Logger.log(formatter.format(date) + " Client: \"" + userName + "\" switched to \"" + joinCommand[1] + "\" channel from \"" + user.getClientChannel() + "\" channel");
            user.setClientChannel(joinCommand[1]);
            sendMessage(userName, formatter.format(date) + " Server: \"" + userName + "\" has joined the channel \"" + user.getClientChannel() + "\"");
        }
    }

    private void listOfUsersCommand(ServerUser user, String userName, String clientMessage) throws RemoteException {
        Logger.log(clientMessage.replace(userName, "at \"" + user.getClientChannel() + "\" channel, \"" + userName + "\""));
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm");
        Date date = new Date(System.currentTimeMillis());
        user.displayMessage(formatter.format(date) + " Server: List of users connected to the channel:");
        for (ServerUser serverUser : usersList) {
            if (!serverUser.getClientName().equals(userName) && serverUser.getClientChannel().equals(user.getClientChannel())) {
                user.displayMessage(serverUser.getClientName());
            }
        }

    }

    public void closeServerSocket() {
        System.exit(0);
    }

    private boolean duplicateNickname(String nickname) {
        ServerUser user = usersList.stream().filter(x -> x.getClientName().equals(nickname)).findFirst().orElse(null);
        return user != null;
    }

    private boolean isNotServerMessage(String clientMessage) {
        return !clientMessage.contains("Server:");
    }

    public void disconnectClient(String userName) throws RemoteException {
        ServerUser user = usersList.stream().filter(x -> x.getClientName().equals(userName)).findFirst().orElse(null);
        String userChannel = user.getClientChannel();
        usersList.remove(user);
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm");
        Date date = new Date(System.currentTimeMillis());
        for (ServerUser serverUser : usersList) {
            if (userChannel.equals(serverUser.getClientChannel())) {
                serverUser.displayMessage(formatter.format(date) + " Server: \"" + userName + "\" has left the chat");
            }
        }
        Logger.log(formatter.format(date) + " Server: \"" + userName + "\" has left the chat");

    }

    public static void main(String[] args) throws RemoteException, AlreadyBoundException {
        System.out.println("Please select port:");
        Scanner scanner = new Scanner(System.in);
        int port;
        while (!scanner.hasNextInt()) {
            scanner.next();
            System.out.println("Please, type in an integer");
        }
        port = scanner.nextInt();
        Registry registry = LocateRegistry.createRegistry(port);
        Server mainServer = new Server();
        registry.bind("ircChat", mainServer);
        mainServer.startServer();
    }

}

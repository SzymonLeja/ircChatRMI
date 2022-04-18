package com.company.Client;

import com.company.Server.serverRMI;

import java.rmi.ConnectException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Scanner;

public class Client implements clientRMI {
    private serverRMI server;
    private String username;
    private String channel;
    private boolean isAlive;

    public Client(String username, String channel, serverRMI serverRMI) throws RemoteException {
            this.username = username;
            this.channel = channel;
            this.server = serverRMI;
            this.isAlive=true;
            UnicastRemoteObject.exportObject(this, 0);
    }
    public void start() throws RemoteException {
        while(isAlive){
            sendMessage();
        }
    }
    @Override
    public void ping(){
        //empty method to ping
    }
    public void sendMessage() throws RemoteException {
        Scanner scanner = new Scanner(System.in);
        String message = scanner.nextLine();
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm");
        Date date = new Date(System.currentTimeMillis());
        if(message.equals("/disconnect")){
            disconnectClient();
        } else {
            server.sendMessage(this.username,formatter.format(date) + " " + this.username +": "  + message);
        }
    }
    @Override
    public void addMessage(String message){
        System.out.println(message);
    }
    public void disconnectClient() throws RemoteException {
        System.out.println("Disconnected");
        server.disconnectClient(this.username);
        this.isAlive= false;
    }
    public static void main(String[] args) throws NotBoundException, RemoteException {
        System.out.println("Please select port:");
        Scanner scanner = new Scanner(System.in);
        int port = 0;
        while (!scanner.hasNextInt()) {
            scanner.next();
            System.out.println("Please, type in an integer");
        }
        port = scanner.nextInt();
        Registry registry;
        serverRMI server = null;
        try{
            registry = LocateRegistry.getRegistry("localhost",port);
            server = (serverRMI) registry.lookup("ircChat");
        } catch (ConnectException e){
            System.out.println("There is no server bound to this port!");
            System.exit(0);
        }
        scanner = new Scanner(System.in);
        System.out.println("Enter your username: ");
        String username =  scanner.nextLine();
        System.out.println("Enter channel you would like to join: ");
        String channel = scanner.nextLine();
        Client client = new Client(username, channel, server);
        String serverResponse = server.InitialConnection(username + ":" + channel, client);
        if(!serverResponse.contains("Nickname is taken!")){
            client.start();
        } else {
            System.out.println(serverResponse);
            System.exit(0);
        }



    }
}

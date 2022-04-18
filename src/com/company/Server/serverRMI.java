package com.company.Server;

import com.company.Client.Client;
import com.company.Client.clientRMI;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface serverRMI extends Remote {
    void sendMessage(String userName, String clientMessage) throws RemoteException;
    String InitialConnection(String input, clientRMI client) throws RemoteException;
    void disconnectClient(String userName) throws RemoteException;
}

package com.company.Client;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface clientRMI extends Remote {
    void addMessage(String message) throws RemoteException;
    void ping() throws RemoteException;

}

package com.company.Server;

import com.company.Client.clientRMI;

import java.rmi.RemoteException;

public class ServerUser {
    private String clientName;
    private String clientChannel;
    private clientRMI clientRMI;

    public ServerUser(String clientName, String clientChannel, clientRMI clientRMI) {
        this.clientName = clientName;
        this.clientChannel = clientChannel;
        this.clientRMI = clientRMI;
    }
    public String getClientName() {
        return clientName;
    }
    public String getClientChannel() {
        return clientChannel;
    }
    public void isConnected() throws RemoteException{
        clientRMI.ping();
    }

    public void setClientChannel(String clientChannel) {
        this.clientChannel = clientChannel;
    }
    public void displayMessage(String message) throws RemoteException {
            clientRMI.addMessage(message);
    }

}

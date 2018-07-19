package Utilities;

import OrderClient.NewOrderSingle;
import Utilities.SocketConnectors.SocketListener;
import Utilities.SocketConnectors.SocketMessenger;

import java.io.*;
import java.net.InetSocketAddress;

public class ClientMessenger {

    SocketMessenger messenger;

    ByteArrayOutputStream baos;
    ObjectOutputStream output;

    public ClientMessenger(InetSocketAddress address) throws InterruptedException {
        messenger = new SocketMessenger(address);
    }

    public void sendOrder(int id, NewOrderSingle nos) throws IOException {
        baos = new ByteArrayOutputStream();
        output = new ObjectOutputStream(baos);
        output.writeObject("newOrderSingle");
        output.writeInt(id);
        output.writeObject(nos);
        output.flush();
        messenger.sendMessage(baos.toByteArray());
    }

    public void sendCancel(int id) throws IOException {
        baos = new ByteArrayOutputStream();
        output = new ObjectOutputStream(baos);
        output.writeObject("cancelOrder");
        output.writeInt(id);
        output.flush();
        messenger.sendMessage(baos.toByteArray());
    }
}

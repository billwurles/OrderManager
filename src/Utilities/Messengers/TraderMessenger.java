package Utilities.Messengers;

import Utilities.SocketConnectors.SocketMessenger;

import java.io.*;
import java.net.InetSocketAddress;

public class TraderMessenger {

    SocketMessenger messenger;

    ByteArrayOutputStream baos;
    ObjectOutputStream output;
    InetSocketAddress address;
    int source;

    public TraderMessenger(InetSocketAddress address, int source) throws InterruptedException, IOException {
        this.address = address;
        System.err.println("TraderMessenger starting");
        messenger = new SocketMessenger(address);
        this.source = source;
    }

    public void acceptOrder(long id) throws IOException {
        baos = new ByteArrayOutputStream();
        output = new ObjectOutputStream(baos);

        output.writeInt(source);
        output.writeObject(address);
        output.writeObject("acceptOrder");
        output.writeLong(id);

        output.flush();
        messenger.sendMessage(baos.toByteArray());
    }

    public void sliceOrder(long id, int sliceSize) throws IOException {
        baos = new ByteArrayOutputStream();
        output = new ObjectOutputStream(baos);

        output.writeInt(source);
        output.writeObject("sliceOrder");
        output.writeLong(id);
        output.writeInt(sliceSize);

        output.flush();
        messenger.sendMessage(baos.toByteArray());
    }

    public void priceAtSizeMsg(long id, int sliceID, double price) throws IOException {
        baos = new ByteArrayOutputStream();
        output = new ObjectOutputStream(baos);

        output.writeInt(source);
        output.writeObject("bestPrice");
        output.writeLong(id);
        output.writeInt(sliceID);
        output.writeDouble(price);

        output.flush();
        messenger.sendMessage(baos.toByteArray());
    }

    public void sendCancel(long id){

    }


}

package Utilities.Messengers;


import Utilities.SocketConnectors.SocketMessenger;

import java.io.*;
import java.net.InetSocketAddress;

public class RouterMessenger {

    SocketMessenger messenger;

    ByteArrayOutputStream baos;
    ObjectOutputStream output;
    int source;

    public RouterMessenger(InetSocketAddress address, int source) throws InterruptedException, IOException {
        messenger = new SocketMessenger(address);
        this.source = source;
    }

    public void routeOrderMsg(long id, int sliceID, int fillSize, double fillPrice) throws IOException {
        baos = new ByteArrayOutputStream();
        output = new ObjectOutputStream(baos);

        output.writeInt(source);
        output.writeObject("newFill");
        output.writeLong(id);
        output.writeInt(sliceID);
        output.writeInt(fillSize);
        output.writeDouble(fillPrice);

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


}
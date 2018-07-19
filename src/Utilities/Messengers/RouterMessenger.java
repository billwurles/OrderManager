package Utilities;


import OrderRouter.Router;
import Ref.Instrument;
import Utilities.SocketConnectors.SocketListener;
import Utilities.SocketConnectors.SocketMessenger;

import java.io.*;
import java.net.InetSocketAddress;

public class RouterMessenger {

    SocketMessenger messenger;

    ByteArrayOutputStream baos;
    ObjectOutputStream output;

    public RouterMessenger(InetSocketAddress address) throws InterruptedException, IOException {
        messenger = new SocketMessenger(address);
    }

    public void routeOrderMsg(int id, int sliceID, int fillSize, double fillPrice) throws IOException {
        baos = new ByteArrayOutputStream();
        output = new ObjectOutputStream(baos);

        output.writeObject("newFill");
        output.writeInt(id);
        output.writeInt(sliceID);
        output.writeInt(fillSize);
        output.writeDouble(fillPrice);

        output.flush();
        messenger.sendMessage(baos.toByteArray());
    }

    public void priceAtSizeMsg(int id, int sliceID, double price) throws IOException {
        baos = new ByteArrayOutputStream();
        output = new ObjectOutputStream(baos);

        output.writeObject("bestPrice");
        output.writeInt(id);
        output.writeInt(sliceID);
        output.writeDouble(price);

        output.flush();
        messenger.sendMessage(baos.toByteArray());
    }


}
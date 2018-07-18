package Utilities.Messengers;

import OrderManager.Order;
import TradeScreen.TradeScreen;
import Utilities.SocketConnectors.SocketListener;
import Utilities.SocketConnectors.SocketMessenger;

import java.io.*;
import java.net.InetSocketAddress;

public class TraderMessenger {

    SocketMessenger messenger;

    ByteArrayOutputStream baos;
    ObjectOutputStream output;

    public TraderMessenger(InetSocketAddress address) throws InterruptedException, IOException {
        System.err.println("TraderMessenger starting");
        messenger = new SocketMessenger(address);
    }

    public void acceptOrder(int id) throws IOException {
        baos = new ByteArrayOutputStream();
        output = new ObjectOutputStream(baos);

        output.writeObject("acceptOrder");
        output.writeInt(id);

        output.flush();
        messenger.sendMessage(baos.toByteArray());
    }

    public void slideOrder(int id, int sliceSize) throws IOException {
        baos = new ByteArrayOutputStream();
        output = new ObjectOutputStream(baos);

        output.writeObject("sliceOrder");
        output.writeInt(id);
        output.writeInt(sliceSize);

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

    public void sendCancel(int id){

    }


}

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
    InetSocketAddress address;
    int port;

    public TraderMessenger(InetSocketAddress address) throws InterruptedException, IOException {
        this.address = address;
        System.err.println("TraderMessenger starting");
        messenger = new SocketMessenger(address);
        this.port = address.getPort();
    }

    public void acceptOrder(long id) throws IOException {
        baos = new ByteArrayOutputStream();
        output = new ObjectOutputStream(baos);

        output.writeInt(port);
        output.writeObject(address);
        output.writeObject("acceptOrder");
        output.writeLong(id);

        output.flush();
        messenger.sendMessage(baos.toByteArray());
    }

    public void sliceOrder(long id, int sliceSize) throws IOException {
        baos = new ByteArrayOutputStream();
        output = new ObjectOutputStream(baos);

        output.writeInt(port);
        output.writeObject("sliceOrder");
        output.writeLong(id);
        output.writeInt(sliceSize);

        output.flush();
        messenger.sendMessage(baos.toByteArray());
    }

    public void priceAtSizeMsg(long id, int sliceID, double price) throws IOException {
        baos = new ByteArrayOutputStream();
        output = new ObjectOutputStream(baos);

        output.writeInt(port);
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

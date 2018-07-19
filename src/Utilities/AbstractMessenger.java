package Utilities;

import java.io.*;
import java.net.InetSocketAddress;

/**
 * Created by alumniCurie16 on 17/07/2018.
 */
public abstract class AbstractMessenger {
    SocketMessenger messenger;
    SocketListener listener;

    ByteArrayOutputStream baos;
    ObjectOutputStream output;
    ObjectInputStream input;

    /**
     *
     * @param address
     * @throws InterruptedException
     * @throws IOException
     */
    AbstractMessenger(InetSocketAddress address) throws InterruptedException, IOException {
        messenger = new SocketMessenger(address);
        baos = new ByteArrayOutputStream();
        output = new ObjectOutputStream(baos);
    }

    abstract void sendMessage();

    abstract String[] recieveResponse() throws IOException, ClassNotFoundException;

    abstract void sendCancel(int id);
}

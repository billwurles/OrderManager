package Utilities.Listeners;

import OrderClient.Client;
import Utilities.SocketConnectors.SocketListener;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.InetSocketAddress;

/**
 * Created by alumniCurie16 on 19/07/2018.
 */
public class ClientListener {

    SocketListener listener;
    ObjectInputStream input;

    public ClientListener(InetSocketAddress address) throws InterruptedException, IOException {
        listener = new SocketListener(address);
    }

    public String[] receiveResponse() throws IOException, ClassNotFoundException {
        listener.listenForMessage();

        String[] fixTags;
        if(listener.hasResponse()) {
            byte[] response = listener.getResponse();
            input = new ObjectInputStream(new ByteArrayInputStream(response));

            String fix = (String) input.readObject();
            fixTags = fix.split(";");
            System.out.println(Thread.currentThread().getName() + " received fix message: " + fix);
        } else {
            fixTags = new String[] {};
            //TODO (Will) THROW SOME EXCEPTION
        }
        return fixTags;
    }
}

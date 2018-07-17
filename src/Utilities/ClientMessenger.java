package Utilities;

import OrderClient.NewOrderSingle;

import java.io.*;
import java.net.InetSocketAddress;

public class ClientMessenger {

    SocketMessenger messenger;
    SocketListener listener;

    ByteArrayOutputStream baos;
    ObjectOutputStream output;
    ObjectInputStream input;

    public ClientMessenger(InetSocketAddress address) throws InterruptedException, IOException {
        messenger = new SocketMessenger(address);
    }

    public void sendMessage(int id, NewOrderSingle nos) throws IOException {
        baos = new ByteArrayOutputStream();
        output = new ObjectOutputStream(baos);
        output.writeInt(id);
        output.writeObject(nos);
        output.flush();
        messenger.sendMessage(baos.toByteArray());
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

    public void sendCancel(int id){

    }
}

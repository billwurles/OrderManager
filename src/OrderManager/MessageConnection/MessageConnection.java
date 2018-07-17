package OrderManager.MessageConnection;

import OrderManager.OrderManager;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;

public abstract class MessageConnection {

    Socket this_connection;

    public MessageConnection(InetSocketAddress location) {
        boolean connected = false;
        int tryCounter = 0;
        while (!connected && tryCounter < 600) { //FIXME (Kel): Why are we trying this number of times?
            try {
                Socket this_connection = new Socket(location.getHostName(), location.getPort());
                this_connection.setKeepAlive(true);
            } catch (IOException e) {
                //Thread.sleep(1000);
                tryCounter++;
            }
        }
        System.out.println("Failed to connect to " + location.toString());
    }

    abstract public void sendMessage();

}

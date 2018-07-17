package Utilities;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.channels.SocketChannel;

/**
 * Created by alumniCurie16 on 17/07/2018.
 */
public class SocketConnection {

    Socket socket;
    InetSocketAddress location;
    boolean connected;

    public SocketConnection(InetSocketAddress location) throws InterruptedException {
        this.location=location;
        int tryCounter = 0;
        connected=false;
        while(!connected&&tryCounter<600) {
            try {
                socket = new Socket(location.getHostName(), location.getPort());
                socket.setKeepAlive(true);
                connected=true;
            } catch (IOException e) {
                Thread.sleep(500);
                tryCounter++;
            }
        }
    }

    private Socket connect(InetSocketAddress location) throws InterruptedException{
        boolean connected=false;
        int tryCounter=0;
        while(!connected&&tryCounter<600){
            try{
                Socket s=new Socket(location.getHostName(),location.getPort());
                s.setKeepAlive(true);
                return s;
            }catch (IOException e) {
                Thread.sleep(1000);
                tryCounter++;
            }
        }
        System.out.println("Failed to connect to "+location.toString());
        return null;
    }

    private void sendMessage(){
        ObjectOutputStream ost=new ObjectOutputStream(trader.getOutputStream());
        ost.writeObject(method);
        ost.writeInt(id);
        ost.writeObject(o);
        ost.flush();
    }
}

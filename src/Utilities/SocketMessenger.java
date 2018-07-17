package Utilities;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

/**
 * Created by alumniCurie16 on 17/07/2018.
 */
public class SocketMessenger {

    private SocketChannel channel;
    private InetSocketAddress address;

    private ByteBuffer buffer;
    private boolean connected;

    Logger logger;

    public SocketMessenger(InetSocketAddress location) throws InterruptedException {
        System.err.printf("%s attempting connection to %s:%s\n",Thread.currentThread().getName(),location.getHostName(),location.getPort());
        this.address=location;
        int tryCounter = 0;
        connected=false;
        logger= Logger.getLogger(SocketConnection.class.getName());
        while(!connected&&tryCounter<600) {
            try {
                channel = SocketChannel.open(location);
                logger.info("Connection "+Thread.currentThread().getName()+"to "+location.getHostName()+":"+location.getPort()+"made");
                connected=true;
            } catch (IOException e) {
                System.err.printf("Connection %s to %s:%s failed\n",Thread.currentThread().getName(),location.getHostName(),location.getPort());
                Thread.sleep(500);
                tryCounter++;
            }
        }
    }

    public void sendMessage(String message) throws IOException {
        byte[] data = message.getBytes();
        buffer = ByteBuffer.wrap(data);
        channel.write(buffer);
        System.err.printf("Conn. %s sent data to %s:%s\n",Thread.currentThread().getName(),address.getHostName(),address.getPort());
        buffer.clear();
        //sleep(5000);
    }
}

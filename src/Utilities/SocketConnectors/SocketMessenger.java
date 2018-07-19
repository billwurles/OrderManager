package Utilities.SocketConnectors;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.logging.Logger;

import static java.lang.Thread.sleep;

public class SocketMessenger {

    private SocketChannel channel;
    private InetSocketAddress address;


    private ByteBuffer buffer;
    private boolean connected;

    Logger logger;

    public SocketMessenger(InetSocketAddress address) throws InterruptedException {
        System.err.printf("%s attempting connection to %s:%s\n",Thread.currentThread().getName(),address.getHostName(),address.getPort());
        this.address=address;
        logger= Logger.getLogger(SocketConnection.class.getName());
    }

    public void sendMessage(byte[] data) throws IOException {
        int tryCounter = 0;
        connected=false;
        while(!connected&&tryCounter<10) {
            try {
                channel = SocketChannel.open(address);
                channel.configureBlocking(false);
                logger.info("Connection "+Thread.currentThread().getName()+" to "+address.getHostName()+":"+address.getPort()+" made");
                connected=true;
            } catch (IOException e) {
                System.err.printf("Connection %s to %s:%s failed\n",Thread.currentThread().getName(),address.getHostName(),address.getPort());
                try {
                    sleep(500);
                } catch (InterruptedException e1) {
                    e1.printStackTrace();
                }
                tryCounter++;
            }
        }
        buffer = ByteBuffer.wrap(data);
        channel.write(buffer);
        System.err.printf("Conn. %s sent data to %s:%s\n",Thread.currentThread().getName(),address.getHostName(),address.getPort());
        buffer.clear();
        //sleep(5000);
    }
}
package Utilities;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.*;
import java.util.logging.Logger;

public class SocketListener {

    private SocketChannel channel;
    private ServerSocketChannel server;
    private Socket socket;
    private InetSocketAddress address;

    private Selector selector;
    private Iterator keys;
    private ByteBuffer buffer;
    private Map<SocketChannel, List> dataMap;

    Logger logger;

    public SocketListener(InetSocketAddress location) throws InterruptedException {
        this.address=location;
        logger= Logger.getLogger(SocketConnection.class.getName());
        dataMap = new HashMap<>();
    }

    public String listenForMessage() throws IOException {
        System.err.printf("%s attempting connection to %s:%s\n",Thread.currentThread().getName(),address.getHostName(),address.getPort());
        this.selector = Selector.open();
        server = ServerSocketChannel.open();
        server.configureBlocking(false); //TODO maybe we want this to be true?

        server.socket().bind(address);
        server.register(selector, SelectionKey.OP_ACCEPT);

        while(true){
            System.err.printf("Conn. %s listening on %s:%s\n",Thread.currentThread().getName(),address.getHostName(),address.getPort());
            this.selector.select();

            keys = this.selector.selectedKeys().iterator();
            while(keys.hasNext()){
                SelectionKey key = (SelectionKey) keys.next();
                keys.remove();
                if(!key.isValid()){
                    continue;
                } else if(key.isAcceptable()){
                    accept(key);
                }
                if(key.isReadable()){
                    read(key);
                }

            }

            System.err.printf("Conn. %s recieved data from %s:%s\n",Thread.currentThread().getName(),address.getHostName(),address.getPort());
        }
    }

    private void accept(SelectionKey key) throws IOException {
        server = (ServerSocketChannel) key.channel();
        channel = server.accept();
        channel.configureBlocking(false);
        socket = channel.socket();

        SocketAddress sockAddr = socket.getRemoteSocketAddress();
        System.err.printf("Connection to %s accepted\n",sockAddr);

        dataMap.put(channel,new ArrayList());
        channel.register(selector, SelectionKey.OP_READ);
    }

    private byte[] read(SelectionKey key) throws IOException{
        channel = (SocketChannel) key.channel();
        buffer = ByteBuffer.allocate(2048);
        int numRead = -1;
        numRead = channel.read(buffer);

        if(numRead == -1){
            dataMap.remove(channel);
            socket = channel.socket();
            SocketAddress sockAddr = socket.getRemoteSocketAddress();
            System.err.printf("Conn. %s closed by %s\n",Thread.currentThread().getName(),sockAddr);
            channel.close();
            key.cancel();
            return new byte[]{};
        }

        String data = new String(buffer.array());
        System.out.printf("Conn. %s recieved data from %s:%s \n\n%s\n\n",Thread.currentThread().getName(),address.getHostName(),address.getPort(),data);

        return buffer.array();
    }
}

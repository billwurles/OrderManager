package Utilities.SocketConnectors;

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

public class SocketListener {

    private SocketChannel channel;
    private ServerSocketChannel server;
    private Socket socket;
    private InetSocketAddress[] addresses;

    InetSocketAddress address; //FIXME DELETE ME

    private Selector selector;
    private Iterator keys;
    private ByteBuffer buffer;
    private Map<SocketChannel, List> dataMap;

    private byte[] response;
    private boolean hasResponse;

    public SocketListener(InetSocketAddress address) throws InterruptedException, IOException {
        this.address= address;
        dataMap = new HashMap<>();
        hasResponse=false;
        this.selector = Selector.open();
//        server.socket().bind(addresses[0]);
        System.err.printf("%s binding to %s:%s\n",Thread.currentThread().getName(),address.getHostName(),address.getPort());
        server = ServerSocketChannel.open();
        server.socket().bind(address);
        server.configureBlocking(false); //TODO maybe we want this to be true?
        server.register(selector, SelectionKey.OP_ACCEPT);
        System.err.printf("%s listener starting connection to %s:%s\n",Thread.currentThread().getName(),address.getHostName(),address.getPort());
    }

//    public SocketListener(InetSocketAddress[] addresses) throws InterruptedException {
//        this.addresses=addresses;
//        address = addresses[0]; //FIXME this is pretty bad
//        dataMap = new HashMap<>();
//        hasResponse=false;
//        System.err.printf("%s listener starting connections to %s:%s\n",Thread.currentThread().getName(),address.getHostName(),address.getPort());
//    }

    public void listenForMessage() throws IOException {
        //System.err.printf("%s attempting connection to %s:%s\n",Thread.currentThread().getName(),address.getHostName(),address.getPort());

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
                    return;
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

    private void read(SelectionKey key) throws IOException{
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
        }

        String data = new String(buffer.array());
        System.err.printf("Connection %s recieved data from %s:%s \n\n%s\n\n",Thread.currentThread().getName(),address.getHostName(),address.getPort(),data);

        response = buffer.array();
        hasResponse=true;
    }

    public byte[] getResponse() { return response; }

    public boolean hasResponse() {
        return hasResponse;
    }
}

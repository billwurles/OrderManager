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

/**
 * Created by alumniCurie16 on 17/07/2018.
 */
public class SocketConnection {

    SocketChannel channel;
    ServerSocketChannel server;
    Socket socket;
    InetSocketAddress address;
    Selector selector;
    Iterator keys;

    ByteBuffer buffer;
    Map<SocketChannel, List> dataMap;

    boolean connected;



    public SocketConnection(InetSocketAddress location) throws InterruptedException {
        this.address=location;
        int tryCounter = 0;
        connected=false;
        while(!connected&&tryCounter<600) {
            try {
                channel = SocketChannel.open(location);
                System.err.printf("Connection %s to %s:%s made\n",Thread.currentThread().getName(),location.getHostName(),location.getPort());
                connected=true;
            } catch (IOException e) {
                Thread.sleep(500);
                tryCounter++;
            }
        }
        dataMap = new HashMap<>();
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

    private void sendMessage(String message) throws IOException {
        byte[] data = message.getBytes();
        buffer = ByteBuffer.wrap(data);
        channel.write(buffer);
        System.err.printf("Conn. %s sent data to %s:%s\n",Thread.currentThread().getName(),address.getHostName(),address.getPort());
        buffer.clear();
        //sleep(5000);
    }

    private String listenForMessage() throws IOException {
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
        System.err.printf("Connected to: %s",sockAddr);

        dataMap.put(channel,new ArrayList());
        channel.register(selector, SelectionKey.OP_READ);
    }

    private String read(SelectionKey key) throws IOException{
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
            return "";
        }

        String data = new String(buffer.array());
        System.err.printf("Conn. %s recieved data from %s:%s\n%s",Thread.currentThread().getName(),address.getHostName(),address.getPort(),data);

        return data;
    }
}

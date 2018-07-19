package UnitTests;

import OrderClient.NewOrderSingle;
import Ref.Instrument;
import Ref.Ric;
import Utilities.ClientMessenger;
import Utilities.OrderManagerMessenger;
import Utilities.SocketListener;
import Utilities.SocketMessenger;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.net.InetSocketAddress;

import static java.lang.Thread.sleep;

public class MessengerTests {

    final InetSocketAddress address = new InetSocketAddress("localhost",2020);
    final int id = 123;
    final NewOrderSingle nos = new NewOrderSingle(1,4321,765,new Instrument(new Ric("myric.L")));

    @Before
    public void setUpTest(){

    }

    @Test
    public void clientToOMMsgTest(){
        try {

            ClientMessenger clientMessenger = new ClientMessenger(address);

            Thread omThread = new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        OrderManagerMessenger omMessenger = new OrderManagerMessenger(address);
                        OrderManagerMessenger.ClientMessage message = omMessenger.receiveClientMessage();
                        assert(message.id == id);
                        assert(nos.getInstrument().equals(message.order.getInstrument()));
                    } catch (InterruptedException | IOException | ClassNotFoundException e) {
                        e.printStackTrace();
                    }
                }
            });
            Thread clientThread = new Thread(new Runnable() {
                @Override
                public void run() {
                    OrderManagerMessenger omMessenger = null;
                    try {
                        ClientMessenger clientMessenger = new ClientMessenger(address);
                        clientMessenger.sendMessage(id,nos);
                    } catch (InterruptedException | IOException e) {
                        e.printStackTrace();
                    }
                }
            });

            omThread.start();
            clientThread.start();

        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    @Test
    public void socketListenerMessengerTest(){
        try {
            byte[] dataToSend;
            final StringBuilder builder = new StringBuilder();
            String result = "";
            boolean returned;
            //builder = new StringBuilder();
            String theString = "This is the string";

            dataToSend = theString.getBytes();



            Runnable clientThread = new Runnable() {
                @Override
                public void run() {
                    try {
                        SocketMessenger messenger = new SocketMessenger(new InetSocketAddress("localhost",2020));
                        messenger.sendMessage(dataToSend);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            };

            Runnable serverThread = new Runnable() {
                @Override
                public synchronized void run() {
                    try {
                        //builder = new StringBuilder();
                        SocketListener listener = new SocketListener(new InetSocketAddress("localhost",2020));
                        listener.listenForMessage();
                        while(listener.hasResponse()){
                            byte[] bytes = listener.getResponse();
                            System.err.println("the bytes are :"+bytes);
                            for(byte b : bytes){

                                //result+=(char) b;
                                builder.append((char) b + " ");
                                System.out.print((char) b);
                            }
                            assert(builder.toString().equals(theString));
                            System.out.println("Asserting "+result+" == "+theString);
                            //result = builder.toString();
                        }
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            };



            Thread server = new Thread(serverThread);
            Thread client = new Thread(clientThread);
            server.start();
            client.start();

            client.join();
            sleep(1500);

        } catch (InterruptedException e) {
            e.printStackTrace();
        }
//        //TODO: USE LINKED LIST FOR TOP THREE
    }

}

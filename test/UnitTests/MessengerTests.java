package UnitTests;

import OrderClient.NewOrderSingle;
import Ref.Instrument;
import Ref.Ric;
import Utilities.Listeners.OrderManagerListener;
import Utilities.Messengers.ClientMessenger;
import Utilities.Messengers.OrderManagerMessenger;
import Utilities.SocketConnectors.SocketListener;
import Utilities.SocketConnectors.SocketMessenger;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.net.InetSocketAddress;

import static java.lang.Thread.sleep;

public class MessengerTests {

    final InetSocketAddress address = new InetSocketAddress("localhost",2020);
    final int id = 123;
    final String method = "TheMethod!!";
    final NewOrderSingle nos = new NewOrderSingle(1,4321,765,new Instrument(new Ric("myric.L")));

    @Before
    public void setUpTest(){

    }

    @Test
    public void clientToOMMsgTest(){
        Thread omThread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    InetSocketAddress[] addresses = new InetSocketAddress[]{address};
                    OrderManagerListener omMessenger = new OrderManagerListener(addresses,addresses,address);
                    OrderManagerListener.OrderManagerMessage message = omMessenger.receiveMessage();
                    assert(message.method.equals("newOrderSingle"));
                    assert(message.clientOrderID == id);
                    assert(nos.getInstrument().toString().equals(message.order.getInstrument().toString()));
                } catch (InterruptedException | IOException | ClassNotFoundException e) {
                    e.printStackTrace();
                }
            }
        });
        omThread.setName("OrderManagerTest");
        Thread clientThread = new Thread(new Runnable() {
            @Override
            public void run() {
                OrderManagerMessenger omMessenger = null;
                try {
                    ClientMessenger clientMessenger = new ClientMessenger(address);
                    clientMessenger.sendOrder(id,nos);
                } catch (InterruptedException | IOException e) {
                    e.printStackTrace();
                }
            }
        });
        clientThread.setName("ClientMessengerTest");

        omThread.start();
        clientThread.start();
        try {
            omThread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

    }

    @Test
    public void socketListenerMessengerTest(){
        try {
            byte[] dataToSend;
            String result = "";
            boolean returned;
            StringBuilder builder = new StringBuilder();
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
                            System.err.println("\n\n\n\n\nthe bytes are :"+bytes);
                            for(byte b : bytes){
                                builder.append((char) b);
                            }
                            System.out.println("Asserting '"+builder.toString()+"' == '"+theString+"'");
                            sleep(1000);
                            assert(builder.toString().equals(theString));
                        }
                    } catch (InterruptedException | IOException e) {
                        e.printStackTrace();
                    }
                }
            };
            Thread server = new Thread(serverThread);
            Thread client = new Thread(clientThread);
            server.setName("Server");
            server.start();
            client.setName("Client");
            client.start();

            client.join();
            sleep(1500);

        } catch (InterruptedException e) {
            e.printStackTrace();
        }
//        //TODO: USE LINKED LIST FOR TOP THREE
    }

}

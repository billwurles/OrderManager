import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;

import OrderClient.Client;
import OrderClient.NewOrderSingle;
import OrderManager.Order;
import Ref.Instrument;
import Ref.Ric;

public class SampleClient extends Mock implements Client {
    private static final Random RANDOM_NUM_GENERATOR = new Random();
    private final HashMap<Integer,NewOrderSingle> OUT_QUEUE = new HashMap<>(); //queue for outgoing orders
    private static AtomicInteger id = new AtomicInteger(0); //message id number
    private Socket omConn; //connection to order manager

    public SampleClient(int port) throws IOException {
        //OM will connect to us
        omConn = new ServerSocket(port).accept();
        System.out.println("OM connected to client port " + port);
    }


    @Override
    public int sendOrder(Object par0) throws IOException {
        NewOrderSingle newOrder = (NewOrderSingle) par0;

        show("sendOrder: id=" + id + " size=" + newOrder.getSize() + " instrument=" + newOrder.getInstrument().toString());
        if (OUT_QUEUE.put(id.get(), newOrder) != null)
            System.err.println("ERROR!?: Previous ID replaced");
        if (omConn.isConnected()) {
            ObjectOutputStream outputStream = new ObjectOutputStream(omConn.getOutputStream());
            outputStream.writeObject("newOrderSingle");
            //os.writeObject("35=D;");
            outputStream.writeInt(id.get());
            outputStream.writeObject(newOrder);
            outputStream.flush();
        }
        return id.getAndIncrement();
    }

    @Override
    public void sendCancel(int idToCancel) throws IOException {
        show("sendCancel: id=" + idToCancel);
        if (omConn.isConnected()) {
            ObjectOutputStream orderManagerStream = new ObjectOutputStream(omConn.getOutputStream());
            orderManagerStream.writeObject("cancelOrder");
            orderManagerStream.writeInt(idToCancel);
            orderManagerStream.flush();
        }
    }

    @Override
    public void partialFill(Order order) {
        show("" + order);
    }

    @Override
    public void fullyFilled(Order order) {
        show("" + order);
        OUT_QUEUE.remove(order.getClientOrderID());
    }

    @Override
    public void cancelled(Order order) {
        show("" + order);
        OUT_QUEUE.remove(order.getClientOrderID());
    }

    enum methods {newOrderSingleAcknowledgement, dontKnow}

    @Override
    public void messageHandler() {

        ObjectInputStream is;
        try {
            while (true) {
                //is.wait(); //this throws an exception!!
                is = new ObjectInputStream(omConn.getInputStream());

                String fix = (String) is.readObject();
                System.out.println(Thread.currentThread().getName() + " received fix message: " + fix);
                String[] fixTags = fix.split(";");
                int OrderId = -1;
                char MsgType;
                int OrdStatus;
                methods whatToDo = methods.dontKnow;
                //String[][] fixTagsValues=new String[fixTags.length][2];
                for (String fixTag : fixTags) {
                    String[] tag_value = fixTag.split("=");
                    switch (tag_value[0]) {
                        case "11":
                            OrderId = Integer.parseInt(tag_value[1]);
                            break;
                        case "35":
                            MsgType = tag_value[1].charAt(0);
                            if (MsgType == 'A') whatToDo = methods.newOrderSingleAcknowledgement;
                            break;
                        case "39":
                            OrdStatus = tag_value[1].charAt(0);
                            break;
                    }
                }
                switch (whatToDo) {
                    case newOrderSingleAcknowledgement:
                        newOrderSingleAcknowledgement(OrderId);
                        break;
                }
					/*message=connection.getMessage();
					char type;
					switch(type){
						case 'C':cancelled(message);break;
						case 'P':partialFill(message);break;
						case 'F':fullyFilled(message);
					}*/
                show("messageHandler loop");
            }
        } catch (IOException | ClassNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    void newOrderSingleAcknowledgement(int OrderId) {
        System.out.println(Thread.currentThread().getName() + " called newOrderSingleAcknowledgement");
        //do nothing, as not recording so much state in the NOS class at present
    }
/*listen for connections
once order manager has connected, then send and cancel orders randomly
listen for messages from order manager and print them to stdout.*/
}
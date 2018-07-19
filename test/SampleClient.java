import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;

import OrderClient.Client;
import OrderClient.NewOrderSingle;
import OrderManager.Order;
import java.util.logging.Logger;
import java.util.logging.Level;



public class SampleClient extends Mock implements Client {
    private final HashMap<Integer,NewOrderSingle> OUT_QUEUE = new HashMap<>(); //queue for outgoing orders
    private int id;
    private Socket omConn; //connection to order manager
    enum methods {newOrderSingleAcknowledgement, dontKnow}
    private static final Logger logger = Logger.getLogger(SampleClient.class.getName());

    /**
     *
     * @param port
     * @throws IOException
     */
    public SampleClient(int port) throws IOException {
        //OM will connect to us
        omConn = new ServerSocket(port).accept();
        System.out.println("OM connected to client port " + port);
        id = 0;
    }

    /**
     *
     * @param par0
     * @return
     * @throws IOException
     */
    @Override
    public int sendOrder(Object par0) throws IOException {
        NewOrderSingle newOrder = (NewOrderSingle) par0;

        show("sendOrder: id=" + id + " size=" + newOrder.getSize() + " instrument=" + newOrder.getInstrument().toString());
        if (OUT_QUEUE.put(id, newOrder) != null)
            System.err.println("ERROR!?: Previous ID replaced");
        if (omConn.isConnected()) {
            ObjectOutputStream outputStream = new ObjectOutputStream(omConn.getOutputStream());
            outputStream.writeObject("newOrderSingle");
            //os.writeObject("35=D;");
            outputStream.writeInt(id);
            outputStream.writeObject(newOrder);
            outputStream.flush();
        }
        return id++;
    }

    /**
     *
     * @param idToCancel
     * @throws IOException
     */
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

    /**
     *
     * @param orderID
     */
    @Override
    public void partialFill(long orderID){
        System.out.println(Thread.currentThread().getName()+" received Partial Fill message for order: " + orderID);
    }

    /**
     *
     * @param orderID
     */
    @Override
    public void fullyFilled(long orderID) {
        System.out.println(Thread.currentThread().getName()+" received Complete Fill message for order: "+ orderID);
        OUT_QUEUE.remove(orderID);
    }

    /**
     *
     * @param order
     */
    public void cancelled(Order order) {
        show("" + order);
        OUT_QUEUE.remove(order.getClientOrderID());
    }

    /**
     *
     * @param OrderId
     */
    void newOrderSingleAcknowledgement(int OrderId) {
        System.out.println(Thread.currentThread().getName() + " called newOrderSingleAcknowledgement");
    }


    @Override
    public void messageHandler() {
        logger.entering(getClass().getName(), "entering messageHandler");
        ObjectInputStream is;
        try {
            while (true) {
                //is.wait(); //this throws an exception!!
                is = new ObjectInputStream(omConn.getInputStream());
                String fix = (String) is.readObject();
                //System.out.println(Thread.currentThread().getName() + " received fix message: " + fix);
                String[] fixTags = fix.split(";");
                int OrderId = -1;
                char MsgType;
                int OrdStatus;

                switch(fixTags[0]){
                    case "F":long ClientOrderId=Long.parseLong(fixTags[1]);fullyFilled(ClientOrderId); break;
                    case "P":long ClientOrderId1=Long.parseLong(fixTags[1]);partialFill(ClientOrderId1); break;
                }
                methods whatToDo = methods.dontKnow;

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
            }
        } catch (IOException | ClassNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            logger.log(Level.SEVERE, "Error in messageHandler", e);
        }
        logger.exiting(getClass().getName(), "exiting messageHandler");
    }
/*listen for connections
once order manager has connected, then send and cancel orders randomly
listen for messages from order manager and print them to stdout.*/
}
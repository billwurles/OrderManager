package Utilities.Listeners;

import OrderClient.NewOrderSingle;
import Utilities.SocketConnectors.SocketListener;
import Utilities.SocketConnectors.SocketMessenger;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.InetSocketAddress;

public class OrderManagerListener {

    ObjectInputStream input;
    SocketListener listener;

    public OrderManagerListener(InetSocketAddress[] clients, InetSocketAddress[] routers, InetSocketAddress trader) throws InterruptedException, IOException {
        System.err.println(trader.getAddress());
        InetSocketAddress[] addresses = new InetSocketAddress[clients.length+routers.length+1];
        int count = 0;
        for(InetSocketAddress address:clients){
            addresses[count] = address;
            count++;
        }
        for(InetSocketAddress address:routers){
            addresses[count] = address;
            count++;
        }
        addresses[count] = trader;
        listener = new SocketListener(addresses);
    }

    public OrderManagerMessage receiveMessage() throws IOException, ClassNotFoundException {
        listener.listenForMessage();

        if(listener.hasResponse()) {
            byte[] response = listener.getResponse();
            input = new ObjectInputStream(new ByteArrayInputStream(response));
            int source = input.readInt();
            String method = (String) input.readObject();
            switch (method){
                case "newOrderSingle": //Client response
                    return new OrderManagerMessage(method, source, input.readInt(), (NewOrderSingle) input.readObject());
                case "cancelOrder": //Client Response
                    return new OrderManagerMessage(method, source, input.readInt());
                case "bestPrice": //Router response
                    return new OrderManagerMessage(method, source, input.readLong(), input.readInt(), input.readDouble());
                case "newFill": //Router response
                    return new OrderManagerMessage(method, source, input.readLong(), input.readInt(), input.readInt(), input.readDouble());
                case "acceptOrder": //trader response
                    return new OrderManagerMessage(input.readInt(), source, method);
                case "sliceOrder": //Trader response
                    return new OrderManagerMessage(method, source, input.readLong(), input.readInt());
            }
        } else {
            //TODO (Will) THROW SOME EXCEPTION
        }
        return new OrderManagerMessage("ERROR404", 0);//FIXME This could cause issues
    }

    public class OrderManagerMessage {
        public final String method;
        public final int clientOrderID, sliceID, size, source;
        public final long orderID;
        public final double bestPrice;
        public final NewOrderSingle order;


        public OrderManagerMessage(String method, int source, int clientOrderID, NewOrderSingle order) { // newOrderSingle
            this.method = method;
            this.clientOrderID = clientOrderID;
            this.order = order;
            this.source = source;

            this.size = Integer.MIN_VALUE;
            this.sliceID = Integer.MIN_VALUE;
            this.bestPrice = Double.MIN_VALUE;
            this.orderID = Long.MIN_VALUE;
        }

        public OrderManagerMessage(String method,  int source, int clientOrderID) { //cancelOrder
            this.method = method;
            this.clientOrderID = clientOrderID;
            this.source = source;

            this.size = Integer.MIN_VALUE;
            this.sliceID = Integer.MIN_VALUE;
            this.bestPrice = Double.MIN_VALUE;
            this.orderID = Long.MIN_VALUE;
            this.order = null;
        }

        public OrderManagerMessage(String method,  int source, long orderID, int sliceID, Double bestPrice) { //bestPrice
            this.method = method;
            this.sliceID = sliceID;
            this.orderID = orderID;
            this.bestPrice = bestPrice;
            this.source = source;

            this.size = Integer.MIN_VALUE;
            this.clientOrderID = Integer.MIN_VALUE;
            this.order = null;
        }

        public OrderManagerMessage(String method, int source, long orderID, int sliceID, int size, Double bestPrice) { //newFill
            this.method = method;
            this.orderID = orderID;
            this.sliceID = sliceID;
            this.bestPrice = bestPrice;
            this.source = source;

            this.size = Integer.MIN_VALUE;
            this.clientOrderID = Integer.MIN_VALUE;
            this.order = null;
        }

        public OrderManagerMessage(int orderID, int source, String method) { //acceptOrder
            this.method = method;
            this.orderID = orderID;
            this.source = source;

            this.size = Integer.MIN_VALUE;
            this.sliceID = Integer.MIN_VALUE;
            this.bestPrice = Double.MIN_VALUE;
            this.clientOrderID = Integer.MIN_VALUE;
            this.order = null;
        }

        public OrderManagerMessage(String method, int source, long orderID, int size) { // sliceOrder
            this.method = method;
            this.orderID = orderID;
            this.size = size;
            this.source = source;

            this.clientOrderID = Integer.MIN_VALUE;
            this.sliceID = Integer.MIN_VALUE;
            this.bestPrice = Double.MIN_VALUE;
            this.order = null;
        }

        public OrderManagerMessage(String method, int source) { // sliceOrder
            this.method = method;
            this.source = source;

            this.orderID = Long.MIN_VALUE;
            this.size = Integer.MIN_VALUE;
            this.clientOrderID = Integer.MIN_VALUE;
            this.sliceID = Integer.MIN_VALUE;
            this.bestPrice = Double.MIN_VALUE;
            this.order = null;
        }

    }
}

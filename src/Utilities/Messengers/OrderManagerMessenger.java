package Utilities.Messengers;

import OrderClient.NewOrderSingle;
import OrderManager.Order;
import OrderRouter.Router;
import Ref.Instrument;
import TradeScreen.TradeScreen;
import Utilities.SocketConnectors.SocketListener;
import Utilities.SocketConnectors.SocketMessenger;

import java.io.*;
import java.net.InetSocketAddress;
import java.util.HashMap;

public class OrderManagerMessenger {
    SocketMessenger messenger;

    ByteArrayOutputStream baos;
    ObjectOutputStream output;

    public OrderManagerMessenger(InetSocketAddress address) throws InterruptedException, IOException {
        messenger = new SocketMessenger(address);
    }

    public void newOrder(int clientOrderID) throws IOException {
        baos = new ByteArrayOutputStream();
        output = new ObjectOutputStream(baos);

        output.writeObject("11=" + clientOrderID + ";35=A;39=A;");

        output.flush();
        messenger.sendMessage(baos.toByteArray());
    }

    public void sendOrderToTrader(Order order, TradeScreen.api method) throws IOException {
        baos = new ByteArrayOutputStream();
        output = new ObjectOutputStream(baos);

        output.writeObject(method);
        output.writeObject(order);

        output.flush();
        messenger.sendMessage(baos.toByteArray());
    }

    public void acceptOrder(int clientOrderID) throws IOException {
        baos = new ByteArrayOutputStream();
        output = new ObjectOutputStream(baos);

        output.writeObject("11=" + clientOrderID + ";35=A;39=0;");

        output.flush();
        messenger.sendMessage(baos.toByteArray());
    }

    public void routeOrder(long id, int slideID, int size, Instrument instrument) throws IOException {
        baos = new ByteArrayOutputStream();
        output = new ObjectOutputStream(baos);

        output.writeObject(Router.api.priceAtSize);
        output.writeLong(id);
        output.writeInt(slideID);
        output.writeObject(instrument);
        output.writeInt(size);

        output.flush();
        messenger.sendMessage(baos.toByteArray());
    }

    public void reallyRouteOrder(long id, int slideID, int sizeRemaining, Instrument instrument) throws IOException {
        baos = new ByteArrayOutputStream();
        output = new ObjectOutputStream(baos);

        output.writeObject(Router.api.routeOrder);
        output.writeLong(id);
        output.writeInt(slideID);
        output.writeInt(sizeRemaining);
        output.writeObject(instrument);

        output.flush();
        messenger.sendMessage(baos.toByteArray());
    }
}


//    public RouterMessage receiveRouterMessage() throws IOException, ClassNotFoundException {
//        listener.listenForMessage();
//
//        if(listener.hasResponse()) {
//            byte[] response = listener.getResponse();
//            input = new ObjectInputStream(new ByteArrayInputStream(response));
//
//        } else {
//            //TODO (Will) THROW SOME EXCEPTION
//        }
//        return new RouterMessage(null, Integer.MIN_VALUE, Integer.MIN_VALUE, Integer.MIN_VALUE, Double.MIN_VALUE); //FIXME This could cause issues
//    }



//    public TraderMessage receiveTraderMessage() throws IOException, ClassNotFoundException {
//        listener.listenForMessage();
//
//        if(listener.hasResponse()) {
//            byte[] response = listener.getResponse();
//            input = new ObjectInputStream(new ByteArrayInputStream(response));
//            String method = (String) input.readObject();
//            switch (method){
//
//            }
//        } else {
//            //TODO (Will) THROW SOME EXCEPTION
//        }
//        return new TraderMessage(null, Integer.MIN_VALUE, Integer.MIN_VALUE); //FIXME This could cause issues
//    }
//
//
//
//    public class RouterMessage {
//        public final String method;
//        public final int id, orderID, sliceID;
//        public final double bestPrice;
//
//        public RouterMessage(String method, int id, int orderID, int sliceID, double bestPrice) {
//            this.method = method;
//            this.id = id;
//            this.orderID = orderID;
//            this.sliceID = sliceID;
//            this.bestPrice = bestPrice;
//        }
//    }
//
//    public class ClientMessage {
//        public final String method;
//        public final int id, clientOrderID;
//        public final NewOrderSingle order;
//
//        public ClientMessage(String method, int id, int clientOrderID, NewOrderSingle order) {
//            this.method = method;
//            this.id = id;
//            this.clientOrderID = clientOrderID;
//            this.order = order;
//        }
//    }

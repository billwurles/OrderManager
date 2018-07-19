package Utilities;

import OrderClient.NewOrderSingle;
import OrderManager.Order;
import OrderRouter.Router;
import Ref.Instrument;

import java.io.*;
import java.net.InetSocketAddress;

public class OrderManagerMessenger {
    SocketMessenger messenger;
    SocketListener listener;

    ByteArrayOutputStream baos;
    ObjectOutputStream output;
    ObjectInputStream input;

    /**
     *
     * @param address
     * @throws InterruptedException
     * @throws IOException
     */
    public OrderManagerMessenger(InetSocketAddress address) throws InterruptedException, IOException {
        messenger = new SocketMessenger(address);
    }

    /**
     *
     * @param clientOrderID
     * @throws IOException
     */
    public void newOrder(int clientOrderID) throws IOException {
        baos = new ByteArrayOutputStream();
        output = new ObjectOutputStream(baos);

        output.writeObject("11="+clientOrderID+";35=A;39=A;");

        output.flush();
        messenger.sendMessage(baos.toByteArray());
    }


    /**
     *
     * @param id
     * @param order
     * @param method
     * @throws IOException
     */
    public void sendOrderToTrader(int id, Order order, Object method) throws IOException {
        baos = new ByteArrayOutputStream();
        output = new ObjectOutputStream(baos);

        output.writeObject(method);
        output.writeInt(id);
        output.writeObject(order);

        output.flush();
        messenger.sendMessage(baos.toByteArray());
    }

    /**
     *
     * @param clientOrderID
     * @throws IOException
     */
    public void acceptOrder(int clientOrderID) throws IOException {
        baos = new ByteArrayOutputStream();
        output = new ObjectOutputStream(baos);

        output.writeObject("11="+clientOrderID+";35=A;39=0;");

        output.flush();
        messenger.sendMessage(baos.toByteArray());
    }

    /**
     *
     * @param id
     * @param slideID
     * @param size
     * @param instrument
     * @throws IOException
     */
    public void routeOrder(int id, int slideID, int size, Instrument instrument) throws IOException {
        baos = new ByteArrayOutputStream();
        output = new ObjectOutputStream(baos);

        output.writeObject(Router.api.priceAtSize);
        output.writeInt(id);
        output.writeInt(slideID);
        output.writeObject(instrument);
        output.writeInt(size);

        output.flush();
        messenger.sendMessage(baos.toByteArray());
    }

    /**
     *
     * @param id
     * @param slideID
     * @param sizeRemaining
     * @param instrument
     * @throws IOException
     */
    public void reallyRouteOrder(int id, int slideID, int sizeRemaining, Instrument instrument) throws IOException {
        baos = new ByteArrayOutputStream();
        output = new ObjectOutputStream(baos);

        output.writeObject(Router.api.routeOrder);
        output.writeInt(id);
        output.writeInt(slideID);
        output.writeInt(sizeRemaining);
        output.writeObject(instrument);

        output.flush();
        messenger.sendMessage(baos.toByteArray());
    }

    /**
     *
     * @return
     * @throws IOException
     * @throws ClassNotFoundException
     */
    public ClientMessage receiveClientMessage() throws IOException, ClassNotFoundException {
        listener.listenForMessage();

        if(listener.hasResponse()) {
            byte[] response = listener.getResponse();
            input = new ObjectInputStream(new ByteArrayInputStream(response));

            return new ClientMessage((String) input.readObject(), input.readInt(), input.readInt(), (NewOrderSingle) input.readObject());

        } else {
            //TODO (Will) THROW SOME EXCEPTION
        }
        return new ClientMessage(null, Integer.MIN_VALUE, Integer.MIN_VALUE, null);//FIXME This could cause issues
    }

    /**
     *
     * @return
     * @throws IOException
     * @throws ClassNotFoundException
     */
    public RouterMessage receiveRouterMessage() throws IOException, ClassNotFoundException {
        listener.listenForMessage();

        if(listener.hasResponse()) {
            byte[] response = listener.getResponse();
            input = new ObjectInputStream(new ByteArrayInputStream(response));
            String method = (String) input.readObject();
            switch (method){
                case "bestPrice":
                    return new RouterMessage(method, Integer.MIN_VALUE, input.readInt(), input.readInt(), input.readDouble());
                case "newFill":
                    return new RouterMessage(method, input.readInt(), input.readInt(), input.readInt(), input.readDouble());
            }
        } else {
            //TODO (Will) THROW SOME EXCEPTION
        }
        return new RouterMessage(null, Integer.MIN_VALUE, Integer.MIN_VALUE, Integer.MIN_VALUE, Double.MIN_VALUE); //FIXME This could cause issues
    }

    /**
     *
     * @return
     * @throws IOException
     * @throws ClassNotFoundException
     */
    public TraderMessage receiveTraderMessage() throws IOException, ClassNotFoundException {
        listener.listenForMessage();

        if(listener.hasResponse()) {
            byte[] response = listener.getResponse();
            input = new ObjectInputStream(new ByteArrayInputStream(response));
            String method = (String) input.readObject();
            switch (method){
                case "acceptOrder":
                    return new TraderMessage(method, input.readInt(), Integer.MIN_VALUE);
                case "sliceOrder":
                    return new TraderMessage(method, input.readInt(), input.readInt());
            }
        } else {
            //TODO (Will) THROW SOME EXCEPTION
        }
        return new TraderMessage(null, Integer.MIN_VALUE, Integer.MIN_VALUE); //FIXME This could cause issues
    }

    public class TraderMessage {
        public final String method;
        public final int id, sliceSize;

        /**
         *
         * @param method
         * @param id
         * @param sliceSize
         */
        public TraderMessage(String method, int id, int sliceSize) {
            this.method = method;
            this.id = id;
            this.sliceSize = sliceSize;
        }
    }

    public class RouterMessage {
        public final String method;
        public final int id, orderID, sliceID;
        public final double bestPrice;

        /**
         *
         * @param method
         * @param id
         * @param orderID
         * @param sliceID
         * @param bestPrice
         */
        public RouterMessage(String method, int id, int orderID, int sliceID, double bestPrice) {
            this.method = method;
            this.id = id;
            this.orderID = orderID;
            this.sliceID = sliceID;
            this.bestPrice = bestPrice;
        }
    }

    public class ClientMessage {
        public final String method;
        public final int id, clientOrderID;
        public final NewOrderSingle order;

        /**
         *
         * @param method
         * @param id
         * @param clientOrderID
         * @param order
         */
        public ClientMessage(String method, int id, int clientOrderID, NewOrderSingle order) {
            this.method = method;
            this.id = id;
            this.clientOrderID = clientOrderID;
            this.order = order;
        }
    }
}

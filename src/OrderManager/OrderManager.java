package OrderManager;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.*;

import LiveMarketData.LiveMarketData;
import OrderClient.NewOrderSingle;
import OrderRouter.Router;
import TradeScreen.TradeScreen;
import Utilities.Listeners.OrderManagerListener;
import Utilities.Messengers.OrderManagerMessenger;

public class OrderManager {
    private static LiveMarketData liveMarketData;
    private HashMap<Long, Order> orders = new HashMap<>(); //debugger will do this line as it gives state to the object
    private HashMap<Integer, Integer> clientIDs;
    private HashMap<Integer, Integer> routerIDs;

    //currently recording the number of new order messages we get. TODO why? use it for more?
    //private long id;
    private Socket[] orderRouters;
    private Socket[] clients;
    private Socket trader;
    private OrderManagerMessenger messenger;
    private OrderManagerListener listener;

    //@param args the command line arguments
    public OrderManager(InetSocketAddress managerAddress, InetSocketAddress[] orderRouters, InetSocketAddress[] clients, InetSocketAddress trader, LiveMarketData liveMarketData) {
        OrderManager.liveMarketData = liveMarketData;
        clientIDs = new HashMap<>();
        routerIDs = new HashMap<>();
        try {
            listener = new OrderManagerListener(managerAddress);
            messenger = new OrderManagerMessenger(trader);
        } catch (InterruptedException | IOException e) {
            e.printStackTrace();
        }

        int i = 0; //need a counter for the the output array
        this.orderRouters = new Socket[orderRouters.length];
        for (InetSocketAddress location : orderRouters) {
            routerIDs.put(location.getPort(),i);
            //this.orderRouters[i] = connect(location);
            i++;
        }

        i = 0;
        this.clients = new Socket[clients.length];
        for (InetSocketAddress location : clients) {
            clientIDs.put(location.getPort(),i);
            //this.clients[i] = connect(location);
            i++;
        }
    }

    int cyclenum = 0;

    public int getClientId(int port){
        return clientIDs.get(port);
    }
    public int getRouterId(int port){
        return routerIDs.get(port);
    }

    public void mainLoop() throws IOException, ClassNotFoundException, InterruptedException {
        boolean stillalive = true;
        while (stillalive) {
            //TODO (Kel): Can we think of a better polling technique than using 20 ns sleeps?
            //we want to use the arrayindex as the clientId, so use traditional for loop instead of foreach

//            pollClients();
//            pollRouters();
//            pollTraders();

            OrderManagerListener.OrderManagerMessage message = listener.receiveMessage();
            int source = message.source;
            int clientID, routerId;
            switch (message.method) { //determine the type of message and process it
                //call the newOrder message with the clientID and the message (clientMessageId,NewOrderSingle)
                case "newOrderSingle":
                    clientID = getClientId(source);
                    newOrder(clientID, message.clientOrderID, message.order);
                    break;
                case "cancelOrder":
                    clientID = getClientId(source);
                    cancelOrder(clientID, message.clientOrderID);
                    break;
                case "bestPrice":
                    routerId = getRouterId(source);
                    long OrderId = message.orderID;
                    int SliceId = message.sliceID;

                    Order slice = orders.get(OrderId).slices.get(SliceId);
                    slice.bestPrices[routerId] = message.bestPrice;
                    slice.bestPriceCount++;
                    if (slice.bestPriceCount == slice.bestPrices.length)
                        reallyRouteOrder(SliceId, slice);
                    break;
                case "newFill":
                    long id = message.orderID;
                    newFill(id, message.sliceID, message.size, message.bestPrice);
                    //price(id,orders.get(id));
                    break;
                case "acceptOrder":
                    acceptOrder(message.orderID);
                    break;
                case "sliceOrder":
                    sliceOrder(message.orderID, message.size);
                    break;
                default:
                    System.err.println("Unknown message type: "+message.method);
                    break;
            }
            if (cyclenum % 500 == 0)
                routeUnfilledOrders();
            ++cyclenum;
            Thread.sleep(0, 20);
        }
    }

    void pollClients() throws IOException, ClassNotFoundException {
        for (int clientID = 0; clientID < this.clients.length; clientID++) { //check if we have data on any of the sockets
            Socket client = this.clients[clientID];
            if (0 < client.getInputStream().available()) { //if we have part of a message ready to read, assuming this doesn't fragment messages
                ObjectInputStream is = new ObjectInputStream(client.getInputStream()); //create an object inputstream, this is a pretty stupid way of doing it, why not create it once rather than every time around the loop
                String method = (String) is.readObject();
                System.out.println(Thread.currentThread().getName() + " calling " + method);
                switch (method) { //determine the type of message and process it
                    //call the newOrder message with the clientID and the message (clientMessageId,NewOrderSingle)
                    case "newOrderSingle":
                        newOrder(clientID, is.readInt(), (NewOrderSingle) is.readObject()); //newOrder (clientID,
                        break;
                    case "cancelOrder":
                        cancelOrder(clientID, is.readInt());
                        break;
                    default:
                        System.err.println("Unknown message type");
                        break;
                }
            }
        }
    }

    void pollRouters() throws IOException, ClassNotFoundException {
        for (int routerId = 0; routerId < this.orderRouters.length; routerId++) { //check if we have data on any of the sockets
            Socket router = this.orderRouters[routerId];
            if (0 < router.getInputStream().available()) { //if we have part of a message ready to read, assuming this doesn't fragment messages
                ObjectInputStream is = new ObjectInputStream(router.getInputStream()); //create an object inputstream, this is a pretty stupid way of doing it, why not create it once rather than every time around the loop
                String method = (String) is.readObject();
                System.out.println(Thread.currentThread().getName() + " calling " + method);
                switch (method) { //determine the type of message and process it
                    case "bestPrice":
                        long OrderId = is.readLong();
                        int SliceId = is.readInt();

                        Order slice = orders.get(OrderId).slices.get(SliceId);
                        slice.bestPrices[routerId] = is.readDouble();
                        slice.bestPriceCount++;
                        if (slice.bestPriceCount == slice.bestPrices.length)
                            reallyRouteOrder(SliceId, slice);
                        break;
                    case "newFill":
                        long id = is.readLong();
                        newFill(id, is.readInt(), is.readInt(), is.readDouble());
                        //price(id,orders.get(id));
                        break;
                }
            }
        }
    }

    void pollTraders() throws IOException, ClassNotFoundException {
        if (0 < this.trader.getInputStream().available()) {
            ObjectInputStream is = new ObjectInputStream(this.trader.getInputStream());
            String method = (String) is.readObject();
            System.out.println(Thread.currentThread().getName() + " calling " + method);
            switch (method) {
                case "acceptOrder":
                    acceptOrder(is.readLong());
                    break;
                case "sliceOrder":
                    sliceOrder(is.readLong(), is.readInt());
                    break;
            }
        }
    }

    void routeUnfilledOrders() throws IOException {
        for (Order order : orders.values())
        {
            if (order.sizeRemaining() == 0) continue;
            for (int i = 0; i < order.slices.size(); ++i)
            {
                if (order.slices.get(i).sizeRemaining() < 0) continue;
                routeOrder(order.getId(),i,order.slices.get(i).sizeRemaining(),order.slices.get(i));
            }
        }
    }

    private void newOrder(int clientID, int clientOrderId, NewOrderSingle nos) throws IOException {
        Order order = new Order(clientID, nos.getSide(), nos.getInstrument(), nos.getSize(), clientOrderId);
        orders.put(order.getId(), order);
        messenger.newOrder(order.getClientOrderID());
        //send a message to the client with 39=A; //OrdStatus is Fix 39, 'A' is 'Pending New'
//        ObjectOutputStream os = new ObjectOutputStream(clients[clientID].getOutputStream());

        //ClOrdId is 11=
//        os.writeObject("11=" + clientOrderId + ";35=A;39=A;"); //FIXME (Kel): Should this be an *object* output steam?
//        os.flush();
        sendOrderToTrader(order, TradeScreen.api.newOrder);
        //send the new order to the trading screen
        //don't do anything else with the order, as we are simulating high touch orders and so need to wait for the trader to accept the order
    }

    private void sendOrderToTrader(Order order, TradeScreen.api method) throws IOException {
//        ObjectOutputStream ost = new ObjectOutputStream(trader.getOutputStream());
//        ost.writeObject(method);
//        ost.writeObject(o);
//        ost.flush();
        messenger.sendOrderToTrader(order, method);
    }

//    private void sendOrderToTrader(long id, Object method) throws IOException {
//        ObjectOutputStream ost = new ObjectOutputStream(trader.getOutputStream());
//        ost.writeObject(method);
//        ost.writeLong(id);
//        ost.flush();
//    }

    public void acceptOrder(long id) throws IOException {
        Order o = orders.get(id);
        if (o.getOrdStatus() != 'A') { //Pending New
            System.out.println("error accepting order that has already been accepted");
            return;
        }
        o.setOrdStatus('0'); //New
        messenger.acceptOrder(o.getClientOrderID());
//        ObjectOutputStream os = new ObjectOutputStream(clients[o.getClientID()].getOutputStream());
        //newOrderSingle acknowledgement
        //ClOrdId is 11=
//        os.writeObject("11=" + o.getClientOrderID() + ";35=A;39=0");
        //os.flush();

        price(id);
    }

    public void sliceOrder(long id, int sliceSize) throws IOException {
        Order order = orders.get(id);
        //slice the order. We have to check this is a valid size.
        //Order has a list of slices, and a list of fills, each slice is a child order and each fill is associated with either a child order or the original order
        OrderSlicer.sliceOrder(order, sliceSize);
        OrderSlicer.sliceOrder(order, order.getSize() - order.sliceSizes());
        //internalCross(id, slice);
        for (int sliceID = 0; sliceID < order.slices.size(); ++sliceID) {
            internalCross(sliceID, order);
            Order slice = order.slices.get(sliceID);
            System.out.println("Order " + id + ", slice " + sliceID + ": Size " + slice.getSize() + ", Filled " + slice.sizeFilled() + ", Remaining " + slice.sizeRemaining());
            if (slice.sizeRemaining() > 0)
                routeOrder(id, sliceID, order.slices.get(sliceID).sizeRemaining(), order.slices.get(sliceID));
        }
    }

    private void internalCross(int sliceID, Order o) throws IOException {
        for (Map.Entry<Long, Order> entry : orders.entrySet()) {
            Order matchingOrder = entry.getValue();
            if (matchingOrder.getInstrumentRIC().equals(o.getInstrumentRIC()) && matchingOrder.initialMarketPrice == o.initialMarketPrice && matchingOrder.getSide() != o.getSide() && matchingOrder.getOrdStatus() != 'A') {
                //TODO add support here and in Order for limit orders
                System.out.println("OrderManager calling internal cross " + o.getId() + " on " + matchingOrder.getId());
                int sizeBefore = o.sizeRemaining();
                o.cross(sliceID, matchingOrder);
                if (sizeBefore != o.sizeRemaining()) {
                    sendOrderToTrader(o, TradeScreen.api.cross);
                }
            }
        }
    }

    private void cancelOrder(int clientID, int clientOrderID) {
        System.out.println("Cancel received from client  " + clientID + " on client order " + clientOrderID);
        Order orderToCancel = null;
        for (Map.Entry<Long, Order> orderEntry : orders.entrySet()){
            Order order = orderEntry.getValue();
            if (order.getClientID() == clientID && order.getClientOrderID() == clientOrderID) {
                orderToCancel = order;
                break;
            }
        }
        orders.remove(orderToCancel.getId());
    }

    private void newFill(long id, int sliceId, int size, double price) throws IOException {
        Order o = orders.get(id);
        o.slices.get(sliceId).createFill(size, price);
        System.out.println("Order " + id + " size = " + o.getSize() + " size filled = " + o.sizeFilled());
        sendOrderToTrader(o, TradeScreen.api.fill);
    }

    private void routeOrder(long id, int sliceId, int size, Order order) throws IOException {
        for (Socket r : orderRouters) {
            messenger.routeOrder(id,sliceId,size,order.getInstrument());
//            ObjectOutputStream os = new ObjectOutputStream(r.getOutputStream());
//            os.writeObject(Router.api.priceAtSize);
//            os.writeLong(id);
//            os.writeInt(sliceId);
//            os.writeObject(order.getInstrument());
//            os.writeInt(size);
//            os.flush();
        }
        //need to wait for these prices to come back before routing
        order.bestPrices = new double[orderRouters.length];
        order.bestPriceCount = 0;
    }

    private void reallyRouteOrder(int sliceId, Order o) throws IOException {
        //TODO this assumes we are buying rather than selling
        int minIndex = 0;
        double min = o.bestPrices[0];
        for (int i = 1; i < o.bestPrices.length; i++) {
            if (min > o.bestPrices[i]) {
                minIndex = i;
                min = o.bestPrices[i];
            }
        }
        ObjectOutputStream os = new ObjectOutputStream(orderRouters[minIndex].getOutputStream());
        os.writeObject(Router.api.routeOrder);
        os.writeLong(o.getId());
        os.writeInt(sliceId);
        os.writeInt(o.sizeRemaining());
        os.writeObject(o.getInstrument());
        os.flush();
    }

    private void sendCancel(Order order, Router orderRouter) {
        //orderRouter.sendCancel(order);
        //order.orderRouter.writeObject(order);
    }

    private void price(long id) throws IOException {
        liveMarketData.setPrice(orders.get(id));
        sendOrderToTrader(orders.get(id), TradeScreen.api.price);
    }
}
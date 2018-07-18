package OrderManager;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.*;

import Database.Database;
import LiveMarketData.LiveMarketData;
import OrderClient.NewOrderSingle;
import OrderRouter.Router;
import TradeScreen.TradeScreen;

public class OrderManager {
    private static LiveMarketData liveMarketData;
    private HashMap<Long, Order> orders = new HashMap<>(); //debugger will do this line as it gives state to the object
    //currently recording the number of new order messages we get. TODO why? use it for more?
    private long id;
    private Socket[] orderRouters;
    private Socket[] clients;
    private Socket trader;

    private Socket connect(InetSocketAddress location) {
        boolean connected = false;
        int tryCounter = 0;
        while (!connected && tryCounter < 600) { //FIXME (Kel) Why are we trying this number of times?
            try {
                Socket s = new Socket(location.getHostName(), location.getPort());
                s.setKeepAlive(true);
                return s;
            } catch (IOException e) {
                //Thread.sleep(1000);
                tryCounter++;
            }
        }
        System.out.println("Failed to connect to " + location.toString());
        return null;
    }

    //@param args the command line arguments
    public OrderManager(InetSocketAddress[] orderRouters, InetSocketAddress[] clients, InetSocketAddress trader, LiveMarketData liveMarketData) {
        OrderManager.liveMarketData = liveMarketData;
        this.trader = connect(trader);

        this.orderRouters = new Socket[orderRouters.length];

        int i = 0; //need a counter for the the output array
        for (InetSocketAddress location : orderRouters) {
            this.orderRouters[i] = connect(location);
            i++;
        }

        this.clients = new Socket[clients.length];
        i = 0;
        for (InetSocketAddress location : clients) {
            this.clients[i] = connect(location);
            i++;
        }
    }

    public void mainLoop() throws IOException, ClassNotFoundException, InterruptedException {
        boolean stillalive = true;
        while (stillalive) {
            //TODO (Kel): Can we think of a better polling technique than using 20 ns sleeps?
            //we want to use the arrayindex as the clientId, so use traditional for loop instead of foreach
            pollClients();
            pollRouters();
            pollTraders();
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

    void routeUnfilledOrders() {

    }

    private void newOrder(int clientID, int clientOrderId, NewOrderSingle nos) throws IOException {
        orders.put(id, new Order(id, clientID, nos.getSide(), nos.getInstrument(), nos.getSize(), clientOrderId));
        //send a message to the client with 39=A; //OrdStatus is Fix 39, 'A' is 'Pending New'
        ObjectOutputStream os = new ObjectOutputStream(clients[clientID].getOutputStream());

        //ClOrdId is 11=
        os.writeObject("11=" + clientOrderId + ";35=A;39=A;"); //FIXME (Kel): Should this be an *object* output steam?
        os.flush();
        sendOrderToTrader(orders.get(id), TradeScreen.api.newOrder);
        //send the new order to the trading screen
        //don't do anything else with the order, as we are simulating high touch orders and so need to wait for the trader to accept the order
        id++;
    }

    private void sendOrderToTrader(Order o, Object method) throws IOException {
        ObjectOutputStream ost = new ObjectOutputStream(trader.getOutputStream());
        ost.writeObject(method);
        ost.writeObject(o);
        ost.flush();
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
        ObjectOutputStream os = new ObjectOutputStream(clients[o.getClientID()].getOutputStream());
        //newOrderSingle acknowledgement
        //ClOrdId is 11=
        os.writeObject("11=" + o.getClientOrderID() + ";35=A;39=0");
        os.flush();

        price(id);
    }

    public void sliceOrder(long id, int sliceSize) throws IOException {
        Order order = orders.get(id);
        //slice the order. We have to check this is a valid size.
        //Order has a list of slices, and a list of fills, each slice is a child order and each fill is associated with either a child order or the original order
        OrderSlicer.sliceOrder(order, sliceSize);
        OrderSlicer.sliceOrder(order, order.getSize() - order.sliceSizes());
        //internalCross(id, slice);
        for (int sliceId = 0; sliceId < order.slices.size(); ++sliceId) {
            internalCross(sliceId, order);
            if (order.slices.get(sliceId).sizeRemaining() > 0)
                routeOrder(id, sliceId, order.slices.get(sliceId).sizeRemaining(), order.slices.get(sliceId));
        }
    }

    private void internalCross(long id, Order o) throws IOException {
        for (Map.Entry<Long, Order> entry : orders.entrySet()) {
            Order matchingOrder = entry.getValue();
            if (matchingOrder.getInstrumentRIC().equals(o.getInstrumentRIC()) && matchingOrder.initialMarketPrice == o.initialMarketPrice && matchingOrder.getSide() != o.getSide()) {
                //TODO add support here and in Order for limit orders
                int sizeBefore = o.sizeRemaining();
                o.cross(matchingOrder);
                if (sizeBefore != o.sizeRemaining()) {
                    sendOrderToTrader(o, TradeScreen.api.cross);
                }
            }
        }
    }

    /*private void cancelOrder() {

    }*/

    private void newFill(long id, int sliceId, int size, double price) throws IOException {
        Order o = orders.get(id);
        //This is horrible practice, what we're doing here is ensuring that a new fill for a slice
        //is also present on the parent, moreover we're ensuring that we can later find out which slices fills
        //came from by comparing the unique ID of the fills. This should happen automatically in the createFill
        //method, though this would require changing the syntax of cr
        o.slices.get(sliceId).createFill(size, price);
        if (o.sizeRemaining() > 0)
            o.setOrdStatus('1');
        else
            o.setOrdStatus('2');
        if (o.sizeRemaining() == 0) {
            Database.write(o);
        }
        System.out.println("Order " + id + " size = " + o.getSize() + " size filled = " + o.sizeFilled());
        sendOrderToTrader(o, TradeScreen.api.fill);
    }

    private void routeOrder(long id, int sliceId, int size, Order order) throws IOException {
        for (Socket r : orderRouters) {
            ObjectOutputStream os = new ObjectOutputStream(r.getOutputStream());
            os.writeObject(Router.api.priceAtSize);
            os.writeLong(id);
            os.writeInt(sliceId);
            os.writeObject(order.getInstrument());
            os.writeInt(size);
            os.flush();
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
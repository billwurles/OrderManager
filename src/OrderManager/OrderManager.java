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
import java.util.logging.Level;
import java.util.logging.Logger;

public class OrderManager {
    private static LiveMarketData liveMarketData;
    private HashMap<Long, Order> orders = new HashMap<>(); //debugger will do this line as it gives state to the object
    private Socket[] orderRouters;
    private Socket[] clients;
    private Socket trader;
    private int cyclenum = 0;
    private final static Logger logger = Logger.getLogger(OrderManager.class.getName());

    /**
     *
     * @param location
     * @return
     */
    private Socket connect(InetSocketAddress location) {
        logger.entering(getClass().getName(), "connecting to socket in OrderManager");
        boolean connected = false;
        int tryCounter = 0;
        while (!connected && tryCounter < 600) { //FIXME (Kel) Why are we trying this number of times?
            try {
                Socket s = new Socket(location.getHostName(), location.getPort());
                s.setKeepAlive(true);
                return s;
            } catch (IOException e) {
                logger.log(Level.SEVERE, "Error trying to connect in class OrderManager", e);
                tryCounter++;
            }
        }
        System.out.println("Failed to connect to " + location.toString());
        logger.exiting(getClass().getName(), "exiting connecting to socket in OrderManager");
        return null;
    }


    /**
     *
     * @param orderRouters
     * @param clients
     * @param trader
     * @param liveMarketData
     */
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

    /**
     *
     * @throws IOException
     * @throws ClassNotFoundException
     * @throws InterruptedException
     */
    public void mainLoop() throws IOException, ClassNotFoundException, InterruptedException {
        logger.info("Entering the main loop");
        boolean stillalive = true;
        while (stillalive) {
            //TODO (Kel): Can we think of a better polling technique than using 20 ns sleeps?
            //we want to use the arrayindex as the clientId, so use traditional for loop instead of foreach
            pollClients();
            pollRouters();
            pollTraders();
            if (cyclenum % 500 == 0)
                routeUnfilledOrders();
            ++cyclenum;
            Thread.sleep(0, 20);
        }
    }

    /**
     *
     * @throws IOException
     * @throws ClassNotFoundException
     */
    void pollClients() throws IOException, ClassNotFoundException {
        logger.info("Initializing pollClients");
        for (int clientID = 0; clientID < this.clients.length; clientID++) { //check if we have data on any of the sockets
            Socket client = this.clients[clientID];
            if (0 < client.getInputStream().available()) { //if we have part of a message ready to read, assuming this doesn't fragment messages
                ObjectInputStream is = new ObjectInputStream(client.getInputStream()); //create an object inputstream, this is a pretty stupid way of doing it, why not create it once rather than every time around the loop
                String method = (String) is.readObject();

                System.out.println(Thread.currentThread().getName() + " calling " + method);
                switch (method) {
                    case "newOrderSingle":
                        newOrder(clientID, is.readInt(), (NewOrderSingle) is.readObject()); //newOrder (clientID,
                        break;

                    default:
                        System.err.println("Unknown message type");
                        logger.log(Level.SEVERE, "Unknown message type");
                        break;
                }
                String[]methodCancel = method.split("=");

                switch (methodCancel[0]) {
                    case "cancelOrder":
                        cancelOrder(Long.parseLong(methodCancel[1]));
                        break;
                }
            }
        }
    }

    /**
     *
     * @throws IOException
     * @throws ClassNotFoundException
     */
    void pollRouters() throws IOException, ClassNotFoundException {
        logger.info("Initializing pollRouters");
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
                        orders.get(id).unlockOrder();
                        newFill(id, is.readInt(), is.readInt(), is.readDouble());
                        break;
                }
            }
        }
    }

    /**
     *
     * @throws IOException
     * @throws ClassNotFoundException
     */
    void pollTraders() throws IOException, ClassNotFoundException {
        logger.info("Initializing pollTraders");
        if (0 < this.trader.getInputStream().available()) {
            logger.info("InputStream available");
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
                case "cancel":
                    cancelOrder(is.readLong());
                    break;
            }
        }
        else{
            logger.info("No InputStream available");
        }
    }

    void crossUnfilledOrders() {

    }

    /**
     *
     * @throws IOException
     */
    void routeUnfilledOrders() throws IOException {
        logger.info("UnfilledOrder");
        for (Order order : orders.values())
        {
            order.lockOrder();
            if (order.sizeRemaining() == 0) continue;
            for (int i = 0; i < order.slices.size(); ++i) {
                if (order.slices.get(i).sizeRemaining() <= 0) continue;
                routeOrder(order.getId(), i, order.slices.get(i).sizeRemaining(), order.slices.get(i));
            }
        }
    }

    /**
     *
     * @param clientID
     * @param clientOrderId
     * @param nos
     * @throws IOException
     */
    private void newOrder(int clientID, int clientOrderId, NewOrderSingle nos) throws IOException {
        logger.info("NewOrder");
        Order order = new Order(clientID, nos.getSide(), nos.getInstrument(), nos.getSize(), clientOrderId);
        orders.put(order.getId(), order);
        ObjectOutputStream os = new ObjectOutputStream(clients[clientID].getOutputStream());

        os.writeObject("11=" + clientOrderId + ";35=A;39=A;"); //FIXME (Kel): Should this be an *object* output steam?
        os.flush();
        sendOrderToTrader(order, TradeScreen.api.newOrder);
}

    /**
     *
     * @param o
     * @param method
     * @throws IOException
     */
    private void sendOrderToTrader(Order o, Object method) throws IOException {
        logger.info("send order to trader");
        ObjectOutputStream ost = new ObjectOutputStream(trader.getOutputStream());
        ost.writeObject(method);
        ost.writeObject(o);
        ost.flush();
    }

    /**
     *
     * @param id
     * @throws IOException
     */
    public void acceptOrder(long id) throws IOException {
        logger.info("accept order");
        Order o = orders.get(id);
        if (o != null) {
            if (o.getOrdStatus() != 'A') { //Pending New
                System.out.println("error accepting order that has already been accepted");
                return;
            }
            o.setOrdStatus('0'); //New
            ObjectOutputStream os = new ObjectOutputStream(clients[o.getClientID()].getOutputStream());
            os.writeObject("11=" + o.getClientOrderID() + ";35=A;39=0");
            os.flush();

            price(id);
        }
        else{
            logger.info("orders are null");
        }
    }

    /**
     *
     * @param id
     * @param sliceSize
     * @throws IOException
     */
    public void sliceOrder(long id, int sliceSize) throws IOException {
        logger.info("slicing order in OrderManager");
        Order order = orders.get(id);
        //slice the order. We have to check this is a valid size.
        //Order has a list of slices, and a list of fills, each slice is a child order and each fill is associated with either a child order or the original order
        OrderSlicer.sliceOrder(order, sliceSize);
        OrderSlicer.sliceOrder(order, order.getSize() - order.sliceSizes());
        //internalCross(id, slice);
        for (int sliceID = 0; sliceID < order.slices.size(); ++sliceID) {
            if (!order.getLockState())
                internalCross(sliceID, order);
            Order slice = order.slices.get(sliceID);
            System.out.println("Order " + id + ", slice " + sliceID + ": Size " + slice.getSize() + ", Filled " + slice.sizeFilled() + ", Remaining " + slice.sizeRemaining());
            if (slice.sizeRemaining() > 0) {
                order.lockOrder();
                routeOrder(id, sliceID, order.slices.get(sliceID).sizeRemaining(), order.slices.get(sliceID));
            }
        }
    }

    /**
     *
     * @param sliceID
     * @param o
     * @throws IOException
     */
    private void internalCross(int sliceID, Order o) throws IOException {
        logger.info("InternalCross");
        for (Order matchingOrder : orders.values()) {
            if (!matchingOrder.getInstrumentRIC().equals(o.getInstrumentRIC())) continue;
            if (matchingOrder.initialMarketPrice != o.initialMarketPrice) continue;
            if (matchingOrder.getSide() == o.getSide()) continue;
            if (matchingOrder.getOrdStatus() == 'A') continue;
            if (matchingOrder.getLockState()) continue;
            if (o.getLockState()) continue;

            //TODO add support here and in Order for limit orders
            System.out.println("OrderManager calling internal cross " + o.getId() + " on " + matchingOrder.getId());
            int sizeBefore = o.sizeRemaining();
            Order currentSlice = o.slices.get(sliceID);
            int currentSliceRemaining = currentSlice.sizeRemaining();
            if (currentSliceRemaining > 0 && matchingOrder.sizeRemaining() > 0 && matchingOrder.slices.size() > 0) {
                for (int matchingSliceId = 0; matchingSliceId < matchingOrder.slices.size(); ++matchingSliceId ) {
                    int matchingSliceRemaining = matchingOrder.slices.get(matchingSliceId).sizeRemaining();
                    if (matchingSliceRemaining > 0) {
                        int amountToFill = Math.min(currentSliceRemaining, matchingSliceRemaining);
                        newFill(o.getId(), sliceID, amountToFill, currentSlice.initialMarketPrice);
                        newFill(matchingOrder.getId(), matchingSliceId, amountToFill, currentSlice.initialMarketPrice);
                        currentSliceRemaining = currentSlice.sizeRemaining();
                        if (currentSliceRemaining == 0 || matchingOrder.sizeRemaining() == 0)
                            break;
                    }
                }
            }
            //o.cross(sliceID, matchingOrder);
            //newFill(o.getId(), sliceID, )
            if (sizeBefore != o.sizeRemaining()) {
                sendOrderToTrader(o, TradeScreen.api.cross);
            }
        }
    }

    private void cancelOrder(int clientID, int clientOrderID) {
        System.out.println("Cancel received from client  " + clientID + " on client order " + clientOrderID);
        Order orderToCancel = null;
        for (Map.Entry<Long, Order> orderEntry : orders.entrySet()) {
            Order order = orderEntry.getValue();
            if (order.getClientID() == clientID && order.getClientOrderID() == clientOrderID) {
                orderToCancel = order;
                break;
            }
        }
        orders.remove(orderToCancel.getId());
    }

    /**
     *
     * @param id
     * @param sliceId
     * @param size
     * @param price
     * @throws IOException
     */
    private void newFill(long id, int sliceId, int size, double price) throws IOException {
        Order o = orders.get(id);
        o.slices.get(sliceId).createFill(size, price);
        System.out.println("Order " + id + " size = " + o.getSize() + " size filled = " + o.sizeFilled());
        sendOrderToTrader(o, TradeScreen.api.fill);
        if (o.sizeRemaining() == 0) {
            SendMessageToClient(id, "F;");
        }
        else {
            SendMessageToClient(id, "P;");
        }

    }

    /**
     *
     * @param id
     * @param sliceId
     * @param size
     * @param order
     * @throws IOException
     */
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

    /**
     *
     * @param sliceId
     * @param o
     * @throws IOException
     */
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

    /**
     *
     * @param id
     * @throws IOException
     */
    private void price(long id) throws IOException {
        liveMarketData.setPrice(orders.get(id));
        sendOrderToTrader(orders.get(id), TradeScreen.api.price);
    }

    /**
     *
     * @param id
     * @param message
     */
    private void SendMessageToClient(long id,String message ) {
        logger.entering(getClass().getName(), "sending message to client");
        try {
            Order o = orders.get(id);
            ObjectOutputStream os = new ObjectOutputStream(clients[o.getClientID()].getOutputStream());
            os.writeObject(message + o.getClientOrderID() );
            os.flush();
        }catch (IOException e) {
            logger.log(Level.SEVERE, "Error trying to send message to client in class OrderManager", e);
            e.printStackTrace();}
        logger.exiting(getClass().getName(), "exiting sending message to client");
    }

    /**
     *
     * @param id
     * @throws IOException
     */
    public void cancelOrder(long id) throws IOException {
        logger.info("canceling order");
        Order o = orders.get(id);
        sendOrderToTrader(o, TradeScreen.api.cancel);
        orders.remove(id);
        if (orders.get(id) == null) {
            System.out.println("Order " + id + " has been cancelled");

        } else{
            logger.info("order was not cancelled");
            System.out.println("Order " + id + " has NOT been deleted");}

    }
}
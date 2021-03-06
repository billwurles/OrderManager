import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.logging.Level;
import java.util.logging.Logger;

import LiveMarketData.LiveMarketData;
import OrderManager.OrderManager;


class MockOM extends Thread {
    InetSocketAddress[] clients;
    InetSocketAddress[] routers;
    InetSocketAddress trader;
    LiveMarketData liveMarketData;
    private static final Logger logger = Logger.getLogger(MockOM.class.getName());
    /**
     *
     * @param name
     * @param routers
     * @param clients
     * @param trader
     * @param liveMarketData
     */
    MockOM(String name, InetSocketAddress[] routers, InetSocketAddress[] clients, InetSocketAddress trader, LiveMarketData liveMarketData) {
        this.clients = clients;
        this.routers = routers;
        this.trader = trader;
        this.liveMarketData = liveMarketData;
        this.setName(name);
    }

    @Override
    public void run() {
        logger.entering(getClass().getName(), "entering run method");
        try {
            //In order to debug constructors you can do F5 F7 F5
            (new OrderManager(routers, clients, trader, liveMarketData)).mainLoop();
        } catch (IOException | ClassNotFoundException | InterruptedException ex) {
            logger.log(Level.SEVERE, "Error in run method", ex);
        }
        logger.exiting(getClass().getName(), "exiting run method");
    }
}
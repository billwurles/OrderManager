import java.net.InetSocketAddress;

import LiveMarketData.LiveMarketData;
import OrderClient.NewOrderSingle;
import Ref.Instrument;
import Ref.Ric;

import static java.lang.Thread.sleep;

public class Main {
    public static void main(String[] args) throws InterruptedException {
        System.out.println("TEST: this program tests ordermanager");

        InetSocketAddress orderManagerAddress = new InetSocketAddress("localhost",2040);

        InetSocketAddress trader = new InetSocketAddress("localhost", 2020);
        InetSocketAddress[] clients = {new InetSocketAddress("localhost", 2000)};
        InetSocketAddress[] routers = {new InetSocketAddress("localhost", 2010),
                                       new InetSocketAddress("localhost", 2011)};


        LiveMarketData liveMarketData = new SampleLiveMarketData();
        MockOM mock = new MockOM("Order Manager", routers, orderManagerAddress, clients, trader, liveMarketData);
        mock.start();
        //start sample clients
        (new MockClient("Client 1", clients[0], orderManagerAddress)).start();
        //(new MockClient("Client 2", 2001)).start();

        //start sample routers
        (new SampleRouter("Router LSE", routers[0], orderManagerAddress)).start();
        (new SampleRouter("Router BATE", routers[1], orderManagerAddress)).start();

        (new Trader("Trader James", trader, orderManagerAddress)).start();
        //start order manager



    }
}
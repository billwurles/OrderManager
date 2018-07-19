import java.net.InetSocketAddress;

import LiveMarketData.LiveMarketData;
import OrderClient.NewOrderSingle;
import Ref.Instrument;
import Ref.Ric;

import static java.lang.Thread.sleep;

public class Main {
    public static void main(String[] args) throws InterruptedException {
        System.out.println("TEST: this program tests ordermanager");


        InetSocketAddress trader = new InetSocketAddress("localhost", 2020);
        InetSocketAddress[] clients = {new InetSocketAddress("localhost", 2000)};
        InetSocketAddress[] routers = {new InetSocketAddress("localhost", 2010),
                                       new InetSocketAddress("localhost", 2011)};


        LiveMarketData liveMarketData = new SampleLiveMarketData();
        MockOM mock = new MockOM("Order Manager", routers, clients, trader, liveMarketData);
        mock.start();
        sleep(10000);
        //start sample clients
        (new MockClient("Client 1", clients[0])).start();
        //(new MockClient("Client 2", 2001)).start();

        //start sample routers
        (new SampleRouter("Router LSE", routers[0])).start();
        (new SampleRouter("Router BATE", routers[1])).start();

        (new Trader("Trader James", trader)).start();
        System.err.println("nonononononononoi");
        //start order manager



    }
}
import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.logging.Level;
import java.util.logging.Logger;

import LiveMarketData.LiveMarketData;
import OrderManager.OrderManager;

public class Main{
	public static void main(String[] args) throws IOException{
		System.out.println("TEST: this program tests ordermanager");

		//start sample clients
		MockClient client1=new MockClient("Client 1",2000);
		MockClient client2=new MockClient("Client 2",2001);
		SampleRouter router1=new SampleRouter("Router LSE",2010));
		SampleRouter router2=new SampleRouter("Router BATE",2011));

		Thread client1Thread = new Thread(client1);
		Thread client2Thread = new Thread(client1);
		client1Thread.start();
		client2Thread.start();
		
		//start sample routers
		router1.start();
		router2.start();

		Trader traderJames = new Trader("Trader James",2020);
		traderJames.start();

		//start order manager
		InetSocketAddress[] clients={new InetSocketAddress("localhost",2000),
		                     new InetSocketAddress("localhost",2001)};
		InetSocketAddress[] routers={new InetSocketAddress("localhost",2010),
		                     new InetSocketAddress("localhost",2011)};
		InetSocketAddress trader=new InetSocketAddress("localhost",2020);
		LiveMarketData liveMarketData=new SampleLiveMarketData();

		MockOM orderManager = new MockOM("Order Manager",routers,clients,trader,liveMarketData));
		Thread thread = new Thread(orderManager);
		thread.start();
	}
}
class MockClient implements Runnable{
	private int port;
	private String name;

	MockClient(String name,int port){
		this.port=port;
		this.name=name;
	}
	public void run(){
		try {
			SampleClient client=new SampleClient(port);
			if(port==2000){
				//TODO why does this take an arg?
				client.sendOrder(null);
				int id=client.sendOrder(null);
				//TODO client.sendCancel(id);
				client.messageHandler();
			}else{
				client.sendOrder(null);
				client.messageHandler();
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
}

class MockOM implements Runnable{
	private InetSocketAddress[] clients;
	private InetSocketAddress[] routers;
	private InetSocketAddress trader;
	private LiveMarketData liveMarketData;
	private String name;
	MockOM(String name,InetSocketAddress[] routers,InetSocketAddress[] clients,InetSocketAddress trader,LiveMarketData liveMarketData){
		this.clients=clients;
		this.routers=routers;
		this.trader=trader;
		this.liveMarketData=liveMarketData;
		this.name = name;
	}
	@Override
	public void run(){
		try{
			//In order to debug constructors you can do F5 F7 F5
			new OrderManager(routers,clients,trader,liveMarketData);
		}catch(IOException | ClassNotFoundException | InterruptedException ex){
			Logger.getLogger(this.name).log(Level.SEVERE,null,ex);
		}
	}
}
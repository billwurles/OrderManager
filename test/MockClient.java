import OrderClient.NewOrderSingle;
import OrderManager.Order;
import Ref.Instrument;
import Ref.Ric;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.Random;

class MockClient extends Thread{
	InetSocketAddress address;

	private static final Instrument[] INSTRUMENTS = {new Instrument(new Ric("VOD.L")), new Instrument(new Ric("BP.L")), new Instrument(new Ric("BT.L"))};
	private Random numberGenerator;

	public NewOrderSingle newMessage(int side, int size, float price, int instrumentID){
		return new NewOrderSingle(side, size, price, INSTRUMENTS[instrumentID]);
	}

	MockClient(String name, InetSocketAddress address){
		this.address=address;
		this.setName(name);
		this.numberGenerator = new Random();
	}

	public void run(){
		try {
			SampleClient client=new SampleClient(address);
			if (address.getPort() == 2000)
				client.sendOrder(newMessage(1, 100, 100.0f, 0));
			else
				client.sendOrder(newMessage(2, 100, 100.0f, 0));
//			for (int currentOrder = 0; currentOrder < 10; currentOrder++) {
//				client.sendOrder(newMessage(numberGenerator.nextInt(2), numberGenerator.nextInt(1000), numberGenerator.nextFloat() * 200, numberGenerator.nextInt(3)));
//			}
			client.messageHandler();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
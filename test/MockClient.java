import OrderClient.NewOrderSingle;
import Ref.Instrument;
import Ref.Ric;

import java.io.IOException;
import java.util.Random;

class MockClient extends Thread{
	int port;

	private static final Instrument[] INSTRUMENTS = {new Instrument(new Ric("VOD.L")), new Instrument(new Ric("BP.L")), new Instrument(new Ric("BT.L"))};
	private Random numberGenerator;

	public NewOrderSingle newMessage(int side, int size, float price, int instrumentID){
		return new NewOrderSingle(side, size, price, INSTRUMENTS[instrumentID]);
	}

	MockClient(String name,int port){
		this.port=port;
		this.setName(name);
		this.numberGenerator = new Random();
	}

	public void run(){
		try {
			SampleClient client=new SampleClient(port);
			//int numOrders = numberGenerator.nextInt(10);
			for (int currentOrder = 0; currentOrder < 10; currentOrder++) {
			int id = 	client.sendOrder(newMessage(numberGenerator.nextInt(2), numberGenerator.nextInt(1000), numberGenerator.nextFloat() * 200, numberGenerator.nextInt(3)));
				client.sendCancel(id);
				//			if(port==2000){
//				int id=client.sendOrder(newMessage(numberGenerator.nextInt(2), numberGenerator.nextInt(1000), numberGenerator.nextFloat()*200, numberGenerator.nextInt(3)));
//				//TODO client.sendCancel(id);200.0f
//				client.messageHandler();
//			}else{
//				client.sendOrder(null);
//				client.messageHandler();
//			}
			}
			client.messageHandler();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
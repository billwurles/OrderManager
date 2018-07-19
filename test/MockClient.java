import OrderClient.NewOrderSingle;
import Ref.Instrument;
import Ref.Ric;

import java.io.IOException;
import java.util.Random;
import java.util.logging.Logger;
import java.util.logging.Level;

class MockClient extends Thread{
	int port;

	private static final Instrument[] INSTRUMENTS = {new Instrument(new Ric("VOD.L")), new Instrument(new Ric("BP.L")), new Instrument(new Ric("BT.L"))};
	private Random numberGenerator;
	private static final Logger logger = Logger.getLogger(MockClient.class.getName());

	/**
	 *
	 * @param side
	 * @param size
	 * @param price
	 * @param instrumentID
	 * @return
	 */
	public NewOrderSingle newMessage(int side, int size, float price, int instrumentID){
		return new NewOrderSingle(side, size, price, INSTRUMENTS[instrumentID]);
	}

	/**
	 *
	 * @param name
	 * @param port
	 */
	MockClient(String name,int port){
		this.port=port;
		this.setName(name);
		this.numberGenerator = new Random();
	}

	public void run(){
		logger.entering(getClass().getName(), "entering run method");
		try {
			SampleClient client=new SampleClient(port);

			for (int currentOrder = 0; currentOrder < 10; currentOrder++) {
				client.sendOrder(newMessage(numberGenerator.nextInt(2), numberGenerator.nextInt(1000), numberGenerator.nextFloat() * 200, numberGenerator.nextInt(3)));
				//TODO client.sendCancel(id);200.0f
			}
            long a=9;
			client.sendCancel(a);
			client.messageHandler();

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			logger.log(Level.SEVERE, "Error run method in MockClient", e);
		}
		logger.exiting(getClass().getName(), "exiting run method");
	}
}
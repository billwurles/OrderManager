package OrderClient;

import OrderManager.Order;

import java.io.IOException;

public interface Client{
	//Outgoing messages
	int sendOrder(Object par0)throws IOException;
	void sendCancel(int id);
	
	//Incoming messages
	void partialFill(Order order);
	void fullyFilled(Order order);
	void cancelled(Order order);
	
	void messageHandler();
}
package OrderClient;

import OrderManager.Order;

<<<<<<< HEAD
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
=======
public interface Client {
    //Outgoing messages
    int sendOrder(Object par0) throws IOException;

    void sendCancel(int id);

    //Incoming messages
    void partialFill(Order order);

    void fullyFilled(Order order);

    void cancelled(Order order);

    void messageHandler();
>>>>>>> 380d7a761bc670f79c3aa1dbf8616e56fc5767c2
}
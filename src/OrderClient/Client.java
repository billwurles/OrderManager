package OrderClient;

import java.io.IOException;

import OrderManager.Order;

public interface Client {
    //Outgoing messages
    int sendOrder(Object par0) throws IOException;

    void sendCancel(long id) throws IOException;

    //Incoming messages
    void partialFill(Order order);

    void fullyFilled(Order order);

    void cancelled(Order order);

    void messageHandler();
}
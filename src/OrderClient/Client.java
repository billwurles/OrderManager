package OrderClient;

import java.io.IOException;

import OrderManager.Order;

public interface Client {
    //Outgoing messages
    int sendOrder(Object par0) throws IOException;

    void sendCancel(int id) throws IOException;
    //Incoming messages

    void partialFill(long order);

    void fullyFilled(long order);

    void cancelled(Order order);

    void messageHandler();
}
package TradeScreen;

import OrderManager.Order;

import java.io.IOException;

public interface TradeScreen {
    enum api {newOrder, price, fill, cross}

    void newOrder(int id, Order order) throws IOException;

    void acceptOrder(int id) throws IOException;

    void sliceOrder(int id, int sliceSize) throws IOException;

    void price(int id, Order o) throws InterruptedException, IOException;
}

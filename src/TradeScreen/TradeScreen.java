package TradeScreen;

import java.io.IOException;

import OrderManager.Order;

public interface TradeScreen {
    enum api {newOrder, price, fill, cross}

    void newOrder(Order order) throws IOException;

    void acceptOrder(long id) throws IOException;

    void sliceOrder(long id, int sliceSize) throws IOException;

    void price(Order o) throws InterruptedException, IOException;
}

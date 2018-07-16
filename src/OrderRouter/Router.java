package OrderRouter;

import java.io.IOException;

import OrderManager.Order;
import Ref.Instrument;

public interface Router {
    enum api {routeOrder, sendCancel, priceAtSize}

    void routeOrder(int id, int sliceId, int size, Instrument i) throws IOException;

    void sendCancel(int id, int sliceId, int size, Instrument i);

    void priceAtSize(int id, int sliceId, Instrument i, int size) throws IOException;

}

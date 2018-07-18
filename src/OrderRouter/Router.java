package OrderRouter;

import java.io.IOException;

import OrderManager.Order;
import Ref.Instrument;

public interface Router {
    enum api {routeOrder, sendCancel, priceAtSize}

    void routeOrder(long id, int sliceId, int size, Instrument i) throws IOException;

    void sendCancel(long id, int sliceId, int size, Instrument i);

    void priceAtSize(long id, int sliceId, Instrument i, int size) throws IOException;

}

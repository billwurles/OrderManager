package OrderRouter;

import Ref.Instrument;

import java.io.IOException;

public interface Router {
    enum api {routeOrder, sendCancel, priceAtSize}

    void routeOrder(int id, int sliceId, int size, Instrument i) throws IOException;

    void sendCancel(int id, int sliceId, int size, Instrument i);

    void priceAtSize(int id, int sliceId, Instrument i, int size) throws IOException;

}

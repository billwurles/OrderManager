package LiveMarketData;

import OrderManager.Order;
import Ref.Instrument;

public interface LiveMarketData {
    void setPrice(Order o);
}

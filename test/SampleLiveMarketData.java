import java.util.Random;

import LiveMarketData.LiveMarketData;
import OrderManager.Order;
import Ref.Instrument;

//TODO this should really be in its own thread
public class SampleLiveMarketData implements LiveMarketData {
    private static final Random RANDOM_NUM_GENERATOR = new Random();

    /**
     *
     * @param o
     */
    public void setPrice(Order o) {
        o.initialMarketPrice = 100;
        //o.initialMarketPrice = 199 * RANDOM_NUM_GENERATOR.nextDouble();
    }
}

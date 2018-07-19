package OrderClient;

import java.io.Serializable;

import Ref.Instrument;

public class NewOrderSingle implements Serializable {
    private int side;
    private int size;
    private float price;
    private Instrument instrument;

    /**
     *
     * @param side Which side the trade is on, 1=buy, 2=sell.
     * @param size How large the order is.
     * @param price The price of each security.
     * @param instrument The instrument being traded.
     */
    public NewOrderSingle(int side, int size, float price, Instrument instrument) {
        this.side = side;
        this.size = size;
        this.price = price;
        this.instrument = instrument;
    }

    /**
     *
     * @return Order side.
     */
    public int getSide() {
        return this.side;
    }

    /**
     *
     * @return
     */
    public int getSize() {
        return this.size;
    }

    /**
     *
     * @return
     */
    public float getPrice() {
        return this.price;
    }

    /**
     *
     * @return
     */
    public Instrument getInstrument() {
        return this.instrument;
    }
}
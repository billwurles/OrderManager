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
     * @param side
     * @param size
     * @param price
     * @param instrument
     */
    public NewOrderSingle(int side, int size, float price, Instrument instrument) {
        this.side = side;
        this.size = size;
        this.price = price;
        this.instrument = instrument;
    }

    /**
     *
     * @return
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
package OrderClient;

import java.io.Serializable;

import Ref.Instrument;

public class NewOrderSingle implements Serializable {
    private int side;
    private int size;
    private float price;
    private Instrument instrument;

    public NewOrderSingle(int side, int size, float price, Instrument instrument) {
        this.side = side;
        this.size = size;
        this.price = price;
        this.instrument = instrument;
    }

    public int getSide() {
        return this.side;
    }

    public int getSize() {
        return this.size;
    }

    public float getPrice() {
        return this.price;
    }

    public Instrument getInstrument() {
        return this.instrument;
    }
}
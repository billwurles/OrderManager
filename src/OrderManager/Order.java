package OrderManager;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicLong;

import Ref.Instrument;

public class Order implements Serializable {
    private long id; //TODO these should all be longs
    private static AtomicLong idCounter = new AtomicLong(0);
    private int clientID;
    private int side;
    private Instrument instrument;
    private int size;
    private int clientOrderID;
    private Order parentOrder = null;
    double[] bestPrices;
    int bestPriceCount;


    public double initialMarketPrice;
    ArrayList<Order> slices;
    private ArrayList<Fill> fills;
    private char OrdStatus = 'A'; //OrdStatus is Fix 39, 'A' is 'Pending New'

    /**
     *
     * @return
     */
    public int sliceSizes() {
        int totalSizeOfSlices = 0;
        for (Order c : slices) totalSizeOfSlices += c.size;
        return totalSizeOfSlices;
    }

    /**
     *
     * @param sliceSize
     * @return
     */
    public int newSlice(int sliceSize) {
        slices.add(new Order(id, clientID, side, instrument, sliceSize, clientOrderID, this));
        return slices.size() - 1;
    }

    /**
     *
     * @return
     */
    public int sizeFilled() {
        int filledSoFar = 0;
        for (Fill f : fills) { //TODO (Kel): Really? Scan through every fill every time?
            filledSoFar += f.size;
        }
        return filledSoFar;
    }

    /**
     *
     * @return
     */
    public int sizeRemaining() {
        return size - sizeFilled();
    }

    /**
     *
     * @return
     */
    //Status state;
    float price() {
        float sum = 0;
        for (Fill fill : fills) {
            sum += fill.price;
        }
        return sum / fills.size();
    }

    /**
     *
     * @param size
     * @param price
     * @return
     */

    Fill createFill(int size, double price) {
        Fill newFill = new Fill(size, price);
        parentOrder.fills.add(newFill);
        fills.add(newFill);
        if (sizeRemaining() == 0) {
            OrdStatus = '2';
        } else {
            OrdStatus = '1';
        }
        if (parentOrder.sizeRemaining() == 0) {
            parentOrder.OrdStatus = '2';
        } else {
            parentOrder.OrdStatus = '1';
        }
        return newFill;
    }

    /**
     *
     * @param matchingOrder
     */
    void cross(Order matchingOrder) {
        //pair slices first and then parent

        if (matchingOrder.slices.size() == 0)
        {
            matchingOrder.newSlice(matchingOrder.sizeRemaining());
        }

        for (Order slice : slices) {
            if (slice.sizeRemaining() == 0) continue;
            //TODO could optimise this to not start at the beginning every time
            for (Order matchingSlice : matchingOrder.slices) {
                int msze = matchingSlice.sizeRemaining();
                if (msze == 0) continue;
                int sze = slice.sizeRemaining();
                if (sze <= msze) {
                    slice.createFill(sze, initialMarketPrice);
                    matchingSlice.createFill(sze, initialMarketPrice);
                    break;
                }
                //sze>msze
                slice.createFill(msze, initialMarketPrice);
                matchingSlice.createFill(msze, initialMarketPrice);
            }
            int sze = slice.sizeRemaining();
            int mParent = matchingOrder.sizeRemaining() - matchingOrder.sliceSizes();
            if (sze > 0 && mParent > 0) {
                if (sze >= mParent) {
                    slice.createFill(sze, initialMarketPrice);
                    matchingOrder.createFill(sze, initialMarketPrice);
                } else {
                    slice.createFill(mParent, initialMarketPrice);
                    matchingOrder.createFill(mParent, initialMarketPrice);
                }
            }
            //no point continuing if we didn't fill this slice, as we must already have fully filled the matchingOrder
            if (slice.sizeRemaining() > 0) break;
        }
    }

    /**
     *
     * @param clientID
     * @param side
     * @param instrument
     * @param size
     * @param clientOrderID
     */
    public Order(int clientID, int side, Instrument instrument, int size, int clientOrderID) {
        this(idCounter.getAndIncrement(), clientID, side, instrument, size, clientOrderID,null);
    }

    /**
     *
     * @param id
     * @param clientID
     * @param side
     * @param instrument
     * @param size
     * @param clientOrderID
     */
    public Order(long id, int clientID, int side, Instrument instrument, int size, int clientOrderID) {
        this(id, clientID, side, instrument, size, clientOrderID,null);
    }

    /**
     *
     * @param id
     * @param clientID
     * @param side
     * @param instrument
     * @param size
     * @param clientOrderID
     * @param parentOrder
     */
    public Order(long id, int clientID, int side, Instrument instrument, int size, int clientOrderID, Order parentOrder) {
        this.id = id;
        this.clientID = clientID;
        this.side = side;
        this.instrument = instrument;
        this.size = size;
        this.clientOrderID = clientOrderID;
        this.parentOrder = parentOrder;
        fills = new ArrayList<>();
        slices = new ArrayList<>();
    }

    /**
     *
     * @return
     */
    public long getId() {
        return this.id;
    }

    /**
     *
     * @return
     */
    public int getClientID() {
        return this.clientID;
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
    public Instrument getInstrument() {
        return this.instrument;
    }

    /**
     *
     * @return
     */
    public String getInstrumentRIC() {
        return this.instrument.toString();
    }

    /**
     *
     * @return
     */
    public int getClientOrderID() {
        return this.clientOrderID;
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
    public char getOrdStatus() {
        return this.OrdStatus;
    }

    /**
     *
     * @param newStatus
     */
    public void setOrdStatus(char newStatus) {
        this.OrdStatus = newStatus;
    }
}


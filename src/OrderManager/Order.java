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
    private short orderRouter;
    private int clientOrderID;
    private Order parentOrder = null;
    private volatile boolean outAtRouter;
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
    void cross(int sliceID, Order matchingOrder) {
        //pair slices first and then parent
        Order currentSlice = slices.get(sliceID);
        int currentSliceRemaining = currentSlice.sizeRemaining();

        if (currentSliceRemaining > 0 && matchingOrder.sizeRemaining() > 0) {
            if (matchingOrder.slices.size() > 0) {
                for (Order matchingSlice : matchingOrder.slices) {
                    int matchingSliceRemaining = matchingSlice.sizeRemaining();
                    if (matchingSliceRemaining > 0) {
                        int amountToFill = Math.min(currentSliceRemaining, matchingSliceRemaining);
                        currentSlice.createFill(amountToFill, initialMarketPrice);
                        matchingSlice.createFill(amountToFill, initialMarketPrice);
                        currentSliceRemaining = currentSlice.sizeRemaining();
                        if (currentSliceRemaining == 0 || matchingOrder.sizeRemaining() == 0)
                            break;
                    }
                }
            }
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
        this(idCounter.getAndIncrement(), clientID, side, instrument, size, clientOrderID, null);
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
        this(id, clientID, side, instrument, size, clientOrderID, null);
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
        outAtRouter = false;
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

    public boolean getLockState() {
        return this.outAtRouter;
    }

    public void lockOrder() {
        this.outAtRouter = true;
    }

    public void unlockOrder() {
        this.outAtRouter = false;
    }
}


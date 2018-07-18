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
    private int clientOrderID; //TODO refactor to lowercase C
    private Order parentOrder = null;
    double[] bestPrices;
    int bestPriceCount;

    public double initialMarketPrice;
    ArrayList<Order> slices;
    private ArrayList<Fill> fills;
    private char OrdStatus = 'A'; //OrdStatus is Fix 39, 'A' is 'Pending New'


    public int sliceSizes() {
        int totalSizeOfSlices = 0;
        for (Order c : slices) totalSizeOfSlices += c.size;
        return totalSizeOfSlices;
    }

    public int newSlice(int sliceSize) {
        slices.add(new Order(id, clientID, side, instrument, sliceSize, clientOrderID, this));
        return slices.size() - 1;
    }

    public int sizeFilled() {
        int filledSoFar = 0;
        for (Fill f : fills) { //TODO (Kel): Really? Scan through every fill every time?
            filledSoFar += f.size;
        }
        /*for (Order c : slices) {
            filledSoFar += c.sizeFilled();
        }*/
        return filledSoFar;
    }

    public int sizeRemaining() {
        return size - sizeFilled();
    }


    //Status state;
    float price() {
        //TODO this is buggy as it doesn't take account of slices. Let them fix it
        float sum = 0;
        for (Fill fill : fills) {
            sum += fill.price;
        }
        return sum / fills.size();
    }

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

        /*if (sizeRemaining() > 0) {
            for (Order matchingSlice : matchingOrder.slices) {
                int msze = matchingSlice.sizeRemaining();
                if (msze == 0) continue;
                int sze = sizeRemaining();
                if (sze <= msze) {
                    createFill(sze, initialMarketPrice);
                    matchingSlice.createFill(sze, initialMarketPrice);
                    break;
                }
                //sze>msze
                createFill(msze, initialMarketPrice);
                matchingSlice.createFill(msze, initialMarketPrice);
            }
            int sze = sizeRemaining();
            int mParent = matchingOrder.sizeRemaining() - matchingOrder.sliceSizes();
            if (sze > 0 && mParent > 0) {
                if (sze >= mParent) {
                    createFill(sze, initialMarketPrice);
                    matchingOrder.createFill(sze, initialMarketPrice);
                } else {
                    createFill(mParent, initialMarketPrice);
                    matchingOrder.createFill(mParent, initialMarketPrice);
                }
            }
        }*/
    }

    /*void cancel() {
        //state=cancelled
    }*/

    public Order(int clientID, int side, Instrument instrument, int size, int clientOrderID) {
        this(idCounter.getAndIncrement(), clientID, side, instrument, size, clientOrderID);
    }

    public Order(long id, int clientID, int side, Instrument instrument, int size, int clientOrderID) {
        this.id = id;
        this.clientID = clientID;
        this.side = side;
        this.instrument = instrument;
        this.size = size;
        this.clientOrderID = clientOrderID;
        fills = new ArrayList<>();
        slices = new ArrayList<>();
    }

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

    public long getId() {
        return this.id;
    }

    public int getClientID() {
        return this.clientID;
    }

    public int getSide() {
        return this.side;
    }

    public Instrument getInstrument() {
        return this.instrument;
    }

    public String getInstrumentRIC() {
        return this.instrument.toString();
    }

    public int getClientOrderID() {
        return this.clientOrderID;
    }

    public int getSize() {
        return this.size;
    }

    public char getOrdStatus() {
        return this.OrdStatus;
    }

    public void setOrdStatus(char newStatus) {
        this.OrdStatus = newStatus;
    }
}


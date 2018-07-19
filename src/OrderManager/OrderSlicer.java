package OrderManager;
import java.util.logging.Level;
import java.util.logging.Logger;

public class OrderSlicer {
    private final static Logger logger = Logger.getLogger(OrderSlicer.class.getName());
    /**
     *
     * @param order
     * @param sliceSize
     * @return
     */
    static int sliceOrder(Order order, int sliceSize) //Slices the order and returns the sliceId
    {
        logger.setLevel(Level.INFO);
        logger.info("Slicing Order");
        System.out.println("Slice size: " + sliceSize + "\nUnfilled order size: " + order.sizeRemaining() + "\nSliced order size: " + order.sliceSizes());
        if (sliceSize > order.sizeRemaining() - order.sliceSizes()) {
            System.out.println("error sliceSize is bigger than remaining size to be filled on the order");
            throw new RuntimeException();
        }
        else {
            int sliceId = order.newSlice(sliceSize);
            logger.info("Slice id" + sliceId);
            return sliceId;
        }
    }
}

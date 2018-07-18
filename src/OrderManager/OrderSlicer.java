package OrderManager;

public class OrderSlicer {

    static int sliceOrder(Order order, int sliceSize) //Slices the order and returns the sliceId
    {
        System.out.println("Slice size: " + sliceSize + "\nUnfilled order size: " + order.sizeRemaining() + "\nSliced order size: " + order.sliceSizes());
        if (sliceSize > order.sizeRemaining() - order.sliceSizes()) {
            System.out.println("error sliceSize is bigger than remaining size to be filled on the order");
            throw new RuntimeException();
        }
        else {
            int sliceId = order.newSlice(sliceSize);
            return sliceId;
        }
    }
}

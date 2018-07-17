package OrderManager;

public class OrderSlicer {

    static int sliceOrder(Order order, int sliceSize) //Slices the order and returns the sliceId
    {
        if (sliceSize > order.sizeRemaining() - order.sliceSizes()) {
            System.out.println("error sliceSize is bigger than remaining size to be filled on the order");
            throw new RuntimeException();
        }
        int sliceId = order.newSlice(sliceSize);
        return sliceId;
    }
}

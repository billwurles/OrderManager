package OrderManager;

import Utilities.GCDMath;

import java.util.Arrays;

public class Basket {
    Order[] orders;
    /**
     * Contains the smallest satisfiable unit of the basket
    */
    int[] ratios;

    Basket(Order orders[]) {
        this.orders = orders;
        int[] sizes = new int[orders.length];
        for (int i = 0; i < orders.length; i++) {
            sizes[i] = orders[i].getSize();
        }
        int gcd = GCDMath.GCDArray(sizes);
        ratios = new int [orders.length];
        for (int i = 0; i < orders.length; i++) {
            ratios[i] = sizes[i]/gcd;
        }
    }



}

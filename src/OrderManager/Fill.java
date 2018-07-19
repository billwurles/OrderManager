package OrderManager;

import java.io.Serializable;
import java.util.concurrent.atomic.AtomicLong;

class Fill implements Serializable {
    static final AtomicLong count = new AtomicLong(0);
    final long id;
    final int size;
    final double price;

    Fill(int size, double price) {
        this.size = size;
        this.price = price;
        id = count.get();
        count.incrementAndGet();
    }
}

import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;

public class RouterInstrumentQuantities implements Runnable{

    final AtomicInteger[] INSTRUMENTQUANTITY;

    RouterInstrumentQuantities(AtomicInteger[] INSTRUMENTQUANTITY)
    {
        this.INSTRUMENTQUANTITY = INSTRUMENTQUANTITY;
    }


    /**
     * Perturbs the input such that it will not be negative
     */
    int perturbNonNegative(int input)
    {
        Random rand = new Random();
        int perturbation = rand.nextInt(10);
        if (rand.nextBoolean()) {
            //Add
            input += perturbation;
        }
        else {
            //Subtract
            if (input > perturbation)
                input -= perturbation;
            else
                input = 0;
        }
        return input;
    }

    @Override
    public void run() {
        while (true) {
            for (AtomicInteger atint : INSTRUMENTQUANTITY)
                atint.updateAndGet(i -> perturbNonNegative(i));
            try {
                Thread.sleep(20);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}

package Utilities;

import java.util.Arrays;

public class GCDMath {

    //Implements Euclid's algorithm
    public static int GCD(int a, int b){
        if (b < a)
            return GCD(b,a);
        if (b==a)
            return a;
        //We now know that a < b
        int multiplier = b/a;
        b = b - multiplier*a;
        if (b==0)
            return a;
        return (GCD(a,b));
    }

    public static int GCDArray (int[] numbers)
    {
        if (numbers.length == 2)
            return GCD(numbers[0],numbers[1]);
        return GCD(GCDArray(Arrays.copyOfRange(numbers,0,numbers.length-1)),numbers[numbers.length-1]);
    }
}

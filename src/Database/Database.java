package Database;

//TODO figure out how to make this abstract or an interface, but want the method to be static

// Exists only to print order details to screen after a new fill.
public class Database {
    public static void write(Object o) {
        System.out.println(o.toString());
    }
}
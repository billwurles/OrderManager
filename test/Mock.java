public class Mock {
    /**
     *
     * @param out
     */
    public static void show(String out) {
        System.err.println(Thread.currentThread().getName() + ":" + out);
    }
}
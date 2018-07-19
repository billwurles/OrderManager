import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.HashMap;
import javax.net.ServerSocketFactory;
import OrderManager.Order;
import TradeScreen.TradeScreen;
import java.util.logging.Logger;
import java.util.logging.Level;

public class Trader extends Thread implements TradeScreen {
    private HashMap<Long, Order> orders = new HashMap<>();
    private static Socket omConn;
    private int port;
    private  ObjectInputStream is;
    private ObjectOutputStream os;
    private static final Logger logger = Logger.getLogger(Trader.class.getName());


    /**
     *
     * @param name
     * @param port
     */
    Trader(String name, int port) {
        this.setName(name);
        this.port = port;
    }

    public void run() {
        logger.entering(getClass().getName(), "entering run method");
        //OM will connect to us
        try {
            omConn = ServerSocketFactory.getDefault().createServerSocket(port).accept();
            InputStream s = omConn.getInputStream(); //if i try to create an objectinputstream before we have data it will block
            while (true) {
                is = new ObjectInputStream(s);  //TODO check if we need to create each time. this will block if no data, but maybe we can still try to create it once instead of repeatedly
                api method = (api) is.readObject();
                System.out.println(Thread.currentThread().getName() + " calling: " + method);
                switch (method) {
                    case newOrder:
                        newOrder((Order) is.readObject());
                        break;
                    case price:
                        price((Order) is.readObject());
                        break;
                    case cross:
                        is.readObject();
                        break; //TODO
                    case fill:
                        Object o = is.readObject();
                        break; //TODO
                    case cancel:cancelOrderT((Order)is.readObject());break;
                }
            }
        } catch (IOException | ClassNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            logger.log(Level.SEVERE, "Error in run method", e);
        }
        logger.exiting(getClass().getName(), "exiting run method");
    }

    /**
     *
     * @param order
     * @throws IOException
     */
    public void newOrder(Order order) throws IOException {
        //TODO the order should go in a visual grid, but not needed for test purposes
        orders.put(order.getId(), order);
        acceptOrder(order.getId());
    }

    /**
     *
     * @param id
     * @throws IOException
     */
    public void acceptOrder(long id) throws IOException {
        os = new ObjectOutputStream(omConn.getOutputStream());
        os.writeObject("acceptOrder");
        os.writeLong(id);
        os.flush();
    }

    /**
     *
     * @param id
     * @param sliceSize
     * @throws IOException
     */
    @Override
    public void sliceOrder(long id, int sliceSize) throws IOException {
        os = new ObjectOutputStream(omConn.getOutputStream());
        os.writeObject("sliceOrder");
        os.writeLong(id);
        os.writeInt(sliceSize);
        os.flush();
    }

    /**
     *
     * @param o
     * @throws IOException
     */
    @Override
    public void price(Order o) throws IOException {
        //TODO should update the trade screen
        sliceOrder(o.getId(), o.sizeRemaining() / 2); //FIXME (Kel): Are we sure that we don't get rounding errors? Can we error here and send bad results?
    }

    /**
     *
     * @param order
     * @throws IOException
     */
    @Override
    public void cancelOrderT(Order order) throws IOException {
        os = new ObjectOutputStream(omConn.getOutputStream());
        os.writeObject("cancelOrder");
        os.flush();
    }
}

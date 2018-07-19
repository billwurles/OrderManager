import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.Random;
import javax.net.ServerSocketFactory;
import OrderRouter.Router;
import Ref.Instrument;
import Ref.Ric;
import java.util.logging.Logger;
import java.util.logging.Level;


public class SampleRouter extends Thread implements Router {
    private static final Random RANDOM_NUM_GENERATOR = new Random();
    private static final Instrument[] INSTRUMENTS = {new Instrument(new Ric("VOD.L")), new Instrument(new Ric("BP.L")), new Instrument(new Ric("BT.L"))};
    private Socket omConn;
    private int port;
    private ObjectInputStream is;
    private ObjectOutputStream os;
    private static final Logger logger = Logger.getLogger(SampleRouter.class.getName());


    /**
     *
     * @param name
     * @param port
     */
    public SampleRouter(String name, int port) {
        this.setName(name);
        this.port = port;
    }

    public void run() {
        //OM will connect to us
        logger.entering(getClass().getName(), "entering run method");
        try {
            omConn = ServerSocketFactory.getDefault().createServerSocket(port).accept();
            while (true) {
                is = new ObjectInputStream(omConn.getInputStream());
                Router.api methodName = (Router.api) is.readObject();
                System.out.println("Order Router received method call for:" + methodName);
                switch (methodName) {
                    case routeOrder:
                        routeOrder(is.readLong(), is.readInt(), is.readInt(), (Instrument) is.readObject());
                        break;
                    case priceAtSize:
                        priceAtSize(is.readLong(), is.readInt(), (Instrument) is.readObject(), is.readInt());
                        break;
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
     * @param id
     * @param sliceId
     * @param size
     * @param i
     * @throws IOException
     */
    @Override
    public void routeOrder(long id, int sliceId, int size, Instrument i) throws IOException { //MockI.show(""+order);
        int fillSize = RANDOM_NUM_GENERATOR.nextInt(size+1);
        System.err.println(currentThread().getName() + ", filled " + fillSize);
        //TODO have this similar to the market price of the instrument
        double fillPrice = 199 * RANDOM_NUM_GENERATOR.nextDouble();
        //Thread.sleep(42);
        os = new ObjectOutputStream(omConn.getOutputStream());
        os.writeObject("newFill");
        os.writeLong(id);
        os.writeInt(sliceId);
        os.writeInt(fillSize);
        os.writeDouble(fillPrice);
        os.flush();
    }

    /**
     *
     * @param id
     * @param sliceId
     * @param size
     * @param i
     */
    @Override
    public void sendCancel(long id, int sliceId, int size, Instrument i) { //MockI.show(""+order);
    }

    /**
     *
     * @param id
     * @param sliceId
     * @param i
     * @param size
     * @throws IOException
     */
    @Override
    public void priceAtSize(long id, int sliceId, Instrument i, int size) throws IOException {
        os = new ObjectOutputStream(omConn.getOutputStream());
        os.writeObject("bestPrice");
        os.writeLong(id);
        os.writeInt(sliceId);
        os.writeDouble(199 * RANDOM_NUM_GENERATOR.nextDouble());
        os.flush();
    }
}
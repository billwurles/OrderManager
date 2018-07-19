import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.Random;

import OrderRouter.Router;
import Ref.Instrument;
import Ref.Ric;
import Utilities.Listeners.RouterListener;
import Utilities.Messengers.RouterMessenger;

public class SampleRouter extends Thread implements Router {
    private static final Random RANDOM_NUM_GENERATOR = new Random();
    private static final Instrument[] INSTRUMENTS = {new Instrument(new Ric("VOD.L")), new Instrument(new Ric("BP.L")), new Instrument(new Ric("BT.L"))};
    //private Socket omConn;
    private InetSocketAddress address;
    private RouterListener listener;
    private RouterMessenger messenger;

    public SampleRouter(String name, InetSocketAddress address) {
        this.setName(name);
        this.address = address;

    }

//    private ObjectInputStream is;
//    private ObjectOutputStream os;

    public void run() {
        //OM will connect to us
        try {
            try {
                listener = new RouterListener(address);
                messenger = new RouterMessenger(address);
            } catch (InterruptedException | IOException e) {
                e.printStackTrace();
            }
            //omConn = ServerSocketFactory.getDefault().createServerSocket(port).accept();
            while (true) {
                RouterListener.RouterResponse response = listener.receiveResponse();
//                is = new ObjectInputStream(omConn.getInputStream());
//                Router.api methodName = (Router.api) is.readObject();
//                System.out.println("Order Router received method call for:" + methodName);
                switch (response.methodName) {
                    case routeOrder:
                        routeOrder(response.id, response.sliceID, response.size, response.instrument);
                        //routeOrder(is.readLong(), is.readInt(), is.readInt(), (Instrument) is.readObject());
                        break;
                    case priceAtSize:
                        priceAtSize(response.id, response.sliceID, response.instrument, response.size);
                        //priceAtSize(is.readLong(), is.readInt(), (Instrument) is.readObject(), is.readInt());
                        break;
                }
            }
        } catch (IOException | ClassNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    @Override
    public void routeOrder(long id, int sliceId, int size, Instrument i) throws IOException { //MockI.show(""+order);
        int fillSize = RANDOM_NUM_GENERATOR.nextInt(size+1);
        System.err.println(currentThread().getName() + ", filled " + fillSize);
        //TODO have this similar to the market price of the instrument
        double fillPrice = 199 * RANDOM_NUM_GENERATOR.nextDouble();
        //Thread.sleep(42);
        messenger.routeOrderMsg(id, sliceId, fillSize, fillPrice);
//        os = new ObjectOutputStream(omConn.getOutputStream());
//        os.writeObject("newFill");
//        os.writeLong(id);
//        os.writeInt(sliceId);
//        os.writeInt(fillSize);
//        os.writeDouble(fillPrice);
//        os.flush();
    }

    @Override
    public void sendCancel(long id, int sliceId, int size, Instrument i ) { //MockI.show(""+order);
    }

    @Override
    public void priceAtSize(long id, int sliceId, Instrument i, int size) throws IOException {
        messenger.priceAtSizeMsg(id,sliceId,199 * RANDOM_NUM_GENERATOR.nextDouble());
//        os = new ObjectOutputStream(omConn.getOutputStream());
//        os.writeObject("bestPrice");
//        os.writeLong(id);
//        os.writeInt(sliceId);
//        os.writeDouble();
//        os.flush();
    }
}
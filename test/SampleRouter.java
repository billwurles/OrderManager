import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.Random;

import javax.net.ServerSocketFactory;

import OrderManager.Order;
import OrderRouter.Router;
import Ref.Instrument;
import Ref.Ric;
import Utilities.RouterMessenger;

public class SampleRouter extends Thread implements Router {
    private static final Random RANDOM_NUM_GENERATOR = new Random();
    private static final Instrument[] INSTRUMENTS = {new Instrument(new Ric("VOD.L")), new Instrument(new Ric("BP.L")), new Instrument(new Ric("BT.L"))};

    private int port;
    RouterMessenger messenger;

    public SampleRouter(String name, String host, int port) {
        this.setName(name);
        this.port = port;
        try {
            messenger = new RouterMessenger(new InetSocketAddress(host,port));
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }



    public void run() {
        //OM will connect to us
        try {
//            omConn = ServerSocketFactory.getDefault().createServerSocket(port).accept();
            while (true) {
                RouterMessenger.RouterResponse response = messenger.receiveResponse();
                switch (response.methodName) {
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
        }
    }

    @Override
    public void routeOrder(long id, int sliceId, int size, Instrument i) throws IOException { //MockI.show(""+order);
        int fillSize = RANDOM_NUM_GENERATOR.nextInt(size);
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

    @Override
    public void sendCancel(long id, int sliceId, int size, Instrument i) { //MockI.show(""+order);
    }

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

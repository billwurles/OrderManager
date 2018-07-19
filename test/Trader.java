import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.HashMap;

import OrderManager.Order;
import TradeScreen.TradeScreen;
import Utilities.Listeners.TraderListener;
import Utilities.Messengers.TraderMessenger;

import javax.net.ServerSocketFactory;

public class Trader extends Thread implements TradeScreen {
    private HashMap<Long, Order> orders = new HashMap<>();
    private static Socket omConn;
    private TraderListener listener;
    private int port;

    Trader(String name, InetSocketAddress address) {
        this.setName(name);
        this.port = port;
        try {
            listener = new TraderListener(address);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

//    ObjectInputStream is;
    ObjectOutputStream os;

    public void run() {
        //OM will connect to us
        try {
            TraderListener.TraderResponse response = listener.receiveResponse();
            omConn = ServerSocketFactory.getDefault().createServerSocket(port).accept();
//
//            //is=new ObjectInputStream( omConn.getInputStream());
//            InputStream s = omConn.getInputStream(); //if i try to create an objectinputstream before we have data it will block
//            while (true) {
//                is = new ObjectInputStream(s);  //TODO check if we need to create each time. this will block if no data, but maybe we can still try to create it once instead of repeatedly
//                api method = (api) is.readObject();
//                System.out.println(Thread.currentThread().getName() + " calling: " + method);
            switch (response.method) {
                case newOrder:
                    newOrder(response.order);
                    break;
                case price:
                    price(response.order);
                    break;
                case cross:
                    //is.readObject();
                    break; //TODO
                case fill:
                    //Object o = is.readObject();
                    break; //TODO
            }
//            }
        } catch (IOException | ClassNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    //@Override
    public void newOrder(Order order) throws IOException {
        //TODO the order should go in a visual grid, but not needed for test purposes
        //Thread.sleep(2134);
        orders.put(order.getId(), order);
        acceptOrder(order.getId());
    }

    //@Override
    public void acceptOrder(long id) throws IOException {
        os = new ObjectOutputStream(omConn.getOutputStream());

        os.writeObject("acceptOrder");
        os.writeLong(id);
        os.flush();
    }

    @Override
    public void sliceOrder(long id, int sliceSize) throws IOException {
        os = new ObjectOutputStream(omConn.getOutputStream());
        os.writeObject("sliceOrder");
        os.writeLong(id);
        os.writeInt(sliceSize);
        os.flush();
    }

    @Override
    public void price(Order o) throws IOException {
        //TODO should update the trade screen
        //Thread.sleep(2134);
        sliceOrder(o.getId(), o.sizeRemaining() / 2); //FIXME (Kel): Are we sure that we don't get rounding errors? Can we error here and send bad results?
    }
}

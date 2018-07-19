package Utilities;

import OrderManager.Order;
import OrderRouter.Router;
import Ref.Instrument;
import TradeScreen.TradeScreen;

import java.io.*;
import java.net.InetSocketAddress;

public class TraderMessenger {

    SocketMessenger messenger;
    SocketListener listener;
    ByteArrayOutputStream baos;
    ObjectOutputStream output;
    ObjectInputStream input;

    /**
     *
     * @param address
     * @throws InterruptedException
     * @throws IOException
     */
    public TraderMessenger(InetSocketAddress address) throws InterruptedException, IOException {
        messenger = new SocketMessenger(address);
    }

    /**
     *
     * @param id
     * @throws IOException
     */
    public void acceptOrder(int id) throws IOException {
        baos = new ByteArrayOutputStream();
        output = new ObjectOutputStream(baos);

        output.writeObject("acceptOrder");
        output.writeInt(id);

        output.flush();
        messenger.sendMessage(baos.toByteArray());
    }

    /**
     *
     * @param id
     * @param sliceSize
     * @throws IOException
     */
    public void slideOrder(int id, int sliceSize) throws IOException {
        baos = new ByteArrayOutputStream();
        output = new ObjectOutputStream(baos);

        output.writeObject("sliceOrder");
        output.writeInt(id);
        output.writeInt(sliceSize);

        output.flush();
        messenger.sendMessage(baos.toByteArray());
    }

    /**
     *
     * @param id
     * @param sliceID
     * @param price
     * @throws IOException
     */
    public void priceAtSizeMsg(int id, int sliceID, double price) throws IOException {
        baos = new ByteArrayOutputStream();
        output = new ObjectOutputStream(baos);

        output.writeObject("bestPrice");
        output.writeInt(id);
        output.writeInt(sliceID);
        output.writeDouble(price);

        output.flush();
        messenger.sendMessage(baos.toByteArray());
    }

    /**
     *
     * @return
     * @throws IOException
     * @throws ClassNotFoundException
     */
    public TraderResponse receiveResponse() throws IOException, ClassNotFoundException {
        listener.listenForMessage();

        if(listener.hasResponse()) {
            byte[] response = listener.getResponse();
            input = new ObjectInputStream(new ByteArrayInputStream(response));

            return new TraderResponse((TradeScreen.api) input.readObject(), input.readInt(), (Order) input.readObject());

        } else {
            //TODO (Will) THROW SOME EXCEPTION
        }
        return new TraderResponse(null, Integer.MIN_VALUE, null);//FIXME This could cause issues
    }

//    public void sendCancel(int id){
//
//    }

    public class TraderResponse {
        public final TradeScreen.api method;
        public final int id;
        public final Order order;

        public TraderResponse(TradeScreen.api method, int id, Order order) {
            this.method = method;
            this.id = id;
            this.order = order;
        }
    }
}

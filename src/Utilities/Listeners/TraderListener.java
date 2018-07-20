package Utilities.Listeners;

import OrderManager.Order;
import TradeScreen.TradeScreen;
import Utilities.SocketConnectors.SocketListener;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.InetSocketAddress;

/**
 * Created by alumniCurie16 on 18/07/2018.
 */
public class TraderListener {

    SocketListener listener;
    ObjectInputStream input;
    InetSocketAddress address;

    public TraderListener(InetSocketAddress address) throws InterruptedException, IOException {
        listener = new SocketListener(address);
        this.address = address;
    }

    public TraderResponse receiveResponse() throws IOException, ClassNotFoundException {
        System.err.printf("\n\n\n%s Waiting --------------------------------------------\n\n\n",Thread.currentThread().getName());
        listener.listenForMessage();
        System.err.printf("\n\n\n%s Waited --------------------------------------------\n\n\n",Thread.currentThread().getName());

        if(listener.hasResponse()) {
            byte[] response = listener.getResponse();
            input = new ObjectInputStream(new ByteArrayInputStream(response));

            TraderResponse response1 = new TraderResponse((TradeScreen.api) input.readObject(), (Order) input.readObject());
            System.err.printf("ConnectionT %s recieved data from %s:%s\n%s: %s - %s\n",Thread.currentThread().getName(),address.getHostName(),address.getPort(),response1.method,response1.order.getSize(),response1.order.getInstrumentRIC());

            return response1;

        } else {
            //TODO (Will) THROW SOME EXCEPTION
        }
        return new TraderResponse(null, null);//FIXME This could cause issues
    }

    public class TraderResponse {
        public final TradeScreen.api method;
        public final Order order;

        public TraderResponse(TradeScreen.api method, Order order) {
            this.method = method;
            this.order = order;
        }
    }

}

package Utilities.Listeners;

import OrderRouter.Router;
import Ref.Instrument;
import Utilities.SocketConnectors.SocketListener;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.InetSocketAddress;

public class RouterListener {

    SocketListener listener;
    ObjectInputStream input;

    public RouterListener(InetSocketAddress address) throws InterruptedException {
        listener = new SocketListener(address);
    }

    public RouterResponse receiveResponse() throws IOException, ClassNotFoundException {
        listener.listenForMessage();

        if(listener.hasResponse()) {
            byte[] response = listener.getResponse();
            input = new ObjectInputStream(new ByteArrayInputStream(response));

            Router.api methodName = (Router.api) input.readObject();
            System.out.println("Order Router received method call for:" + methodName);

            long id = input.readInt();
            int sliceID = input.readInt();
            int size = input.readInt();
            Instrument instrument = (Instrument) input.readObject();
            return new RouterResponse(methodName, id, sliceID, size, instrument);

        }
        return new RouterResponse(); //FIXME This could cause issues (null values)
    }

    public void sendCancel(int id){

    }

    public class RouterResponse {
        public final Router.api methodName;
        public final int sliceID, size;
        public final long id;
        public final Instrument instrument;

        public RouterResponse() {
            this.methodName = null;
            this.id = Long.MIN_VALUE;
            this.sliceID = Integer.MIN_VALUE;
            this.size = Integer.MIN_VALUE;
            this.instrument = null;
        }

        public RouterResponse(Router.api methodName, long id, int sliceID, int size, Instrument instrument) {
            this.methodName = methodName;
            this.id = id;
            this.sliceID = sliceID;
            this.size = size;
            this.instrument = instrument;
        }

    }
}

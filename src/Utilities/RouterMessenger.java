package Utilities;


import OrderClient.NewOrderSingle;
import OrderRouter.Router;
import Ref.Instrument;

import java.io.*;
import java.net.InetSocketAddress;

public class RouterMessenger {

    SocketMessenger messenger;
    SocketListener listener;

    ByteArrayOutputStream baos;
    ObjectOutputStream output;
    ObjectInputStream input;

    public RouterMessenger(InetSocketAddress address) throws InterruptedException, IOException {
        messenger = new SocketMessenger(address);
    }

    public void routeOrderMsg(int id, int sliceID, int fillSize, double fillPrice) throws IOException {
        baos = new ByteArrayOutputStream();
        output = new ObjectOutputStream(baos);

        output.writeObject("newFill");
        output.writeInt(id);
        output.writeInt(sliceID);
        output.writeInt(fillSize);
        output.writeDouble(fillPrice);

        output.flush();
        messenger.sendMessage(baos.toByteArray());
    }

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

    public RouterResponse receiveResponse() throws IOException, ClassNotFoundException {
        listener.listenForMessage();
        int min = Integer.MIN_VALUE;

        if(listener.hasResponse()) {
            byte[] response = listener.getResponse();
            input = new ObjectInputStream(new ByteArrayInputStream(response));

            Router.api methodName = (Router.api) input.readObject();
            System.out.println("Order Router received method call for:" + methodName);
            int id,sliceID,fillSize,price;
            switch (methodName) {
                case routeOrder:
                    id = input.readInt();
                    sliceID = input.readInt();
                    fillSize = input.readInt();
                    Instrument instrument = (Instrument) input.readObject();
                    return new RouterResponse(methodName, id, sliceID, min, fillSize, instrument);
                case priceAtSize:
                    id = input.readInt();
                    sliceID = input.readInt();
                    instrument = (Instrument) input.readObject();
                    price = input.readInt();
                    return new RouterResponse(methodName, id, sliceID, price, min, instrument);
            }
        } else {
            //TODO (Will) THROW SOME EXCEPTION
        }
        return new RouterResponse(null,min,min,min,min,null); //TODO something better than this
    }

    public void sendCancel(int id){

    }

    public class RouterResponse {
        public final Router.api methodName;
        public final int id, sliceID, price, fillSize;
        final Instrument instrument;

        public RouterResponse(Router.api methodName, int id, int sliceID, int price, int fillSize,Instrument instrument) {
            this.methodName = methodName;
            this.id = id;
            this.sliceID = sliceID;
            this.price = price;
            this.fillSize = fillSize;
            this.instrument = instrument;
        }
    }
}
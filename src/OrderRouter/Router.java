package OrderRouter;

import Ref.Instrument;

import java.io.IOException;

public interface Router {
	public enum api{routeOrder,sendCancel,priceAtSize};
	public void routeOrder(int id,int sliceId,int size,Instrument i) throws IOException, InterruptedException;
	public void sendCancel(int id,int sliceId,int size,Instrument i);
	public void priceAtSize(int id, int sliceId,Instrument i, int size) throws IOException;
	
}

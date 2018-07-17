package TradeScreen;

import OrderManager.Order;

import java.io.IOException;

public interface TradeScreen {
	public enum api{newOrder,price,fill,cross};
	public void newOrder(int id,Order order) throws IOException, InterruptedException;
	public void acceptOrder(int id) throws IOException;
	public void sliceOrder(int id,int sliceSize) throws IOException;
	public void price(int id,Order o) throws InterruptedException, IOException;
}

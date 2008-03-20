package model.agents;

import sim.engine.SimState;
import model.FinancialModel;
import model.market.books.LimitOrder;
import model.market.books.OrderBook.OrderType;

/* Agent that randomly drops a random order at a random orderbook */

public class FarmerPlayer extends GenericPlayer {

	public FarmerPlayer() {

	}

	public void step(SimState state) {

		this.generateOrders();

	}

	private void generateOrders() {

		double rand = myWorld.random.nextDouble();

		if (rand < 0.5) {

			// Drop market order

			OrderType newType;
			int asset = myWorld.random.nextInt(myWorld.myMarket.orderBooks.size());
			rand = myWorld.random.nextDouble();
			if (rand < 0.5) {
				newType = OrderType.PURCHASE;
			} else {
				newType = OrderType.SALE;
			}
			int amount = 1 + myWorld.random.nextInt(10);
			myWorld.myMarket.acceptMarketOrder(newType, asset, amount);

		} else {

			// Drop limit order

			OrderType newType;
			double newPrice;
			int asset = myWorld.random.nextInt(myWorld.myMarket.orderBooks.size());

			rand = myWorld.random.nextDouble();

			if (rand < 0.5) {
				newType = OrderType.PURCHASE;
				newPrice = myWorld.random.nextDouble();

			} else {
				newType = OrderType.SALE;
				newPrice = myWorld.random.nextDouble();
			}

			double expirationTime = myWorld.schedule.getTime() - Math.log(myWorld.random.nextDouble());
			int amount = 1 + myWorld.random.nextInt(10);

			LimitOrder newOrder = new LimitOrder(this, newType, asset, newPrice, amount, expirationTime);

			myWorld.myMarket.acceptOrder(newOrder);

		}

	}

}

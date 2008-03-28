package model.agents;

import sim.engine.SimState;
import model.FinancialModel;
import model.market.books.LimitOrder;
import model.market.books.OrderBook.OrderType;
import support.Distributions;

/* Agent that randomly drops a random order at a random orderbook */

public class FarmerPlayer extends GenericPlayer {

	private Distributions randDist;

	public FarmerPlayer() {
		
	}

	public void step(SimState state) {
		
		if (this.randDist == null ) {
			randDist = new Distributions(myWorld.random);	
		}

		this.generateOrders();
		
		//double nextActivation = myWorld.schedule.getTime() + 

	}

	private void generateOrders() {

		// TODO: Use this: to decide how many orders to place, if you place 1
		// order every 10 ticks.
		// int numOrdersPlaced = randDist.nextPoisson(0.1);

		double rand = myWorld.random.nextDouble();

		if (rand < 0.45) {

			// Drop market order

			OrderType newType;
			int asset = myWorld.random.nextInt(myWorld.myMarket.orderBooks.size());
			rand = myWorld.random.nextDouble();
			if (rand < 0.5) {
				newType = OrderType.PURCHASE;
			} else {
				newType = OrderType.SALE;
			}
			int amount = 1;// + myWorld.random.nextInt(10);
			myWorld.myMarket.acceptMarketOrder(newType, asset, amount);

		} else {

			// Drop limit order

			OrderType newType;
			double newPrice;
			int asset = myWorld.random.nextInt(myWorld.myMarket.orderBooks.size());

			rand = myWorld.random.nextDouble();
            
			// just made this up.. needs to be adjusted ala Farmer
			double offset = 2*myWorld.random.nextDouble();
			//Math.abs(myWorld.random.nextGaussian());
			
			if (rand < 0.5) {
				
				// Try to buy shares 
				newType = OrderType.PURCHASE;
				newPrice = Math.max(myWorld.myMarket.orderBooks.get(asset).getAskPrice() - offset, myWorld.parameterMap.get("minPrice")); 

			} else {
				newType = OrderType.SALE;
				newPrice = Math.min(myWorld.myMarket.orderBooks.get(asset).getBidPrice() + offset, myWorld.parameterMap.get("maxPrice"));
			}

			double expirationTime = myWorld.schedule.getTime() + 10.0;
			int amount = 1;// + myWorld.random.nextInt(10);

			LimitOrder newOrder = new LimitOrder(this, newType, asset, newPrice, amount, expirationTime);

			myWorld.myMarket.acceptOrder(newOrder);

		}

	}

}

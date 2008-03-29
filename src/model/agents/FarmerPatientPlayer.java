package model.agents;

import sim.engine.SimState;
import model.FinancialModel;
import model.market.books.LimitOrder;
import model.market.books.OrderBook.OrderType;
import support.Distributions;
import java.util.ArrayList;

/* Agent that places limit orders according to Farmer's Model */
public class FarmerPatientPlayer extends GenericPlayer {

	private Distributions randDist;

	// TODO: Parameterize me:
	private double alpha=0.7; // Poisson rate: orders placed per step
	private double delta=0.02; // Poisson rate: orders cancelled per step	
	private int sigma = 1; // order size
	
	ArrayList<LimitOrder> myOrders;
	
	public FarmerPatientPlayer() {
		myOrders = new ArrayList<LimitOrder>();
	}

	public void step(SimState state) {
		
		if (this.randDist == null ) {
			randDist = new Distributions(myWorld.random);	
		}

		this.cancelOldOrders();
		this.generateOrders();

		this.manageOrderExecution();
		
	}

	private void generateOrders() {

		int numOrdersPlaced = randDist.nextPoisson(alpha);
		
        for (int i=0; i<numOrdersPlaced; i++) {
        	
			OrderType newType;
			double price;
			int asset = myWorld.random.nextInt(myWorld.myMarket.orderBooks.size());

			// TODO: this is totally arbitrary... do something better
			double sd = myWorld.parameterMap.get("maxPrice") - myWorld.parameterMap.get("minPrice");
			sd /= 20.0; // std dev is 1/20 of min-max dist
			
			if (myWorld.random.nextBoolean(0.5)) {
				// Make a purchase limit order 
				newType = OrderType.PURCHASE;

				double ask = myWorld.myMarket.orderBooks.get(asset).getAskPrice();
				// pick a price from a uniform distribution in minprice<p<ask 
				double range = ask - myWorld.parameterMap.get("minPrice");
				double offset = range*myWorld.random.nextDouble();

				// pick a price from a folded normal distribution
//				double offset = sd*Math.abs(myWorld.random.nextGaussian()+0.2);

                price = Math.max(ask - offset, myWorld.parameterMap.get("minPrice"));
			} else {
				// Make a sale limit order
				newType = OrderType.SALE;
	
		 		double bid = myWorld.myMarket.orderBooks.get(asset).getBidPrice();
				// pick a price from a uniform distribution in bid<p<maxprice 
				double range = myWorld.parameterMap.get("maxPrice") - bid;
				double offset = range*myWorld.random.nextDouble();

		 		// pick a price from a folded normal distribution
//				double offset = sd*Math.abs(myWorld.random.nextGaussian()+0.2);

		 		price = Math.min(bid + offset, myWorld.parameterMap.get("maxPrice")); 
			}
	
			double expirationTime = myWorld.schedule.getTime() + 10000.0;
			LimitOrder newOrder = new LimitOrder(this, newType, asset, price, sigma/*quantity*/, expirationTime);
	
			if (myWorld.myMarket.acceptOrder(newOrder)) {
				myOrders.add(newOrder);
			} // TODO else?
        }

	}

	private void cancelOldOrders() {
		
		if (myOrders.isEmpty()) return;
		
		int numOrdersCancelled = randDist.nextPoisson(alpha*myOrders.size());
		
		for (int i=0; i<numOrdersCancelled && !myOrders.isEmpty(); i++) {
			int indexToCancel = myWorld.random.nextInt(myOrders.size());
			LimitOrder lo = myOrders.get(indexToCancel);

			if (myWorld.myMarket.cancelOrder(lo)) {
				myOrders.remove(indexToCancel);
			} // TODO else?			
		}
		
	}
	
	private void manageOrderExecution()
	{
		ArrayList<LimitOrder> ordersToRemove = new ArrayList<LimitOrder>();
		for (LimitOrder lo: myOrders) {
			LimitOrder.LimitStatus status = lo.getStatus();
			if (status != LimitOrder.LimitStatus.PENDING) {
				ordersToRemove.add(lo);
				// TODO: Track earnings/losses
			} 	  
		}
		myOrders.removeAll(ordersToRemove);
	}
	
	
}

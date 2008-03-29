package model.agents;

import sim.engine.SimState;
import model.FinancialModel;
import model.market.books.LimitOrder;
import model.market.books.OrderBook.OrderType;
import support.Distributions;

/* Agent that places Market orders according to Farmer's model */
public class FarmerImpatientPlayer extends GenericPlayer {

	private Distributions randDist;
	
	// TODO: Parameterize me:
	private double mu=0.7; // Poisson rate: ordered placed per step
	private int sigma = 1; // order size

	public FarmerImpatientPlayer() {
		
	}

	public void step(SimState state) 
	{

		if (this.randDist == null ) {
			randDist = new Distributions(myWorld.random);	
		}

		this.generateOrders();

	}

	private void generateOrders() {

		int ordersPlaced = randDist.nextPoisson(mu);

		for (int i=0; i<ordersPlaced; i++) {
			OrderType orderType;
		    int asset = myWorld.random.nextInt(myWorld.myMarket.orderBooks.size());
		    if (myWorld.random.nextBoolean(0.5)) {
    			orderType = OrderType.PURCHASE;
			} else {
				orderType = OrderType.SALE;
			}
			
		    myWorld.myMarket.acceptMarketOrder(orderType, asset, sigma);
		    // TODO Catch exceptions, manage wealth
		}	

	}

}

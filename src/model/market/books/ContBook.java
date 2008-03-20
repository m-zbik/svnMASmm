package model.market.books;

import model.FinancialModel;

public class ContBook implements OrderBook {

	private static final long serialVersionUID = 1L;

	public FinancialModel myWorld;

	public ContBook() {

	}

	public double price_t = 1;

	// set initial excess demand to zero
	public double excessDemand = 0;

	// set initial return rate to zero
	public double returnRate_t = 0;

	

	public void cleanup() {
		// calculate return rate; the excess demand is already known
		// as it is called by agents during order generation phase
		returnRate_t = this.priceImpact(excessDemand / myWorld.parameterMap.get("N"));
		// calculate new price
		price_t = price_t * Math.exp(returnRate_t);
		// clear out excess demand parameter
		excessDemand = 0.0;

	}



	public boolean placeLimitOrder(LimitOrder order) {
	
		// calculate excess demand; the function is called by agents
		// during the order generation phase

		
		if (order.type.equals(OrderType.PURCHASE)) {
			excessDemand += order.quantity;
		} else {
			excessDemand -= order.quantity;
		}
		
		return true;
	}

	

	public double priceImpact(double d) {
		return Math.atan(d / myWorld.parameterMap.get("lambda"));
		// return Math.atan2(d, myWorld.lambda);
		// return d / myWorld.lambda;
	}

	

	public void setMyWorld(FinancialModel myWorld) {
		this.myWorld = myWorld;

	}

	public double getReturnRate() {
		// TODO Auto-generated method stub
		return returnRate_t;
	}
	
	public double getAskPrice() {
		return price_t;
	}

	public double getBidPrice() {
		return price_t;
	}

	public double[] getBuyOrders() {
		// TODO Auto-generated method stub
		return null;
	}

	public double[] getSellOrders() {
		// TODO Auto-generated method stub
		return null;
	}

	public double getSpread() {
		// TODO Auto-generated method stub
		return 0;
	}
	
	public boolean cancelLimitOrder(LimitOrder order) {
		// TODO Auto-generated method stub
		return false;
	}
	
	public double executeMarketOrder(OrderType type, double quantity) throws LiquidityException {
		// TODO Auto-generated method stub
		return 0;
	}



	public void setMyID(int a) {
		// TODO Auto-generated method stub
		
	}

}

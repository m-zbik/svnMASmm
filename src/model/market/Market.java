package model.market;

import java.util.ArrayList;

import model.FinancialModel;
import model.market.books.LimitOrder;
import model.market.books.OrderBook;
import sim.engine.SimState;
import sim.engine.Steppable;

/* Class: Market 
 * Spring 2008
 * FinancialMarketModel Team
 * 
 * Function: generates the market price based on 
 * the orders placed by traders 
 */

public class Market implements Steppable {

	public FinancialModel myWorld;

	// ArrayList of orderBooks (one for each of the assets)
	public ArrayList<OrderBook> orderBooks = new ArrayList<OrderBook>();

	// constructor
	public Market(FinancialModel myWorld) {
		this.myWorld = myWorld;

		for (int o = 0; o < myWorld.parameterMap.get("numAssets"); o++) {

		}

	}

	// function contains the main business logic of the class
	// generates return rate and new price
	public void step(SimState state) {
		for (OrderBook b : this.orderBooks) {
			b.cleanup();
		}
	}

	public double getReturnRateForAsset(int i) {
		return this.orderBooks.get(i).getReturnRate();
	}

	public double getAskPriceForAsset(int i) {
		return this.orderBooks.get(i).getAskPrice();
	}

	public void acceptOrder(LimitOrder tempOrder) {
		this.orderBooks.get(tempOrder.asset).placeLimitOrder(tempOrder);
		
	}

	public double getBidPriceForAsset(int i) {
		return this.orderBooks.get(i).getBidPrice();
	}

}

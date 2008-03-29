/**
 *  DoubleAuctionOrderBook implementation
 */
package model.market.books;

import java.util.HashSet;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.Vector;

import model.FinancialModel;

/**
 * @author jbriggs Implements a double auction order book. The market moves when
 *         market orders fulfill limit orders. - limit orders don't ever execute
 *         when they're placed
 * 
 */
public class DoubleAuctionOrderBook implements OrderBook {

	protected FinancialModel myWorld;

	protected SortedSet<LimitOrder> buyOrders;
	protected SortedSet<LimitOrder> sellOrders;

	int myID;

	public double returnRate_t = 0;
	public double price_t = 1;

	public DoubleAuctionOrderBook() {
		super();

		this.buyOrders = new TreeSet<LimitOrder>();
		this.sellOrders = new TreeSet<LimitOrder>();
	}

	public synchronized boolean placeLimitOrder(LimitOrder order) {
		if (order.quantity <= 0 || order.assetID != myID) {
			return false;
		}

		// give it a new, unique transaction ID within the orderbook
//		order.transactionID.set(nextTransactionID++);
		
		if (order.type == OrderType.PURCHASE) {
			buyOrders.add(order);
		} else {
			sellOrders.add(order);
		}

		return true;
	}

	// Attempt to cancel a given limit order.
	// Returns true on successful cancellation with no units transacted.
	// Returns false otherwise; final status can be found in order.
	public synchronized boolean cancelLimitOrder(LimitOrder order) {

		order.cancelled.set(true);
		if (order.type == OrderType.PURCHASE) {
			buyOrders.remove(order);
		} else {
			sellOrders.remove(order);
		}

		return true;

	}

	// Returns total price of purchasing 'quantity' units if successful.
	// Otherwise, throws LiquidityException, which contains the number
	// successfully executed.
	public synchronized double executeMarketOrder(OrderType type, int quantity) throws LiquidityException {

		int origQuant = quantity;
		double totalPrice = 0.0;
        
		// The operation is the same regardless of whether we're buying or
		// selling. We just need to choose the right orders to work on.
		SortedSet<LimitOrder> orders;
		if (type == OrderType.PURCHASE) {
			orders=sellOrders;
		} else {
			orders=buyOrders;
		}

		while ((quantity > 0) && (!orders.isEmpty())) {

			LimitOrder lo = orders.first();

 			// TODO: ensure this order hasn't expired
 			
			int curQuantity=0;
 			if (lo.quantityPending() >= quantity) {
 				curQuantity = quantity;
 			} else {
 				curQuantity = quantity - lo.quantityPending();
 			}
 			quantity -= curQuantity;
 			
 			// Execute:
 			// Update the LimitOrder by adding to the quantityExecuted
 			lo.quantityExecuted.addAndGet(curQuantity);
 			// Update the market order
 			totalPrice += lo.pricePerUnit * curQuantity;
 			
			if (lo.quantityPending() == 0) {
				// the limitOrder is fully executed; remove it from the pending orders
				orders.remove(lo);
			}
		}

		if (quantity > 0) {
			throw new LiquidityException((int) (origQuant - quantity), totalPrice);
		}

		return totalPrice;
	}

	public synchronized double getBidPrice() {

		if (buyOrders.isEmpty()) {
			return (myWorld.parameterMap.get("minPrice") + myWorld.parameterMap.get("maxPrice")) / 20;
		} else {
			return buyOrders.first().pricePerUnit;
		}

	}

	public synchronized double getAskPrice() {

		if (sellOrders.isEmpty()) {
			return (myWorld.parameterMap.get("minPrice") + myWorld.parameterMap.get("maxPrice")) / 20;
		} else {
			return sellOrders.first().pricePerUnit;
		}

	}

	public synchronized double getSpread() {
		return this.getAskPrice() - this.getBidPrice();	}

	public synchronized void cleanup() {

		/* Clean expired orders */

		HashSet<LimitOrder> ordersToRemove = new HashSet<LimitOrder>();
		double currentTime = myWorld.schedule.getTime();

		for (LimitOrder l : this.sellOrders) {
			if (l.expirationTime <= currentTime) {
				ordersToRemove.add(l);
			}
		}
	    sellOrders.removeAll(ordersToRemove);
		ordersToRemove.clear();

		for (LimitOrder l : this.buyOrders) {
			if (l.expirationTime <= currentTime) {
				ordersToRemove.add(l);
			}
		}
		buyOrders.removeAll(ordersToRemove);


		/* Clean up negative spreads by trading overlapping LimitOrders */
		while ((this.getSpread() <= 0.0) && (buyOrders.size() > 0) && (sellOrders.size() > 0)) {

			LimitOrder firstBuy = buyOrders.first();
			LimitOrder firstSell = sellOrders.first();

			int curQuantity = Math.min(firstBuy.quantityPending(), firstSell.quantityPending());
			firstBuy.quantityExecuted.addAndGet(curQuantity);
			firstSell.quantityExecuted.addAndGet(curQuantity);
			// NB: Any difference in prices is profit for the exchange :)

			if (firstBuy.quantityPending() == 0) {
				buyOrders.remove(firstBuy);
			}

			if (firstSell.quantityPending() == 0) {
				sellOrders.remove(firstSell);
			}

		}

		/* Calculate return rate */
		double newPrice_t = (getAskPrice() + getBidPrice()) / 2;
		returnRate_t = Math.log(newPrice_t / price_t);
		price_t = newPrice_t;

	}

	// returns an array with an entry of the price for each unit of limit order
	public double[] getBuyOrders() {
		Vector<Double> freqVec = new Vector<Double>();

		double min = myWorld.parameterMap.get("minPrice");
		double max = myWorld.parameterMap.get("maxPrice");

		// add limits to get the scale right
		freqVec.add(min);
	    freqVec.add(max);

		// Iterate through limit order queues from askPrice on up
		for (LimitOrder l : buyOrders) {

			// theoretically this should be quantityPending
			for (int i = 0; i < l.quantity; i++) { 
				freqVec.add(Math.min(Math.max(min, l.pricePerUnit), max));
			}

		}

		double[] retArray = new double[freqVec.size()];
		for (int i = 0; i < freqVec.size(); i++) {
			retArray[i] = freqVec.get(i);
		}

    	return retArray;
	}

	// returns an array with an entry of the price for each unit of each limit
	// order
	public double[] getSellOrders() {
		Vector<Double> freqVec = new Vector<Double>();

		double min = myWorld.parameterMap.get("minPrice");
		double max = myWorld.parameterMap.get("maxPrice");

		// add limits to get the scale right
		freqVec.add(min);
	    freqVec.add(max);
		
		// Iterate through limit order queues from askPrice on up
		for (LimitOrder l : sellOrders) {

			// theoretically this should be quantityPending
			for (int i = 0; i < l.quantity; i++) {
				freqVec.add(Math.min(Math.max(min, l.pricePerUnit), max));
			}

		}

		double[] retArray = new double[freqVec.size()];
		for (int i = 0; i < freqVec.size(); i++) {
			retArray[i] = freqVec.get(i);
		}
		return retArray;
	}

	public synchronized void setMyWorld(FinancialModel myWorld) {
		this.myWorld = myWorld;
	}

	public double getReturnRate() {
		return this.returnRate_t;
	}

	public void setMyID(int a) {
		this.myID = a;
	}
}

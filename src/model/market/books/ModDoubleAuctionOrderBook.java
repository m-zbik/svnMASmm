/**
 *  DoubleAuctionOrderBook implementation
 */
package model.market.books;

import java.util.HashSet;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.Vector;

import model.FinancialModel;

/**
 * @author jbriggs Implements a double auction order book. The market moves when
 *         market orders fulfill limit orders. - limit orders don't ever execute
 *         when they're placed
 * 
 * Internally, prices are stored as integers to avoid the imprecision of doubles
 * causing faults
 * 
 * TODO: Handle expiration times
 */
public class ModDoubleAuctionOrderBook implements OrderBook {

	protected FinancialModel myWorld;

	// map of limit order queues for each price level (*1000):
	// a price of 1.234 is stored under 1234 (-1234 for buy orders)
	protected SortedSet<LimitOrder> buyOrderQueues;

	protected SortedSet<LimitOrder> sellOrderQueues;

	int myID;
	
	// set initial return rate to zero
	public double returnRate_t = 0;
	
	public double price_t = 1;

	public ModDoubleAuctionOrderBook() {
		super();

		this.buyOrderQueues = new TreeSet<LimitOrder>();
		this.sellOrderQueues = new TreeSet<LimitOrder>();
	}

	public synchronized boolean placeLimitOrder(LimitOrder order) {

		if (order.quantity <= 0) {
			return false;
		}

		if (order.type == OrderType.PURCHASE) {
			// store in the tree as negative price to allow forward iteration.

			buyOrderQueues.add(order);

		} else {
			// sale order
			// find the correct queue to place this order on

			sellOrderQueues.add(order);
		}
		System.out.println("ask " + this.getAskPrice() + " bid " + this.getBidPrice());

		return true;
	}

	// Attempt to cancel a given limit order.
	// Returns true on successful cancellation with no units transacted.
	// Returns false otherwise; final status can be found in order.
	public synchronized boolean cancelLimitOrder(LimitOrder order) {

		if (order.type == OrderType.PURCHASE) {

			buyOrderQueues.remove(order);

		} else {

			sellOrderQueues.remove(order);

		}

		return true;

	}

	// Returns total price of purchasing 'quantity' units if successful.
	// Otherwise, throws LiquidityException, which contains the number
	// successfully executed.
	public synchronized double executeMarketOrder(OrderType type, double quantity) throws LiquidityException {

		double origQuant = quantity;

		double totalPrice = 0.0;

		if (type == OrderType.PURCHASE) {

			while ((quantity > 0) && (sellOrderQueues.size() > 0)) {

				LimitOrder firstSell = sellOrderQueues.first();

				double amountBought = Math.min(quantity, firstSell.quantity);
				quantity = quantity - amountBought;
				firstSell.quantity = firstSell.quantity - amountBought;
				totalPrice = totalPrice + amountBought * firstSell.pricePerUnit;
				firstSell.reportPartExecution(amountBought);

				// Set order to be removed at next clearMarket execution

				if (firstSell.quantity <= 0) {
					this.cancelLimitOrder(firstSell);
				}
				
				System.out.println(sellOrderQueues.size());

				// MarketOrder has been executed

			}

		} else { // SELL

			while ((quantity > 0) && (buyOrderQueues.size() > 0)) {

				LimitOrder firstBuy = buyOrderQueues.first();

				double amountBought = Math.min(quantity, firstBuy.quantity);
				quantity = quantity - amountBought;
				firstBuy.quantity = firstBuy.quantity - amountBought;
				totalPrice = totalPrice + amountBought * firstBuy.pricePerUnit;
				firstBuy.reportPartExecution(amountBought);

				// Set order to be removed at next clearMarket execution

				if (firstBuy.quantity <= 0) {
					this.cancelLimitOrder(firstBuy);
				}

				
				System.out.println(buyOrderQueues.size());
				// MarketOrder has been executed

			}

		}

		if (quantity > 0) {
			throw new LiquidityException((int) (origQuant - quantity), totalPrice);
		}

		return totalPrice;
	}

	public synchronized double getBidPrice() {

		if (buyOrderQueues.isEmpty()) {
			return myWorld.parameterMap.get("maxPrice");
		} else {
			return buyOrderQueues.first().pricePerUnit;
		}

	}

	public synchronized double getAskPrice() {

		if (sellOrderQueues.isEmpty()) {
			return myWorld.parameterMap.get("minPrice");
		} else {
			return sellOrderQueues.first().pricePerUnit;
		}

	}

	public synchronized double getSpread() {
		return this.getAskPrice() - this.getBidPrice();
	}

	public synchronized void cleanup() {

		/* Clean expired orders */

		HashSet<LimitOrder> ordersToRemove = new HashSet<LimitOrder>();
		double currentTime = myWorld.schedule.getTime();

		for (LimitOrder l : this.sellOrderQueues) {
			if ((l.expirationTime <= currentTime) || (l.quantity <= 0)) {
				ordersToRemove.add(l);
			}
		}

		for (LimitOrder l : this.buyOrderQueues) {
			if ((l.expirationTime <= currentTime) || (l.quantity <= 0)) {
				ordersToRemove.add(l);
			}
		}

		for (LimitOrder l : ordersToRemove) {
			this.cancelLimitOrder(l);
		}

		/* Clean up negative spreads by trading overlapping LimitOrders */

		while ((this.getSpread() < 0) && (buyOrderQueues.size() > 0) && (sellOrderQueues.size() > 0)){

			LimitOrder firstBuy = buyOrderQueues.first();
			LimitOrder firstSell = sellOrderQueues.first();

			double amountBought = Math.min(firstBuy.quantity, firstSell.quantity);

			firstBuy.quantity = firstBuy.quantity - amountBought;
			firstSell.quantity = firstSell.quantity - amountBought;

			if (firstBuy.quantity <= 0) {
				buyOrderQueues.remove(firstBuy);
			}

			if (firstSell.quantity <= 0) {
				sellOrderQueues.remove(firstSell);
			}

		}
		
		/* Calculate return rate */
		
		double newPrice_t = (getAskPrice() + getBidPrice()) / 2;
		returnRate_t = Math.log(newPrice_t / price_t );
		price_t = newPrice_t;

	}

	// returns an array with an entry of the price for each unit of limit order
	public double[] getBuyOrders() {
		Vector<Double> freqVec = new Vector<Double>();

		// Iterate through limit order queues from askPrice on up
		for (LimitOrder l : buyOrderQueues) {

			for (int i = 0; i < l.quantity; i++) {
				freqVec.add(l.pricePerUnit);
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

		// Iterate through limit order queues from askPrice on up
		for (LimitOrder l : sellOrderQueues) {

			for (int i = 0; i < l.quantity; i++) {
				freqVec.add(l.pricePerUnit);
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

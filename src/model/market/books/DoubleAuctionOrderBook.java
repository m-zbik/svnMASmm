/**
 *  DoubleAuctionOrderBook implementation
 */
package model.market.books;

import model.FinancialModel;
import model.market.books.OrderBook.OrderType;

import java.util.HashMap;
import java.util.Vector;
import org.jfree.data.xy.XYSeries;
import java.lang.IllegalArgumentException;

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
public class DoubleAuctionOrderBook implements OrderBook {
	private static int nextOrderBookID = 0;

	// number of possible transaction prices per cash value
	// (for 100.0, trades can execute at the level of cents)
	private final static double granularity = 1000.0;
	public DoubleAuctionOrderBook() {
		
	}
	

	public DoubleAuctionOrderBook(FinancialModel myWorld) {
		this.myWorld = myWorld;
		// this.nextTransactionID=0;
		this.myID = nextOrderBookID++;
	}

	// these will return a transactionID. the price must be rounded to
	// thousandths of a unit. quantity is a whole number to purchase (initially
	// this will probably be 1),
	// expirationSteps is the number of ticks after which the order will be
	// considered to have been cancelled
	// NOTE: prices should be rounded to the nearest 1/granularity or there will
	// be discrepancies
	public synchronized boolean placeLimitOrder(LimitOrder order) {
		int internalPrice = (int) (order.pricePerUnit * granularity);
		if ((double) internalPrice / granularity != order.pricePerUnit) {
			// improperly rounded price
			return false;
		}

		// indicate that this order refers to this orderbook
		order.orderBookID.set(this.myID);

		// Note: We never let limit orders execute immediately.
		// This means it's possible to achieve a negative spread, if an offer is
		// made to
		// sell at below the bid price or buy at above the ask price. This will
		// persist
		// until a market order occurs. In practice, this should almost never
		// occur.
		// This is why we have separate buy and sell orderqueue hashes

		if (order.quantity > 0) { // purchase order
			// find the correct queue to place this order on
			Vector<LimitOrder> priceQueue = buyOrderQueues.get(internalPrice);
			if (priceQueue == null) { // queue doesn't exist, create it
				priceQueue = new Vector<LimitOrder>();
				buyOrderQueues.put(internalPrice, priceQueue);
			}
			priceQueue.add(order); // add order to the end of the vector

			if (internalPrice > bidPrice || !buyOrderQueues.containsKey(bidPrice)) { // if
				// this
				// purchase
				// is
				// bidding
				// more
				// than
				// the
				// rest,
				// or
				// there
				// are
				// no
				// others,
				// set
				// it
				bidPrice = internalPrice;
			}
			if (internalPrice < lowestBid || !buyOrderQueues.containsKey(lowestBid)) {
				lowestBid = internalPrice;
			}
			// TODO: update GUI Quantities
		} else // sale order
		{
			// find the correct queue to place this order on
			Vector<LimitOrder> priceQueue = sellOrderQueues.get(internalPrice);
			if (priceQueue == null) { // queue doesn't exist, create it
				priceQueue = new Vector<LimitOrder>();
				sellOrderQueues.put(internalPrice, priceQueue);
			}
			priceQueue.add(order); // add order to the end of the vector

			if (internalPrice < askPrice || !sellOrderQueues.containsKey(askPrice)) {
				askPrice = internalPrice;
			}
			if (internalPrice > highestAsk || !buyOrderQueues.containsKey(highestAsk)) {
				lowestBid = internalPrice;
			}

			// TODO: update GUI Quantities
		}

		return true;
	}

	// Attempt to cancel a given limit order.
	// Returns true on successful cancellation with no units transacted.
	// Returns false otherwise; final status can be found in order.
	public synchronized boolean cancelLimitOrder(LimitOrder order) {
		return false; // not implemented
	}

	// Returns total price of purchasing 'quantity' units if successful.
	// Otherwise, throws LiquidityException, which contains the number
	// successfully executed.
	public synchronized double executeMarketOrder(OrderType type, int quantity) throws LiquidityException {
		int origQuant = quantity;
		double totalPrice = 0.0;
		if (type == OrderType.PURCHASE) {
			// check for sufficient liquidity
			if (sellOrderQueues.isEmpty() || askPrice > highestAsk) {
				throw new LiquidityException(0, 0.0);
			}

			// Iterate through limit order queues from askPrice on up
			for (int salePrice = askPrice; salePrice <= highestAsk; salePrice++) {
				Vector<LimitOrder> orders = sellOrderQueues.get(salePrice);

				if (orders != null) {

					askPrice = salePrice; // update the askingPrice to reflect
					// this order
					if (quantity == 0) {
						return totalPrice;
					}

					// iterate through the LimitOrders in the queue for this
					// price
					LimitOrder o = null;
					while (!orders.isEmpty()) {
						// pop the first one off the queue
						o = orders.remove(0);
						int curQuantity = 0;
						if (o.quantityPending() > quantity) {
							curQuantity = quantity;
							orders.add(0, o); // adds the order back in since
							// this won't exhaust it
						} else if (o.quantityPending() == quantity) {
							curQuantity = quantity;
						} else {
							curQuantity = quantity - o.quantityPending();
						}

						// Execute:
						// Update the LimitOrder
						o.quantityExecuted.addAndGet(curQuantity);
						// Update the market order
						totalPrice += o.pricePerUnit * curQuantity;
						quantity -= curQuantity;
					}

					if (orders.isEmpty()) { // remove the orderqueue
						orders = null;
						sellOrderQueues.remove(salePrice);
					} else if (quantity == 0) {
						return totalPrice;
					}
				}
			}
			if (quantity != 0) {
				throw new LiquidityException(origQuant - quantity, totalPrice); // TODO:
				// add
				// params
			}
		} else // SALE
		{
			// check for sufficient liquidity
			if (buyOrderQueues.isEmpty() || bidPrice < lowestBid) {
				throw new LiquidityException(0, 0.0); // TODO: add params
			}

			// Iterate through limit order queues from bidPrice on down
			for (int salePrice = bidPrice; salePrice >= lowestBid; salePrice--) {
				Vector<LimitOrder> orders = buyOrderQueues.get(salePrice);

				if (orders != null) {

					bidPrice = salePrice; // update the bidPrice to reflect
					// this order
					if (quantity == 0) {
						return totalPrice;
					}

					// iterate through the LimitOrders in the queue for this
					// price
					LimitOrder o = null;
					while (!orders.isEmpty()) {
						// pop the first one off the queue
						o = orders.remove(0);
						int curQuantity = 0;
						if (o.quantityPending() > quantity) {
							curQuantity = quantity;
							orders.add(0, o); // adds the order back in queue
							// since this won't fully
							// execute it
						} else if (o.quantityPending() == quantity) {
							curQuantity = quantity;
						} else {
							curQuantity = quantity - o.quantityPending();
						}

						// Execute:
						// Update the LimitOrder
						o.quantityExecuted.addAndGet(curQuantity);
						// Update the market order
						totalPrice += o.pricePerUnit * curQuantity;
						quantity -= curQuantity;
					}

					if (orders.isEmpty()) { // remove the orderqueue
						orders = null;
						buyOrderQueues.remove(salePrice);
					} else if (quantity == 0) {
						return totalPrice;
					}
				}
			}
			if (quantity != 0) {
				throw new LiquidityException(origQuant - quantity, totalPrice); // TODO:
				// add
				// params
			}

		}

		// only get here if the queue is empty and we transacted exactly what we
		// wanted.
		return totalPrice;
	}

	public synchronized double getBidPrice() {
		return bidPrice / granularity;
	}

	public synchronized double getAskPrice() {
		return askPrice / granularity;
	}

	public synchronized double getSpread() {
		return (askPrice - bidPrice) / granularity;
	}

	/*
	 * public synchronized boolean registerGUISeries(XYSeries BuyOrdersSeries,
	 * XYSeries SellOrdersSeries) { buyOrdersSeries=BuyOrdersSeries;
	 * sellOrdersSeries=SellOrdersSeries; return true; }
	 */
	public synchronized void cleanup() {
		// do nothing for now
	}

	public double[] getBuyOrders() {
		return null; // not yet implemented
	}

	public double[] getSellOrders() {
		return null; // not yet implemented
	}

	protected int sumOfQuantities(Vector<LimitOrder> orders) {
		int sum = 0;
		for (int i = 0; i < orders.size(); i++) {
			sum += orders.get(i).quantity;
		}
		return sum;
	}

	protected FinancialModel myWorld;

	// map of limit order queues for each price level (*1000):
	// a price of 1.234 is stored under 1234
	protected HashMap<Integer, Vector<LimitOrder>> buyOrderQueues;

	protected HashMap<Integer, Vector<LimitOrder>> sellOrderQueues;

	// used for GUI
	// protected HashMap< Integer, Integer > buyOrdersOutstanding;
	// protected HashMap< Integer, Integer > sellOrdersOutstanding;

	// protected XYSeries buyOrdersSeries=null, sellOrdersSeries=null;

	protected int bidPrice = 0, askPrice = 0;

	protected int lowestBid = 0, highestAsk = 0;

	// protected long nextTransactionID;

	int myID;

	public void setNyWorld(FinancialModel myWorld) {
		// TODO Auto-generated method stub
		
	}


	public void setMyWorld(FinancialModel myWorld) {
		// TODO Auto-generated method stub
		
	}


	public double getReturnRate() {
		// TODO Auto-generated method stub
		return 0;
	}
}

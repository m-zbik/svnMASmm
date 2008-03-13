/**
 *  DoubleAuctionOrderBook implementation
 */
package model.market.books;

import model.FinancialModel;
import model.market.books.OrderBook.OrderType;

import java.util.*;
import java.util.TreeMap;
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
	protected static long nextTransactionID = 0;
	

	// number of possible transaction prices per cash value
	// (for 100.0, trades can execute at the level of cents)
	private final static double granularity = 1000.0;
	
	public DoubleAuctionOrderBook() {
		super();

		this.buyOrderQueues = new TreeMap<Integer, Vector<LimitOrder>>();
		this.sellOrderQueues = new TreeMap<Integer, Vector<LimitOrder>>();
		
		this.myID = nextOrderBookID++;
	}

	public synchronized boolean placeLimitOrder(LimitOrder order) {
		int internalPrice = internalPrice(order.pricePerUnit);
		if ((double) internalPrice / granularity != order.pricePerUnit) {
			// improperly rounded price
			return false;
		}
		if (order.quantity<=0) {
			return false;
		}

		// indicate that this order refers to this orderbook
		order.orderBookID.set(this.myID);
		// give it a new, unique transaction ID within the orderbook
		order.transactionID.set(nextTransactionID++);

		// Note: We never let limit orders execute immediately.
		// This means it's possible to achieve a negative spread, if an offer is
		// made to
		// sell at below the bid price or buy at above the ask price. This will
		// persist
		// until a market order occurs. In practice, this should almost never
		// occur.
		// This is why we have separate buy and sell orderqueue hashes

		if (order.type == OrderType.PURCHASE) {
			// store in the tree as negative price to allow forward iteration.
			internalPrice=-internalPrice; 
			// find the correct queue to place this order on
			Vector<LimitOrder> priceQueue = buyOrderQueues.get(internalPrice);
			if (priceQueue == null) { // queue doesn't exist, create it
				priceQueue = new Vector<LimitOrder>();
				buyOrderQueues.put(internalPrice, priceQueue);
			}
			priceQueue.add(order); // add order to the end of the vector

		} else // sale order
		{
			// find the correct queue to place this order on
			Vector<LimitOrder> priceQueue = sellOrderQueues.get(internalPrice);
			if (priceQueue == null) { // queue doesn't exist, create it
				priceQueue = new Vector<LimitOrder>();
				sellOrderQueues.put(internalPrice, priceQueue);
			}
			priceQueue.add(order); // add order to the end of the vector
		}

		return true;
	}

	// Attempt to cancel a given limit order.
	// Returns true on successful cancellation with no units transacted.
	// Returns false otherwise; final status can be found in order.
	public synchronized boolean cancelLimitOrder(LimitOrder order) {
		Vector<LimitOrder> queue = null;
		if (order.type == OrderType.PURCHASE) {
			int p = -internalPrice(order.pricePerUnit);
			queue=buyOrderQueues.get(p);
			if (queue.size()==1 && queue.get(0) == order) {
				queue=null;
				buyOrderQueues.remove(p);
				return true;
			}
		} else {
			int p = internalPrice(order.pricePerUnit);
			queue=sellOrderQueues.get(internalPrice(order.pricePerUnit));		   			
			if (queue.size()==1 && queue.get(0) == order) {
				queue=null;
				sellOrderQueues.remove(p);
				return true;
			}
		}
		
		boolean succeeded=false;   
		Iterator<LimitOrder> it = queue.iterator ();
		while (it.hasNext ()) {
			if (it.next() == order) {
				it.remove();
				succeeded=true;	break;
			}
		}
		
		return succeeded;
	}

	// Returns total price of purchasing 'quantity' units if successful.
	// Otherwise, throws LiquidityException, which contains the number
	// successfully executed.
	public synchronized double executeMarketOrder(OrderType type, int quantity) throws LiquidityException {
		int origQuant = quantity;
		double totalPrice = 0.0;
		
		Set<Map.Entry<Integer,Vector<LimitOrder>>> queueSet = null;
     	if (type == OrderType.PURCHASE) {
			// Iterate through limit order queues from askPrice on up
			queueSet = sellOrderQueues.entrySet();
     	} else { // SELL
			// Iterate through limit order queues from bidPrice on down
     		queueSet = buyOrderQueues.entrySet();     		
     	}

     	Iterator<Map.Entry<Integer,Vector<LimitOrder>>> qit = queueSet.iterator();
     	while (qit.hasNext() && (quantity != 0))
     	{
     		Vector<LimitOrder> orders = qit.next().getValue();

     		// iterate through the LimitOrders in the queue for this price
     		LimitOrder o = null;
     		while (!orders.isEmpty() && (quantity != 0)) {
     			int curQuantity = 0;

     			// pop the first one off the queue
     			o = orders.remove(0);

     			// TODO: ensure this order hasn't expired
     			
     			if (o.quantityPending() > quantity) {
     				// this won't exhaust the limitOrder
     				curQuantity = quantity;
     				orders.add(0, o); // adds the order back in since
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
     			qit.remove(); // remove the queue from sellOrderQueues
     		} 
     	}		
		if (quantity != 0) {
			throw new LiquidityException(origQuant - quantity, totalPrice); 
		}

		return totalPrice;
	}

	public synchronized double getBidPrice() {
		// the buyqueues key is stored as the negative of the internal price
		return externalPrice(-buyOrderQueues.firstKey()); 
	}

	public synchronized double getAskPrice() {
		return externalPrice(sellOrderQueues.firstKey());
	}

	public synchronized double getSpread() {
		return externalPrice(sellOrderQueues.firstKey() + buyOrderQueues.firstKey());
	}

    public synchronized void cleanup() {
		// do nothing for now
		//TODO: remove expired limit orders?
		//TODO: eliminate overlapping limit orders?
	}

	// returns an array with an entry of the price for each unit of limit order
	public double[] getBuyOrders() {
		Vector<Double> freqVec = new Vector<Double>();
		
		Set<Map.Entry<Integer,Vector<LimitOrder>>> queueSet = null;
    	// Iterate through limit order queues from bidPrice on down
   		queueSet = buyOrderQueues.entrySet();     		

     	Iterator<Map.Entry<Integer,Vector<LimitOrder>>> qit = queueSet.iterator();
     	while (qit.hasNext())
     	{
     		Map.Entry<Integer,Vector<LimitOrder>> queuePair = qit.next();
     		double price = externalPrice(-queuePair.getKey());
     		Vector<LimitOrder> orders = queuePair.getValue();
     		
     		//TODO: remove expired
     		
     		int totalAtThisPrice = sumOfQuantities(orders);
     		for (int i = 0; i< totalAtThisPrice; i++) {
     			freqVec.add(price);
     		}
     	}
		
     	double[] retArray = new double[freqVec.size()];
     	for (int i = 0; i<freqVec.size(); i++) {
     		retArray[i]=freqVec.get(i);
     	}
     	return retArray;
	}

	// returns an array with an entry of the price for each unit of each limit order
	public double[] getSellOrders() {
		Vector<Double> freqVec = new Vector<Double>();
		
		Set<Map.Entry<Integer,Vector<LimitOrder>>> queueSet = null;
		// Iterate through limit order queues from askPrice on up
		queueSet = sellOrderQueues.entrySet();

     	Iterator<Map.Entry<Integer,Vector<LimitOrder>>> qit = queueSet.iterator();
     	while (qit.hasNext())
     	{
     		Map.Entry<Integer,Vector<LimitOrder>> queuePair = qit.next();
     		double price = externalPrice(queuePair.getKey());
     		Vector<LimitOrder> orders = queuePair.getValue();

     		// TODO: remove expired
     		
     		int totalAtThisPrice = sumOfQuantities(orders);
     		for (int i = 0; i< totalAtThisPrice; i++) {
     			freqVec.add(price);
     		}
     	}
		
     	double[] retArray = new double[freqVec.size()];
     	for (int i = 0; i<freqVec.size(); i++) {
     		retArray[i]=freqVec.get(i);
     	}
     	return retArray;
	}

	protected int sumOfQuantities(Vector<LimitOrder> orders) {
		int sum = 0;
		for (int i = 0; i < orders.size(); i++) {
			sum += orders.get(i).quantity;
		}
		return sum;
	}
    
	protected int internalPrice(double p) {
    	return (int) (p * granularity);
    }
	protected double externalPrice(int internalP) {
		return ((double)internalP) / granularity; 
	}
    
	
	protected FinancialModel myWorld;

	// map of limit order queues for each price level (*1000):
	// a price of 1.234 is stored under 1234 (-1234 for buy orders)
	protected TreeMap<Integer, Vector<LimitOrder>> buyOrderQueues;
	protected TreeMap<Integer, Vector<LimitOrder>> sellOrderQueues;

	int myID;

	public synchronized void setMyWorld(FinancialModel myWorld) {
		this.myWorld = myWorld;
	}

	public double getReturnRate() {
		// TODO Auto-generated method stub
		return 0;
	}


	public void setMyID(int a) {
		this.myID = a;
	}
}

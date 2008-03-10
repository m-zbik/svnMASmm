package model;

import java.util.concurrent.atomic.AtomicInteger;

import model.OrderBook.OrderType;

public class LimitOrder {

	static public enum LimitStatus {
		PENDING,            // Status while not expired or fully executed
		EXPIRED,            // Expired, not executed
		PARTIALLY_EXECUTED, // Expired, some but not all units executed
		FULLY_EXECUTED      // All units executed
	};
	
	public LimitOrder(OrderType type, double price,
	                  int quantity, double expirationTime)
	{
		this.type=type;
		this.quantity=quantity;
		this.pricePerUnit=price;	
		this.expirationTime=expirationTime;
	}

	// Get the status of this LimitOrder
	public LimitStatus getStatus(double timeNow) {
		int currentQuantityExecuted = quantityExecuted.get(); 
		if (quantity==currentQuantityExecuted) {
			return LimitStatus.FULLY_EXECUTED;
		} else if (timeNow>expirationTime) {
			if (currentQuantityExecuted!=0) {
				return LimitStatus.PARTIALLY_EXECUTED;
			} else {
				return LimitStatus.EXPIRED;
			}
		} else {
			return LimitStatus.PENDING;
		}
	}
	
	public int quantityPending() {
		 return quantity - quantityExecuted.get(); 
	}

	final public OrderType type;
	final public int quantity;
	final public double pricePerUnit;
	// Global time after which this order will expire (it is valid when time <= expirationTime)
	final public double expirationTime;


	// These items set when order is placed
	//AtomicLong transactionID; //Not needed?
	public AtomicInteger orderBookID;

	// This item is set as order is executed
	public AtomicInteger quantityExecuted; 
	
}

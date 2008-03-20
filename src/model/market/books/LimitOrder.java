package model.market.books;

import model.agents.GenericPlayer;
import model.market.books.OrderBook.OrderType;

public class LimitOrder implements Comparable {

	final public OrderType type;

	final public int asset;

	public double quantity;

	final public double pricePerUnit;

	public double expirationTime;

	public double entryTime;

	static public enum LimitStatus {
		PENDING, // Status while not expired or fully executed
		EXPIRED, // Expired, not executed
		PARTIALLY_EXECUTED, // Expired, some but not all units executed
		FULLY_EXECUTED
		// All units executed
	};

	public LimitOrder(GenericPlayer investor, OrderType type, int asset, double price, int quantity, double expirationTime) {

		this.type = type;
		this.asset = asset;
		this.quantity = quantity;
		this.pricePerUnit = price;
		this.expirationTime = expirationTime;
		this.entryTime = investor.myWorld.schedule.getSteps();

	}

	// Get the status of this LimitOrder
	public LimitStatus getStatus(double timeNow) {
		return null;
	}

	public void reportPartExecution(double amountBought) {

	}

	public int quantityPending() {

		return (int) quantity;
	}

	public int compareTo(Object arg0) {

		LimitOrder target = (LimitOrder) arg0;

		if (this.type == OrderType.SALE) {

			if (target.pricePerUnit > this.pricePerUnit) {
				return -1;
			} else if (target.pricePerUnit < this.pricePerUnit) {
				return 1;
			} else {
				if (target.entryTime > this.entryTime) {
					return -1;
				} else if (target.entryTime < this.entryTime) {
					return 1;
				} else {
					return 0;
				}

			}

		} else {

			if (target.pricePerUnit < this.pricePerUnit) {
				return -1;
			} else if (target.pricePerUnit > this.pricePerUnit) {
				return 1;
			} else {
				if (target.entryTime > this.entryTime) {
					return -1;
				} else if (target.entryTime < this.entryTime) {
					return 1;
				} else {
					return 0;
				}
			}

		}

	}
}

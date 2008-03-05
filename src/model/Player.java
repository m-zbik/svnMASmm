package model;

import sim.engine.SimState;
import sim.engine.Steppable;

/* Class: Player 
 * Spring 2008
 * FinancialMarketModel Team
 * 
 * Function: trader agent; generates orders and updates thresholds 
 */

public class Player implements Steppable {
	
	// trader's threshold
	double threshold;
	// trader's id
	int id;
	// instantiate ContModel class in order to access its variables
	public ContModel myWorld;
	
	// constructor
	public Player(ContModel myWorld, int id) {
		
		this.myWorld = myWorld;
		this.id = id;
		
		
	}
	
	// main logic function; not really used in this case
	public void step(SimState state) {

		System.out.println("myid " + id + " time " + myWorld.schedule.getTime());
		
		
	}
	
	// generate an order if trader's threshold is above or below 
	// a market wide parameter epsilon_t
	public void generateOrders() {		
		int order;
		
		// if epsilon_t is below the negative value of threshold
		// issue an order to sell
		if (myWorld.epsilon_t < -1*this.threshold) {
			order = -1;
		// if epsilon_t is above the positive value of threshold
		// issue an order to buy
		} else if (myWorld.epsilon_t > this.threshold) {
			order = 1;
		// otherwise do nothing
		} else {
			order = 0;
		}
		
		// pass the order value to the market agent
		this.myWorld.myMarket.acceptOrder(order);	
		
	}
	
	// update thresholds for 's' fraction of traders
	public void updateThresholds() {
		// generate a random value between 0 and 1 from uniform distribution
		double u_i_t = myWorld.random.nextDouble();
		// get the current return rate from the market agent
		double r_t = myWorld.myMarket.returnRate_t;
		
		// update threshold if a random value u_i_t is below 
		// the given update frequency 's' 
		if (u_i_t < myWorld.s) {
			// set a new threshold
			this.threshold = Math.abs(r_t);
		} 		
	}
	

}

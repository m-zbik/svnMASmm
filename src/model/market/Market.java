package model.market;

import model.ContModel;
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
	// instantiate ContModel class in order to access its variables
	public ContModel myWorld;
	// set initial market price to one
	public double price_t = 1;
	// set initial excess demand to zero
	public double excessDemand = 0;
	// set initial return rate to zero
	public double returnRate_t = 0; 
	
	// constructor
	public Market(ContModel myWorld) {
		this.myWorld = myWorld;
	}
	
	// function contains the main business logic of the class
	// generates return rate and new price
	public void step(SimState state) 
	{
		// calculate return rate; the excess demand is already known 
		// as it is called by agents during order generation phase
		returnRate_t = this.priceImpact(excessDemand / myWorld.parameterMap.get("N"));
		// calculate new price
		price_t = price_t * Math.exp(returnRate_t);
		// clear out excess demand parameter
		excessDemand = 0.0;				
	}
	
	// calculate the return rate
	public double priceImpact(double d) 
	{
		return Math.atan(d/myWorld.parameterMap.get("lambda"));
		//return Math.atan2(d, myWorld.lambda);
		//return d / myWorld.lambda; 
	}
	
	// calculate excess demand; the function is called by agents 
	// during the order generation phase
	public void acceptOrder(int order) 
	{
		excessDemand += order;
	}

}

package model;

import sim.engine.SimState;
import sim.engine.Steppable;

public class Market implements Steppable {

	public ContModel myWorld;
	
	public double price_t = 1;
	
	public double excessDemand = 0;
	
	public double returnRate_t = 0; 
	
	public Market(ContModel myWorld) {
		this.myWorld = myWorld;
	}
	
	
	public void step(SimState state) {
		
		
		returnRate_t = this.priceImpact(excessDemand / myWorld.N);
		
		price_t = price_t * Math.exp(returnRate_t);
		
		excessDemand = 0;
		
		
		 // System.out.println(price_t);
		
		// System.out.println("fgfgfMarket clears");
		
	}
	
	public double priceImpact(double d) {
		
		return d / myWorld.lambda; 
		
	}
	
	public void acceptOrder(int order) {
		
		excessDemand += order;
	}

}

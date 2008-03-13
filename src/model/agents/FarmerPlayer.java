package model.agents;

import sim.engine.SimState;
import model.FinancialModel;

/* Agent that randomly drops a random order at a random orderbook */

public class FarmerPlayer extends GenericPlayer {
	
	
	private FinancialModel myWorld;
	private int id;

	public FarmerPlayer(FinancialModel myWorld, int id) {

		this.myWorld = myWorld;
		this.id = id;

	}
	
	public void step(SimState state) {
		
		this.generateOrders();

	}

	private void generateOrders() {
		// TODO Auto-generated method stub
		
	}

}

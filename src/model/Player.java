package model;

import sim.engine.SimState;
import sim.engine.Steppable;

public class Player implements Steppable {
	
	
	double threshold;
	
	int id;
	
	public ContModel myWorld;
	
	public Player(ContModel myWorld, int id) {
		
		this.myWorld = myWorld;
		this.id = id;
		
		
	}
	

	public void step(SimState state) {

		System.out.println("myid " + id + " time " + myWorld.schedule.getTime());
		
		
	}
	
	public void generateOrders() {
		
		
		int order;
		
		if (myWorld.epsilon_t < -1*this.threshold) {
			order = -1;
		} else if (myWorld.epsilon_t > this.threshold) {
			order = 1;
		} else {
			order = 0;
		}
		
		
		this.myWorld.myMarket.acceptOrder(order);
		
		// System.out.println("Agent " + id + " generates orders " + myWorld.schedule.getTime());
		
		
	}
	
	public void updateThresholds() {
		
		double u_i_t = myWorld.random.nextDouble();
		
		double r_t = myWorld.myMarket.returnRate_t;
		
		if (u_i_t < myWorld.s) {
			this.threshold = Math.abs(r_t);
		} 
		
		
		// System.out.println("Agent " + id + " updates beliefs " + myWorld.schedule.getTime());
			
		
	}
	

}

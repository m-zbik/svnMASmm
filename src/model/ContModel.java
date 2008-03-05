package model;

import java.util.ArrayList;

import sim.engine.SimState;
import sim.engine.Steppable;
import sim.util.Interval;

/* Class: ContModel 
 * Spring 2008
 * FinancialMarketModel Team
 * 
 * Function: the main class of the applications. 
 * Initializes N traders and runs trades for a maxT number of times
 */

public class ContModel extends SimState {

	// number of traders in the market
	int N = 1500;
	// max number of ticks
	int maxT = 100000;
	// standard deviation of the noise (used for calculation of epsilon_t)
	public double D = 0.001;
	// external signal to update threshold
	public double epsilon_t;
	// array list of traders
	ArrayList<Player> agentList;
	// Market class: calculates excess demand and return rate
	public Market myMarket;
	// market depth 
	public double lambda = 10;
	// ratio of traders updating threshold
	public double s = 0.1;

	// access method for number of traders N
	public int getN() {
		return N;
	}
	// access method for number of traders N
	public void setN(int n) {
		N = n;
	}
	// access method for number of ticks
	public int getMaxT() {
		return maxT;
	}
	// access method for number of ticks
	public void setMaxT(int maxT) {
		this.maxT = maxT;
	}
	// access method for standard deviation D
	public double getD() {
		return D;
	}
	// access method for standard deviation D
	public void setD(double d) {
		D = d;
	}

	public Object domD() {
		return new Interval(0, 100);
	}
	// access method for market depth 
	public double getLambda() {
		return lambda;
	}
	// access method for market depth 
	public void setLambda(double lambda) {
		this.lambda = lambda;
	}

	public Object domLambda() {
		return new Interval(0, 100);
	}
	// access method for ratio of traders updating
	public double getS() {
		return s;
	}
	// access method for ratio of traders updating
	public void setS(double s) {
		this.s = s;
	}

	public Object domS() {
		return new Interval(0, 100);
	}

	// constructor
	public ContModel(long seed) {
		super(seed);
	}

	public void start() {
		// initialize an array list of traders
		agentList = new ArrayList<Player>();

		// initialize traders and add them to the list
		for (int i = 0; i < N; i++) {
			// create new trader
			Player tempAgent = new Player(this, i);
			// assign a random threshold; values range from 0 to 1
			tempAgent.threshold = this.random.nextDouble();
			// add trader to the list
			agentList.add(tempAgent);
		}
		// initialize market agent
		myMarket = new Market(this);

		// the following two embedded classes instantiate agent list 
		// and either generate orders or update thresholds
		// the design ensures that all agents first generate orders 
		// before updating thresholds (both classes used in the scheduler)
		
		// instantiate agent list and generate orders for all agents
		final Steppable orderPhase = new Steppable() {
			public void step(SimState state) {

				for (Player p : agentList) {
					p.generateOrders();
				}
			}
		};
		
		// instantiate agent list and update thresholds for all agents
		final Steppable updatePhase = new Steppable() {
			public void step(SimState state) {

				for (Player p : agentList) {
					p.updateThresholds();
				}
			}
		};

		// another embedded class; generates a new epsilon value
		final Steppable signalAgent = new Steppable() {
			public void step(SimState state) {

				epsilon_t = D * state.random.nextGaussian();

			}
		};

		// schedule classes to run in the specified order
		this.schedule.scheduleRepeating(signalAgent, 0, 1.0);
		this.schedule.scheduleRepeating(orderPhase, 1, 1.0);
		this.schedule.scheduleRepeating(myMarket, 2, 1.0);
		this.schedule.scheduleRepeating(updatePhase, 3, 1.0);

		// embedded class; stops the the model run (used in scheduler)
		final Steppable finalAgent = new Steppable() {

			private static final long serialVersionUID = 6184761986120478954L;

			public void step(SimState state) {

				state.finish();

			}
		};
		
		// run the model maxT times and then finish the execution
		schedule.scheduleOnce(maxT, (int) schedule.MAXIMUM_INTEGER, finalAgent);
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// run the model 
		doLoop(ContModel.class, args);
		System.exit(0);

	}

}

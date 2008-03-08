package model;

import java.util.ArrayList;
import java.util.HashMap;

import model.agents.ContPlayer;
import model.market.Market;

import sim.engine.SimState;
import sim.engine.Steppable;
import sim.util.Interval;
import support.ModelFactory;
import support.Reporter;

/* Class: ContModel 
 * Spring 2008
 * FinancialMarketModel Team
 * 
 * Function: the main class of the applications. 
 * Initializes N traders and runs trades for a maxT number of times
 */

public class ContModel extends SimState {

	private static final long serialVersionUID = 1L;

	public Reporter myReporter;
	
	public ModelFactory myCreator;
	
	public HashMap<String, Double> parameterMap = new HashMap<String, Double>();

	public HashMap<String, String> optionsMap = new HashMap<String, String>();
	
	// array list of traders
	public ArrayList<ContPlayer> agentList;
	
	// Market class: calculates excess demand and return rate
	public Market myMarket;
	
	// external signal to update threshold
	public double epsilon_t;
	
	public int runID;

	// constructor
	public ContModel(long seed) {
		super(seed);
		this.myReporter = new Reporter(this);
		this.myCreator = new ModelFactory(this);
	}

	public void start() {
		
		myCreator.buildContAgents();

		// another embedded class; generates a new epsilon value
		final Steppable signalAgent = new Steppable() {
			public void step(SimState state) {

				epsilon_t = parameterMap.get("D") * state.random.nextGaussian();

			}
		};

		// schedule classes to run in the specified order
		this.schedule.scheduleRepeating(signalAgent, 0, 1.0);
		

		// embedded class; stops the the model run (used in scheduler)
		final Steppable finalAgent = new Steppable() {

			private static final long serialVersionUID = 6184761986120478954L;

			public void step(SimState state) {

				state.finish();

			}
		};
		
		// run the model maxT times and then finish the execution
		schedule.scheduleOnce(this.parameterMap.get("maxT"), (int) schedule.MAXIMUM_INTEGER, finalAgent);
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

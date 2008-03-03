package model;

import java.util.ArrayList;

import sim.engine.SimState;
import sim.engine.Steppable;
import sim.util.Interval;

public class ContModel extends SimState {

	int N = 1500;

	int maxT = 100000;

	public double D = 0.001;

	public double epsilon_t;

	ArrayList<Player> agentList;

	public Market myMarket;

	public double lambda = 10;

	public double s = 0.1;

	public int getN() {
		return N;
	}

	public void setN(int n) {
		N = n;
	}

	public int getMaxT() {
		return maxT;
	}

	public void setMaxT(int maxT) {
		this.maxT = maxT;
	}

	public double getD() {
		return D;
	}

	public void setD(double d) {
		D = d;
	}

	public Object domD() {
		return new Interval(0, 100);
	}

	public double getLambda() {
		return lambda;
	}

	public void setLambda(double lambda) {
		this.lambda = lambda;
	}

	public Object domLambda() {
		return new Interval(0, 100);
	}

	public double getS() {
		return s;
	}

	public void setS(double s) {
		this.s = s;
	}

	public Object domS() {
		return new Interval(0, 100);
	}

	public ContModel(long seed) {
		super(seed);
	}

	public void start() {

		agentList = new ArrayList<Player>();

		for (int i = 0; i < N; i++) {
			Player tempAgent = new Player(this, i);
			tempAgent.threshold = this.random.nextDouble();
			agentList.add(tempAgent);
		}

		myMarket = new Market(this);

		final Steppable orderPhase = new Steppable() {
			public void step(SimState state) {

				for (Player p : agentList) {
					p.generateOrders();
				}
			}
		};

		final Steppable updatePhase = new Steppable() {
			public void step(SimState state) {

				for (Player p : agentList) {
					p.updateThresholds();
				}
			}
		};

		final Steppable signalAgent = new Steppable() {
			public void step(SimState state) {

				epsilon_t = D * state.random.nextGaussian();

			}
		};

		this.schedule.scheduleRepeating(signalAgent, 0, 1.0);
		this.schedule.scheduleRepeating(orderPhase, 1, 1.0);
		this.schedule.scheduleRepeating(myMarket, 2, 1.0);
		this.schedule.scheduleRepeating(updatePhase, 3, 1.0);

		final Steppable finalAgent = new Steppable() {

			private static final long serialVersionUID = 6184761986120478954L;

			public void step(SimState state) {

				state.finish();

			}
		};

		schedule.scheduleOnce(maxT, (int) schedule.MAXIMUM_INTEGER, finalAgent);
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		doLoop(ContModel.class, args);
		System.exit(0);

	}

}

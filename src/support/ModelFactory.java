package support;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Properties;

import model.ContModel;
import model.agents.ContPlayer;
import model.market.Market;



public class ModelFactory {

	public boolean returnSim = false;

	public ContModel target;

	String fileName;

	public ModelFactory(ContModel target) {

		/* Read in logging settings. */

		if (target == null) {
			returnSim = true;
			target = new ContModel(System.currentTimeMillis());
		} else {
			this.target = target;
		}

		/* Read in simulation settings and set them to the target. */

		Properties properties = new Properties();
		try {
			properties.load(new FileInputStream("main.properties"));
		} catch (IOException e) {
		}

		target.parameterMap.put("N", new Double(properties.getProperty("N", "1000")));
		target.parameterMap.put("s", new Double(properties.getProperty("s", "0.1")));
		target.parameterMap.put("D", new Double(properties.getProperty("D", "2")));
		target.parameterMap.put("lambda", new Double(properties.getProperty("lambda", "10")));
		target.parameterMap.put("maxT", new Double(properties.getProperty("maxT", "100000")));
				
	}
	
	public void buildContAgents() {
		
		// initialize an array list of traders
		target.agentList = new ArrayList<ContPlayer>();

		// initialize traders and add them to the list
		for (int i = 0; i < target.parameterMap.get("N"); i++) {
			// create new trader
			ContPlayer tempAgent = new ContPlayer(target, i);
			// assign a random threshold; values range from 0 to 1
			tempAgent.threshold = target.random.nextDouble();
			// add trader to the list
			target.agentList.add(tempAgent);
			// schedule agents
			target.schedule.scheduleRepeating(tempAgent, 1, 1.0);
			
		}
		// initialize market agent
		target.myMarket = new Market(target);
		target.schedule.scheduleRepeating(target.myMarket, 2, 1.0);
		
		
	}
}

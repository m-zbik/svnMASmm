package support;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Properties;

import model.FinancialModel;
import model.agents.ContPlayer;
import model.agents.GenericPlayer;
import model.market.Market;
import model.market.books.*;

public class ModelFactory {

	public boolean returnSim = false;

	public FinancialModel target;

	String fileName;

	public ModelFactory(FinancialModel target) {

		/* Read in logging settings. */

		if (target == null) {
			returnSim = true;
			target = new FinancialModel(System.currentTimeMillis());
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
		target.parameterMap.put("numAssets", new Double(properties.getProperty("numAssets", "1")));
		target.parameterMap.put("mu", new Double(properties.getProperty("mu", "1")));
		target.parameterMap.put("delta", new Double(properties.getProperty("delta", "1")));
		target.parameterMap.put("lambda", new Double(properties.getProperty("lambda", "1")));
		target.parameterMap.put("minPrice", new Double(properties.getProperty("minPrice", "0")));
		target.parameterMap.put("maxPrice", new Double(properties.getProperty("maxPrice", "10")));
		target.parameterMap.put("agentClass1Prob", new Double(properties.getProperty("agentClass1Prob", "1.0")));		
		
		target.optionsMap.put("agentClass1", properties.getProperty("agentClass1", "ContPlayer"));
		target.optionsMap.put("agentClass2", properties.getProperty("agentClass2", "ContPlayer"));
		target.optionsMap.put("orderBookClass", properties.getProperty("orderBookClass", "ContBook"));

	}

	public void buildContAgents() {

		// initialize an array list of traders
		target.agentList = new ArrayList<GenericPlayer>();

		try {

			// initialize traders and add them to the list
			double p1 = target.parameterMap.get("agentClass1Prob");
			for (int i = 0; i < target.parameterMap.get("N"); i++) {
				// create new trader
				GenericPlayer tempAgent = null;
				if (target.random.nextBoolean(p1)) {
					tempAgent = (GenericPlayer) Class.forName("model.agents." + target.optionsMap.get("agentClass1")).newInstance();
				}
				else { //agentClass2
					tempAgent = (GenericPlayer) Class.forName("model.agents." + target.optionsMap.get("agentClass2")).newInstance();
				}				

				tempAgent.myWorld = target;
				tempAgent.id = i;

				// add trader to the list
				target.agentList.add(tempAgent);
				// schedule agents
				target.schedule.scheduleRepeating(tempAgent, 1, 1.0);
			}

		} catch (InstantiationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		// initialize market agent
		target.myMarket = new Market(target);

		try {
			for (int a = 0; a < target.parameterMap.get("numAssets"); a++) {
				target.myMarket.orderBooks.add((OrderBook) Class.forName("model.market.books." + target.optionsMap.get("orderBookClass")).newInstance());
			}
		} catch (InstantiationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		for (int a = 0; a < target.parameterMap.get("numAssets"); a++) {
			target.myMarket.orderBooks.get(a).setMyWorld(target);
			target.myMarket.orderBooks.get(a).setMyID(a);
		}

		target.schedule.scheduleRepeating(target.myMarket, 2, 1.0);

	}
}

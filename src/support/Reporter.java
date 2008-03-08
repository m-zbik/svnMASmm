package support;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;

import model.ContModel;

import sim.engine.SimState;
import sim.engine.Steppable;

public class Reporter implements Steppable {

	private static final long serialVersionUID = 1L;

	BufferedWriter outPrices;
	
	BufferedWriter outParameters;

	ContModel myModel;

	public Reporter(ContModel myModel) {

		this.myModel = myModel;

		try {

			outPrices = new BufferedWriter(new FileWriter("timeSeries.txt", true));
			
			
			String temp = "";
			temp = temp + "runID" + ";";
			temp = temp + "time" + ";";
			temp = temp + "price" + ";";
			temp = temp + "return" ;
			

			outPrices.write(temp);
			outPrices.newLine();
			
		} catch (IOException e) {

			e.printStackTrace();
		}

	}

	
	public void step(SimState state) {

		if (myModel.schedule.getTime() == 0) {
			
		
			

		}

		

		try {

			String temp = "";
			temp = temp + myModel.runID + ";";
			temp = temp + myModel.schedule.getTime() + ";";
			temp = temp + myModel.myMarket.price_t + ";";
			temp = temp + myModel.myMarket.returnRate_t + ";";
		

			outPrices.write(temp);
			outPrices.newLine();			

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	public void finishAll() {

		try {
			outPrices.flush();
			outPrices.close();
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

}

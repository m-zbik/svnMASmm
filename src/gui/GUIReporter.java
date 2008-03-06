package gui;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Vector;

import model.ContModel;

import org.jfree.data.xy.XYSeries;

import sim.engine.SimState;
import sim.engine.Steppable;

public class GUIReporter implements Steppable {

	public XYSeries priceSeries = new XYSeries("prices");

	public XYSeries returnSeries = new XYSeries("returns");

	public XYSeries acfAbsReturnsSeries = new XYSeries("acfAbsReturns");

	public HashMap<Integer, Double> priceMemory = new HashMap<Integer, Double>();

	public Vector<Double> returnMemory = new Vector<Double>();
	ContModel myModel;
	
	public int lastUpdate = 0;

	public GUIReporter(ContModelWithUI contModelWithUI) {
		myModel = (ContModel) contModelWithUI.state;

		// maximum item count only needs to be set once
		priceSeries.setMaximumItemCount(2000);
		returnSeries.setMaximumItemCount(2000);
		// initialize
		for (int i=0; i<200; i++) {
		  acfAbsReturnsSeries.add(i,0.0);
		}
		
	}

	public void step(SimState state) {

		priceMemory.put((int) myModel.schedule.getTime(), myModel.myMarket.price_t);
		returnMemory.add(myModel.myMarket.returnRate_t);
		
	}

	public void setSeries() {

		System.out.println("Updating series");

		// priceSeries.clear();
		
		int start = lastUpdate;
		int stop = (int) myModel.schedule.getTime();
		

		for (int i = start; i < stop; i++) {

			priceSeries.add(i, priceMemory.get(i));
			returnSeries.add(i, returnMemory.get(i));

			if (i % ((int) 1 + (priceMemory.size() - priceSeries.getItemCount()) / 10) == 0) {
				System.out.print(".");
			}

		}
		
		if (stop>start) lastUpdate = stop;

//		System.out.println();
		System.out.println("Finished updating series");
	}

	public void acfAbsReturns() {

		System.out.println("Computing ACF");

		// make an array of the absolute value of the returns
		double[] absRetArray = new double[returnMemory.size()];
        for (int i=0; i < absRetArray.length; i++) {
        	absRetArray[i] = Math.abs(returnMemory.get(i));
		}
		
		for (int lag = 1; lag < Math.min(200, returnMemory.size()); lag++) 
		{
             
			double autocorr_lag = autocorrelation(absRetArray, lag);
		    // instead of clearing and adding, we just replace the earlier values
		    acfAbsReturnsSeries.updateByIndex(lag, autocorr_lag);
//			if (lag % 20 == 0) {
//				System.out.print(".");
//			}
		}
		System.out.println();
		System.out.println("Finished ACF");

	}

	public static double correlation(double[] rankArray1, double[] rankArray2) 
	{

		int nObs = rankArray1.length;
		double mua = 0;
		double mub = 0;
		double prod = 0;
		double a2 = 0;
		double b2 = 0;

		for (int i = 0; i < nObs; i++) {
			prod += rankArray1[i] * rankArray2[i];
			a2 += rankArray1[i] * rankArray1[i];
			b2 += rankArray2[i] * rankArray2[i];
			mua += rankArray1[i];
			mub += rankArray2[i];
		}
		return ((nObs * prod) - (mua) * (mub)) / Math.sqrt(((nObs * a2 - mua * mua)) * ((nObs * b2 - mub * mub)));
	}
	
	public static double autocorrelation(double[] array, int lag) 
	{
        if (lag>=array.length) {
        	return 0.0;
        }
        
		int arraySize=array.length - lag;
        double[] rankArray1=new double[arraySize];
		double[] rankArray2=new double[arraySize];

		System.arraycopy(array, 0, rankArray1, 0, array.length-lag);
		System.arraycopy(array, lag, rankArray2, 0, array.length-lag);

		return correlation(rankArray1, rankArray2);

	}
	

}

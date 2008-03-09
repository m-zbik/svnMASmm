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
	public XYSeries absReturnSeries = new XYSeries("absReturns");

	public XYSeries acfAbsReturnsSeries = new XYSeries("acAbsoluteReturns");
	public XYSeries acfReturnsSeries = new XYSeries("acReturns");

	public Vector<Double> priceMemory = new Vector<Double>();

	public Vector<Double> returnMemory = new Vector<Double>();

	public int NumViewable=2000; // also, the #/data points used in calculating acf
	public int ACFViewable=200;
	
	ContModel myModel;

	public int nextUpdate = 0;
	
	public int lastUpdate = 0;

	public GUIReporter(ContModelWithUI contModelWithUI) {
		myModel = (ContModel) contModelWithUI.state;

		// maximum item count only needs to be set once
		priceSeries.setMaximumItemCount(NumViewable);
		returnSeries.setMaximumItemCount(NumViewable);
		absReturnSeries.setMaximumItemCount(NumViewable);
		// initialize
		for (int i = 1; i < ACFViewable; i++) {
			acfAbsReturnsSeries.add(i, 0.0);
			acfReturnsSeries.add(i, 0.0);
		}

	}

	public void step(SimState state) {

		if (state.schedule.getTime() >= nextUpdate) {

			priceMemory.add(myModel.myMarket.price_t);
			returnMemory.add(myModel.myMarket.returnRate_t);

			nextUpdate = nextUpdate + 1;
		}

	}

	public void setSeries() {

		System.out.println("Updating series");

		int stop = (int) myModel.schedule.getTime();
		int start = Math.max(lastUpdate, stop-NumViewable);

		for (int i = start; i < stop; i++) {

			priceSeries.add(i, priceMemory.get(i));
			returnSeries.add(i, returnMemory.get(i));
			absReturnSeries.add(i, Math.abs(returnMemory.get(i)));

			if (i % ((int) 1 + (priceMemory.size() - priceSeries.getItemCount()) / 10) == 0) {
				System.out.print(".");
			}

		}

		if (stop > start)
			lastUpdate = stop;

		System.out.println();
		System.out.println("Finished updating series");
	}

	public void acfAbsReturns() {

		System.out.println("Computing ACF");
		
		// make an array of the absolute value of the (last NumViewable) returns
		int arrayLen=Math.min(NumViewable,returnMemory.size());
		int offset=returnMemory.size()-arrayLen;
		double[] absRetArray = new double[arrayLen];
		double[] retArray = new double[arrayLen];
		for (int i = 0; i < absRetArray.length; i++) {
			absRetArray[i] = Math.abs(returnMemory.get(i+offset)); 
		    retArray[i] = returnMemory.get(i+offset); 
		}

        int numLags = Math.min(ACFViewable, returnMemory.size());
		for (int lag = 1; lag < numLags; lag++) 
		{
			double abs_autocorr_lag = autocorrelation(absRetArray, lag);
			double autocorr_lag = autocorrelation(retArray, lag);
			
			// instead of clearing and adding, we just replace the earlier
			// values
			acfAbsReturnsSeries.updateByIndex(lag-1, abs_autocorr_lag);
			acfReturnsSeries.updateByIndex(lag-1, autocorr_lag);			
			// if (lag % 20 == 0) {
			// System.out.print(".");
			// }
		}
//		System.out.println();
		System.out.println("Finished ACF");

	}

	public static double correlation(double[] rankArray1, double[] rankArray2) 
	{
		if (rankArray1.length!=rankArray2.length || rankArray1.length<1) {
			return 0.0;
		}
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
		return ((nObs * prod) - (mua * mub)) / 
		        ( Math.sqrt(nObs * a2 - mua * mua) * Math.sqrt(nObs * b2 - mub * mub) );
	}

	public static double autocorrelation(double[] array, int lag) {
		if (lag >= array.length || array.length<1) {
			return 0.0;
		}

		int nObs = array.length-lag;
		double mua = 0;
		double mub = 0;
		double prod = 0;
		double a2 = 0;
		double b2 = 0;

		for (int i = 0; i < nObs; i++) {
			prod += array[i] * array[i+lag];
			a2 += array[i] * array[i];
			b2 += array[i+lag] * array[i+lag];
			mua += array[i];
			mub += array[i+lag];
		}
		return ((nObs * prod) - (mua * mub)) / 
		        ( Math.sqrt(nObs * a2 - mua * mua) * Math.sqrt(nObs * b2 - mub * mub) );
	}

}

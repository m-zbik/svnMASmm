package gui;

import java.util.ArrayList;

import model.ContModel;

import org.jfree.data.xy.XYSeries;

import sim.engine.SimState;
import sim.engine.Steppable;

public class GUIReporter implements Steppable {

	public XYSeries priceSeries = new XYSeries("prices");

	public XYSeries returnSeries = new XYSeries("returns");

	public XYSeries acfAbsReturnsSeries = new XYSeries("acfAbsReturns");

	public ArrayList<Double> priceMemory = new ArrayList<Double>();

	public ArrayList<Double> returnMemory = new ArrayList<Double>();

	ContModel myModel;

	public GUIReporter(ContModelWithUI contModelWithUI) {
		myModel = (ContModel) contModelWithUI.state;

	}

	public void step(SimState state) {

		priceMemory.add(myModel.myMarket.price_t);
		returnMemory.add(myModel.myMarket.returnRate_t);

	}

	public void setSeries() {

		System.out.println("Updating series");

		// priceSeries.clear();

		for (int i = priceSeries.getItemCount(); i < priceMemory.size(); i++) {

			priceSeries.add(i, priceMemory.get(i));
			returnSeries.add(i, returnMemory.get(i));

			if (i % ((int) 1 + (priceMemory.size() - priceSeries.getItemCount()) / 10) == 0) {
				System.out.print(".");
			}

		}

		System.out.println();
		System.out.println("Finished updating series");
	}

	public void acfAbsReturns() {

		System.out.println("Computing ACF");

		acfAbsReturnsSeries.clear();

		for (int lag = 1; lag < Math.min(200, returnMemory.size()); lag++) {

			double[] array1 = new double[returnMemory.size() - lag];
			double[] array2 = new double[returnMemory.size() - lag];

			int i = 0;
			while (i < returnMemory.size() - lag) {
				array1[i] = Math.abs(returnMemory.get(i));
				array2[i] = Math.abs(returnMemory.get(i + lag));
				i++;
			}

			double correlation = correlation(array1, array2);
			acfAbsReturnsSeries.add(lag, correlation);

			if (lag % 20 == 0) {
				System.out.print(".");
			}
		}
		System.out.println();
		System.out.println("Finished ACF");

	}

	public static double correlation(double[] array1, double[] array2) {

		double[] rankArray1 = array1;
		double[] rankArray2 = array2;

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

}

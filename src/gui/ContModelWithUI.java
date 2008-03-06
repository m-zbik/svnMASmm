/*
 Copyright 2006 by Sean Luke and George Mason University
 Licensed under the Academic Free License version 3.0
 See the file "LICENSE" for more information
 */

package gui;

import java.awt.BorderLayout;

import javax.swing.JFrame;

import model.ContModel;
import sim.display.Console;
import sim.display.Controller;
import sim.display.GUIState;
import sim.engine.SimState;
import sim.engine.Steppable;
import sim.util.media.chart.HistogramGenerator;
import sim.util.media.chart.TimeSeriesChartGenerator;

/**
 * @author Maciek
 */
public class ContModelWithUI extends GUIState {

	public TimeSeriesChartGenerator priceChart;

	public JFrame priceFrame;

	public TimeSeriesChartGenerator acfChart;

	public JFrame acfFrame;

	public HistogramGenerator returnHist;

	public JFrame returnHistFrame;

	public GUIReporter myReporter;

	public double nextUpdate = 0;

	public static void main(String[] args) {
		ContModelWithUI householdWorld = new ContModelWithUI();
		Console c = new Console(householdWorld);
		c.setBounds(600, 0, 425, 470);
		c.setVisible(true);
	}

	public ContModelWithUI() {
		super(new ContModel(System.currentTimeMillis()));
	}

	public ContModelWithUI(SimState state) {
		super(state);
	}

	public static String getName() {
		return "ContModel Simulation";
	}

	public Object getSimulationInspectedObject() {
		return state;
	}

	public void start() {
		super.start();
		setupPortrayals();
	}

	public void load(SimState state) {
		super.load(state);
		setupPortrayals();
	}

	public void setupPortrayals() {

		this.scheduleImmediateRepeat(true, myReporter);

		final Steppable histUpdater = new Steppable() {

			private static final long serialVersionUID = 1L;

			public void step(SimState state) {

				if (state.schedule.getTime() >= nextUpdate) {

					if (returnHistFrame.isVisible()) {
						Double[] tempArray = new Double[myReporter.returnMemory.size()];
						tempArray = myReporter.returnMemory.toArray(tempArray);
						double[] temp2Array = new double[myReporter.returnMemory.size()];
						for (int i = 0; i < temp2Array.length; i++) {
							temp2Array[i] = tempArray[i].doubleValue();
						}
						if (temp2Array.length > 0) {
							returnHist.updateSeries(0, temp2Array, false);
						}
					}

					if (acfFrame.isVisible()) {
						myReporter.acfAbsReturns();
					}

					if (priceFrame.isVisible()) {
						priceChart.disable();
						myReporter.setSeries();
						priceChart.enable();
					}

					nextUpdate = nextUpdate + 1;
				}
			}
		};

		this.scheduleImmediateRepeat(false, histUpdater);

	}

	public void init(Controller c) {
		super.init(c);

		myReporter = new GUIReporter(this);

		priceChart = new TimeSeriesChartGenerator();
		priceChart.setTitle("Price and returns plot");
		priceChart.setDomainAxisLabel("Step");
		priceChart.setRangeAxisLabel("Price");
		priceChart.addSeries(myReporter.priceSeries, null);
		priceChart.addSeries(myReporter.returnSeries, null);
		priceFrame = priceChart.createFrame(this);
		priceFrame.getContentPane().setLayout(new BorderLayout());
		priceFrame.getContentPane().add(priceChart, BorderLayout.CENTER);
		priceFrame.pack();
		c.registerFrame(priceFrame);

		acfChart = new TimeSeriesChartGenerator();
		acfChart.setTitle("ACF of absolute returns");
		acfChart.setDomainAxisLabel("Lag");
		acfChart.setRangeAxisLabel("Correlation");
		acfChart.addSeries(myReporter.acfAbsReturnsSeries, null);
		acfFrame = acfChart.createFrame(this);
		acfFrame.getContentPane().setLayout(new BorderLayout());
		acfFrame.getContentPane().add(acfChart, BorderLayout.CENTER);
		acfFrame.pack();
		c.registerFrame(acfFrame);

		double[] fakeArray = { 1, 2, 3 };

		returnHist = new HistogramGenerator();
		returnHist.setTitle("Returns histogram");
		returnHist.setDomainAxisLabel("Value");
		returnHist.setRangeAxisLabel("Number of observations");
		returnHist.addSeries(fakeArray, 20, "Return histogram", null);
		returnHist.update();
		returnHistFrame = returnHist.createFrame(this);
		returnHistFrame.getContentPane().setLayout(new BorderLayout());
		returnHistFrame.getContentPane().add(returnHist, BorderLayout.CENTER);
		returnHistFrame.pack();
		returnHistFrame.setVisible(false);
		c.registerFrame(returnHistFrame);

	}

	public void quit() {
		super.quit();

	}

}

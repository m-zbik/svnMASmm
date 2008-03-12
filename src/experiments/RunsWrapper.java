package experiments;

import model.FinancialModel;

public class RunsWrapper {

	/**
	 * @param args
	 */
	public static void main(String[] args) {

		FinancialModel hhw = new FinancialModel(System.currentTimeMillis());
		hhw.wrapperActive = true;

		for (int t = 0; t < 6; t++) {

			System.out.println("Run " + t);
			hhw.parameterMap.put("D", Math.pow(10, -1*t));
			System.out.println(hhw.parameterMap.get("D"));
			
			hhw.start();
			while ((hhw.schedule.step(hhw))) {
			}
			hhw.finish();

		}

		hhw.myReporter.finishAll();
	}

}

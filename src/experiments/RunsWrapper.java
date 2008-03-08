package experiments;

import model.ContModel;


public class RunsWrapper {

	/**
	 * @param args
	 */
	public static void main(String[] args) {

		ContModel hhw = new ContModel(System.currentTimeMillis());

		for (int t = 2; t < 10; t++) {

			System.out.println("Run " + t);

			

			hhw.start();

			while ((hhw.schedule.step(hhw))) {
			}
			hhw.finish();

		}

		hhw.myReporter.finishAll();
	}

}

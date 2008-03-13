package model.agents;

import model.FinancialModel;
import sim.engine.Steppable;

public abstract class GenericPlayer implements Steppable {
	
	// trader's id
	public int id;

	// instantiate ContModel class in order to access its variables
	public FinancialModel myWorld;

}

package org.javarosa.entity.activity;
import org.javarosa.core.api.Transitions;


public interface EntitySelectTransitions extends Transitions {

	void newEntity ();
	
	void entitySelected (int id);

	void cancel ();
	
    void empty ();
	
}

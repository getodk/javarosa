package org.javarosa.entity.api.transitions;


public interface EntitySelectTransitions {

	void newEntity ();
	
	void entitySelected (int id);

	void cancel ();
	
    void empty ();
	
}

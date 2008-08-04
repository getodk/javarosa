package org.javarosa.model;

import java.util.Vector;

import org.javarosa.core.model.IDataReference;
import org.javarosa.core.model.data.IAnswerData;
import org.javarosa.core.model.instance.QuestionDataGroup;

public class GraphDataGroup extends QuestionDataGroup {
	public static final int GRAPH_DATA_ID = 11;
	
	IDataReference reference;
	
	public GraphDataGroup() {
		super();
	}
	
	public void setReference(IDataReference reference) {
		this.reference = reference;
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.javarosa.core.model.instance.TreeElement#matchesReference(org.javarosa.core.model.IDataReference)
	 */
	public boolean matchesReference(IDataReference reference) {
		return reference.equals(this.reference); 
	}
	
	public IAnswerData getValue() {
		//Parse this subtree and create the appropriate GraphData value
		return null;
	}
	
	public void setValue(IAnswerData data) {
		
	}
}

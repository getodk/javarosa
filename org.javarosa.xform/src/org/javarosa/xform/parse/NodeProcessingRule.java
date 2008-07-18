package org.javarosa.xform.parse;

import java.util.*;

//unused for now

public class NodeProcessingRule {
	public String name;
	public boolean allowUnknownChildren;
	public boolean allowChildText;
	public Hashtable childRules;
	
	public NodeProcessingRule (String name, boolean allowUnknownChildren, boolean allowChildText) {
		this.name = name;
		this.allowUnknownChildren = allowUnknownChildren;
		this.allowChildText = allowChildText;
		childRules = new Hashtable();
	}
	
	public void addChild (ChildProcessingRule cpr) {
		childRules.put(cpr.name, cpr);
	}
}

package org.javarosa.xform.parse;


//unused for now

public class ChildProcessingRule {
	public static final int MULT_NOT_ALLOWED = 1;
	public static final int MULT_PROCESS_FIRST_ONLY = 2;
	public static final int MULT_PROCESS_ALL = 3;
 
	public String name;
	public IElementHandler handler;
	public boolean required;
	public boolean anyLevel;
	public int multiplicity;

	public ChildProcessingRule (String name, IElementHandler handler, boolean required, boolean anyLevel, int multiplicity) {
		this.name = name;
		this.handler = handler;
		this.required = required;
		this.anyLevel = anyLevel;
		this.multiplicity = multiplicity;
	}
}

package org.javarosa.core.model;

public class DataBinding {
	private IDataReference ref;
	private int dataType;
	//private ... constraints;
	private Condition condition;
	private boolean required;
	
	private String preload;
	private String preloadParams;
	
	public IDataReference getReference() {
		return ref;
	}
	
	public void setReference(IDataReference ref) {
		this.ref = ref;
	}
	
	public int getDataType() {
		return dataType;
	}
	
	public void setDataType(int dataType) {
		this.dataType = dataType;
	}
	
	public Condition getCondition() {
		return condition;
	}
	
	public void setCondition(Condition condition) {
		this.condition = condition;
	}
	
	public boolean isRequired() {
		return required;
	}
	
	public void setRequired(boolean required) {
		this.required = required;
	}
	
	public String getPreload() {
		return preload;
	}
	
	public void setPreload(String preload) {
		this.preload = preload;
	}
	
	public String getPreloadParams() {
		return preloadParams;
	}
	
	public void setPreloadParams(String preloadParams) {
		this.preloadParams = preloadParams;
	}
	
	
}

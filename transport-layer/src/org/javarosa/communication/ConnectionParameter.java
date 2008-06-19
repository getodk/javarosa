package org.javarosa.communication;

public class ConnectionParameter {

	private int connectionType;
	private String name;
	private String value;
	
	public ConnectionParameter(int connectionType, String name, String value) {
		super();
		this.connectionType = connectionType;
		this.name = name;
		this.value = value;
	}

	public int getConnectionType() {
		return connectionType;
	}

	public void setConnectionType(int connectionType) {
		this.connectionType = connectionType;
	}

	public String getName() {
		return name;
	}

	public void setName(String key) {
		this.name = key;
	}

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}
}

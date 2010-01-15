package org.javarosa.formmanager.api.transitions;

public interface HttpFetchTransitions {
	public void cancel();
	public byte[] fetched();
}

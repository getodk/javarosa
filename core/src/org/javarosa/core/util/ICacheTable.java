package org.javarosa.core.util;

public interface ICacheTable<K> {

	public K intern(K k);

	public void reset();
}

package org.javarosa.core.services.transport;

/**
 * 
 * @author <a href="mailto:m.nuessler@gmail.com">Matthias Nuessler</a>
 */
public interface MessageListener {

	public static final int TYPE_INFO = 0;
	public static final int TYPE_WARNING = 1;
	public static final int TYPE_ERROR = 2;

	/**
	 * @param message
	 */
	public void onMessage(String message, int messageType);

}

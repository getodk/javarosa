package org.javarosa.core.services.transport;

import org.javarosa.core.api.IActivity;
import org.javarosa.core.services.ITransportManager;

/**
 * Interface all transport methods have to implement.
 * 
 * @author <a href="mailto:m.nuessler@gmail.com">Matthias Nuessler</a>
 */
public interface TransportMethod {

	public static final int HTTP_GCF = 0;
	public static final int FILE = 1;
	public static final int SERIAL = 2;
	public static final int BLUETOOTH = 3;
	public static final int HTTP_IO = 4;
	public static final int SMS = 5;
	
	public static final String DESTINATION_KEY = "destination";

	/**
	 * @param message
	 * @param manager
	 */
	public void transmit(TransportMessage message, ITransportManager manager);

	/**
	 * @return the name of the transport method
	 */
	public String getName();

	/**
	 * @return the id of the transport method
	 */
	public int getId();

	/**
	 * @return Gets the default destination for Messages of this specific
	 * Method.
	 */
	public ITransportDestination getDefaultDestination();
	
	public void setDestinationRetrievalActivity(IActivity activity);
	
	public IActivity getDestinationRetrievalActivity();
	
	public void closeConnections();
	
}

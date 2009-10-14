/*
 * Copyright (C) 2009 JavaRosa
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

package org.javarosa.core.services.transport;

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
    public static final int RHTTP_GCF = 6;
	
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
	
	public void closeConnections();
	
}

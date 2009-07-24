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

package org.javarosa.formmanager.utility;

import java.util.Vector;

import org.javarosa.core.Context;
import org.javarosa.core.services.transport.ITransportDestination;
import org.javarosa.core.services.transport.TransportMethod;

public class TransportContext extends Context{
	
	private final static String MULTI_DATA_KEY = "mult-trans-data";
	
	public final static String MAIN_MENU = "main";
	public final static String MESSAGE_VIEW = "messages";
	public final static String SEND_DATA = "send";
	public final static String SEND_MULTIPLE_DATA = "send_multi";
	
	public static final String FORM_TRANSPORT_VIEW = "form-transport-view";
	
	public TransportContext(Context context) {
		super(context);
	}
	
	public String getRequestedTask() {
		if(contextObject.containsKey(FORM_TRANSPORT_VIEW)) {
			return (String)contextObject.get(FORM_TRANSPORT_VIEW);
		}
		else {
			return MAIN_MENU;
		}
	}
	
	public void setRequestedTask(String view) {
		contextObject.put(FORM_TRANSPORT_VIEW, view);
	}
	
	public void setDestination(ITransportDestination destination) {
		contextObject.put(TransportMethod.DESTINATION_KEY, destination);
	}
	
	public ITransportDestination getDestination() {
		return (ITransportDestination)contextObject.get(TransportMethod.DESTINATION_KEY);
	}
	
	public void setMultipleData(Vector data) {
		this.setElement(MULTI_DATA_KEY, data);
	}
	
	public Vector getMultipleData() { 
		return (Vector)this.getElement(MULTI_DATA_KEY);
	}
}

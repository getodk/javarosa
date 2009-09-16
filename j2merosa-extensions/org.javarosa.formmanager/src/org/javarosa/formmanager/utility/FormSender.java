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

import java.io.IOException;
import java.util.Enumeration;
import java.util.Vector;

import org.javarosa.core.JavaRosaServiceProvider;
import org.javarosa.core.model.instance.DataModelTree;
import org.javarosa.core.model.utils.IDataModelSerializingVisitor;
import org.javarosa.core.services.ITransportManager;
import org.javarosa.core.services.transport.IDataPayload;
import org.javarosa.core.services.transport.ITransportDestination;
import org.javarosa.formmanager.view.ISubmitStatusObserver;
import org.javarosa.formmanager.view.transport.FormTransportSubmitStatusScreen;
import org.javarosa.formmanager.view.transport.FormTransportViews;
import org.javarosa.formmanager.view.transport.MultiSubmitStatusScreen;
import org.javarosa.j2me.view.J2MEDisplay;

/**
 * Managing sending forms, both single forms, and multiple forms together
 * 
 */
public class FormSender implements Runnable {

	/**
	 * true if many forms will be sent at once
	 * 
	 * TODO eliminate - look at vector to determine single or multiple
	 */
	private boolean multiple;

	/**
	 * The data to be sent when multiple = false
	 * 
	 * TODO: eliminate in favour of using Vector for both multiple and single
	 */
	private DataModelTree data;

	/**
	 * The data to be sent when multiple = true
	 */
	private Vector multiData;

	/**
	 * 
	 */
	private IDataModelSerializingVisitor serializer;
	/**
	 * this is also used as a flag TODO: eliminate its usage as a flag
	 */
	private ITransportDestination destination;
	
	private ISubmitStatusObserver observer;
	
	private FormTransportViews views;

	/**
	 * @param shell
	 * @param activity
	 */
	public FormSender(FormTransportViews views, ITransportDestination destination) {
		this.views = views;
		this.destination = destination;
	}
	
	public void sendData() {
		// #debug debug
		System.out.println("Sending data .. multiple=" + multiple);
		
		initDisplay();
		
		new Thread(this).start();
	}

	/**
	 * @param mainMenu
	 * @throws IOException
	 */
	private void sendSingle() throws IOException {

		if (this.data == null)
			throw new RuntimeException(
					"null data when trying to send single data");

		IDataPayload payload = this.serializer
				.createSerializedPayload(this.data);

		// #debug debug
		System.out.println("Sending single datum, serialized id="
				+ this.data.getID() + " length=" + payload.getLength());

		send(payload, this.data.getID());
	}
	
	private void initDisplay() {

		if (this.multiple) {
			MultiSubmitStatusScreen s = views.getMultiSubmitStatusScreen();

			boolean noData = (this.multiData == null)
					|| (this.multiData.size() == 0);

			if (noData) {
				s.reinitNodata();
			} else {

				String idsStr = "";
				// #debug debug
				System.out.println("Multi send");
				int[] ids = new int[this.multiData.size()];

				for (int i = 0; i < ids.length; ++i) {
					ids[i] = ((IDataPayload) this.multiData.elementAt(i))
							.getTransportId();
					idsStr += " " + ids[i];
				}

				s.reinit(ids);

				// #debug debug
				System.out.println("ids: " + idsStr);
			}

			J2MEDisplay.getDisplay().setCurrent(s);
			setObserver(s);
		}
		else {
			FormTransportSubmitStatusScreen statusScreen = views.getSubmitStatusScreen();
			statusScreen.reinit(this.data.getID());
			J2MEDisplay.getDisplay().setCurrent(statusScreen);
			setObserver(statusScreen);
		}
	}

	/**
	 * @throws IOException
	 */
	private void sendMultiData() throws IOException {
		
		boolean noData = (this.multiData == null)
		|| (this.multiData.size() == 0);
		
		if (!noData) {
			for (Enumeration en = this.multiData.elements(); en
					.hasMoreElements();) {
				IDataPayload payload = (IDataPayload) en.nextElement();
				send(payload, payload.getTransportId());
			}
		}

	}

	private void send(IDataPayload payload, int id) throws IOException {
		JavaRosaServiceProvider.instance().getTransportManager().enqueue(
				payload, this.destination, getCurrentTransportMethod(), id);
	}

	/**
	 * @return
	 */
	private ITransportDestination getDefaultDestination() {
		ITransportManager tmanager = JavaRosaServiceProvider.instance()
				.getTransportManager();
		int currentMethod = tmanager.getCurrentTransportMethod();
		ITransportDestination d = tmanager
				.getDefaultTransportDestination(currentMethod);
		return d;
	}

	public void setDefaultDestination() {
		this.destination = getDefaultDestination();
	}

	private int getCurrentTransportMethod() {
		return JavaRosaServiceProvider.instance().getTransportManager()
				.getCurrentTransportMethod();

	}

	// ----------- getters and setters
	
	public void setObserver(ISubmitStatusObserver o) {
		this.observer = o;
	}
	public DataModelTree getData() {
		return this.data;
	}

	public void setData(DataModelTree data) {
		this.data = data;
	}

	public void setSerializer(IDataModelSerializingVisitor serializer) {
		this.serializer = serializer;
	}

	public ITransportDestination getDestination() {
		return this.destination;
	}

	public void setDestination(ITransportDestination destination) {
		this.destination = destination;
	}

	public boolean isMultiple() {
		return this.multiple;
	}

	public void setMultiple(boolean multiple) {
		this.multiple = multiple;
	}

	// -- multiple
	public Vector getMultiData() {
		return this.multiData;
	}

	public void setMultiData(Vector multiData) {
		this.multiData = multiData;
	}

	public void run() {
		if (this.multiple) {
			try {
				sendMultiData();
			}
			catch(IOException e) {
				e.printStackTrace();
				if(observer != null) {
					observer.receiveMessage(ISubmitStatusObserver.ERROR, e.getMessage());
				}
			}

		} else {
			try{ 
				sendSingle();
			}
			catch(IOException e) {
				e.printStackTrace();
				if(observer != null) {
					observer.receiveMessage(ISubmitStatusObserver.ERROR, e.getMessage());
				}
			}
		}
	}

}

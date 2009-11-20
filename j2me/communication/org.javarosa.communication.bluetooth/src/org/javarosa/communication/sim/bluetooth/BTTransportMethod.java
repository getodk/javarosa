package org.javarosa.communication.sim.bluetooth;

import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Vector;
import java.io.*;

import javax.bluetooth.BluetoothStateException;
import javax.microedition.io.*;
import javax.microedition.lcdui.*;
import javax.microedition.midlet.*;

import javax.microedition.io.Connection;
import javax.microedition.io.Connector;
import javax.microedition.io.HttpConnection;
import javax.microedition.io.StreamConnection;


//import javax.microedition.io.HttpConnection;
import javax.microedition.lcdui.Alert;
import javax.microedition.lcdui.AlertType;
import javax.microedition.lcdui.Image;


import org.javarosa.core.JavaRosaServiceProvider;
import org.javarosa.core.api.IActivity;
import org.javarosa.core.api.IView;
import org.javarosa.core.services.ITransportManager;
import org.javarosa.core.services.transport.ByteArrayPayload;
import org.javarosa.core.services.transport.IDataPayload;
import org.javarosa.core.services.transport.ITransportDestination;
import org.javarosa.core.services.transport.TransportMessage;
import org.javarosa.core.services.transport.TransportMethod;

/**
 * 
 * @author Simon Peter Muwanga
 */
public class BTTransportMethod implements TransportMethod {

	private static final String name = "Bluetooth";

	private ITransportManager manager;
	
	private IActivity destinationRetrievalActivity;
	

	
	
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see org.openmrs.transport.TransportMethod#transmit(org.openmrs.transport.TransportMessage)
	 */
	public void transmit(TransportMessage message, ITransportManager manager) {
		try{
		cacheURL(message);		
		this.manager = manager;
		
		
		new Thread(new WorkerThread(message)).start();
		
		
		
		}//End try
		 catch(Exception e){
		    AlertEngine alertEngine = new AlertEngine("ERROR! ioe", e.getMessage()+" Testing", null, AlertType.ERROR);
			JavaRosaServiceProvider.instance().showView(alertEngine);
		}// end catch
	}
	
	protected void cacheURL(TransportMessage message) {
		try{
		String destinationUrl = ((BTTransportDestination)message.getDestination()).getURL();
		//destinationUrl="btspp://00037ABEFE97:1";
		
		Vector existingURLs = JavaRosaServiceProvider.instance().getPropertyManager().getProperty(BTTransportProperties.POST_URL_LIST_PROPERTY);
		
		if(existingURLs!=null){
			if (!existingURLs.contains(destinationUrl)) {
				
				existingURLs.addElement(destinationUrl);
				
				JavaRosaServiceProvider.instance().getPropertyManager().setProperty(BTTransportProperties.POST_URL_LIST_PROPERTY,existingURLs);
			}	
		}else{
			//add code to add urls outside the HttpTransportProperties 
		}
		
	}//End try
	 catch(Exception e){
		 AlertEngine alertEngine = new AlertEngine("ERROR! ioe", e.getMessage()+" cacheURL()", null, AlertType.ERROR);
			JavaRosaServiceProvider.instance().showView(alertEngine);
	}// end catch
		
	}

	/**
	 * 
	 * @author <a href="mailto:m.nuessler@gmail.com">Matthias Nuessler</a>
	 */
	class WorkerThread implements Runnable {
		
		
		
		private TransportMessage message;
		
		public WorkerThread(TransportMessage message){
			this.message  = message;
		}
		
		
		/*public void setMessage(TransportMessage message) {
			this.message  = message;
		}*/
		
		
		/*public void cleanStreams(){
			if (in != null) {
				try {
					in.close();
				} catch (IOException e) {
					// ignore
				}
			}
			if (out != null) {
				try {
					out.close();
				} catch (IOException e) {
					// ignore
				}
			}
			if (con != null) {
				try {
					con.close();
				} catch (IOException e) {
					// ignore
				}
			}
			
		}
*/
		/*
		 * (non-Javadoc)
		 * 
		 * @see java.lang.Thread#run()
		 */
		public void run() {
			
			//This code works fine. Its for sending a text to the server
			/*try{
		    	 StreamConnection con = (StreamConnection)Connector.open("btspp://00037ABEFE97:1");
		    	 
				 DataInputStream in = con.openDataInputStream();
				 DataOutputStream out = con.openDataOutputStream();
				 String theMessage = "Hello from JR Client";
				 
				 out.writeUTF(theMessage);
				 out.flush();
				 
				 String returnedString = in.readUTF();
				 AlertEngine alertEngine = new AlertEngine("Server Response", returnedString, null, AlertType.ERROR);
					JavaRosaServiceProvider.instance().showView(alertEngine);
					
					in.close();
					out.close();
					con.close();
		    	}
		    	catch(IOException e){
		    		AlertEngine alertEngine = new AlertEngine("ERROR! ioe", e.getMessage(), null, AlertType.ERROR);
					JavaRosaServiceProvider.instance().showView(alertEngine);
		    	}*/
			IDataPayload payload = null;
				
			try{
				
				payload = message.getPayloadData();
				
					
					AlertEngine alertEngine1 = new AlertEngine("payload size: "+ payload.getLength(), "InputStream error, here", null, AlertType.ERROR);
					JavaRosaServiceProvider.instance().showView(alertEngine1);
					
				
				//IDataPayload httpload = (IDataPayload)payload.accept(visitor);
					
				/*	HttpTransportDestination destination = (HttpTransportDestination)message.getDestination();
					con = (HttpConnection) Connector.open(destination.getURL());*/
				
					BTTransportDestination destination = (BTTransportDestination)message.getDestination();
				 
					//StreamConnection con = (StreamConnection)Connector.open("btspp://00037ABEFE97:1");
		    	 
					StreamConnection con = (StreamConnection)Connector.open(destination.getURL());
				 DataInputStream in = con.openDataInputStream();
				 DataOutputStream out = con.openDataOutputStream();
				 
				 InputStream valueStream = null;
				 int val = -1;
				 try{
				 valueStream = payload.getPayloadStream();
				 
				 val = valueStream.read();//reads the next byte from the inputStream. The byte's value is an int, which is stored in val
				 
				 }
				 catch(Exception e){
				 AlertEngine alertEngine = new AlertEngine("Inputstream: "+val, "InputStream error, here", null, AlertType.ERROR);
				 JavaRosaServiceProvider.instance().showView(alertEngine);
				 
				 }
				
				
				   while(val != -1) 
				   {		
					 out.write(val);
					 val = valueStream.read();
					 
				   }
				   
				
				//#if debug.output==verbose
				//System.out.println("PAYLOADDATA:"+new String(message.getPayloadData())+"\nENDPLDATA\n");
				//#endif
				valueStream.close();
				out.flush();
				
				
				// Get the length and process the data
				byte[] data; 
				//int len = (int)con.getLength();
				int len = in.read();
				int read;
				if (len >= 0) {
					data = new byte[len];
					read = 0;
					while (read < len) {
						int k = in.read(data, read, len - read);
						if (k == -1)
							break;
						read += k;
					}
				} else {
					ByteArrayOutputStream buffer = new ByteArrayOutputStream();
					while (true) {
						int b = in.read();
						if (b == -1) {
							break;
						}
						buffer.write(b);
					}
					data = buffer.toByteArray();
					read = data.length;
				}
				
				process(data);
				
				// update status
				message.setStatus(TransportMessage.STATUS_DELIVERED);
				//#if debug.output==verbose
				System.out.println("Status: " + message.getStatus());
				//#endif
				message.setChanged();
				message.notifyObservers(message.getReplyloadData());
			
			
			}catch(Exception e){
				AlertEngine alertEngine = new AlertEngine("Err:... "+payload+" ...HH","Hi", null, AlertType.ERROR);
				JavaRosaServiceProvider.instance().showView(alertEngine);
			}
		
		}

		private void process(byte data) {
			System.out.print(data);
			byte[] temp = new byte[1];
			temp[0] = data;

			message.setReplyloadData(temp);
		}

		private void process(byte[] data) {
			//#if debug.output==verbose
			System.out.println(new String(data));
			//#endif
			message.setReplyloadData(data);
		}

	}// end Workthread Class

	//=====================================================
	private void cleanUp(DataInputStream in) {
		if (in != null) {
			try {
				in.close();
			} catch (IOException e) {
				// ignore

				AlertEngine alertEngine = new AlertEngine("ERROR! Clean up", e.getMessage(), null, AlertType.ERROR);
				JavaRosaServiceProvider.instance().showView(alertEngine);
			}
		}
	}

	public void cleanUp(DataOutputStream out) {
		if (out != null) {
			try {
				out.close();
			} catch (IOException e) {
				// ignore

				AlertEngine alertEngine = new AlertEngine("ERROR! clean up", e.getMessage(), null, AlertType.ERROR);
				JavaRosaServiceProvider.instance().showView(alertEngine);
			}
		}
	}

	/**
	 * @param con
	 *            the connection
	 */
	private void cleanUp(Connection con) {
		if (con != null) {
			try {
				con.close();
			} catch (IOException e) {
				// ignore
			}
		}

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.openmrs.transport.TransportMethod#getName()
	 */
	public String getName() {
		return name;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.openmrs.transport.TransportMethod#getId()
	 */
	public int getId() {
		return TransportMethod.BLUETOOTH;
	}

	/*
	 * (non-Javadoc)
	 * @see org.javarosa.core.services.transport.TransportMethod#getDefaultDestination()
	 */
	public ITransportDestination getDefaultDestination() {
		String url = JavaRosaServiceProvider.instance().getPropertyManager().getSingularProperty(BTTransportProperties.POST_URL_PROPERTY);
		
		if(url == null) {
			return null;
		} else {
			//url = "btspp://00037ABEFE97:1";

			String showURL = "Now using "+url;
			AlertEngine alertEngine = new AlertEngine("ERROR! ioe", showURL, null, AlertType.ERROR);
			JavaRosaServiceProvider.instance().showView(alertEngine);
			
			return new BTTransportDestination(url);
		}
	}
	public void setDestinationRetrievalActivity(IActivity activity) {
		destinationRetrievalActivity = activity;
	}
	
	public IActivity getDestinationRetrievalActivity() {
		return destinationRetrievalActivity;
	}

	
	public void closeConnections() {
		/*if(primaryWorker!=null){
			primaryWorker.cleanStreams();
		}*/

	}
	
}

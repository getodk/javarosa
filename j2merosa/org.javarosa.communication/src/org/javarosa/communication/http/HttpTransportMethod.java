package org.javarosa.communication.http;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Vector;

import javax.microedition.io.Connection;
import javax.microedition.io.Connector;
import javax.microedition.io.HttpConnection;

import org.javarosa.core.JavaRosaServiceProvider;
import org.javarosa.core.api.IActivity;
import org.javarosa.core.services.ITransportManager;
import org.javarosa.core.services.transport.ITransportDestination;
import org.javarosa.core.services.transport.TransportMessage;
import org.javarosa.core.services.transport.TransportMethod;

/**
 * 
 * @author <a href="mailto:m.nuessler@gmail.com">Matthias Nuessler</a>
 */
public class HttpTransportMethod implements TransportMethod {

	private static final String name = "HTTP";

	private TransportMessage message;

	private ITransportManager manager;
	
	private IActivity destinationRetrievalActivity;
	private WorkerThread primaryWorker;

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.openmrs.transport.TransportMethod#transmit(org.openmrs.transport.TransportMessage)
	 */
	public void transmit(TransportMessage message, ITransportManager manager) {
		cacheURL(message);
		this.message = message;
		this.manager = manager;
		primaryWorker = new WorkerThread();
		new Thread(primaryWorker).start();
	}
	
	private void cacheURL(TransportMessage message) {
		String destinationUrl = ((HttpTransportDestination)message.getDestination()).getURL();
		Vector existingURLs = JavaRosaServiceProvider.instance()
		.getPropertyManager().getProperty(
				HttpTransportProperties.POST_URL_LIST_PROPERTY);
		
		if(existingURLs!=null){
			if (!existingURLs.contains(destinationUrl)) {
				existingURLs.addElement(destinationUrl);
				JavaRosaServiceProvider.instance().getPropertyManager()
				.setProperty(
						HttpTransportProperties.POST_URL_LIST_PROPERTY,
						existingURLs);
			}	
		}else{
			//add code to add urls outside the HttpTransportProperties 
		}
		
	}

	/**
	 * 
	 * @author <a href="mailto:m.nuessler@gmail.com">Matthias Nuessler</a>
	 */
	private class WorkerThread implements Runnable {
		private HttpConnection con = null;
		private InputStream in = null;
		private OutputStream out = null;
		
		
		public void cleanStreams(){
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

		/*
		 * (non-Javadoc)
		 * 
		 * @see java.lang.Thread#run()
		 */
		public void run() {
			
			int responseCode;
			try {
				HttpTransportDestination destination = (HttpTransportDestination)message.getDestination();
				con = (HttpConnection) Connector.open(destination.getURL());

				con.setRequestMethod(HttpConnection.POST);
				con.setRequestProperty("User-Agent",
						"Profile/MIDP-2.0 Configuration/CLDC-1.1");
				con.setRequestProperty("Content-Language", "en-US");

				out = con.openOutputStream();
				out.write(message.getPayloadData());
				//#if debug.output==verbose
				System.out.println("PAYLOADDATA:"+new String(message.getPayloadData())+"\nENDPLDATA\n");
				//#endif
				out.flush();

				responseCode = con.getResponseCode();
				if (responseCode != HttpConnection.HTTP_OK) {
					throw new IOException("Response code: " + responseCode);
				}

				in = con.openInputStream();

				// Get the ContentType
				String type = con.getType();
				// processType(type);
				System.out.println("Content type: " + type);

				// Get the length and process the data
				int len = (int) con.getLength();
				if (len > 0) {
					int actual = 0;
					int bytesread = 0;
					byte[] data = new byte[len];
					while ((bytesread != len) && (actual != -1)) {
						actual = in.read(data, bytesread, len - bytesread);
						bytesread += actual;
					}
					process(data);
					//#if debug.output==verbose
					System.out.println("PRCSS DATA end");
					//#endif
				} else {
					int ch;
					while ((ch = in.read()) != -1) {
						process((byte) ch);
					}
				}

				// update status
				message.setStatus(TransportMessage.STATUS_DELIVERED);
				//#if debug.output==verbose
				System.out.println("Status: " + message.getStatus());
				//#endif
				message.setChanged();
				message.notifyObservers(message.getReplyloadData());

			} catch (ClassCastException e) {
				throw new IllegalArgumentException(message.getDestination()
						+ " is not a valid HTTP URL");
			} catch (IOException e) {
				//#if debug.output==verbose || debug.output==exception
				System.out.println(e.getMessage());
				//#endif
			} catch(java.lang.SecurityException se) {
	             /***
                 * This exception was added to deal with the user denying access to airtime
                 */
			 // update status
                message.setStatus(TransportMessage.STATUS_FAILED);
        		//#if debug.output==verbose || debug.output==exception
                System.out.println("Status: " + message.getStatus());
                //#endif
                message.setChanged();
                message.notifyObservers(null); 
		    }finally {
				cleanUp(in);
				cleanUp(out);
				cleanUp(con);
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

	}

	private void cleanUp(InputStream in) {
		if (in != null) {
			try {
				in.close();
			} catch (IOException e) {
				// ignore
			}
		}
	}

	public void cleanUp(OutputStream out) {
		if (out != null) {
			try {
				out.close();
			} catch (IOException e) {
				// ignore
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
		return TransportMethod.HTTP_GCF;
	}

	/*
	 * (non-Javadoc)
	 * @see org.javarosa.core.services.transport.TransportMethod#getDefaultDestination()
	 */
	public ITransportDestination getDefaultDestination() {
		String url = JavaRosaServiceProvider.instance().getPropertyManager().getSingularProperty(HttpTransportProperties.POST_URL_PROPERTY);
		if(url == null) {
			return null;
		} else {
			return new HttpTransportDestination(url);
		}
	}
	public void setDestinationRetrievalActivity(IActivity activity) {
		destinationRetrievalActivity = activity;
	}
	
	public IActivity getDestinationRetrievalActivity() {
		return destinationRetrievalActivity;
	}

	
	public void closeConnections() {
		if(primaryWorker!=null){
			primaryWorker.cleanStreams();
		}
		
		
	}
}

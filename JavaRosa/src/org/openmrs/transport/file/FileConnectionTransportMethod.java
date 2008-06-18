package org.openmrs.transport.file;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Enumeration;

import javax.microedition.io.Connector;
import javax.microedition.io.file.FileConnection;
import javax.microedition.io.file.FileSystemRegistry;

import org.openmrs.transport.MessageListener;
import org.openmrs.transport.Serializer;
import org.openmrs.transport.TransportManager;
import org.openmrs.transport.TransportMessage;
import org.openmrs.transport.TransportMethod;
import org.openmrs.transport.util.Logger;

/**
 * 
 * @author <a href="mailto:m.nuessler@gmail.com">Matthias Nuessler</a>
 */
public class FileConnectionTransportMethod implements TransportMethod {

	private static final String name = "File";

	private TransportMessage message;

	private TransportManager manager;

	/**
	 * Creates a new instance of <code>FileConnectionTransportMethod</code>
	 */
	public FileConnectionTransportMethod() {
		// nothing
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.openmrs.transport.TransportMethod#transmit(org.openmrs.transport.TransportMessage,
	 *      org.openmrs.transport.TransportManager)
	 */
	public void transmit(TransportMessage message, TransportManager manager) {
		System.out.println("File connection: transmit");
		this.message = message;
		this.manager = manager;
		new Thread(new WorkerThread()).start();
	}

	/**
	 * 
	 * @author <a href="mailto:m.nuessler@gmail.com">Matthias Nuessler</a>
	 */
	private class WorkerThread implements Runnable {

		/*
		 * (non-Javadoc)
		 * 
		 * @see java.lang.Runnable#run()
		 */
		public void run() {
			for (Enumeration en = FileSystemRegistry.listRoots(); en
					.hasMoreElements();) {
				String root = (String) en.nextElement();
				Logger.log("Root: " + root);
			}
			// String root = (String)
			// FileSystemRegistry.listRoots().nextElement();
			String root = "myModels/";
			String filename = "test.txt";
			String uri = "file:///" + root + filename;
			uri = message.getDestination();
			Logger.log("File URI: " + uri);
			FileConnection fcon = null;
			try {
				fcon = (FileConnection) Connector.open(uri);
				if (!fcon.exists()) {
					fcon.create();
				}
				OutputStream out = fcon.openOutputStream();
				out.write(Serializer.serialize(message));
				Logger.log("Data written to file " + uri);
				//update status
				message.setStatus(TransportMessage.STATUS_DELIVERED);
				message.setChanged();
				message.notifyObservers(null);
			} catch (SecurityException e) {
				Logger.log(e);
				manager.showMessage("Not permitted to write to file",
						MessageListener.TYPE_ERROR);
			} catch (IOException e) {
				Logger.log(e);
			} catch (Exception e) {
				Logger.log(e);
			} finally {
				if (fcon != null) {
					try {
						fcon.close();
					} catch (Exception e) {
						// ignore
					}
				}
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
		return TransportMethod.FILE;
	}

}

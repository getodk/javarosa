/**
 * 
 */
package org.javarosa.j2me.file;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Hashtable;

import javax.microedition.io.Connector;
import javax.microedition.io.file.FileConnection;

import org.javarosa.core.reference.Reference;

/**
 * A J2ME File reference is a reference type which refers to a 
 * FileConnection on a j2me phone. It is assumed that the
 * file reference is provided with a local file root
 * which is valid on the device (For which helper utilities
 * can be found in the J2meFileSystemProperties definition).
 * 
 * Note: J2ME File Connections must be managed carefully 
 * (Multiple connections to a single file cannot exist),
 * and this object cannot guarantee (yet) thread safety
 * on access to a single connection.
 * 
 * @author ctsims
 *
 */
public class J2meFileReference implements Reference
{
	private static Hashtable<String, FileConnection> connections = new Hashtable<String, FileConnection>(); 
	
	String localPart;
	String referencePart;
	
	/**
	 * Creates a J2ME file reference of the format 
	 * 
	 * "jr://file"+referencePart"
	 * 
	 * which refers to the URI
	 * 
	 * "file:///" + localPart + referencePart
	 * 
	 * @param localPart
	 * @param referencePart
	 */
	public J2meFileReference(String localPart, String referencePart) {
		this.localPart = localPart;
		this.referencePart = referencePart;
	}

	/*
	 * (non-Javadoc)
	 * @see org.javarosa.core.reference.Reference#getStream()
	 */
	public boolean doesBinaryExist() throws IOException {
		return connector().exists();
	}

	/*
	 * (non-Javadoc)
	 * @see org.javarosa.core.reference.Reference#getStream()
	 */
	public InputStream getStream() throws IOException {
		return connector().openInputStream();
	}

	/*
	 * (non-Javadoc)
	 * @see org.javarosa.core.reference.Reference#getURI()
	 */
	public String getURI() {
		return "jr://file" + referencePart;
	}

	/*
	 * (non-Javadoc)
	 * @see org.javarosa.core.reference.Reference#isReadOnly()
	 */
	public boolean isReadOnly() {
		return false;
	}
	

	/*
	 * (non-Javadoc)
	 * @see org.javarosa.core.reference.Reference#getOutputStream()
	 */
	public OutputStream getOutputStream() throws IOException {
		FileConnection connector = connector();
		if(!connector.exists()) {
			connector.create();
		} else {
			//TODO: Delete exist file, maybe? Probably....
		}
		return connector.openOutputStream();
	}
	
	private FileConnection connector() throws IOException {
		String uri = getLocalURI();
		
		synchronized (connections) {

			// We only want to allow one connection to a file at a time.
			// Otherwise we can get into trouble when we want to remove it.
			if (connections.containsKey(uri)) {
				return connections.get(uri);
			} else {
				FileConnection connection = (FileConnection) Connector.open(uri);
				// Store the newly opened connection for reuse.
				connections.put(uri, connection);

				return connection;
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * @see org.javarosa.core.reference.Reference#remove()
	 */
	public void remove() throws IOException {
		FileConnection con = connector();
		
		//TODO: this really needs to be written better, but 
		//for now avoiding deadlock is better than ensuring
		//thread safety
		
		//Take a lock on the connection so that nothing tries
		//to access it while this happens
		synchronized(con) {
			con.delete();
			con.close();
		}
		
		//Take a lock on the connections so that
		//nothing can retrieve the connection
		synchronized(connections) {
			//Remove the local connection now that it's 
			//closed.
			connections.remove(getLocalURI());
		}
	}

	/*
	 * (non-Javadoc)
	 * @see org.javarosa.core.reference.Reference#getLocalURI()
	 */
	public String getLocalURI() {
		return "file:///" + localPart + referencePart;
	}
}

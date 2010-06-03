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
 * @author ctsims
 *
 */
public class J2meFileReference implements Reference
{
	private static Hashtable<String, FileConnection> connections = new Hashtable<String, FileConnection>(); 
	
	String localPart;
	String referencePart;
	
	public J2meFileReference(String localPart, String referencePart) {
		this.localPart = localPart;
		this.referencePart = referencePart;
	}

	public boolean doesBinaryExist() throws IOException {
		return connector().exists();
	}

	public InputStream getStream() throws IOException {
		return connector().openInputStream();
	}

	public String getURI() {
		return "jr://file" + referencePart;
	}

	public boolean isReadOnly() {
		return false;
	}
	

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
		if(connections.containsKey(uri)) {
			System.out.println("Retrieving existing connection for file " + uri);
			return connections.get(uri);
		} else {
			System.out.println("Opening file connector for file: " + uri);
			FileConnection connection = (FileConnection) Connector.open(uri);
			//We only want to allow one connection to a file at a time. Otherwise
			//we can get into trouble when we want to remove it.
			
			connections.put(uri, connection);
			return connection;
		}
	}

	public void remove() throws IOException {
		FileConnection con = connector();
		con.delete();
		
		con.close();
		connections.remove(getLocalURI());
	}

	public String getLocalURI() {
		return "file:///" + localPart + referencePart;
	}
}

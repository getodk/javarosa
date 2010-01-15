package org.javarosa.server.bluetooth;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.*;

import javax.microedition.io.*;
import javax.bluetooth.*;

public class RfcommServer implements Runnable{

	private LocalDevice localDevice;// local Bluetooth Manager
	
	// Bluetooth Service name
	private static final String myServiceName = "MyBtService";
	
	// Bluetooth Service UUID of interest
	private static final String myServiceUUID ="e01ea52ad90f10008001001742893406";
	private UUID MYSERVICEUUID_UUID = new UUID(myServiceUUID,false);//btspp://00037ABEFE97:1
	
	// Define the server connection URL
	String connURL = "btspp://localhost:"+MYSERVICEUUID_UUID.toString()+";name="+myServiceName;
	
	private void btInit() throws IOException{
		localDevice = null;
		
		//Retrieve local device to get the Bluetooth manager
		localDevice = LocalDevice.getLocalDevice();
		
		//Servers set the discoverable mode to GIAC
		localDevice.setDiscoverable(DiscoveryAgent.GIAC);
		
		//Create a server connection
		StreamConnectionNotifier scn = (StreamConnectionNotifier) Connector.open(connURL);
		
		// Accept a new client connection
		StreamConnection conn = scn.acceptAndOpen();
		// New client connection accepted; get a handle on it
		RemoteDevice rd = RemoteDevice.getRemoteDevice(conn);
		System.out.println("New client connection..."+rd.getFriendlyName(false));
		
		//read input message, in this case a string
		DataInputStream in = conn.openDataInputStream();
		/*String s = dataIn.readUTF();
		System.out.println("Received message: "+s);*/
		// Get the length and process the data
		byte[] data; 
		System.out.println("Hi JR");
		//int len = (int)con.getLength();
		int len = in.read();
		System.out.println("Hi JR2");
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
			
			System.out.println("Hi JR2");
		} else {
			System.out.println("Hi JR else: "+len);
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
		}// end else
		
		
		DataOutputStream output = conn.openDataOutputStream();
		String strData = "Just gotten your data";
		
		byte [] b ;
		b=strData.getBytes();
		output.write(b);
		output.flush();
		
		System.out.println("Data Sent succefully");
		//closing the service
		scn.close();
	}
	
	private void btInit2() throws IOException{
         localDevice = null;
		
		//Retrieve local device to get the Bluetooth manager
		localDevice = LocalDevice.getLocalDevice();
		
		//Servers set the discoverable mode to GIAC
		localDevice.setDiscoverable(DiscoveryAgent.GIAC);
		
		//Create a server connection
		StreamConnectionNotifier scn = (StreamConnectionNotifier) Connector.open(connURL);
		
		// Accept a new client connection
		StreamConnection conn = scn.acceptAndOpen();
		// New client connection accepted; get a handle on it
		RemoteDevice rd = RemoteDevice.getRemoteDevice(conn);
		System.out.println("New client connection..."+rd.getFriendlyName(false));
		
		//read input message, in this case a string
		DataInputStream dataIn = conn.openDataInputStream();
		String s = dataIn.readUTF();
		System.out.println("Received message: "+s);
		
		DataOutputStream output = conn.openDataOutputStream();
		output.writeUTF(s+" from OpenXdataServer");
		output.flush();
		
		//closing the service
		scn.close();

	}
	
	public void run() {
		// TODO Auto-generated method stub
		try {
			this.btInit();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	
}

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

package org.javarosa.communication.bluetooth.server;


import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Vector;

import javax.bluetooth.DataElement;
import javax.bluetooth.DiscoveryAgent;
import javax.bluetooth.LocalDevice;
import javax.bluetooth.ServiceRecord;
import javax.bluetooth.UUID;
import javax.microedition.io.Connector;
import javax.microedition.io.StreamConnection;
import javax.microedition.io.StreamConnectionNotifier;


/**
 * Acts as a bluetooth server using the JSR-82 API Desktop implementation.
 * NOTE: This server has been tested to only work Microsoft Bluetooth Stack.
 * You can use this link (http://hellalame.com/bluetooth.htm)
 * to help you install it if you have third party 
 * bluetooth stack drivers like the ones from TOSHIBA, etc.
 * 
 * If your server throws a BluetoothStateException even after your bluetooth
 * is turned on, you may need to ensure that the bluetooth-server project
 * is the first in your classpath entries.
 * 
 * @author Daniel Kayiwa
 *
 */
public final class BluetoothServer implements Runnable {
    /**
     *  Describes this server 
     */
    private UUID SERVER_UUID; //new UUID("F0E0D0C0B0A000908070605040302010", false);

    /**
     *  Keeps the local device reference. */
    private LocalDevice localDevice;

    /**
     *  Accepts new connections. */
    private StreamConnectionNotifier notifier;

    /** 
     * Keeps the information about this server. */
    private ServiceRecord record;

    /** 
     * Keeps the reference to the event listener to process specific actions. */
    private BluetoothServerListener eventListener;

    /** 
     * Becomes 'true' when this component is finalized. */
    private boolean isClosed;
    
    /** 
     * The name of this server. */
    private String name = "Bluetooth Server";

    /** 
     * Creates notifier and accepts clients to be processed. */
    private Thread accepterThread;

    /**
     *  Process the particular client from queue. */
    private ClientProcessor processor;

    /**
     * Constructs the bluetooth server, but it is initialized
     * in the different thread to "avoid dead lock".
     */
    public BluetoothServer(String name,String uuid,BluetoothServerListener eventListener) {
    	this.name = name;
    	this.SERVER_UUID = new UUID(uuid,false);
        this.eventListener = eventListener;
     }
    
    /**
     * Starts the the bluetooth server.
     *
     */
    public void start(){
        // we have to initialize a system in different thread...
        accepterThread = new Thread(this);
        accepterThread.start();	
    }

    /**
     * Accepts a new client and send him/her the requested data from server.
     */
    public void run() {

        try {
            // create/get a local device
            localDevice = LocalDevice.getLocalDevice();  

            // set we are discoverable
            if (!localDevice.setDiscoverable(DiscoveryAgent.GIAC))
            	raiseError("Can't set discoverable mode", null);

            // prepare a URL to create a notifier
            StringBuffer url = new StringBuffer("btspp://");

            // indicate this is a server
            url.append("localhost").append(':');

            // add the UUID to identify this service
            url.append(SERVER_UUID.toString());

            // add the name for our service
            url.append(";name=" + name);

            // request all of the client not to be authorized
            // some devices fail on authorize=true
            url.append(";authorize=false");

            // create notifier now
            notifier = (StreamConnectionNotifier)Connector.open(url.toString());

            // and remember the service record for the later updates
            record = localDevice.getRecord(notifier);
            
 			DataElement fullyAvailable = new DataElement(
					DataElement.U_INT_1, 0xFF);
			record.setAttributeValue(0x0008, fullyAvailable);
			localDevice.updateRecord(record);

        } catch (Exception e) {
        	raiseError("Can't initialize bluetooth: ",e);
        	return;
        }

        // ok, start processor now
        processor = new ClientProcessor(eventListener);

        // ok, start accepting connections then
        while (!isClosed) {
            StreamConnection conn = null;

            try {
            	//notifier = (StreamConnectionNotifier)Connector.open(url.toString());
            	System.out.println("Waiting for connections...");
                conn = notifier.acceptAndOpen();
               
                System.out.println("Accepted connection...");
            } catch (IOException e) {
                // wrong client or interrupted - continue anyway
            	System.out.println("IOException thrown");
                continue;
            }
            catch(Exception e){
            	e.printStackTrace();
            }

            processor.addConnection(conn);
        }
    }

    /**
     * Stop a work with bluetooth - exits the accepting
     * thread and close notifier.
     */
    public void stop() {
        isClosed = true;

        // finalize notifier work
        if (notifier != null) {
            try {
                notifier.close();
            } catch (IOException e) {
            } // ignore
        }

        // wait for acceptor thread is done
        try {
            accepterThread.join();
        } catch (InterruptedException e) {
        } // ignore

        // finalize processor
        if (processor != null) {
            processor.stop(true);
        }

        processor = null;
    }
     
    /**
     * Informs the event listener about a problem.
     * 
     * @param message - the error message.
     * @param e - the exception, if any, that caused this problem.
     */
    private void raiseError(String message, Exception e){
    	this.eventListener.errorOccured(message,e);
    }
   
    /**
     * Organizes the queue of clients to be processed,
     * processes the clients one by one until stopped.
     */
    private class ClientProcessor implements Runnable {
        private Thread processorThread;
        private Vector queue = new Vector();
        private BluetoothServerListener eventListener;

        ClientProcessor(BluetoothServerListener eventListener) {
        	this.eventListener = eventListener;
            processorThread = new Thread(this);
            processorThread.start();
        }

        public void run() {
            while (!isClosed) {
                // wait for new task to be processed
                synchronized (this) {
                    if (queue.size() == 0) {
                        try {
                            wait();
                        } catch (InterruptedException e) {
                            raiseError("Unexpected exception: " , e);
                            stop(false);

                            return;
                        }
                    }
                }

                // process connection
                StreamConnection conn;

                synchronized (this) {
                    // may be awaked by "stop" method.
                    if (isClosed) {
                        return;
                    }

                    conn = (StreamConnection)queue.firstElement();
                    queue.removeElementAt(0);
                    processConnection(conn);
                }
            }
        }

        /**
         *  Adds the connection to queue and notifies the thread. */
        private void addConnection(StreamConnection conn) {
            synchronized (this) {
                queue.addElement(conn);
                notify();
            }
        }

        /**
         *  Closes the connections and . */
        private void stop(boolean needJoin) {
            StreamConnection conn;

            synchronized (this) {
                notify();

                while (queue.size() != 0) {
                    conn = (StreamConnection)queue.firstElement();
                    queue.removeElementAt(0);

                    try {
                        conn.close();
                    } catch (IOException e) {
                    } // ignore
                }
            }

            // wait until dispatching thread is done
            try {
                processorThread.join();
            } catch (InterruptedException e) {
            } // ignore
        }
        
        /**
         * Let the event listener process the connection.
         */
        private void processConnection(StreamConnection conn) {
        	
        	 DataOutputStream dos = null;
        	 DataInputStream dis = null;
        	 
        	 try{ 			
    	    	 dos = new DataOutputStream(conn.openDataOutputStream());
    	    	 dis = new DataInputStream(conn.openDataInputStream());
    	    	 this.eventListener.processConnection(dis, dos);
        	 }catch(IOException e){
        		 raiseError("Error getting data stream",e);
        	 }  
           }
    }
}

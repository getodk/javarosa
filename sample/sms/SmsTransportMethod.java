package org.openmrs.transport.sms;

import org.openmrs.transport.MessageListener;
import org.openmrs.transport.TransportManager;
import org.openmrs.transport.TransportMessage;
import org.openmrs.transport.TransportMethod;
import org.openmrs.transport.util.Logger;

public class SmsTransportMethod implements TransportMethod, MessageListener{
	private static final String name = "SMS";

	private TransportMessage message;

	private TransportManager manager;

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.openmrs.transport.TransportMethod#transmit(org.openmrs.transport.TransportMessage)
	 */
	public void transmit(TransportMessage message, TransportManager manager) {
		Logger.log("transmit()" + message.toString() +"</transmit>\n");
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
		 * @see java.lang.Thread#run()
		 */
		public void run() {
			try {
				MessageConnection con = (MessageConnection)Connector.open("sms://:3333"); //TODO You do not have to hardcode this address
				((MessageConnection)con).setMessageListener(this);
				
				BinaryMessage msg = (BinaryMessage)((MessageConnection)con).newMessage(MessageConnection.BINARY_MESSAGE);
				msg.setPayloadData(message.getPayloadData());
				msg.setAddress(message.getDestination());
				((MessageConnection)con).send(msg);
				
				System.out.println("PAYLOADDATA:"+new String(message.getPayloadData())+"\nENDPLDATA\n");

				// update status
				message.setStatus(TransportMessage.STATUS_DELIVERED);
				System.out.println("Status: " + message.getStatus());
				// manager.updateMessage(message);
				message.setChanged();
				message.notifyObservers(null);

			} catch (IOException e) {
				System.out.println(e.getMessage());
			}
		}

		private void process(byte data) {
			System.out.print(data);
		}

		private void process(byte[] data) {
			System.out.println(new String(data));
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
		return TransportMethod.SMS;
	}
	
	public void notifyIncomingMessage(MessageConnection msgCon){
		try{
			Message msg = msgCon.receive();
			
			if(msg instanceof BinaryMessage)
				process(((BinaryMessage)msg).getPayloadData());
			else
				System.out.println("notifyIncomingMessage: Some non non binary text message.");
		}
		catch(Exception e){
			e.printStackTrace();
		}
	}
}

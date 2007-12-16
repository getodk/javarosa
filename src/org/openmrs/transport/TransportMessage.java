package org.openmrs.transport;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Date;

import org.openmrs.util.Observable;

/**
 * 
 * @author <a href="mailto:m.nuessler@gmail.com">Matthias Nuessler</a>
 */
public class TransportMessage extends Observable implements Externalizable {

	/**
	 * Message is new
	 */
	public static final int STATUS_NEW = 1;

	/**
	 * Message has been sent to an intermediate station, but successful delivery
	 * has not yet been confirmed.
	 */
	public static final int STATUS_SENT = 2;

	/**
	 * Message has been successfully delivered to it's destination (server)
	 */
	public static final int STATUS_DELIVERED = 4;

	/**
	 * Message received from another device, should be forwarded to it's final
	 * destination.
	 */
	public static final int STATUS_RECEIVED = 5;

	/**
	 * The actual data to be sent
	 */
	private byte[] payloadData;

	/**
	 * ID or URL of destination
	 */
	private String destination;

	/**
	 * 
	 */
	private Date timestamp = new Date();

	/**
	 * ID of the sender device
	 */
	private String sender;

	/**
	 * Message status
	 */
	private int status = STATUS_NEW;

	/**
	 * ID of the message
	 */
	private int recordId;

	/**
	 * @param payloadData
	 * @param destination
	 * @param sender
	 */
	public TransportMessage(byte[] payloadData, String destination,
			String sender) {
		super();
		this.payloadData = payloadData;
		this.destination = destination;
		this.sender = sender;
	}

	/**
	 * 
	 */
	public TransportMessage() {
		super();
	}

	/**
	 * @return the payloadData
	 */
	public byte[] getPayloadData() {
		return payloadData;
	}

	/**
	 * @param payloadData
	 *            the payloadData to set
	 */
	public void setPayloadData(byte[] payloadData) {
		this.payloadData = payloadData;
	}

	/**
	 * @return the destination
	 */
	public String getDestination() {
		return destination;
	}

	/**
	 * @param destination
	 *            the destination to set
	 */
	public void setDestination(String destination) {
		this.destination = destination;
	}

	/**
	 * @return the timestamp
	 */
	public Date getTimestamp() {
		return timestamp;
	}

	/**
	 * @param timestamp
	 *            the timestamp to set
	 */
	public void setTimestamp(Date timestamp) {
		this.timestamp = timestamp;
	}

	/**
	 * @return the sender
	 */
	public String getSender() {
		return sender;
	}

	/**
	 * @param sender
	 *            the sender to set
	 */
	public void setSender(String sender) {
		this.sender = sender;
	}

	/**
	 * @param in
	 * @throws IOException
	 */
	public void readExternal(DataInputStream in) throws IOException {
		int length = in.readInt();
		this.payloadData = new byte[length];
		// int noOfBytesRead = in.read(this.payloadData);
		in.read(this.payloadData);
		this.destination = in.readUTF();
		this.sender = in.readUTF();
		this.timestamp = new Date(in.readLong());
		this.recordId = in.readInt();
		this.status = in.readInt();
	}

	/**
	 * @param out
	 * @throws IOException
	 */
	public void writeExternal(DataOutputStream out) throws IOException {
		out.writeInt(this.payloadData.length);
		out.write(this.payloadData);
		out.writeUTF(this.destination);
		out.writeUTF(this.sender);
		out.writeLong(this.timestamp.getTime());
		out.writeInt(this.recordId);
		out.writeInt(this.status);
	}

	/**
	 * @param status
	 */
	public void setStatus(int status) {
		this.status = status;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#hashCode()
	 */
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((sender == null) ? 0 : sender.hashCode());
		result = prime * result
				+ ((timestamp == null) ? 0 : timestamp.hashCode());
		return result;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		final TransportMessage other = (TransportMessage) obj;
		if (sender == null) {
			if (other.sender != null)
				return false;
		} else if (!sender.equals(other.sender))
			return false;
		if (timestamp == null) {
			if (other.timestamp != null)
				return false;
		} else if (!timestamp.equals(other.timestamp))
			return false;
		return true;
	}

	/**
	 * @return the recordId
	 */
	public int getRecordId() {
		return recordId;
	}

	/**
	 * @param recordId
	 *            the recordId to set
	 */
	public void setRecordId(int recordId) {
		this.recordId = recordId;
	}

	/**
	 * @return the status
	 */
	public int getStatus() {
		return status;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	public String toString() {
		StringBuffer sb = new StringBuffer("TransportMessage\r\n");
		sb.append("ID: ").append(recordId);
		sb.append("\r\nSender: ").append(sender);
		sb.append("\r\nDestination: ").append(destination);
		sb.append("\r\nTime: ").append(timestamp);
		sb.append("\r\nStatus: ").append(statusToString());
		return sb.toString();
	}

	/**
	 * @return
	 */
	public String statusToString() {
		String s;
		switch (status) {
		case STATUS_DELIVERED:
			s = "Delivered";
			break;
		case STATUS_NEW:
			s = "New";
			break;
		case STATUS_RECEIVED:
			s = "Received";
			break;
		case STATUS_SENT:
			s = "Sent";
			break;
		default:
			s = "N/A";
			break;
		}
		return s;
	}
}
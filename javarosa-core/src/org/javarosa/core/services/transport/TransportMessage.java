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

package org.javarosa.core.services.transport;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Date;

import org.javarosa.core.util.Observable;
import org.javarosa.core.util.externalizable.DeserializationException;
import org.javarosa.core.util.externalizable.ExtUtil;
import org.javarosa.core.util.externalizable.ExtWrapTagged;
import org.javarosa.core.util.externalizable.Externalizable;
import org.javarosa.core.util.externalizable.PrototypeFactory;

/**
 * A message received from a TransportUtility
 *
 * @author <a href="mailto:m.nuessler@gmail.com">Matthias Nuessler</a>
 */
public class TransportMessage extends Observable implements Externalizable {

	/**
	 * Message has not been select for sending
	 */
	public static final int STATUS_NOT_SENT = 0;

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
	 * Message has been sent but no response was received
	 */
	public static final int STATUS_FAILED = 6;

	/**
	 * The actual data to be sent
	 */
	private IDataPayload payloadData;
	/**
	 * The actual data returned
	 */
	private byte[] replyloadData;
	/**
	 * ID or URL of destination
	 */
	private ITransportDestination destination;

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
	 * ID of the associated model
	 */
	private int modelId;

	/**
	 * Creates a new message
	 *
	 * @param payloadData
	 * @param destination
	 * @param sender
	 */
	public TransportMessage(IDataPayload payloadData, ITransportDestination destination,
			String sender, int modelId) {
		super();
		this.payloadData = payloadData;
		this.replyloadData = new byte [1];
		this.destination = destination;
		this.sender = sender;
		this.modelId = modelId;
	}

	/**
	 * Creates a new message
	 *
	 * @param payloadData
	 * @param destination
	 * @param sender
	 */
	public TransportMessage(IDataPayload payloadData, ITransportDestination destination,
			String sender, int modelId, byte [] replyloadDataIn) {
		super();
		this.payloadData = payloadData;
		this.destination = destination;
		this.sender = sender;
		this.modelId = modelId;
		this.replyloadData = replyloadDataIn;
	}

	/**
	 * Creates a new, Empty, message
	 */
	public TransportMessage() {
		super();
		this.replyloadData = new byte [1];
		this.sender = "default_sender";
		this.modelId = -1;
	}

	/**
	 * @return the payloadData
	 */
	public IDataPayload getPayloadData() {
		return payloadData;
	}

	/**
	 * @param payloadData
	 *            the payloadData to set
	 */
	public void setPayloadData(IDataPayload payloadData) {
		this.payloadData = payloadData;
	}

	/**
	 * @return the destination
	 */
	public ITransportDestination getDestination() {
		return destination;
	}

	/**
	 * @param destination
	 *            the destination to set
	 */
	public void setDestination(ITransportDestination destination) {
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

	/*
	 * (non-Javadoc)
	 * @see org.javarosa.core.services.storage.utilities.Externalizable#readExternal(java.io.DataInputStream)
	 */
	public void readExternal(DataInputStream in, PrototypeFactory pf) throws IOException, DeserializationException {
		//payload data is never used again, so it is not in serialized record
		
		this.destination = (ITransportDestination) ExtUtil.read(in,new ExtWrapTagged(),pf);		
		this.sender = in.readUTF();
		this.timestamp = new Date(in.readLong());
		this.recordId = in.readInt();
		this.status = in.readInt();
		this.modelId = in.readInt();
		int length = in.readInt();
		this.replyloadData = new byte[length];
		in.read(this.replyloadData);
	}

	/*
	 * (non-Javadoc)
	 * @see org.javarosa.core.services.storage.utilities.Externalizable#writeExternal(java.io.DataOutputStream)
	 */
	public void writeExternal(DataOutputStream out) throws IOException {
		//payload data need not be serialized, as it is large and never read again by anybody
		
		ExtUtil.write(out, new ExtWrapTagged(this.destination));
		out.writeUTF(this.sender);
		out.writeLong(this.timestamp.getTime());
		out.writeInt(this.recordId);
		out.writeInt(this.status);
		out.writeInt(this.modelId);
		out.writeInt(this.replyloadData.length);
		out.write(this.replyloadData);
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
		if (this.replyloadData != null)
			sb.append("\r\nReply Data: ").append(new String(this.replyloadData));
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
			s = "Delivery Not Confirmed";
			break;
		case STATUS_RECEIVED:
			s = "Received";
			break;
		case STATUS_SENT:
			s = "Sent";
			break;
		case STATUS_NOT_SENT:
			s = "Not Sent";
			break;
		case STATUS_FAILED:
			s = "Failed";
			break;
		default:
			s = "N/A";
			break;
		}
		return s;
	}

	public int getModelId() {
		return modelId;
	}

	public void setModelId(int modelId) {
		this.modelId = modelId;
	}

	public byte[] getReplyloadData() {
		return replyloadData;
	}

	public void setReplyloadData(byte[] replyloadData) {
		this.replyloadData = replyloadData;
	}
}
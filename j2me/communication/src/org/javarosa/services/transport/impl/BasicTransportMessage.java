package org.javarosa.services.transport.impl;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Date;

import org.javarosa.core.util.externalizable.DeserializationException;
import org.javarosa.core.util.externalizable.ExtUtil;
import org.javarosa.core.util.externalizable.PrototypeFactory;
import org.javarosa.services.transport.TransportMessage;

/**
 * Abstract part implementation of TransportMessage, for subclassing by full
 * implementations
 * 
 */
public abstract class BasicTransportMessage implements TransportMessage {

	private String contentType;
	private int status;
	private String failureReason;
	private int failureCount;
	private String queueIdentifier;
	private Date created;
	private Date sent;
	private long queuingDeadline;
	private int recordId = -1;

	public Date getSent() {
		return sent;
	}

	public void setSent(Date sent) {
		this.sent = sent;
	}

	public long getQueuingDeadline() {
		return queuingDeadline;
	}

	public void setSendingThreadDeadline(long queuingDeadline) {
		this.queuingDeadline = queuingDeadline;
	}

	public Date getCreated() {
		return created;
	}

	public void setCreated(Date created) {
		this.created = created;
	}

	public String getContentType() {
		return contentType;
	}

	public void setContentType(String contentType) {
		this.contentType = contentType;
	}

	public int getStatus() {
		return this.status;
	}

	public void setStatus(int status) {
		this.status = status;
	}

	public boolean isSuccess() {
		return this.status == TransportMessageStatus.SENT;
	}

	public String getFailureReason() {
		return failureReason;
	}

	public void setFailureReason(String failureReason) {
		this.failureReason = failureReason;
	}

	public int getFailureCount() {
		return failureCount;
	}

	public void incrementFailureCount() {
		this.failureCount++;
	}

	public String getCacheIdentifier() {
		return queueIdentifier;
	}

	public void setCacheIdentifier(String queueIdentifier) {
		this.queueIdentifier = queueIdentifier;
	}

	public int getID() {
		return recordId;
	}

	public void setID(int ID) {
		this.recordId = ID;
	}
	

	public void readExternal(DataInputStream in, PrototypeFactory pf)
			throws IOException, DeserializationException {
		contentType = ExtUtil.nullIfEmpty(ExtUtil.readString(in));
		status = (int)ExtUtil.readNumeric(in);
		failureReason = ExtUtil.readString(in);
		failureCount = (int)ExtUtil.readNumeric(in);
		queueIdentifier = ExtUtil.readString(in);
		created = ExtUtil.readDate(in);
		created = created.getTime() == 0 ? null : created;
		sent = ExtUtil.readDate(in);
		sent = sent.getTime() == 0 ? null : sent;
		queuingDeadline = ExtUtil.readNumeric(in);
		recordId = (int)ExtUtil.readNumeric(in);
	}

	public void writeExternal(DataOutputStream out) throws IOException {
		ExtUtil.writeString(out, ExtUtil.emptyIfNull(contentType));
		ExtUtil.writeNumeric(out, status);
		ExtUtil.writeString(out, ExtUtil.emptyIfNull(failureReason));
		ExtUtil.writeNumeric(out, failureCount);
		ExtUtil.writeString(out, queueIdentifier);
		ExtUtil.writeDate(out, created == null? new Date(0) : created );
		ExtUtil.writeDate(out, sent == null? new Date(0) : sent );
		ExtUtil.writeNumeric(out, queuingDeadline);
		ExtUtil.writeNumeric(out, recordId);

	}
}

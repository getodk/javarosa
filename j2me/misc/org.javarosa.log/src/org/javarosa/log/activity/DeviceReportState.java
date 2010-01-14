/**
 * 
 */
package org.javarosa.log.activity;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Date;
import java.util.Enumeration;
import java.util.NoSuchElementException;
import java.util.Vector;

import org.javarosa.core.api.State;
import org.javarosa.core.model.utils.DateUtils;
import org.javarosa.core.services.IncidentLogger;
import org.javarosa.core.services.PropertyManager;
import org.javarosa.core.services.properties.IPropertyRules;
import org.javarosa.core.services.properties.JavaRosaPropertyRules;
import org.javarosa.core.services.storage.StorageManager;
import org.javarosa.core.services.storage.StorageUtilAccessor;
import org.javarosa.core.services.storage.WrappingStorageUtility;
import org.javarosa.core.util.TrivialTransitions;
import org.javarosa.j2me.log.StatusReportException;
import org.javarosa.j2me.log.XmlLogSerializer;
import org.javarosa.j2me.log.XmlStatusProvider;
import org.javarosa.services.transport.TransportListener;
import org.javarosa.services.transport.TransportMessage;
import org.javarosa.services.transport.TransportService;
import org.javarosa.services.transport.impl.TransportMessageStatus;
import org.javarosa.services.transport.senders.SenderThread;
import org.kxml2.io.KXmlSerializer;
import org.kxml2.kdom.Document;
import org.kxml2.kdom.Element;
import org.xmlpull.v1.XmlSerializer;

/**
 * @author ctsims
 *
 */
public abstract class DeviceReportState implements State, TrivialTransitions, TransportListener {

	private int reportFormat;
	
	public static final int REPORT_FORMAT_FULL = 1;
	public static final int REPORT_FORMAT_COMPACT = 2;
	
	public DeviceReportState(int reportFormat) {
		this.reportFormat = reportFormat;
	}
	
	public void start() {
		try {
			Document report = createReport();
			InputStream payload = serializeReport(report);
			TransportMessage message = constructMessageFromPayload(payload);
			SenderThread s = TransportService.send(message);
			s.addListener(this);
		} catch (Exception e) {
			// Don't let this code break the application, ever.
			e.printStackTrace();
		}
		done();
	}

	/**
	 * Creates a transport message responsible for sending the report off of the device.
	 * 
	 * NOTE: This message should generally not be cacheable or persisted in any way. 
	 * 
	 * @param reportPayload
	 * @return
	 */
	public abstract TransportMessage constructMessageFromPayload(InputStream reportPayload);
	
	private InputStream serializeReport(Document report) {
		 XmlSerializer ser = new KXmlSerializer();
		 ByteArrayOutputStream bos = new ByteArrayOutputStream();
		 try {
			ser.setOutput(bos, null);
			report.write(ser);
		} catch (IOException e) {
			// We don't actually want to ever fail on this report, 
			e.printStackTrace();
		}
		//Note: If this gets too big, we can just write a wrapper to stream bytes one at a time
		//to the array. It'll probably be the XML DOM itself which blows up the memory, though...
		 ByteArrayInputStream bis = new ByteArrayInputStream(bos.toByteArray());
		 return bis;
	}
	
	private Document createReport() {
		Document xmlDoc = new Document();
		Element errors = xmlDoc.createElement(null,"report_errors");
		addHeader(xmlDoc, errors);
		createDeviceLogSubreport(xmlDoc,errors);
		createTransportSubreport(xmlDoc,errors);
		if(reportFormat == REPORT_FORMAT_FULL) {
			createRmsSubreport(xmlDoc,errors);
			createPropertiesSubreport(xmlDoc,errors);
		}
		if(errors.getChildCount() != 0) {
			xmlDoc.addChild(Element.ELEMENT,errors);
		}
		return xmlDoc;
	}
	
	private void addHeader(Document parent, Element errorsNode) {
		String deviceId = PropertyManager._().getSingularProperty(JavaRosaPropertyRules.DEVICE_ID_PROPERTY);
		String reportDate = DateUtils.formatDate(new Date(), DateUtils.FORMAT_HUMAN_READABLE_SHORT);

		Element id = parent.createElement(null,"device_id");
		id.addChild(Element.TEXT, deviceId);
		parent.addChild(Element.ELEMENT, id);
		
		Element date = parent.createElement(null,"report_date");
		date.addChild(Element.TEXT, reportDate);
		parent.addChild(Element.ELEMENT, date);
	}
	
	private void createTransportSubreport(Document parent, Element errorsNode) {
		try{
			Element report = new Element();
			report.setName("transport_subreport");
			Element unsent = report.createElement(null,"number_unsent");
			unsent.addChild(Element.TEXT, TransportService.getCachedMessagesSize() + "");
			report.addChild(Element.ELEMENT, unsent);
			parent.addChild(Element.ELEMENT,report);
		}
		catch(Exception e) {
			logError(errorsNode, new StatusReportException(e,"transport_subreport","Exception retrieving transport subreport"));
		}
	}
	
	private void createDeviceLogSubreport(Document parent, Element errorsNode) {
		try {
			Element report = IncidentLogger._().serializeLogs(new XmlLogSerializer("log_subreport"));
			parent.addChild(Element.ELEMENT, report);
		} catch(Exception e) {
			logError(errorsNode, new StatusReportException(e,"log_subreport","Exception when writing device log report."));
		}
	}
	
	private void createRmsSubreport(Document root, Element errorsNode)  {
		Element parent = new Element();
		parent.setName("rms_subreport");
		
		String[] utils = StorageManager.listRegisteredUtilities();
		for(int i = 0 ; i < utils.length ; ++ i) {
			//TODO: This is super hacky, revisit it 
			Object storage = StorageManager.getStorage(utils[i]);
			if(storage instanceof WrappingStorageUtility) {
				storage = StorageUtilAccessor.getStorage((WrappingStorageUtility)storage);
			}
			if(storage instanceof XmlStatusProvider) {
				XmlStatusProvider util = (XmlStatusProvider)storage;
			try { 
				parent.addChild(Element.ELEMENT,util.getStatusReport());
			} catch(StatusReportException sre) {
				logError(errorsNode,sre);
			}
			}
		}
		root.addChild(Element.ELEMENT, parent);
	}
	
	
	private void createPropertiesSubreport(Document root, Element errorsNode) {
		Element report = root.createElement(null, "properties_subreport");
		Vector rules = PropertyManager._().getRules();
		for(Enumeration en = rules.elements(); en.hasMoreElements();) {
			IPropertyRules ruleset = (IPropertyRules)en.nextElement();
			for(Enumeration pen = ruleset.allowableProperties().elements(); pen.hasMoreElements() ;) {
				String propertyName = (String)pen.nextElement();
				try {
					Vector list = PropertyManager._().getProperty(propertyName);
					if(list != null) {
						Element property = report.createElement(null,"property");
						property.setAttribute(null,"name", propertyName);
						String humanName = ruleset.getHumanReadableDescription(propertyName);
						if(humanName != propertyName) {
							Element title = property.createElement(null,"title");
							title.addChild(Element.TEXT,humanName);
							property.addChild(Element.ELEMENT,title);
						}
						
						Element value = property.createElement(null,"value");
						String valueXml = "";
						if(list.size() == 1) {
							valueXml = (String)list.elementAt(0);
						} else {
							valueXml = "{";
							for(Enumeration ven = list.elements(); ven.hasMoreElements() ;) {
								valueXml += ven.nextElement();
								if(ven.hasMoreElements()) {
									valueXml += ",";
								}
							}
							valueXml += "}";
						}
						
						value.addChild(Element.TEXT,valueXml);
						property.addChild(Element.ELEMENT,value);
						report.addChild(Element.ELEMENT,property);
					}
				} catch (NoSuchElementException nsee) {
					//Don't sweat it, not important.
				}
			}
		}
		root.addChild(Element.ELEMENT,report);
	}
	
	private void logError(Element errorsNode, StatusReportException sre) {
		Element error = errorsNode.createElement(null,"report_error");
		error.setAttribute(null,"report",sre.getReportName());
		error.addChild(Element.TEXT,sre.getMessage());
		errorsNode.addChild(Element.ELEMENT,error);
	}
	
	public void onStatusChange(TransportMessage message) {
		if(message.getStatus() == TransportMessageStatus.SENT) {
			//The data's off the device. We don't want it to get out of control, so wipe the logs.
			IncidentLogger._().clearLogs();
		}
	}
}

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
import org.javarosa.core.reference.Reference;
import org.javarosa.core.reference.ReferenceManager;
import org.javarosa.core.services.Logger;
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
import org.javarosa.log.util.LogReportUtils;
import org.javarosa.log.util.StreamLogSerializer;
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
 * Note: Much of this behavior should be moved into a controller
 * which is used by multiple states. This one is a non-interactive
 * behind the scenes report, but it's likely that in the future there
 * will exist manual report sending which should be vaguely interactive
 * 
 * @author ctsims
 *
 */
public abstract class DeviceReportState implements State, TrivialTransitions, TransportListener {

	private static final String XMLNS = "http://code.javarosa.org/devicereport";
	
	private int reportFormat;
	private long now;
	
	/**
	 * Create a behind-the-scenes Device Reporting state which manages all operations 
	 * without user intervention.
	 */
	public DeviceReportState() {
		now = new Date().getTime();
		//Get what reports are pending for the week and for the day.
		//If logging is disabled, these methods default to skipping.
		int weeklyPending = LogReportUtils.getPendingWeeklyReportType(now);
		int dailyPending = LogReportUtils.getPendingDailyReportType(now);
		
		//Pick the most verbose pending report.
		this.reportFormat = Math.max(dailyPending,weeklyPending);
	}

	
	public void start() {
		if(reportFormat == LogReportUtils.REPORT_FORMAT_SKIP) {
			//If we've gotten 600 logger lines in less than 7 days,
			//it's a problem
			if(Logger.isLoggingEnabled()) {
				determineLogFallback(600);
			}
			done();
			return;
		}
		try {
			Document report = createReport();
			InputStream payload = serializeReport(report);
			TransportMessage message = constructMessageFromPayload(payload);
			SenderThread s = TransportService.send(message);
			
			//We have no way of knowing whether the message will get 
			//off the phone successfully if it can get cached, the
			//logs are in the transport layer's hands at that point.
			if(message.isCacheable()) {
				Logger._().clearLogs();
			}
			
			s.addListener(this);
			LogReportUtils.setPendingFromNow(now);
		} catch (Exception e) {
			// Don't let this code break the application, ever.
			e.printStackTrace();
			dumpLogFallback();
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
		Element root = xmlDoc.createElement(XMLNS, "device_report");
		xmlDoc.addChild(Element.ELEMENT, root);
		Element errors = root.createElement(null,"report_errors");
		addHeader(root, errors);
		createDeviceLogSubreport(root,errors);
		createTransportSubreport(root,errors);
		if(reportFormat == LogReportUtils.REPORT_FORMAT_FULL) {
			createRmsSubreport(root,errors);
			createPropertiesSubreport(root,errors);
		}
		if(errors.getChildCount() != 0) {
			root.addChild(Element.ELEMENT,errors);
		}
		return xmlDoc;
	}
	
	private void addHeader(Element parent, Element errorsNode) {
		String deviceId = PropertyManager._().getSingularProperty(JavaRosaPropertyRules.DEVICE_ID_PROPERTY);
		String reportDate = DateUtils.formatDate(new Date(), DateUtils.FORMAT_HUMAN_READABLE_SHORT);

		Element id = parent.createElement(null,"device_id");
		id.addChild(Element.TEXT, deviceId);
		parent.addChild(Element.ELEMENT, id);
		
		Element date = parent.createElement(null,"report_date");
		date.addChild(Element.TEXT, reportDate);
		parent.addChild(Element.ELEMENT, date);
	}
	
	private void createTransportSubreport(Element parent, Element errorsNode) {
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
	
	private void createDeviceLogSubreport(Element parent, Element errorsNode) {
		try {
			Element report = Logger._().serializeLogs(new XmlLogSerializer("log_subreport"));
			parent.addChild(Element.ELEMENT, report);
		} catch(Exception e) {
			logError(errorsNode, new StatusReportException(e,"log_subreport","Exception when writing device log report."));
		}
	}
	
	private void createRmsSubreport(Element root, Element errorsNode)  {
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
	
	
	private void createPropertiesSubreport(Element root, Element errorsNode) {
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
	
	

	public void onChange(TransportMessage message, String remark) {
		// TODO Auto-generated method stub
		
	}
	public void onStatusChange(TransportMessage message) {
		if(message.getStatus() == TransportMessageStatus.SENT) {
			//If the message isn't cacheable, we haven't wiped the
			//logs yet, since we needed to wait for success in order
			//to know they'd get off the phone.
			if(!message.isCacheable()) {
				Logger._().clearLogs();
			}
		} else {
			//if we failed we need to determine if the logs are too big
			//and either dump to the fileystem or just clear the logs...
			determineLogFallback(200);
		}
	}
	
	private void determineLogFallback(int size) {
		boolean fallback = false;
		//Figure out if we need to dump the logs because they've gotten
		//big and might be causing problems.
		try{
			if(Logger._().logSize() > size) {
				//This is big enough that we need to try to dump them and move on...
				fallback = true;
			} else {
				fallback = false;
			}
		} catch(Exception e) {
			dumpLogFallback();
			return ;
		}
		//We keep this outside so that the exception handling is reasonable.
		if(fallback) {
			dumpLogFallback();
		}
	}
	
	private void dumpLogFallback() {
		try{
			String dumpRef = "jr://file/jr_log_dump" + DateUtils.formatDateTime(new Date(), DateUtils.FORMAT_TIMESTAMP_SUFFIX) + ".log";
			Reference ref = ReferenceManager._().DeriveReference(dumpRef);
			if(!ref.isReadOnly()) {
				if(Logger._().serializeLogs(new StreamLogSerializer(ref.getOutputStream()))) {
					//Success!
				} else {
					//Not success!
				}
			}
		} catch(Exception e) {
			e.printStackTrace();
		}
		
		try{
			//No matter _what_, clear the logs if a logger is registered
			if(Logger._() == null) {
				System.out.println("Logger is null. Must have failed to initailize");
			}
			Logger._().clearLogs();
		} catch(Exception e) {
			//If this fails it's a serious problem, but not sure what to do about it.
			e.printStackTrace();
		}
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.javarosa.core.util.TrivialTransitions#done()
	 */
	//THIS CANNOT BE REMOVED! S40 phones will fail horribly.
	public abstract void done();
}

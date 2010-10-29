package org.javarosa.j2me.log;

import java.io.IOException;

import org.javarosa.core.log.IFullLogSerializer;
import org.javarosa.core.log.LogEntry;
import org.javarosa.core.log.WrappedException;
import org.javarosa.core.model.utils.DateUtils;
import org.xmlpull.v1.XmlSerializer;

public class XmlStreamLogSerializer implements IFullLogSerializer<Object> {

	XmlSerializer o;
	String ns;
	String rootTag;
	
	public XmlStreamLogSerializer(XmlSerializer o, String ns, String rootTag) {
		this.o = o;
		this.ns = ns;
		this.rootTag = rootTag;
	}
	
	/* (non-Javadoc)
	 * @see org.javarosa.core.log.ILogSerializer#serializeLog(org.javarosa.core.log.IncidentLog)
	 */
	private Object serializeLog(LogEntry log) {
		try {
			o.startTag(ns, "log");
			o.attribute(null, "date", DateUtils.formatDateTime(log.getTime(), DateUtils.FORMAT_ISO8601));
			
			o.startTag(ns, "type");
			o.text(log.getType());
			o.endTag(ns, "type");
			
			o.startTag(ns, "msg");
			o.text(log.getMessage());
			o.endTag(ns, "msg");
			
			o.endTag(ns, "log");
		} catch (IOException ioe) {
			throw new WrappedException(ioe);
		}

		return null;
	}

	/* (non-Javadoc)
	 * @see org.javarosa.core.log.ILogSerializer#serializeLogs(org.javarosa.core.log.IncidentLog[])
	 */
	public Object serializeLogs(LogEntry[] logs) {
		try {
			o.startTag(ns, rootTag);
			for(int i = 0; i < logs.length; ++i ) {
				this.serializeLog(logs[i]);
			}
			o.endTag(ns, rootTag);
		} catch (IOException ioe) {
			throw new WrappedException(ioe);
		}
			
		return null;
	}

}

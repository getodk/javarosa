package org.javarosa.user.transport;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Date;
import java.util.Enumeration;

import org.javarosa.core.model.utils.DateUtils;
import org.javarosa.core.services.PropertyManager;
import org.javarosa.core.services.locale.Localization;
import org.javarosa.core.services.properties.JavaRosaPropertyRules;
import org.javarosa.services.transport.CommUtil;
import org.javarosa.services.transport.UnrecognizedResponseException;
import org.javarosa.services.transport.impl.simplehttp.SimpleHttpTransportMessage;
import org.javarosa.user.model.User;
import org.kxml2.io.KXmlSerializer;
import org.kxml2.kdom.Document;
import org.kxml2.kdom.Element;
import org.xmlpull.v1.XmlSerializer;

public class HttpUserRegistrationTranslator implements UserRegistrationTranslator<SimpleHttpTransportMessage>{
	
	public static final String XMLNS_ORR = "http://openrosa.org/http/response";
	public static final String XMLNS_UR = "http://openrosa.org/user/registration";
	
	User user;
	String registrationUrl;
	
	String prompt;
	
	//A guess for the response OR API version (Used if none is provided by the server).
	String orApiVersion;

	public HttpUserRegistrationTranslator(User user, String registrationUrl, String orApiVersion) {
		this.user = user;
		this.registrationUrl = registrationUrl;
		this.orApiVersion = orApiVersion;
	}
	
	public SimpleHttpTransportMessage getUserRegistrationMessage() throws IOException {
		SimpleHttpTransportMessage message = new SimpleHttpTransportMessage(getStreamFromRegistration(createXmlRegistrationDoc(user)), registrationUrl);
		if("1.0".equals(orApiVersion)) {
			message.setCacheable(true);
		} else {
			//Pre 1.0 we'll assume that user registration is a synchronous process.
			message.setCacheable(false);
		}
		
		return message;
	}
	
	private InputStream getStreamFromRegistration(Document registration) {
		 XmlSerializer ser = new KXmlSerializer();
		 ByteArrayOutputStream bos = new ByteArrayOutputStream();
		 try {
			ser.setOutput(bos, null);
			registration.write(ser);
		} catch (IOException e) {
			// We don't actually want to ever fail on this report, 
			e.printStackTrace();
		}
		//Note: If this gets too big, we can just write a wrapper to stream bytes one at a time
		//to the array. It'll probably be the XML DOM itself which blows up the memory, though...
		 ByteArrayInputStream bis = new ByteArrayInputStream(bos.toByteArray());
		 return bis;

	}
	
	public User readResponse(SimpleHttpTransportMessage message) throws UnrecognizedResponseException {
		this.prompt = null;
		
		byte[] responseBody = message.getResponseBody();
		
		//Not actually a response
		if(responseBody == null) {
			throw new UnrecognizedResponseException("No response body");
		}
		
		String responseVersion = message.getResponseProperties().getORApiVersion();
		if(responseVersion == null) {
			//If there's no version from the server, assume it's the same as the 
			//sent version
			responseVersion = orApiVersion;
		}
		
		Document doc = CommUtil.getXMLResponse(responseBody);
		if (doc == null) {
			throw new UnrecognizedResponseException("can't parse xml");
		}
		
		return readResponseDocument(doc);
	}
	
	/**
	 * 
	 * @param response
	 * @return
	 */
	private User readResponseDocument(Document response) throws UnrecognizedResponseException {
		
		if("OpenRosaResponse".equals(response.getRootElement().getName()) && XMLNS_ORR.equals(response.getRootElement().getNamespace())) {
			return readResponseDocumentNew(response);
		}

		//do we want to look for some kind of 'ok' message? otherwise the server could send back
		//gibberish and we'd still interpret it as a successful registration. ideally, we should
		//require a certain 'ok' token, and throw the exception if it's not present
		
		boolean updates = false;
		for(int i = 0; i < response.getChildCount(); ++i) {
			Object o = response.getChild(i);
			if(!(o instanceof Element)) {
				continue;
			}
			Element e = (Element)o;
			if(e.getName().equals("response-message")) {
				//Do we want to actually just print out the message? That seems weird
				//given the internationalization
			} else if(e.getName().equals("user-data")){
				for(int j = 0; j < response.getChildCount(); ++j) {
					Object data = e.getChild(j);
					if(!(data instanceof Element)) {
						continue;
					}
					Element dataElement = (Element)data;
					String propertyName = dataElement.getAttributeValue(null, "key");
					String property = (String)dataElement.getChild(0);
					user.setProperty(propertyName, property);
					updates = true;
				}
			}
		}
		return user;
	}
	
	
	//TODO: This should get updated to be part of a universal processor
	private User readResponseDocumentNew(Document doc) throws UnrecognizedResponseException {
		//Only relevant (for now!) for Form Submissions
		try{
			Element e = doc.getRootElement().getElement(HttpUserRegistrationTranslator.XMLNS_ORR,"message");
			prompt = e.getText(0);
		} catch(Exception e) {
			//No problem if not.
		}
		
		try{
			Element e = doc.getRootElement().getElement(HttpUserRegistrationTranslator.XMLNS_UR,"Registration");
			
			try { 
				user.setUsername(e.getElement(HttpUserRegistrationTranslator.XMLNS_UR,"username").getText(0));
			} catch(Exception xmlParseError) {
				//XML PARSING
			}
			
			try { 
				user.setPassword(e.getElement(HttpUserRegistrationTranslator.XMLNS_UR,"password").getText(0));
			} catch(Exception xmlParseError) {
				//XML PARSING
			}
			
			try { 
				user.setUuid(e.getElement(HttpUserRegistrationTranslator.XMLNS_UR,"uuid").getText(0));
			} catch(Exception xmlParseError) {
				//XML PARSING
			}
			
			try { 
				e = e.getElement(HttpUserRegistrationTranslator.XMLNS_UR,"user-data");

				for(int j = 0; j < e.getChildCount(); ++j) {
					Object data = e.getChild(j);
					if(!(data instanceof Element)) {
						continue;
					}
					Element dataElement = (Element)data;
					String propertyName = dataElement.getAttributeValue(null, "key");
					String property = (String)dataElement.getChild(0);
					user.setProperty(propertyName, property);
				}
			}
			catch(Exception xmlParseError) {
				//Nothing
			}
		} catch(Exception e) {
			//No registration response, clean user
		}
		return user;
	}
	
	private Document createXmlRegistrationDoc(User u) {
		Document document = new Document();
		Element root = document.createElement(null,"registration");
		root.setNamespace("http://openrosa.org/user-registration");
		
		addChildWithText(root,"username",u.getUsername());
		
		addChildWithText(root,"password",u.getPassword());
		addChildWithText(root,"uuid",u.getUniqueId());
		
		addChildWithText(root,"date",DateUtils.formatDate(new Date(),DateUtils.FORMAT_ISO8601));
		
		addChildWithText(root, "registering_phone_id",PropertyManager._().getSingularProperty(JavaRosaPropertyRules.DEVICE_ID_PROPERTY));
		
		
		Element userData =  root.createElement(null,"user_data");
		
		for(Enumeration en = u.listProperties(); en.hasMoreElements() ;) {
			String property = (String)en.nextElement();
			Element data= userData.createElement(null,"data");
			data.setAttribute(null,"key",property);
			data.addChild(Element.TEXT, u.getProperty(property));
			userData.addChild(Element.ELEMENT, data);
		}
		root.addChild(Element.ELEMENT,userData);
		document.addChild(Element.ELEMENT, root);
		return document;
	}
	private void addChildWithText(Element parent, String name, String text) {
		Element e = parent.createElement(null,name);
		e.addChild(Element.TEXT, text);
		parent.addChild(Element.ELEMENT, e);
	}
	
	public String getResponseMessageString() {
		return prompt;
	}

}

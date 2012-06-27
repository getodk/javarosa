package org.javarosa.demo.applogic;

import java.io.IOException;
import java.util.Vector;

import javax.microedition.midlet.MIDlet;

import org.javarosa.core.model.CoreModelModule;
import org.javarosa.core.model.FormDef;
import org.javarosa.core.model.SubmissionProfile;
import org.javarosa.core.model.condition.IFunctionHandler;
import org.javarosa.core.model.instance.FormInstance;
import org.javarosa.core.model.utils.DateUtils;
import org.javarosa.core.model.utils.IPreloadHandler;
import org.javarosa.core.reference.InvalidReferenceException;
import org.javarosa.core.reference.ReferenceManager;
import org.javarosa.core.reference.RootTranslator;
import org.javarosa.core.services.Logger;
import org.javarosa.core.services.PropertyManager;
import org.javarosa.core.services.locale.Localization;
import org.javarosa.core.services.properties.JavaRosaPropertyRules;
import org.javarosa.core.services.storage.StorageFullException;
import org.javarosa.core.services.storage.StorageManager;
import org.javarosa.core.util.JavaRosaCoreModule;
import org.javarosa.core.util.PropertyUtils;
import org.javarosa.demo.properties.DemoAppProperties;
import org.javarosa.demo.util.MetaPreloadHandler;
import org.javarosa.formmanager.FormManagerModule;
import org.javarosa.formmanager.properties.FormManagerProperties;
import org.javarosa.j2me.J2MEModule;
import org.javarosa.j2me.util.DumpRMS;
import org.javarosa.j2me.view.J2MEDisplay;
import org.javarosa.location.LocationModule;
import org.javarosa.model.xform.XFormsModule;
import org.javarosa.resources.locale.LanguagePackModule;
import org.javarosa.resources.locale.LanguageUtils;
import org.javarosa.services.transport.SubmissionTransportHelper;
import org.javarosa.services.transport.TransportManagerModule;
import org.javarosa.services.transport.TransportMessage;
import org.javarosa.user.activity.UserModule;
import org.javarosa.user.model.User;
import org.javarosa.user.utility.UserUtility;
import org.javarosa.xform.util.XFormUtils;

public class JRDemoContext {

	private static JRDemoContext instance;
	
	public static JRDemoContext _ () {
		if (instance == null) {
			instance = new JRDemoContext();
		}
		return instance;
	}
	
	private MIDlet midlet;
	private User user;

	
	public void setMidlet(MIDlet m) {
		this.midlet = m;
		J2MEDisplay.init(m);
	}
	
	public MIDlet getMidlet() {
		return midlet;
	}
	
	public void init (MIDlet m) {
		DumpRMS.RMSRecoveryHook(m);
		
		loadModules();
		
		//After load modules, so polish translations can be inserted.
		setMidlet(m);
		
		addCustomLanguages();
		setProperties();
			
		UserUtility.populateAdminUser(m);
		loadRootTranslator();
	}	

	private void loadModules() {
		new J2MEModule().registerModule();
		new JavaRosaCoreModule().registerModule();
		new CoreModelModule().registerModule();
		new XFormsModule().registerModule();
		new TransportManagerModule().registerModule();
		new UserModule().registerModule();
		new FormManagerModule().registerModule();
		new LanguagePackModule().registerModule();
		new LocationModule().registerModule();
	}
	
	
	private void addCustomLanguages() {
		Localization.registerLanguageFile("pt", "/messages_jrdemo_pt.txt");
		Localization.registerLanguageFile("fra", "/messages_jrdemo_fra.txt");
		Localization.registerLanguageFile("zh", "/messages_jrdemo_zh.txt");
		Localization.registerLanguageFile("default", "/messages_jrdemo_default.txt");
	}
	
	private void parseAndProcessLanguageResources(String resourceString) {
		Vector<String> resources = DateUtils.split(resourceString, ",", true);
		for(int i = 0 ; i < resources.size() ; i+=2) {
			String langkey = resources.elementAt(i);
			String resource = resources.elementAt(i+1);
			try {
				if(!ReferenceManager._().DeriveReference(resource).doesBinaryExist()) {
					Logger.die("Initialization", new RuntimeException("Language Resource (for locale: " + langkey + ") Unavailable during load. Could not resolve reference for " + resource));
				}
				Localization.registerLanguageReference(langkey, resource);
			} catch (IOException e) {
				Logger.exception(e);
				Logger.die("Initialization", new RuntimeException("Language Resource (for locale: " + langkey + ") Unavailable during load. IO Exception while reading " + resource));
			} catch (InvalidReferenceException e) {
				Logger.exception(e);
				Logger.die("Initialization", new RuntimeException("Language Resource (for locale: " + langkey + ") unavailable. Invalid JR Reference: " + resource));
			}
		}
	}
	
	private void setProperties() {
		final String POST_URL = midlet.getAppProperty("JRDemo-Post-Url");
		final String FORM_URL = midlet.getAppProperty("Form-Server-Url");
		final String VIEW_TYPE = midlet.getAppProperty("Default-View");
		final String LANGUAGE = midlet.getAppProperty("cur_locale");
		
		final String LANGUAGE_RESOURCES = midlet.getAppProperty("Locale-Resources");
		PropertyManager._().addRules(new JavaRosaPropertyRules());
		PropertyManager._().addRules(new DemoAppProperties());

		PropertyUtils.initializeProperty("DeviceID", PropertyUtils.genGUID(25));

		PropertyUtils.initializeProperty(DemoAppProperties.POST_URL_PROPERTY, POST_URL);
		PropertyUtils.initializeProperty(DemoAppProperties.FORM_URL_PROPERTY, FORM_URL);
		PropertyUtils.initializeProperty(FormManagerProperties.VIEW_TYPE_PROPERTY, VIEW_TYPE);
		
		if(LANGUAGE_RESOURCES != null && LANGUAGE_RESOURCES != "") {
			parseAndProcessLanguageResources(LANGUAGE_RESOURCES);
		}
		
		LanguageUtils.initializeLanguage(false, LANGUAGE == null ? "default" : LANGUAGE);

	}
	
	public void setUser (User u) {
		this.user = u;
	}
	
	public User getUser () {
		return user;
	}
	
	
	public TransportMessage buildMessage(FormInstance data, SubmissionProfile profile) {
		//Right now we have to just give the message the stream, rather than the payload,
		//since the transport layer won't take payloads. This should be fixed _as soon 
		//as possible_ so that we don't either (A) blow up the memory or (B) lose the ability
		//to send payloads > than the phones' heap.
		
		if(profile == null) {
			profile = SubmissionTransportHelper.defaultPostSubmission(PropertyManager._().getSingularProperty(DemoAppProperties.POST_URL_PROPERTY));
		}
		
		try {
			return SubmissionTransportHelper.createMessage(data, profile, false);
		} catch (IOException e) {
			e.printStackTrace();
			throw new RuntimeException("Error Serializing Data to be transported");
		}
	}
	
	public Vector<IPreloadHandler> getPreloaders() {
		Vector<IPreloadHandler> handlers = new Vector<IPreloadHandler>();
		MetaPreloadHandler meta = new MetaPreloadHandler(this.getUser());
		handlers.addElement(meta);
		return handlers;		
	}
	
	public Vector<IFunctionHandler> getFuncHandlers () {
		return null;
	}
	
	public void loadRootTranslator(){
		ReferenceManager._().addRootTranslator(new RootTranslator("jr://images/", "jr://resource/"));
		ReferenceManager._().addRootTranslator(new RootTranslator("jr://audio/", "jr://resource/"));
	}
}

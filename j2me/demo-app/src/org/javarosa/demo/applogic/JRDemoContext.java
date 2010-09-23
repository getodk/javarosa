package org.javarosa.demo.applogic;

import java.io.IOException;
import java.util.Vector;

import javax.microedition.midlet.MIDlet;

import org.javarosa.core.model.CoreModelModule;
import org.javarosa.core.model.FormDef;
import org.javarosa.core.model.SubmissionProfile;
import org.javarosa.core.model.condition.IFunctionHandler;
import org.javarosa.core.model.instance.FormInstance;
import org.javarosa.core.model.utils.IPreloadHandler;
import org.javarosa.core.reference.ReferenceManager;
import org.javarosa.core.reference.RootTranslator;
import org.javarosa.core.services.PropertyManager;
import org.javarosa.core.services.locale.Localization;
import org.javarosa.core.services.properties.JavaRosaPropertyRules;
import org.javarosa.core.services.storage.IStorageUtility;
import org.javarosa.core.services.storage.StorageFullException;
import org.javarosa.core.services.storage.StorageManager;
import org.javarosa.core.services.transport.payload.IDataPayload;
import org.javarosa.core.util.JavaRosaCoreModule;
import org.javarosa.core.util.PropertyUtils;
import org.javarosa.demo.properties.DemoAppProperties;
import org.javarosa.demo.util.JRDemoUtil;
import org.javarosa.demo.util.MetaPreloadHandler;
import org.javarosa.formmanager.FormManagerModule;
import org.javarosa.j2me.J2MEModule;
import org.javarosa.j2me.util.DumpRMS;
import org.javarosa.j2me.view.J2MEDisplay;
import org.javarosa.model.xform.SMSSerializingVisitor;
import org.javarosa.model.xform.XFormSerializingVisitor;
import org.javarosa.model.xform.XFormsModule;
import org.javarosa.patient.PatientModule;
import org.javarosa.patient.model.Patient;
import org.javarosa.resources.locale.LanguagePackModule;
import org.javarosa.resources.locale.LanguageUtils;
import org.javarosa.services.transport.SubmissionTransportHelper;
import org.javarosa.services.transport.TransportManagerModule;
import org.javarosa.services.transport.TransportMessage;
import org.javarosa.services.transport.impl.simplehttp.SimpleHttpTransportMessage;
import org.javarosa.services.transport.impl.sms.SMSTransportMessage;
import org.javarosa.user.activity.UserModule;
import org.javarosa.user.model.User;
import org.javarosa.user.utility.UserUtility;
import org.javarosa.xform.util.XFormUtils;

//#if app.uselocation && polish.api.locationapi
import org.javarosa.location.LocationModule;
//#endif

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
	private int patientID;
	
	public void setMidlet(MIDlet m) {
		this.midlet = m;
		J2MEDisplay.init(m);
	}
	
	public MIDlet getMidlet() {
		return midlet;
	}
	
	public void init (MIDlet m) {
		DumpRMS.RMSRecoveryHook(m);
		
		setMidlet(m);
		loadModules();
		
		IStorageUtility forms = StorageManager.getStorage(FormDef.STORAGE_KEY);
		if (forms.getNumRecords() == 0) {
			loadForms(forms);
		}
		addCustomLanguages();
		setProperties();
	
		IStorageUtility patients = StorageManager.getStorage(Patient.STORAGE_KEY);
		if (patients.getNumRecords() == 0) {
			JRDemoUtil.loadDemoPatients(patients);
		}
		
		UserUtility.populateAdminUser();
		loadRootTranslator();
	}
	
	private void loadForms (IStorageUtility forms) {
		try {
			//forms.write(XFormUtils.getFormFromResource("/shortform.xhtml"));
			forms.write(XFormUtils.getFormFromResource("/intelligibleshortform.xhtml"));
			forms.write(XFormUtils.getFormFromResource("/CHMTTL.xhtml"));
			forms.write(XFormUtils.getFormFromResource("/condtest.xhtml"));
			forms.write(XFormUtils.getFormFromResource("/patient-entry.xhtml"));
//			forms.write(XFormUtils.getFormFromResource("/imci.xml"));
			forms.write(XFormUtils.getFormFromResource("/PhysicoChemTestsDemo.xhtml"));
			forms.write(XFormUtils.getFormFromResource("/ImageSelectTester.xhtml"));
			forms.write(XFormUtils.getFormFromResource("/sampleform.xml"));
			forms.write(XFormUtils.getFormFromResource("/submissiontest.xml"));
			forms.write(XFormUtils.getFormFromResource("/smspushtest.xml"));
			
		} catch (StorageFullException e) {
			throw new RuntimeException("uh-oh, storage full [forms]"); //TODO: handle this
		}
	}
	
	private void loadModules() {
		new J2MEModule().registerModule();
		new JavaRosaCoreModule().registerModule();
		new CoreModelModule().registerModule();
		new XFormsModule().registerModule();
		new LanguagePackModule().registerModule();
		new TransportManagerModule().registerModule();
		new UserModule().registerModule();
		new PatientModule().registerModule();
		new FormManagerModule().registerModule();
		new LanguagePackModule().registerModule();
		
		//#if app.uselocation && polish.api.locationapi
		new LocationModule().registerModule();
		//#endif
	}
	
	
	private void addCustomLanguages() {
		Localization.registerLanguageFile("Afrikaans", "./messages_afr.txt");
		Localization.registerLanguageFile("Dari", "./messages_dari.txt");
		Localization.registerLanguageFile("Espagnol", "./messages_es.txt");
		Localization.registerLanguageFile("Swahili", "./messages_sw.txt");
		Localization.registerLanguageFile("English", "./messages_en.txt");		
	}
	
	private void setProperties() {
		final String POST_URL = "http://staging.commcarehq.org/receiver/submit/test";
		
		PropertyManager._().addRules(new JavaRosaPropertyRules());
		PropertyManager._().addRules(new DemoAppProperties());
		PropertyUtils.initializeProperty("DeviceID", PropertyUtils.genGUID(25));

		PropertyUtils.initializeProperty(DemoAppProperties.POST_URL_LIST_PROPERTY, POST_URL);
		PropertyUtils.initializeProperty(DemoAppProperties.POST_URL_PROPERTY, POST_URL);
        
		LanguageUtils.initializeLanguage(true, "en");
	}
	
	public void setUser (User u) {
		this.user = u;
	}
	
	public User getUser () {
		return user;
	}
	
	public void setPatientID (int patID) {
		this.patientID = patID;
	}
	
	public int getPatientID () {
		return this.patientID;
	}
	
	public TransportMessage buildMessage(FormInstance tree, SubmissionProfile profile) {
		if(profile == null) {
			profile = SubmissionTransportHelper.defaultPostSubmission(PropertyManager._().getSingularProperty(DemoAppProperties.POST_URL_PROPERTY));
		}
		
		return SubmissionTransportHelper.createMessage(tree, profile);
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

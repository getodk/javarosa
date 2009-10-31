package org.javarosa.demo.applogic;

import java.io.IOException;
import java.util.Vector;

import javax.microedition.midlet.MIDlet;

import org.javarosa.cases.model.Case;
import org.javarosa.cases.util.CasePreloadHandler;
import org.javarosa.cases.util.ICaseType;
import org.javarosa.chsreferral.model.PatientReferral;
import org.javarosa.chsreferral.util.PatientReferralPreloader;
import org.javarosa.communication.http.HttpTransportProperties;
import org.javarosa.core.model.CoreModelModule;
import org.javarosa.core.model.FormDef;
import org.javarosa.core.model.instance.DataModelTree;
import org.javarosa.core.model.utils.IPreloadHandler;
import org.javarosa.core.services.PropertyManager;
import org.javarosa.core.services.locale.Localization;
import org.javarosa.core.services.properties.JavaRosaPropertyRules;
import org.javarosa.core.services.storage.IStorageUtility;
import org.javarosa.core.services.storage.StorageFullException;
import org.javarosa.core.services.storage.StorageManager;
import org.javarosa.core.services.transport.IDataPayload;
import org.javarosa.core.util.JavaRosaCoreModule;
import org.javarosa.core.util.PropertyUtils;
import org.javarosa.demo.properties.DemoAppProperties;
import org.javarosa.demo.util.JRDemoUtil;
import org.javarosa.formmanager.FormManagerModule;
import org.javarosa.j2me.J2MEModule;
import org.javarosa.j2me.util.DumpRMS;
import org.javarosa.j2me.view.J2MEDisplay;
import org.javarosa.model.xform.XFormsModule;
import org.javarosa.patient.PatientModule;
import org.javarosa.patient.model.Patient;
import org.javarosa.resources.locale.LanguagePackModule;
import org.javarosa.resources.locale.LanguageUtils;
import org.javarosa.services.transport.TransportMessage;
import org.javarosa.services.transport.impl.simplehttp.SimpleHttpTransportMessage;
import org.javarosa.user.activity.UserModule;
import org.javarosa.user.model.User;
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
		
		JRDemoUtil.initAdminUser("234");
	}
	
	private void loadForms (IStorageUtility forms) {
		try {
			forms.write(XFormUtils.getFormFromResource("/shortform.xhtml"));
			forms.write(XFormUtils.getFormFromResource("/CHMTTL.xhtml"));
			forms.write(XFormUtils.getFormFromResource("/condtest.xhtml"));
			forms.write(XFormUtils.getFormFromResource("/patient-entry.xhtml"));
			forms.write(XFormUtils.getFormFromResource("/imci.xml"));
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
		new UserModule().registerModule();
		new PatientModule().registerModule();
		new FormManagerModule().registerModule();
	}
	
	
	private void addCustomLanguages() {
		Localization.registerLanguageFile("default","/messages_cc_default.txt");
		Localization.registerLanguageFile("sw","/messages_cc_swahili.txt");
	}
	
	private void setProperties() {
		final String POST_URL = "http://test.commcarehq.org/submit";
		
		
		PropertyManager._().addRules(new JavaRosaPropertyRules());
		PropertyManager._().addRules(new DemoAppProperties());
		PropertyUtils.initializeProperty("DeviceID", PropertyUtils.genGUID(25));

		PropertyUtils.initializeProperty(HttpTransportProperties.POST_URL_LIST_PROPERTY, POST_URL);
		PropertyUtils.initializeProperty(HttpTransportProperties.POST_URL_PROPERTY, POST_URL);
        
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
	
	
	
	
	
	
	
	
	
	
	
	///////////////////
	
	

	private ICaseType recentCaseType;
	
	private boolean inDemoMode;
	private String weeklySurvey = new String("brac_chp_weekly_update");
	
	public TransportMessage buildMessage(IDataPayload payload) {
		//Right now we have to just give the message the stream, rather than the payload,
		//since the transport layer won't take payloads. This should be fixed _as soon 
		//as possible_ so that we don't either (A) blow up the memory or (B) lose the ability
		//to send payloads > than the phones' heap.
		try {
			return new SimpleHttpTransportMessage(payload.getPayloadStream(), PropertyManager._().getSingularProperty(CommCareProperties.POST_URL_PROPERTY));
		} catch (IOException e) {
			e.printStackTrace();
			throw new RuntimeException("Error Serializing Data to be transported");
		}
	}
	
	
	public Vector<ICaseType> getCaseTypes () {
		Vector<ICaseType> v = new Vector<ICaseType>();
		
		v.addElement(new HouseVisitType());
		v.addElement(new PregnancyType());
	
		return v;
	}
	
	public Vector<IPreloadHandler> getPreloaders() {
		return getPreloaders(null, null);
	}
	
	public Vector<IPreloadHandler> getPreloaders(PatientReferral r) {
		int id = r.getLinkedId();
		Case c = BracUtil.getCase(id);
		return getPreloaders(c, r);
	}
	
	public Vector<IPreloadHandler> getPreloaders(Case c) {
		return getPreloaders(c, null);
	}
	
	public Vector<IPreloadHandler> getPreloaders(Case c, PatientReferral r) {
		Vector<IPreloadHandler> handlers = new Vector<IPreloadHandler>();
		if(c != null) {
			CasePreloadHandler p = new CasePreloadHandler(c);
			handlers.addElement(p);
		}
		if(r != null) {
			PatientReferralPreloader rp = new PatientReferralPreloader(r);
			handlers.addElement(rp);
		}
		MetaPreloadHandler meta = new MetaPreloadHandler(this.getUser());
		handlers.addElement(meta);
		return handlers;		
	}
	
	public ICaseType getLastCaseType() {
		return recentCaseType;
	}
	
	public void setLastCaseType(ICaseType type) {
		recentCaseType = type;
	}
	
	public String getWeeklySurvey() {
		return weeklySurvey;
	}
	
	private void registerDemoStorage (String key, Class type) {
		StorageManager.registerStorage(key, "DEMO_" + key, type);
	}
	
	public void toggleDemoMode(boolean demoOn) {
		boolean changed = false;
		
		if (demoOn != inDemoMode) {
			inDemoMode = demoOn;
			if (demoOn) {
				registerDemoStorage(Case.STORAGE_KEY, Case.class);
				registerDemoStorage(PatientReferral.STORAGE_KEY, PatientReferral.class);
				registerDemoStorage(DataModelTree.STORAGE_KEY, DataModelTree.class);
				//TODO: Use new transport message queue
			} else {
				StorageManager.registerStorage(Case.STORAGE_KEY, Case.class);
				StorageManager.registerStorage(PatientReferral.STORAGE_KEY, PatientReferral.class);
				StorageManager.registerStorage(DataModelTree.STORAGE_KEY, DataModelTree.class);
				//TODO: Use new transport message queue
			}
		}
	}
	
	public void resetDemoData() {
		//#debug debug
		System.out.println("Resetting demo data");
	
		StorageManager.getStorage(Case.STORAGE_KEY).removeAll();
		StorageManager.getStorage(PatientReferral.STORAGE_KEY).removeAll();
		StorageManager.getStorage(DataModelTree.STORAGE_KEY).removeAll();
		//TODO: Use new transport message queue
	}

	
	
	
}

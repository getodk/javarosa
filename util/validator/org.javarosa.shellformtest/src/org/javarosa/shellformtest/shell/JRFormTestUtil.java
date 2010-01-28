package org.javarosa.shellformtest.shell;

import java.util.Vector;

import javax.microedition.midlet.MIDlet;

import org.javarosa.core.model.CoreModelModule;
import org.javarosa.core.model.FormDef;
import org.javarosa.core.model.utils.IPreloadHandler;
import org.javarosa.core.services.PropertyManager;
import org.javarosa.core.services.properties.JavaRosaPropertyRules;
import org.javarosa.core.services.storage.IStorageUtility;
import org.javarosa.core.services.storage.StorageFullException;
import org.javarosa.core.services.storage.StorageManager;
import org.javarosa.core.util.JavaRosaCoreModule;
import org.javarosa.core.util.PropertyUtils;
import org.javarosa.formmanager.FormManagerModule;
import org.javarosa.j2me.J2MEModule;
import org.javarosa.j2me.view.J2MEDisplay;
import org.javarosa.model.xform.XFormsModule;
import org.javarosa.resources.locale.LanguagePackModule;
import org.javarosa.xform.util.XFormUtils;

public class JRFormTestUtil {

	private static MIDlet midlet;
	
	public static void init (MIDlet m) {
		J2MEDisplay.init(m);
		midlet = m;
		
		loadModules();
		
		IStorageUtility forms = StorageManager.getStorage(FormDef.STORAGE_KEY);
		try {
			forms.write(XFormUtils.getFormFromResource("/a.xhtml"));
		} catch (StorageFullException e) {
			throw new RuntimeException("unable to load test form!");
		}

		setProperties();
	}
		
	private static void loadModules() {
		new J2MEModule().registerModule();
		new JavaRosaCoreModule().registerModule();
		new CoreModelModule().registerModule();
		new XFormsModule().registerModule();
		new LanguagePackModule().registerModule();
		new FormManagerModule().registerModule();
	}
		
	private static void setProperties() {
		PropertyManager._().addRules(new JavaRosaPropertyRules());
		PropertyUtils.initializeProperty("DeviceID", PropertyUtils.genGUID(25));
	}
		
	public static Vector<IPreloadHandler> getPreloaders() {
		Vector<IPreloadHandler> handlers = new Vector<IPreloadHandler>();
		return handlers;	
	}

	public static void exit () {
		System.out.println("quitting...");
		midlet.notifyDestroyed();
	}
	
}

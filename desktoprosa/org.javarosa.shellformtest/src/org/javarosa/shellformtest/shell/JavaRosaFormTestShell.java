package org.javarosa.shellformtest.shell;

import java.util.Hashtable;

import javax.microedition.midlet.MIDlet;

import org.javarosa.communication.http.HttpTransportModule;
import org.javarosa.core.Context;
import org.javarosa.core.JavaRosaServiceProvider;
import org.javarosa.core.api.IActivity;
import org.javarosa.core.api.IShell;
import org.javarosa.core.api.IView;
import org.javarosa.core.model.CoreModelModule;
import org.javarosa.core.model.FormDef;
import org.javarosa.core.model.FormIndex;
import org.javarosa.core.model.storage.FormDefRMSUtility;
import org.javarosa.formmanager.FormManagerModule;
import org.javarosa.formmanager.activity.FormEntryActivity;
import org.javarosa.formmanager.activity.FormEntryContext;
import org.javarosa.formmanager.utility.IFormDefRetrievalMethod;
import org.javarosa.formmanager.utility.RMSRetreivalMethod;
import org.javarosa.formmanager.utility.ReferenceRetrievalMethod;
import org.javarosa.j2me.storage.rms.RMSStorageModule;
import org.javarosa.model.xform.XFormsModule;
import org.javarosa.xform.util.XFormUtils;

public class JavaRosaFormTestShell implements IShell {
	private MIDlet midlet;
	private Context context;
	
	public void setMIDlet(MIDlet midlet) {
		this.midlet = midlet;
	}
	
	public JavaRosaFormTestShell() {
		context = new Context();
	}
	
	public void exitShell() {
		midlet.notifyDestroyed();		
	}

	public void returnFromActivity(IActivity activity, String returnCode,
			Hashtable returnArgs) {
		// We should display a screen, but we're just going to exit for now
		exitShell();
	}

	public void run() {
		init();		
	}
	
	private void init() {
		new RMSStorageModule().registerModule(context);
		new XFormsModule().registerModule(context);
		new CoreModelModule().registerModule(context);
		new HttpTransportModule().registerModule(context);
		new FormManagerModule().registerModule(context);

		FormDefRMSUtility formDef = (FormDefRMSUtility)JavaRosaServiceProvider.instance().getStorageManager().getRMSStorageProvider().getUtility(FormDefRMSUtility.getUtilityName());
		int formID = formDef.writeToRMS(XFormUtils.getFormFromResource("/a.xhtml"));
		launchFormEntryActivity(context, formID, -1);
	}
	private void launchFormEntryActivity (Context context, int formID, int instanceID) {
		launchFormEntryActivity(context, formID, instanceID, null, null);
	}
	
	private void launchFormEntryActivity (Context context, int formID, int instanceID, FormIndex selected, IFormDefRetrievalMethod method) {
		FormEntryActivity entryActivity = new FormEntryActivity(this, new FormEntryViewFactory());
		FormEntryContext formEntryContext = new FormEntryContext(context);
		formEntryContext.setFormID(formID);
		formEntryContext.setFirstQuestionIndex(selected);
		if (instanceID != -1)
			formEntryContext.setInstanceID(instanceID);

		if(method != null) {
			entryActivity.setRetrievalMethod(method);
		} else {
			entryActivity.setRetrievalMethod(new RMSRetreivalMethod());
		}
		
		launchActivity(entryActivity, formEntryContext);
	}
	
	private void launchActivity (IActivity activity, Context context) {
		activity.start(context);
	}


	public boolean setDisplay(IActivity callingActivity, IView display) {
		JavaRosaServiceProvider.instance().getDisplay().setView(display);
		return true;
	}
		
}

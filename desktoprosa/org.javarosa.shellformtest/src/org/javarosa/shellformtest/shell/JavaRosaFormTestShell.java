package org.javarosa.shellformtest.shell;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.Hashtable;

import javax.microedition.midlet.MIDlet;

import org.javarosa.communication.http.HttpTransportModule;
import org.javarosa.core.Context;
import org.javarosa.core.JavaRosaServiceProvider;
import org.javarosa.core.api.IActivity;
import org.javarosa.core.api.IShell;
import org.javarosa.core.api.IView;
import org.javarosa.core.model.CoreModelModule;
import org.javarosa.core.model.FormIndex;
import org.javarosa.core.model.instance.DataModelTree;
import org.javarosa.core.model.storage.FormDefRMSUtility;
import org.javarosa.core.services.transport.ByteArrayPayload;
import org.javarosa.core.util.JavaRosaCoreModule;
import org.javarosa.formmanager.FormManagerModule;
import org.javarosa.formmanager.activity.FormEntryActivity;
import org.javarosa.formmanager.activity.FormEntryContext;
import org.javarosa.formmanager.utility.IFormDefRetrievalMethod;
import org.javarosa.formmanager.utility.RMSRetreivalMethod;
import org.javarosa.j2me.J2MEModule;
import org.javarosa.model.xform.XFormSerializingVisitor;
import org.javarosa.model.xform.XFormsModule;
import org.javarosa.resources.locale.LanguagePackModule;
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

	public void returnFromActivity(IActivity activity, String returnCode, Hashtable returnArgs) {
		if (activity instanceof FormEntryActivity) {
			//print xml output to console
			if (((Boolean)returnArgs.get("FORM_COMPLETE")).booleanValue()) {
				DataModelTree instance = (DataModelTree)returnArgs.get("DATA_MODEL");
				ByteArrayPayload payload = null;
				try {
					payload = (ByteArrayPayload)(new XFormSerializingVisitor()).createSerializedPayload(instance);
				} catch (IOException e) {
					throw new RuntimeException("a");
				}
				InputStream is = payload.getPayloadStream();
				int len = (int)(payload.getLength());
				
				byte[] data = new byte[len];
				try {
					is.read(data, 0, len);
				} catch (IOException e) {
					throw new RuntimeException("b");
				}
				
				System.out.println("BEGINXMLOUTPUT");
				try {
					System.out.println(new String(data, "UTF-8"));
				} catch (UnsupportedEncodingException e) {
					throw new RuntimeException("c");
				}
				System.out.println("ENDXMLOUTPUT");
			}			
		}
		
		// We should display a screen, but we're just going to exit for now
		exitShell();
	}

	public void run() {
		init();		
	}
	
	private void init() {
		new JavaRosaCoreModule().registerModule(context);
		new J2MEModule().registerModule(context);
		new LanguagePackModule().registerModule(context);
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

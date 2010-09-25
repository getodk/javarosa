package org.javarosa.demo.applogic;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import org.javarosa.core.api.State;
import org.javarosa.core.model.FormDef;
import org.javarosa.core.model.instance.FormInstance;
import org.javarosa.core.services.storage.IStorageUtility;
import org.javarosa.core.services.storage.StorageFullException;
import org.javarosa.core.services.storage.StorageManager;
import org.javarosa.demo.activity.remoteformlist.JRDemoRemoteFormListController;
import org.javarosa.demo.activity.remoteformlist.JRDemoRemoteFormListTransitions;
import org.javarosa.demo.util.JRDemoUtil;
import org.javarosa.services.properties.api.PropertyUpdateState;
import org.javarosa.user.api.AddUserState;
import org.javarosa.user.model.User;
import org.javarosa.xform.util.XFormUtils;

public class JRDemoRemoteFormListState implements JRDemoRemoteFormListTransitions, State {

	private String formList = null;
	
	public JRDemoRemoteFormListState()
	{
		
	}

	public JRDemoRemoteFormListState(String formList)
	{
		this.formList= formList;
	}

	public void start() {
		JRDemoRemoteFormListController ctrl = new JRDemoRemoteFormListController(this.formList);
	
		ctrl.setTransitions(this);
		ctrl.start();	
	}

	public void back() {
		new JRDemoFormListState().start();
	}

	public void formDownload(String formUrl) {
		new JRGetFormHTTPState(formUrl).start();
		/**String formData = null;
		try {
			formData = JRDemoUtil.retrieveDataFormURL(formUrl);
		} catch (IOException e1) {
			System.out.println("Error in network connection.");
		}
		IStorageUtility forms = StorageManager.getStorage(FormDef.STORAGE_KEY);
		InputStream formInputStream = new ByteArrayInputStream(formData.getBytes());
		try {
			forms.write(XFormUtils.getFormFromInputStream(formInputStream));
		} catch (StorageFullException e) {
			throw new RuntimeException("uh-oh, storage full [forms]"); //TODO: handle this
		}**/
	}

}

package org.javarosa.shellformtest.shell;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.Vector;

import org.javarosa.core.model.FormDef;
import org.javarosa.core.model.instance.FormInstance;
import org.javarosa.core.model.utils.IPreloadHandler;
import org.javarosa.core.services.transport.payload.ByteArrayPayload;
import org.javarosa.form.api.FormEntryModel;
import org.javarosa.formmanager.api.FormEntryState;
import org.javarosa.formmanager.api.JrFormEntryController;
import org.javarosa.formmanager.utility.FormDefFetcher;
import org.javarosa.formmanager.utility.RMSRetreivalMethod;
import org.javarosa.formmanager.view.chatterbox.Chatterbox;
import org.javarosa.model.xform.XFormSerializingVisitor;

public class JRFormTestState extends FormEntryState {

	protected JrFormEntryController getController() {
		
		int formID = 1;
		
		Vector<IPreloadHandler> preloaders = JRFormTestUtil.getPreloaders();
		FormDefFetcher fetcher = new FormDefFetcher(new RMSRetreivalMethod(formID), preloaders);
		FormDef form = fetcher.getFormDef();
		
		JrFormEntryController controller =  new JrFormEntryController(new FormEntryModel(form));
		controller.setView(new Chatterbox("Chatterbox", controller));
		return controller;
	}

	public void abort() {
		JRFormTestUtil.exit();
	}

	public void formEntrySaved(FormDef form, FormInstance instanceData, boolean formWasCompleted) {
		if (formWasCompleted) {
			
			ByteArrayPayload payload = null;
			try {
				payload = (ByteArrayPayload)(new XFormSerializingVisitor()).createSerializedPayload(instanceData);
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

		JRFormTestUtil.exit();
	}

	public void suspendForMediaCapture(int captureType) {
		throw new RuntimeException("not supported");
	}

}

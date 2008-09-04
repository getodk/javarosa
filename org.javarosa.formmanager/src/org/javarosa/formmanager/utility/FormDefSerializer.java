package org.javarosa.formmanager.utility;

import java.io.IOException;
import java.io.OutputStream;

import javax.microedition.io.Connector;
import javax.microedition.io.file.FileConnection;

import org.javarosa.core.model.FormDef;
import org.javarosa.core.services.storage.utilities.Serializer;

/**
 * @author Kieran
 *
 *	Yo dudes. This class is for writing the serialized representation of a
 *	FormDef to file. It works in a seperate thread to prevent it hanging, so
 *  you'll first have to set the attributes (.setForm and .setFname) before calling start() on the thread.
 *
 *  e.g.
 *  FormDefSerializer fds = new FormDefSerializer();
 *  fds.setForm(aFormDefObj);
	fds.setFname("Name.txt");
 *  new Thread(FormDefSerializer).start();
 */

// Clayton Sims - Sep 4, 2008 : This class should be separated out in such a way that FormManager isn't dependent
// on the FileConnection API. Even surrounding the appropriate code sections with preprocessor directives would be
// fine.
public class FormDefSerializer implements Runnable{

	private FormDef form;
	private String fname;

	public FormDefSerializer(){

	}

	public FormDef getForm() {
		return form;
	}

	public void setForm(FormDef form) {
		this.form = form;
	}

	public String getFname() {
		return fname;
	}

	public void setFname(String fname) {
		this.fname = fname;
	}

	public void run() {
		this.saveForm(form, fname);
	}

	public void saveForm(FormDef theForm, String filename){
		byte[] data;
		try {
			data = Serializer.serialize(theForm);
	        //LOG
	        System.out.println("SERIALIZED FORM:"+data+" \nS_FORM END");
	        String root = "root1/";
			String uri = "file:///" + root + filename;
			FileConnection fcon = null;
				fcon = (FileConnection) Connector.open(uri);
			if (!fcon.exists()) {
					fcon.create();
			}
			OutputStream out;
			out = fcon.openOutputStream();
			out.write(data);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}

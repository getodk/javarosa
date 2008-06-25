package org.javarosa.clforms.storage;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Vector;

import javax.microedition.rms.InvalidRecordIDException;
import javax.microedition.rms.RecordEnumeration;
import javax.microedition.rms.RecordStoreException;
import javax.microedition.rms.RecordStoreNotOpenException;

import org.javarosa.clforms.api.Form;
import org.javarosa.clforms.xml.XMLUtil;

/**
 *
 * @author Munier
 */
public class XFormRMSUtility extends RMSUtility {

	public XFormRMSUtility(String name) {
		super(name, RMSUtility.RMS_TYPE_META_DATA);
	}

	public void writeToRMS(Form form) {
		super.writeToRMS(form, new XFormMetaData(form));
	}

	public void writeToRMS(byte[] ba) {
		super.writeBytesToRMS(ba, new XFormMetaData());
	}

	public int getSize(int recordId) {
		XFormMetaData xformMetaData = getMetaDataFromId(recordId);
		return xformMetaData.getSize();
	}

	public String getName(int recordId) {
		XFormMetaData xformMetaData = getMetaDataFromId(recordId);
		return xformMetaData.getName();
	}

	private XFormMetaData getMetaDataFromId(int recordId) {
		XFormMetaData xformMetaData = new XFormMetaData();
		this.retrieveMetaDataFromRMS(recordId, xformMetaData);
		return xformMetaData;
	}

	public Vector getListOfFormNames() {
		Vector listOfNames = new Vector();
		try {
			RecordEnumeration recordEnum = recordStore.enumerateRecords(null,
					null, false);
			while (recordEnum.hasNextElement()) {
				int i = recordEnum.nextRecordId();
				System.out.println("trying record:" + i);
				listOfNames.addElement(this.getName(i));

			}
		} catch (RecordStoreNotOpenException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (RecordStoreException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return listOfNames;
	}

	public Vector getListOfFormNames(Vector formIDs) {
		Vector listOfNames = new Vector();
		try {
			RecordEnumeration recordEnum = recordStore.enumerateRecords(null,
					null, false);
			while (recordEnum.hasNextElement()) {
				int i = recordEnum.nextRecordId();
				System.out.println("trying record:" + i);
				listOfNames.addElement(this.getName(i));
				formIDs.addElement(new Integer(i));
			}
		} catch (RecordStoreNotOpenException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (RecordStoreException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return listOfNames;
	}

	public void writeDummy() {
		/* ORIGINAL SCHEME
		this.open();
		DummyForm dummy = new DummyForm();
		dummy.setDemo();
		//this.writeBytesToRMS(dummy.getData(), new XFormMetaData(dummy.getXFormObject()));
		this.writeToRMS(dummy.getXFormObject(),new XFormMetaData(dummy.getXFormObject()));
//		ExampleForm ef = new ExampleForm();
//		this.writeToRMS(ef.getXFormObject(),new XFormMetaData(ef.getXFormObject()));
		System.out.println("Dummy Record ID : ");
		*/

		/* NEW RESOURCE-BASED SCHEME */
		writeFormFromResource("/hmis-a_draft.xhtml");
		writeFormFromResource("/hmis-b_draft.xhtml");
		writeFormFromResource("/shortform.xhtml");
		
		
		/* LOAD CDC-TZ FORMS
        this.open();
//      CDCXForms cdcXForm = new CDCXForms();
        CDCXFormDemo cdcXForm = new CDCXFormDemo();
        cdcXForm.setDemo();		
        System.out.println("Storing sample Xform");
//      Form form = cdcXForm.getXFormObject();
//      XFormMetaData metaData = new XFormMetaData(cdcXForm.getXFormObject()); 
		this.writeToRMS(cdcXForm.getXFormObject(), new XFormMetaData(cdcXForm.getXFormObject()));
        System.out.println("CDCXForm Stored ");
		*/
	}

	private void writeFormFromResource (String resource) {
		InputStream is = getClass().getResourceAsStream(resource);
		InputStreamReader isr = new InputStreamReader(is);
		if(isr != null) {
			Form xform = XMLUtil.parseForm(isr);
			if(xform != null) {
				writeToRMS(xform, new XFormMetaData(xform));
			}
			else {
				System.out.println("Unable to parse " + resource + ". Xform could not be loaded.");
			}
		}
		else {
			System.out.println("The XFormRMSUtility was unable to obtain a stream " +
					"for the resource " + resource);
		}
		try {
			isr.close();
		}
		catch(IOException e) {
			System.err.println("IO Exception while closing stream.");
			e.printStackTrace();
		}
	}

	public int getIDfromName(String name) {
		// TODO Check if this is still needed / valid - considering two forms
		// can have same name
		int id = -1;
		this.open();
		XFormMetaData xformMetaData = new XFormMetaData();
		try {
			RecordEnumeration recEnum = recordStore.enumerateRecords(null,
					null, false);
			while (recEnum.hasNextElement()) {
				id = recEnum.nextRecordId();
				this.retrieveMetaDataFromRMS(id, xformMetaData);
				if (xformMetaData.getName().equals(name)) {
					break;
				}
				id = -1;
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}

		return id;
	}

	public Vector getXformMetaDataList() {
		Vector metaDataList = new Vector();
		try {
			RecordEnumeration metaEnum = metaDataRMS.enumerateMetaData();
			while (metaEnum.hasNextElement()) {
				int i = metaEnum.nextRecordId();
				metaDataList.addElement(getMetaDataFromId(i));
			}
		} catch (InvalidRecordIDException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return metaDataList;
	}

}
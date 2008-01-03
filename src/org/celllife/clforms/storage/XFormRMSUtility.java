package org.celllife.clforms.storage;

import java.util.Vector;

import javax.microedition.rms.InvalidRecordIDException;
import javax.microedition.rms.RecordEnumeration;
import javax.microedition.rms.RecordStoreException;
import javax.microedition.rms.RecordStoreNotOpenException;

import org.celllife.clforms.api.Form;

/**
 * 
 * @author Munier
 */
public class XFormRMSUtility extends RMSUtility {

	public XFormRMSUtility(String name) {
		super(name, RMSUtility.RMS_TYPE_META_DATA);
//		System.out.println("XFormRMSUtility.XFormRMSUtility()");
	}

	public void writeToRMS(Form form) {
		super.writeToRMS(form, new XFormMetaData(form));
//		System.out.println("XFormRMSUtility.writeToRMS(Form)");		
	}

	public void writeToRMS(byte[] ba) {
		super.writeBytesToRMS(ba, new XFormMetaData());
//		System.out.println("XFormRMSUtility.writeToRMS(byte)");
	}

	public int getSize(int recordId) {
//		System.out.println("XFormRMSUtility.getSize()");
		XFormMetaData xformMetaData = getMetaDataFromId(recordId);
		return xformMetaData.getSize();
	}

	public String getName(int recordId) {
//		System.out.println("XFormRMSUtility.getName()");
		XFormMetaData xformMetaData = getMetaDataFromId(recordId);
		return xformMetaData.getName();
	}

	private XFormMetaData getMetaDataFromId(int recordId) {
//		System.out.println("XFormRMSUtility.getMetaDataFromId()");
		XFormMetaData xformMetaData = new XFormMetaData();
		this.retrieveMetaDataFromRMS(recordId, xformMetaData);
		return xformMetaData;
	}

	public Vector getListOfFormNames() {
//		System.out.println("XFormRMSUtility.getListOfFormNames()");
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
//		System.out.println("XFormRMSUtility.getListOfFormNames(Vector)");
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
//		System.out.println("XFormRMSUtility.writeDummy()");
		this.open();
		DummyForm dummy = new DummyForm();
		dummy.setDemo();
		//this.writeBytesToRMS(dummy.getData(), new XFormMetaData(dummy.getXFormObject()));
		this.writeToRMS(dummy.getXFormObject(),new XFormMetaData(dummy.getXFormObject()));
		System.out.println("Dummy Record ID : ");
	}

	public int getIDfromName(String name) {
//		System.out.println("XFormRMSUtility.getIDfromName()");
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
//		System.out.println("XFormRMSUtility.getXformMetaDataList()");
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
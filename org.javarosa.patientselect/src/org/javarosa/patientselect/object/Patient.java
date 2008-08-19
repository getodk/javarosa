package org.javarosa.patientselect.object;


import java.util.Vector;
import java.io.IOException;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import org.javarosa.core.model.utils.ExternalizableHelper;
import org.javarosa.core.util.Externalizable;
import org.javarosa.core.util.UnavailableExternalizerException;

/**
* This encapsulates the Externalizable object to be passed to the RMS
* 
* 
* @author Mark Gerard
*
*/

public class Patient implements Externalizable{
	
	private int patientId;
	private String patientName;
	private String patientAge;
	private String patientDiagnosis;
	private String patientTreatment;
	private String patientLocation;
	private String patientHealthCenter;
	
	private String reportDate;
	private String nextVisitDate;
	
	private Vector patientData;
	private ExternalizableObject externPatientData;
	private String title = "Patient Data";
	
	public Patient(){
		
		patientData = new Vector();
		
		externPatientData = new ExternalizableObject(title);
	}
	
	public Patient (int patientID, Vector patData){
		
		this.patientId = patientID;
		this.patientData = patData;
	}
	
	public int getPatientId(){
		
		return patientId;
	}
	
	public String getPatientName(){
		return patientName;
	}
	
	public String getPatientAge(){
		return patientAge;
	}
	
	public String getPatientDiagnosis(){
		return patientDiagnosis;
	}
	
	public String getPatientTreatment(){
		return patientTreatment;
	}
	
	public String getPatientLocation(){
		return patientLocation;
	}
	
	public String getPatientHealthCenter(){
		return patientHealthCenter;
	}
	
	public String getReportDate(){
		return reportDate;
	}
	
	public String getNextVisitDate(){
		return nextVisitDate;
	}
	
	public int setPatientId(int patId){
		this.patientId = patId;
		
		return patientId;
	}
	
	public String setPatientName(String patName){
		this.patientName = patName;
		return patientName;
	}
	
	public String setPatientAge(String patAge){
		this.patientAge = patAge;
		return patientAge;
	}
	
	public String setPatientDiagnosis(String patDiagnosis){
		this.patientDiagnosis = patDiagnosis;
		return patientDiagnosis;
	}
	
	public String setPatientTreatment(String patTreatment){
		this.patientTreatment = patTreatment;
		return patientTreatment;
	}
	
	public String setPatientLocation(String patLocation){
		this.patientLocation = patLocation;
		return patientLocation;
	}
	
	public String setPatientHealthCenter(String healthCenter){
		this.patientHealthCenter = healthCenter;
		return patientHealthCenter;
	}
	
	public String setPatientReportDate(String patientReportDate){
		this.reportDate = patientReportDate;
		return reportDate;
	}
	
	public String setPatientNextVisitDate(String patientNextVisitDate){
		this.nextVisitDate = patientNextVisitDate;
		return nextVisitDate;
	}
	
	public void readExternal(DataInputStream in) throws IOException,InstantiationException, IllegalAccessException,UnavailableExternalizerException {
		
		patientId = ExternalizableHelper.readNumInt(in, ExternalizableHelper.ENCODING_NUM_DEFAULT);
		patientName = ExternalizableHelper.readUTF(in);
		
		patientData = ExternalizableHelper.readUTFs(in);		
	}

	public void writeExternal(DataOutputStream out) throws IOException {
		
		ExternalizableHelper.writeBytes(patientData, out);		
	}

}

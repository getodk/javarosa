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
	
	/*
	 * Constructor to create patient object with vector to hold patient data
	 */
	public Patient(){
		
		patientData = new Vector();
		
	}
	
	/*
	 * Constructor to create patient with patient Id and vector
	 */
	public Patient (int patientID, Vector patData){
		
		this.patientId = patientID;
		this.patientData = patData;
	}
	
	/*
	 * Property to index patientId
	 */
	public int getPatientId(){
		
		return patientId;
	}
	
	/*
	 * Property to index the patientName
	 */
	public String getPatientName(){
		return patientName;
	}
	
	/*
	 * Property to index the patientAge
	 */
	public String getPatientAge(){
		return patientAge;
	}
	
	/*
	 * Property to index the patient Diagnosis
	 */
	public String getPatientDiagnosis(){
		return patientDiagnosis;
	}
	
	/*
	 * Property to index the patient treatment
	 */
	public String getPatientTreatment(){
		return patientTreatment;
	}
	
	/*
	 * Property to index the patient location
	 */
	public String getPatientLocation(){
		return patientLocation;
	}
	
	/*
	 * Property to index the patient Health center
	 */
	public String getPatientHealthCenter(){
		return patientHealthCenter;
	}
	
	/*
	 * Property to index the patient report Date
	 */
	public String getReportDate(){
		return reportDate;
	}
	
	/*
	 * Property to index the patient Next visit Date
	 */
	public String getNextVisitDate(){
		return nextVisitDate;
	}
	
	/*
	 * Method to set the patient Id
	 */
	public int setPatientId(int patId){
		this.patientId = patId;
		
		return patientId;
	}
	
	/*
	 * Method to set the patient Name
	 */
	public String setPatientName(String patName){
		this.patientName = patName;
		return patientName;
	}
	
	/*
	 * Method to set the patient Age
	 */
	public String setPatientAge(String patAge){
		this.patientAge = patAge;
		return patientAge;
	}
	
	/*
	 * Method to set the patient Diagnosis
	 */
	public String setPatientDiagnosis(String patDiagnosis){
		this.patientDiagnosis = patDiagnosis;
		return patientDiagnosis;
	}
	
	/*
	 * Method to set the Patient Treatment
	 */
	public String setPatientTreatment(String patTreatment){
		this.patientTreatment = patTreatment;
		return patientTreatment;
	}
	
	/*
	 * Method to set the patient Location
	 */
	public String setPatientLocation(String patLocation){
		this.patientLocation = patLocation;
		return patientLocation;
	}
	
	/*
	 * Method to set the patient health center
	 */
	public String setPatientHealthCenter(String healthCenter){
		this.patientHealthCenter = healthCenter;
		return patientHealthCenter;
	}
	
	/*
	 * Method to set the patient Report date
	 */
	public String setPatientReportDate(String patientReportDate){
		this.reportDate = patientReportDate;
		return reportDate;
	}
	
	/*
	 * Method to set the patient Next visit Date
	 */
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

package org.javarosa.patientselect;

public interface ISearch {
	
	/*
	 * Method to search for patient using the patientName
	 */
	public String searchByName(String patientName);
	
	/*
	 * Method to search for patient using the patientCode
	 */
	public String searchByCode(String code);
	
	/*
	 * Method to search for a patient using a known Index... Testing purposes only
	 */
	public void searchByBoth(String patientName, String patientCode);
	
	/**
	 * Method to select a particular patient
	 * Specified patients is called using patientId
	 * @param patientId gets particular patient
	 */
	public String selectPatient(String patientId);
	
	/**
	 * Cleans up anything that needs to be manually destroyed in a module
	 */
	public void destroy();
	
	/**
	 * Displays appropriate message to the user
	 */
	public void showAppMessage();

}


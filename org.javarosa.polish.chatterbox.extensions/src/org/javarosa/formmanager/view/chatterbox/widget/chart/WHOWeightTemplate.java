package org.javarosa.formmanager.view.chatterbox.widget.chart;

import java.util.Vector;

import org.javarosa.core.model.utils.DateUtils;
import org.javarosa.patient.model.Patient;

/**
 * The WHO Weight Template displays the WHO's 0-5 year old's weight growth
 * recording chart, including the lines for standard growth, and standard
 * deviations thereof.
 * 
 * The data for this widget comes from the WHO's records at
 * http://www.who.int/childgrowth/standards/weight_for_age/en/index.html
 * 
 * 
 * @author Clayton Sims
 *
 */
public class WHOWeightTemplate implements IGraphTemplate {
	
	public static final int WHO_WEIGHT_TEMPLATE_ID = 1;
	public static final String WHO_WEIGHT_TEMPLATE_NAME = "WHOWeightGraph";
	
	private Patient patient;

	/*
	 * (non-Javadoc)
	 * @see org.javarosa.formmanager.view.chatterbox.widget.chart.IGraphTemplate#getTemplateName()
	 */
	public String getTemplateName() {
		return WHO_WEIGHT_TEMPLATE_NAME;
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.javarosa.formmanager.view.chatterbox.widget.chart.IGraphTemplate#getTemplateId()
	 */
	public int getTemplateId() {
		return WHO_WEIGHT_TEMPLATE_ID;
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.javarosa.formmanager.view.chatterbox.widget.chart.IGraphTemplate#getDataPoints()
	 */
	public Vector getLines(Vector data) {
		
		//This function will give a number of months. We can use it 
		//DateUtils.getMonthsDifference(earlierDate, laterDate)
		
		//TODO: Scale X axis from 0 to 5 years. Display the data from the website
		Vector lines = new Vector();
		Vector points = new Vector();
		
		lines.addElement(points);
        int [] chartX2PointsArray = {15, 20, 30, 35, 45, 55, 68, 79, 90};
        int [] chartY2PointsArray = {2, 8, 16, 32, 48, 55, 64, 70, 80};
        
        
        for(int i = 0; i < chartX2PointsArray.length; i++) {
        	points.addElement(new LinePointsItem("", chartX2PointsArray[i], chartY2PointsArray[i], 0,  255,   255));
        }
        
		return lines;
	}

	/**
	 * @param patient the patient to set
	 */
	public void setPatient(Patient patient) {
		this.patient = patient;
	}

}

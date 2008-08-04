package org.javarosa.formmanager.view.chatterbox.widget.chart;

import java.util.Vector;

public class WHOWeightTemplate implements IGraphTemplate {
	
	public static final int WHO_WEIGHT_TEMPLATE_ID = 1;
	public static final String WHO_WEIGHT_TEMPLATE_NAME = "WHOWeightGraph";

	public String getTemplateName() {
		return WHO_WEIGHT_TEMPLATE_NAME;
	}
	
	public int getTemplateId() {
		return WHO_WEIGHT_TEMPLATE_ID;
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.javarosa.formmanager.view.chatterbox.widget.chart.IGraphTemplate#getDataPoints()
	 */
	public Vector getLines() {
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

}

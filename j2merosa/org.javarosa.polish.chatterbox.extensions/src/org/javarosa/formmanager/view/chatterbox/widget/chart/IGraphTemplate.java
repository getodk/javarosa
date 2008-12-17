package org.javarosa.formmanager.view.chatterbox.widget.chart;

import java.util.Vector;

public interface IGraphTemplate {
	
	/**
	 * @return the name that is used in XFroms, etc, to distinguish this 
	 * template. 
	 */
	String getTemplateName();
	
	/**
	 * @param data A Vector<DateValueTuple> that represents the data
	 * to be drawn on the graph
	 * @return A Vector<Vector<LinePointsItem>> of data values which
	 * includes the data which was passed in, along with any other
	 * lines for the template.
	 */
	Vector getLines(Vector data);
}

package org.javarosa.formmanager.view.chatterbox.widget.chart;

import java.util.Vector;

public interface IGraphTemplate {
	
	int getTemplateId();
	
	String getTemplateName();
	
	/**
	 * @return A Vector<Vector<LinePointsItem>> of data values which
	 * determine a template for the graph to be drawn against
	 */
	Vector getLines();
}

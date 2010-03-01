/*
 * Copyright (C) 2009 JavaRosa
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

package org.javarosa.formmanager.view.chatterbox.extendedwidget;

import java.util.Enumeration;
import java.util.Vector;

import javax.microedition.lcdui.Font;

import org.javarosa.core.model.QuestionDef;
import org.javarosa.core.model.data.IAnswerData;
import org.javarosa.core.util.Map;
import org.javarosa.form.api.FormEntryCaption;
import org.javarosa.form.api.FormEntryPrompt;
import org.javarosa.formmanager.view.chatterbox.extendedwidget.chart.IGraphTemplate;
import org.javarosa.formmanager.view.chatterbox.extendedwidget.chart.LineChart;
import org.javarosa.formmanager.view.chatterbox.extendedwidget.chart.LinePointsItem;
import org.javarosa.formmanager.view.chatterbox.widget.ChatterboxWidget;
import org.javarosa.formmanager.view.chatterbox.widget.ExpandedWidget;
import org.javarosa.formmanager.view.chatterbox.widget.WidgetEscapeComponent;
import org.javarosa.patient.util.DateValueTuple;

import de.enough.polish.ui.Item;

/**
 * GraphWidget is an extended chatterbox widget that draws sets
 * of DateValueTuples onto the screen with dates scaled on the X
 * axis, and values scaled on the Y axis of a colored graph.
 * 
 * @author Clayton Sims
 *
 */
public class GraphWidget extends ExpandedWidget {
	private WidgetEscapeComponent wec = new WidgetEscapeComponent();

	public final static int CONTROL_GRAPH = 11; 
	
	private int resolution = 100;
	
	//The actual chart widget
	LineChart chart;
	
	Integer[] chartXPointsArray = {};
	Integer[] chartYPointsArray = {};
	String [] chartXPointsLabelArray;
	
	/** Integer->IGraphTemplate */
	Map templates = new Map();
	
	IGraphTemplate currentTemplate = null;
	
	Vector data;
	
	public GraphWidget() {
		init();
	}
	
	/**
	 * Do the initial setup for the Chart
	 */
	private void init() {
        //#style lineChart
        chart = new LineChart(""); 
        chart.setUseDefaultColor(false);
        
        chart.setFont(Font.FACE_PROPORTIONAL,Font.STYLE_PLAIN,Font.SIZE_SMALL);
        chart.setDrawAxis(true);
        chart.setPreferredSize(240, 200);
        chart.setMargins(5,3,30,35);
        chart.makeShadowVisible(true);
        chart.setShadowColor(20,20,20);
        chart.setColor(0, 0, 0);
        chart.resetData();

        //chart.setMaxYScaleFactor(100);
        //chart.setMaxXScaleFactor(18);
        wec.init();
	}
	
	/**
	 * Commits the data line (not the template lines), onto the actual chart widget
	 */
	private void applyData() {
		for(int i = 0; i < chartXPointsArray.length; i++) {
            chart.insertItem("", chartYPointsArray[i].intValue(), chartXPointsArray[i].intValue(), 0,  0,   255);
        }
        
	}
	
	/**
	 * Applies the data from the graph's template to this chart
	 * @param data a Vector<DateValueTuple> of measurements that
	 * should be used to scale the lines returned by the template
	 */
	private void applyTemplate(Vector data) {
		if(currentTemplate != null) {
			Vector lines = currentTemplate.getLines(data);
			if(lines != null) {
				Enumeration en = lines.elements();
				while(en.hasMoreElements()) {
					Vector dataPoints = (Vector)en.nextElement();
					for(int i = 0; i < dataPoints.size(); i++) {
			            chart.insertItem((LinePointsItem)dataPoints.elementAt(i));
			        }
				}
		        chart.startNewLine();
			}
		}
	}
	
	/**
	 * Registers a new template for displaying records on the line chart
	 * @param template An IGraphTemplate object that this widget will register
	 * to use in displaying graph data.
	 */
	public void registerTemplate(IGraphTemplate template) {
		templates.put(template.getTemplateName(), template);
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.javarosa.formmanager.view.chatterbox.widget.ExpandedWidget#getEntryWidget(org.javarosa.core.model.QuestionDef)
	 */
	protected Item getEntryWidget(FormEntryPrompt prompt) {		
		return wec.wrapEntryWidget(chart);
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.javarosa.formmanager.view.chatterbox.widget.ExpandedWidget#getEntryWidget(org.javarosa.core.model.QuestionDef)
	 */
	public Item getInteractiveWidget() {		
		return wec.wrapInteractiveWidget(super.getInteractiveWidget());
	}

	/*
	 * (non-Javadoc)
	 * @see org.javarosa.formmanager.view.chatterbox.widget.ExpandedWidget#getNextMode()
	 */
	public int getNextMode () {
		return wec.wrapNextMode(ChatterboxWidget.NEXT_ON_SELECT);
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.javarosa.formmanager.view.chatterbox.widget.ExpandedWidget#getWidgetValue()
	 */
	protected IAnswerData getWidgetValue() {
		return null;
	}

	/*
	 * (non-Javadoc)
	 * @see org.javarosa.formmanager.view.chatterbox.widget.ExpandedWidget#setWidgetValue(java.lang.Object)
	 */
	protected void setWidgetValue(Object o) {
		if(o instanceof Vector) {
			data = (Vector)o;
			
			
			int numPoints = data.size();
			
			int scaledRes = resolution/numPoints;
			
			chartXPointsArray = new Integer[numPoints];
			chartYPointsArray = new Integer[numPoints];
			
			Long minDate = null;
			Long maxDate = null;
			
			Integer minVal = null;
			Integer maxVal = null;
			
			
			Enumeration en = data.elements();
			while(en.hasMoreElements()) {
				DateValueTuple tuple = (DateValueTuple)en.nextElement();
				long time = tuple.date.getTime();
				if(minDate == null || minDate.longValue() > time) {
					minDate = new Long(time);
				}
				if(maxDate == null || maxDate.longValue() < time) {
					maxDate = new Long(time);
				}
				
				int val = tuple.value;
				if(minVal == null || minVal.intValue() > val) {
					minVal = new Integer(val);
				}
				if(maxVal == null || maxVal.intValue() < val) {
					maxVal = new Integer(val);
				}
			}
			
			long dateSpan = maxDate.longValue() - minDate.longValue();
			if(dateSpan == 0) {
				dateSpan = 1;
			}
			//int valSpan = maxVal.intValue() - minVal.intValue();
			
			long dateRes = dateSpan / numPoints;
			
			//int valRes = valSpan / numPoints;
			
			
			Vector xpoints = new Vector();
			Vector ypoints = new Vector();
			en = data.elements();
			while(en.hasMoreElements()) {
				DateValueTuple tuple = (DateValueTuple)en.nextElement();
				long time = tuple.date.getTime();
				long timeUnit = (time - minDate.longValue())*scaledRes;
				long scaledTime = (timeUnit/dateRes);
				int intScaled = (int)scaledTime;
				int finalTime = intScaled;
				Integer xPoint = new Integer(finalTime);
				Integer val = new Integer(tuple.value);
				
				xpoints.addElement(xPoint);
				ypoints.addElement(val);
			}
			xpoints.copyInto(chartXPointsArray);
			ypoints.copyInto(chartYPointsArray);
			
	        //chart.setMaxYScaleFactor((maxVal.intValue()*2)/3);
	        //chart.setMinYScaleFactor(minVal.intValue());
			
			chart.resetData();
			applyTemplate(data);
			applyData();
		}        
	}

	/*
	 * (non-Javadoc)
	 * @see org.javarosa.formmanager.view.chatterbox.widget.ExpandedWidget#updateWidget(org.javarosa.core.model.QuestionDef)
	 */
	protected void updateWidget(FormEntryPrompt prompt) {
		currentTemplate = (IGraphTemplate)templates.get(prompt.getPromptAttributes());
        chart.resetData();
		applyTemplate(data);
		applyData();
	}

	/*
	 * (non-Javadoc)
	 * @see org.javarosa.formmanager.view.chatterbox.widget.IWidgetStyle#widgetType()
	 */
	public int widgetType() {
		return CONTROL_GRAPH;
	}
}

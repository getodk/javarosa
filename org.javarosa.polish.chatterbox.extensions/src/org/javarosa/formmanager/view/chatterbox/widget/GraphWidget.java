package org.javarosa.formmanager.view.chatterbox.widget;

import java.util.Enumeration;
import java.util.Vector;

import javax.microedition.lcdui.Font;

import org.javarosa.core.model.QuestionDef;
import org.javarosa.core.model.data.IAnswerData;
import org.javarosa.core.util.Map;
import org.javarosa.formmanager.view.chatterbox.widget.chart.IGraphTemplate;
import org.javarosa.formmanager.view.chatterbox.widget.chart.LineChart;
import org.javarosa.formmanager.view.chatterbox.widget.chart.LinePointsItem;
import org.javarosa.formmanager.view.chatterbox.widget.chart.WHOWeightTemplate;

import de.enough.polish.ui.Item;

public class GraphWidget extends ExpandedWidget {
	public final static int CONTROL_GRAPH = 101; 
	
	LineChart chart;
	
	/** Integer->IGraphTemplate */
	Map templates = new Map();
	
	IGraphTemplate currentTemplate = null;
	
	public GraphWidget() {
		init();
		this.registerTemplate(new WHOWeightTemplate());
	}
	
	private void init() {
        //#style lineChart
        chart = new LineChart(""); 
        chart.setUseDefaultColor(false);
        
        chart.setFont(Font.FACE_PROPORTIONAL,Font.STYLE_PLAIN,Font.SIZE_SMALL);
        chart.setDrawAxis(true);
        chart.setPreferredSize(240, 120);
        chart.setMargins(5,3,30,35);
        chart.makeShadowVisible(true);
        chart.setShadowColor(20,20,20);
        chart.setColor(0, 0, 0);
        chart.resetData();

        chart.setMaxYScaleFactor(100);
        chart.setMaxXScaleFactor(18);
	}
	
	private void applyData() {
		int [] chartXPointsArray = {5, 10, 20, 25, 35, 45, 58, 69, 80, 99};
        int [] chartYPointsArray = {2, 8, 16, 32, 48, 55, 64, 70, 80, 87};
        String [] chartXPointsLabelArray = {"a", "b", "c", "d", "e", "f", "g", "h", "i", "j"};
        
        for(int i = 0; i < chartXPointsArray.length; i++) {
            chart.insertItem(chartXPointsLabelArray[i], chartXPointsArray[i], chartYPointsArray[i], 0,  0,   255);
        }
        
	}
	
	private void applyTemplate() {
		if(currentTemplate != null) {
			Vector lines = currentTemplate.getLines();
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
	
	public void registerTemplate(IGraphTemplate template) {
		templates.put(new Integer(template.getTemplateId()), template);
	}
	
	protected Item getEntryWidget(QuestionDef question) {		
		return chart;
	}

	public int getNextMode () {
		return ChatterboxWidget.NEXT_ON_SELECT;
	}
	
	protected IAnswerData getWidgetValue() {
		return null;
	}

	
	protected void setWidgetValue(Object o) {
	}

	
	protected void updateWidget(QuestionDef question) {
		currentTemplate = (IGraphTemplate)templates.get(new Integer(question.getDataType()));
        chart.resetData();
		applyTemplate();
		applyData();
	}

	public int widgetType() {
		return CONTROL_GRAPH;
	}
}

package org.javarosa.formmanager.view.chatterbox.widget;

import javax.microedition.lcdui.Font;

import org.javarosa.core.model.QuestionDef;
import org.javarosa.core.model.data.IAnswerData;
import org.javarosa.formmanager.view.chatterbox.widget.chart.LineChart;

import de.enough.polish.ui.Item;
import de.enough.polish.ui.Style;

public class GraphWidget extends ExpandedWidget {
	public final static int CONTROL_GRAPH = 9;
	
	LineChart chart;
	
	public GraphWidget() {
		int [] chartXPointsArray = {5, 10, 20, 25, 35, 45, 58, 69, 80, 99};
        int [] chartYPointsArray = {2, 8, 16, 32, 48, 55, 64, 70, 80, 87};
        String [] chartXPointsLabelArray = {"a", "b", "c", "d", "e", "f", "g", "h", "i", "j"};
        
        int [] chartX2PointsArray = {15, 20, 30, 35, 45, 55, 68, 79, 90};
        int [] chartY2PointsArray = {2, 8, 16, 32, 48, 55, 64, 70, 80};
        //String [] chartX2PointsLabelArray = {"a", "b", "c", "d", "e", "f", "g", "h", "i", "j"};
        
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
        
        for(int i = 0; i < chartXPointsArray.length; i++) {
            chart.insertItem(chartXPointsLabelArray[i], chartXPointsArray[i], chartYPointsArray[i], 0,  0,   255);
        }
        
        chart.startNewLine();
        
        for(int i = 0; i < chartX2PointsArray.length; i++) {
            chart.insertItem("", chartX2PointsArray[i], chartY2PointsArray[i], 0,  255,   255);
        }
        
        chart.setMaxYScaleFactor(100);
        chart.setMaxXScaleFactor(18);
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
	}

	public int widgetType() {
		return CONTROL_GRAPH;
	}
}

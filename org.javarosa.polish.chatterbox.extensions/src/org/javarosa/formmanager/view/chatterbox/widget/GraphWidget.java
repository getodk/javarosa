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
import org.javarosa.patient.util.DateValueTuple;

import de.enough.polish.ui.Item;

public class GraphWidget extends ExpandedWidget {
	public final static int CONTROL_GRAPH = 11; 
	
	private int resolution = 100;
	
	LineChart chart;
	
	Integer[] chartXPointsArray = {};
	Integer[] chartYPointsArray = {};
	String [] chartXPointsLabelArray;
	
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

        //chart.setMaxYScaleFactor(100);
        //chart.setMaxXScaleFactor(18);
	}
	
	private void applyData() {
		//int [] chartXPointsArray = {5, 10, 20, 25, 35, 45, 58, 69, 80, 99};
        //int [] chartYPointsArray = {2, 8, 16, 32, 48, 55, 64, 70, 80, 87};
        //String [] chartXPointsLabelArray = {"a", "b", "c", "d", "e", "f", "g", "h", "i", "j"};
        
        for(int i = 0; i < chartXPointsArray.length; i++) {
            chart.insertItem("", chartYPointsArray[i].intValue(), chartXPointsArray[i].intValue(), 0,  0,   255);
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
		if(o instanceof Vector) {
			Vector data = reverseVector((Vector)o);
			
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
			int valSpan = maxVal.intValue() - minVal.intValue();
			
			long dateRes = dateSpan / numPoints;
			
			int valRes = valSpan / numPoints;
			
			
			Vector xpoints = new Vector();
			Vector ypoints = new Vector();
			en = data.elements();
			while(en.hasMoreElements()) {
				DateValueTuple tuple = (DateValueTuple)en.nextElement();
				long time = tuple.date.getTime();
				System.out.println("Pure " + time);
				long timeUnit = (time - minDate.longValue())*scaledRes;
				System.out.println("Time " + timeUnit);
				System.out.println("Scaler " + dateRes);
				long scaledTime = (timeUnit/dateRes);
				System.out.println("Scaled " + scaledTime);
				int intScaled = (int)scaledTime;
				System.out.println("Int scaled " + intScaled);
				int finalTime = intScaled;
				System.out.println("Final " + finalTime);
				Integer xPoint = new Integer(finalTime);
				Integer val = new Integer(tuple.value);
				//Integer yPoint = new Integer(((val - minVal.intValue())/valRes)*scaledRes);
				
				xpoints.addElement(xPoint);
				ypoints.addElement(val);
			}
			xpoints.copyInto(chartXPointsArray);
			ypoints.copyInto(chartYPointsArray);
			
	        //chart.setMaxYScaleFactor((maxVal.intValue()*2)/3);
	        //chart.setMinYScaleFactor(minVal.intValue());
			
			chart.resetData();
			applyTemplate();
			applyData();
		}        
	}
	
	private Vector reverseVector(Vector vector) {
		Vector v = new Vector();
		for(int i = vector.size() -1 ; i >= 0 ; --i ){
			v.addElement(vector.elementAt(i));
		}
		return v;
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

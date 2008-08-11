package org.javarosa.core.model.utils;

import java.util.Date;

import org.javarosa.core.Context;
import org.javarosa.core.JavaRosaServiceProvider;
import org.javarosa.core.model.IDataReference;
import org.javarosa.core.model.IFormDataModel;
import org.javarosa.core.model.data.DateData;
import org.javarosa.core.model.data.IAnswerData;
import org.javarosa.core.model.data.StringData;
import org.javarosa.core.util.Map;

/**
 * The Question Preloader is responsible for maintaining a set of handlers which are capable 
 * of parsing 'preload' elements, and their parameters, and returning IAnswerData objects.
 * 
 * @author Clayton Sims
 *
 */
public class QuestionPreloader {
	/* String -> IPreloadHandler */
	private Map preloadHandlers;
	
	/**
	 * Creates a new Preloader with default handlers
	 */
	public QuestionPreloader() {
		preloadHandlers = new Map();
		initPreloadHandlers();
	}
	
	/**
	 * Initializes the default preload handlers
	 */
	private void initPreloadHandlers() {
		IPreloadHandler date = new IPreloadHandler() {
			public String preloadHandled() {
				return "date";
			}
			
			public IAnswerData handlePreload(String preloadParams) {
				return preloadDate(preloadParams);
			}
			
			public boolean handlePostProcess(IFormDataModel model, IDataReference ref, String params) {
				//do nothing
				return false;
			}
		};
		
		IPreloadHandler property = new IPreloadHandler() {
			public String preloadHandled() {
				return "property";
			}
			
			public IAnswerData handlePreload(String preloadParams) {
				return preloadProperty(preloadParams);
			}
			
			public boolean handlePostProcess(IFormDataModel model, IDataReference ref, String params) {
				saveProperty(params, ref, model);
				return false;
			}
		};
		
		IPreloadHandler timestamp = new IPreloadHandler() {
			public String preloadHandled() {
				return "timestamp";
			}
			
			public IAnswerData handlePreload(String preloadParams) {
				return ("start".equals(preloadParams) ? getTimestamp() : null);
			}
			
			public boolean handlePostProcess(IFormDataModel model, IDataReference ref, String params) {
				if ("end".equals(params)) {
					model.updateDataValue(ref, getTimestamp());
					return true;
				} else {
					return false;
				}
			}
		};
		
		addPreloadHandler(date);
		addPreloadHandler(property);
		addPreloadHandler(timestamp);
	}
	
	/**
	 * Adds a new preload handler to this preloader. 
	 * 
	 * @param handler an IPreloadHandler that can handle a preload type
	 */
	public void addPreloadHandler(IPreloadHandler handler) {
		preloadHandlers.put(handler.preloadHandled(), handler);
	}
	
	/**
	 * Returns the IAnswerData preload value for the given preload type and parameters
	 *  
	 * @param preloadType The type of the preload to be returned
	 * @param preloadParams Parameters for the preload handler
	 * @return An IAnswerData corresponding to a pre-loaded value for the given
	 * Arguments. null if no preload could successfully be derived due to either
	 * the lack of a handler, or to invalid parameters
	 */
	public IAnswerData getQuestionPreload(String preloadType, String preloadParams) {
		IPreloadHandler handler = (IPreloadHandler)preloadHandlers.get(preloadType);
		if(handler != null) {
			return handler.handlePreload(preloadParams);
		} else {
			System.err.println("Do not know how to handle preloader [" + preloadType + "]");
			return null;
		}
	}
	
	public boolean questionPostProcess (IDataReference ref, String preloadType, String params, IFormDataModel model) {
		IPreloadHandler handler = (IPreloadHandler)preloadHandlers.get(preloadType);
		if(handler != null) {
			return handler.handlePostProcess(model, ref, params);
		} else {
			System.err.println("Do not know how to handle preloader [" + preloadType + "]");
			return false;
		}
	}
	
	/**
	 * Preloads a DateData object for the preload type 'date'
	 * 
	 * @param preloadParams The parameters determining the date
	 * @return A preload date value if the parameters can be parsed,
	 * null otherwise
	 */
	private IAnswerData preloadDate(String preloadParams) {
		Date d = null;
		if (preloadParams.equals("today")) {
			d = new Date();
		} else if (preloadParams.substring(0, 11).equals("prevperiod-")) {
			String[] params = DateUtils.split(preloadParams.substring(11), "-");
			
			try {
				String type = params[0];
				String start = params[1];
				
				boolean beginning;
				if (params[2].equals("head")) beginning = true;
				else if (params[2].equals("tail")) beginning = false;
				else throw new RuntimeException();					
				
				boolean includeToday;
				if (params.length >= 4) {
					if (params[3].equals("x")) includeToday = true;
					else if (params[3].equals("")) includeToday = false;
					else throw new RuntimeException();											
				} else {
					includeToday = false;
				}
				
				int nAgo;
				if (params.length >= 5) {
					nAgo = Integer.parseInt(params[4]);
				} else {
					nAgo = 1;
				}
	
					d = DateUtils.getPastPeriodDate(new Date(), type, start, beginning, includeToday, nAgo);
			} catch (Exception e) {
				throw new IllegalArgumentException("invalid preload params for preload mode 'date'");
			}	
		}
		DateData data = new DateData(d);
		return data;
	}
	
	/**
	 * Preloads a StringData object for the preload type 'property'
	 * 
	 * @param preloadParams The parameters determining the property to be retrieved
	 * @return A preload property value if the parameters can be parsed,
	 * null otherwise
	 */
	private IAnswerData preloadProperty(String preloadParams) {
		String propname = preloadParams;
		String propval = JavaRosaServiceProvider.instance().getPropertyManager().getSingularProperty(propname);
		StringData data = null;
		if (propval != null && propval.length() > 0) {
			data = new StringData(propval);
		}
		return data;
	}
	
	private void saveProperty (String propName, IDataReference ref, IFormDataModel model) {
		IAnswerData answer = model.getDataValue(ref);
		String value = (answer == null ? null : answer.getDisplayText());
		if (propName != null && propName.length() > 0 && value != null && value.length() > 0)
			JavaRosaServiceProvider.instance().getPropertyManager().setProperty(propName, value);
	}
	
	private StringData getTimestamp() {
		return new StringData(DateUtils.formatDateToTimeStamp(new Date()));
	}
}

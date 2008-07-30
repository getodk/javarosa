package org.javarosa.core.model.utils;

import java.util.Date;

import org.javarosa.core.JavaRosaServiceProvider;
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
			public IAnswerData handle(String preloadParams) {
				return preloadDate(preloadParams);
			}
		};
		IPreloadHandler property = new IPreloadHandler() {
			public IAnswerData handle(String preloadParams) {
				return preloadProperty(preloadParams);
			}
		};
		IPreloadHandler timestamp = new IPreloadHandler() {
			public IAnswerData handle(String preloadParams) {
				return preloadTimestamp(preloadParams);
			}
		};

		addPreloadHandler("date", date);
		addPreloadHandler("property", property);
		addPreloadHandler("timestamp", timestamp);
	}
	
	/**
	 * Adds a new preload handler to this preloader. 
	 * 
	 * @param preloadType The type of the preload to register a handler for. If
	 * one already exists, it will be overwritten.
	 * @param handler an IPreloadHandler that can handle a preload type
	 */
	public void addPreloadHandler(String preloadType, IPreloadHandler handler) {
		preloadHandlers.put(preloadType, handler);
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
			return handler.handle(preloadParams);
		}
		else {
			return null;
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
		String preloadVal = null;
		if (propval != null && propval.length() > 0) {
			preloadVal = propval;
		}
		StringData data = new StringData(preloadVal);
		return data;
	}
	
	/**
	 * Preloads a StringData object for the preload type 'timestamp'
	 * 
	 * @param preloadParams The parameters determining the timestamp
	 * @return A preload string value if the parameters can be parsed,
	 * null otherwise
	 */
	//TODO: we should really have a native DateTimeData type
	private IAnswerData preloadTimestamp(String preloadParams) {
		String value = null;
		if ("start".equals(preloadParams)) { //timestamp 'end' should not be preloaded
			value = DateUtils.formatDateToTimeStamp(new Date());
		}
		return new StringData(value);
	}
}

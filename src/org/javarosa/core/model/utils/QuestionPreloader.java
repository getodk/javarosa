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

package org.javarosa.core.model.utils;

import java.util.Date;
import java.util.List;

import org.javarosa.core.model.data.DateData;
import org.javarosa.core.model.data.DateTimeData;
import org.javarosa.core.model.data.IAnswerData;
import org.javarosa.core.model.data.StringData;
import org.javarosa.core.model.instance.TreeElement;
import org.javarosa.core.services.PropertyManager;
import org.javarosa.core.util.Map;
import org.javarosa.core.util.PropertyUtils;

/**
 * The Question Preloader is responsible for maintaining a set of handlers which are capable 
 * of parsing 'preload' elements, and their parameters, and returning IAnswerData objects.
 * 
 * @author Clayton Sims
 *
 */
public class QuestionPreloader {
	/* String -> IPreloadHandler */
	// NOTE: this is not java.util.Map!!!
	private Map<String,IPreloadHandler> preloadHandlers;
	
	/**
	 * Creates a new Preloader with default handlers
	 */
	public QuestionPreloader() {
		preloadHandlers = new Map<String,IPreloadHandler>();
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
			
			public boolean handlePostProcess(TreeElement node, String params) {
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
			
			public boolean handlePostProcess(TreeElement node, String params) {
				saveProperty(params, node);
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
			
			public boolean handlePostProcess(TreeElement node, String params) {
				if ("end".equals(params)) {
					node.setAnswer(getTimestamp());
					return true;
				} else {
					return false;
				}
			}
		};
		
		IPreloadHandler uid = new IPreloadHandler() {
			public String preloadHandled() {
				return "uid";
			}
			public IAnswerData handlePreload(String preloadParams) {
				return new StringData(PropertyUtils.genGUID(25));
			}
			
			public boolean handlePostProcess(TreeElement node, String params) {
				return false;
			}
		};
		
		/*
		//TODO: Finish this up.
		IPreloadHandler meta = new IPreloadHandler() {
			public String preloadHandled() {
				return "meta";
			}
			public IAnswerData handlePreload(String preloadParams) {
				//TODO: Ideally, we want to handle this preloader by taking in the
				//existing structure. Resultantly, we don't want to mess with this.
				//We should be enforcing that we don't.
				return null;
			}
			
			public boolean handlePostProcess(TreeElement node, String params) {
				List kids = node.getChildren();
				Enumeration en = kids.elements();
				while(en.hasMoreElements()) {
					TreeElement kid = (TreeElement)en.nextElement();
					if(kid.getName().equals("uid")) {
						kid.setValue(new StringData(PropertyUtils.genGUID(25)));
					}
				}
				return true;
			}
		};
		*/
		addPreloadHandler(date);
		addPreloadHandler(property);
		addPreloadHandler(timestamp);
		addPreloadHandler(uid);
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
		IPreloadHandler handler = preloadHandlers.get(preloadType);
		if(handler != null) {
			return handler.handlePreload(preloadParams);
		} else {
			System.err.println("Do not know how to handle preloader [" + preloadType + "]");
			return null;
		}
	}
	
	public boolean questionPostProcess (TreeElement node, String preloadType, String params) {
		IPreloadHandler handler = preloadHandlers.get(preloadType);
		if(handler != null) {
			return handler.handlePostProcess(node, params);
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
         List<String> v = DateUtils.split(preloadParams.substring(11), "-", false);
			String[] params = new String[v.size()];
			for (int i = 0; i < params.length; i++)
				params[i] = (String)v.get(i);
			
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
		String propval = PropertyManager._().getSingularProperty(propname);
		StringData data = null;
		if (propval != null && propval.length() > 0) {
			data = new StringData(propval);
		}
		return data;
	}
	
	private void saveProperty (String propName, TreeElement node) {
		IAnswerData answer = node.getValue();
		String value = (answer == null ? null : answer.getDisplayText());
		if (propName != null && propName.length() > 0 && value != null && value.length() > 0)
			PropertyManager._().setProperty(propName, value);
	}
	
	private DateTimeData getTimestamp() {
		return new DateTimeData(new Date());
	}
}

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

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

package org.javarosa.formmanager.view.chatterbox.extendedwidget.table;

import org.javarosa.patient.model.data.ImmunizationData;

import de.enough.polish.ui.Container;
import de.enough.polish.ui.Style;

public class VaccinationTable extends Container {
	Table table;
	
    protected boolean traverse(int dir, int viewportWidth, int viewportHeight, int[] visRect_inout) {
    	return table.traverse(dir, viewportWidth, viewportHeight, visRect_inout);
    }
    public VaccinationTable(boolean focusFirstElement) {
    	super(focusFirstElement);
    	table = new Table("",this);
    	this.add(table);
    }
	
	public VaccinationTable(boolean focusFirstElement, Style style) {
		super(focusFirstElement, style);
		table = new Table("",this);
		this.add(table);
	}
	public void setData(ImmunizationData data) 
    {
    	table.setData(data);
    }
    
    public ImmunizationData getData() {
    	return table.getData();
    }
	
}

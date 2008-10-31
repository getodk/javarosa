package org.javarosa.formmanager.view.chatterbox.widget.table;

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

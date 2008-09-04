package org.javarosa.core.model;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Vector;

import org.javarosa.core.model.utils.ExternalizableHelper;
import org.javarosa.core.util.Externalizable;
import org.javarosa.core.util.UnavailableExternalizerException;

/**
 * This class holds a collection of study definitions.
 * NOTE CAREFULLY: None of the studies contains any forms because this is to just provide
 * info on what studies are available.
 * 
 * @author Mark Gerard
 *
 */
public class StudyDefList implements Externalizable {

	/** Collection of study definitions (StudyDef objects). */
	private Vector studies;
	
	/** Constructs a new study collection. */
	public StudyDefList(){
		
	}
	
	/** Copy Constructor. */
	public StudyDefList(StudyDefList studyDefList){
		studies = new Vector();
		for(byte i=0; i<studyDefList.size(); i++)
			studies.addElement(new StudyDef(studyDefList.getStudy(i)));
	}
	
	public StudyDefList(Vector studies){
		setStudies(studies);
	}
	
	public Vector getStudies() {
		return studies;
	}
	
	public int size(){
		if(studies == null)
			return 0;
		return studies.size();
	}

	public void setStudies(Vector studies) {
		this.studies = studies;
	}
	
	public StudyDef getStudy(byte index){
		return (StudyDef)studies.elementAt(index);
	}
	
	public void addStudy(StudyDef studyDef){
		if(studies == null)
			studies = new Vector();
		studies.addElement(studyDef);
	}
	
	public void addStudies(Vector studyList){
		if(studyList != null){
			if(studies == null)
				studies = studyList;
			else{
				for(byte i=0; i<studyList.size(); i++ )
					studies.addElement(studyList.elementAt(i));
			}
		}
	}
	

	/** 
	 * Reads the study collection object from the stream.
	 * 
	 * @param dis - the stream to read from.
	 * @throws IOException
	 * @throws InstantiationException
	 * @throws IllegalAccessException
	 */
	public void readExternal(DataInputStream in) throws IOException,
			InstantiationException, IllegalAccessException,
			UnavailableExternalizerException {
		if(!ExternalizableHelper.isEOF(in))
			setStudies(ExternalizableHelper.readBig(in,new StudyDef().getClass()));
		
	}

	/** 
	 * Writes the study collection object to the stream.
	 * 
	 * @param dos - the stream to write to.
	 * @throws IOException
	 */
	public void writeExternal(DataOutputStream out) throws IOException {
		ExternalizableHelper.writeBig(getStudies(), out);
		
	}

}

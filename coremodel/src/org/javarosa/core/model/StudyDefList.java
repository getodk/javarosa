package org.javarosa.core.model;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.*;

import org.javarosa.util.db.Persistent;
import org.javarosa.util.db.PersistentHelper;


/**
 * This class holds a collection of study definitions.
 * NOTE CAREFULLY: None of the studies contains any forms because this is to just provide
 * info on what studes are available.
 * 
 * @author Daniel Kayiwa
 *
 */
public class StudyDefList implements Persistent{
	
	/** Collection of studies. */
	private Vector studies;
	
	/** Constructs a new study collection. */
	public StudyDefList(){
		super();
	}
	
	public StudyDefList(Vector studies){
		this();
		setStudies(studies);
	}
	
	public Vector getStudies() {
		return studies;
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
	 * Reads the study collection object from the supplied stream.
	 * 
	 * @param dis - the stream to read from.
	 * @throws IOException
	 * @throws InstantiationException
	 * @throws IllegalAccessException
	 */
	public void read(DataInputStream dis) throws IOException, InstantiationException, IllegalAccessException {
		if(!PersistentHelper.isEOF(dis))
			setStudies(PersistentHelper.read(dis,new StudyDef().getClass()));
	}

	/** 
	 * Writes the study collection object to the supplied stream.
	 * 
	 * @param dos - the stream to write to.
	 * @throws IOException
	 */
	public void write(DataOutputStream dos) throws IOException {
		PersistentHelper.write(getStudies(), dos);
	}
}

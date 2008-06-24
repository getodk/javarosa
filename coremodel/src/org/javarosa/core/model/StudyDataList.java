package org.javarosa.core.model;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Vector;

import org.javarosa.util.db.Persistent;
import org.javarosa.util.db.PersistentHelper;


/**
 * This class holds a collection of study data.
 * 
 * @author Daniel Kayiwa
 *
 */
public class StudyDataList  implements Persistent{
	
	/** Collection of studies. */
	private Vector studies;
	
	/** Constructs a new study data collection. */
	public StudyDataList(){
		super();
	}
	
	public StudyDataList(Vector studies){
		this();
		setStudies(studies);
	}
	
	public Vector getStudies() {
		return studies;
	}

	public void setStudies(Vector studies) {
		this.studies = studies;
	}
	
	public void addStudy(StudyData studyData){
		if(studies == null)
			studies = new Vector();
		studies.addElement(studyData);
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
			setStudies(PersistentHelper.read(dis,new StudyData().getClass()));
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

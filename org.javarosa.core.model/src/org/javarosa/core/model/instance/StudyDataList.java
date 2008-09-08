package org.javarosa.core.model.instance;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Vector;

import org.javarosa.core.model.utils.ExternalizableHelper;
import org.javarosa.core.util.Externalizable;
import org.javarosa.core.util.UnavailableExternalizerException;

/**
 * This class holds a collection of study data.
 * 
 * @author Mark Gerard
 *
 */
public class StudyDataList implements Externalizable {

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
			setStudies(ExternalizableHelper.readBig(in,new StudyData().getClass()));
		
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

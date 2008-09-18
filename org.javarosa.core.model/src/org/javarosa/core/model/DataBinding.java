package org.javarosa.core.model;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import org.javarosa.core.JavaRosaServiceProvider;
import org.javarosa.core.model.condition.Condition;
import org.javarosa.core.model.storage.FormDefRMSUtility;
import org.javarosa.core.model.utils.ExternalizableHelper;
import org.javarosa.core.model.utils.PrototypeFactory;
import org.javarosa.core.util.Externalizable;
import org.javarosa.core.util.UnavailableExternalizerException;

/**
 * A data binding is an object that represents how a
 * data element is to be used in a form entry interaction.
 * 
 * It contains a reference to where the data should be retreived
 * and stored, as well as the preload parameters, and the
 * conditional logic for the question.
 * 
 * The class relies on any Data References that are used
 * in a form to be registered with the FormDefRMSUtility's
 * prototype factory in order to properly deserialize.
 * 
 * @author Drew Roos
 *
 */
public class DataBinding  implements Externalizable {
	private String id;
	private IDataReference ref;
	private int dataType;
	//private ... constraints;

	//do getters/setters later
	public Condition relevancyCondition;
	public Condition requiredCondition;
	public boolean requiredAbsolute;
	public Condition readonlyCondition;
	public boolean readonlyAbsolute;
	
	private String preload;
	private String preloadParams;
	
	public DataBinding () {
		requiredAbsolute = false;
		readonlyAbsolute = false;
	}
	
	/**
	 * @return The data reference
	 */
	public IDataReference getReference() {
		return ref;
	}
	
	/**
	 * @param ref the reference to set
	 */
	public void setReference(IDataReference ref) {
		this.ref = ref;
	}

	/**
	 * @return the id
	 */
	public String getId() {
		return id;
	}

	/**
	 * @param id the id to set
	 */
	public void setId(String id) {
		this.id = id;
	}

	/**
	 * @return the dataType
	 */
	public int getDataType() {
		return dataType;
	}

	/**
	 * @param dataType the dataType to set
	 */
	public void setDataType(int dataType) {
		this.dataType = dataType;
	}

	/**
	 * @return the preload
	 */
	public String getPreload() {
		return preload;
	}

	/**
	 * @param preload the preload to set
	 */
	public void setPreload(String preload) {
		this.preload = preload;
	}

	/**
	 * @return the preloadParams
	 */
	public String getPreloadParams() {
		return preloadParams;
	}

	/**
	 * @param preloadParams the preloadParams to set
	 */
	public void setPreloadParams(String preloadParams) {
		this.preloadParams = preloadParams;
	}

	/* (non-Javadoc)
	 * @see org.javarosa.core.services.storage.utilities.Externalizable#readExternal(java.io.DataInputStream)
	 */
	public void readExternal(DataInputStream in) throws IOException,
			InstantiationException, IllegalAccessException,
			UnavailableExternalizerException {
		this.setId(ExternalizableHelper.readUTF(in));
		this.setDataType(in.readInt());
		this.setPreload(ExternalizableHelper.readUTF(in));
		this.setPreloadParams(ExternalizableHelper.readUTF(in));

		String factoryName = in.readUTF();
		FormDefRMSUtility fdrms = (FormDefRMSUtility)JavaRosaServiceProvider.instance().getStorageManager().getRMSStorageProvider().getUtility(FormDefRMSUtility.getUtilityName());
		PrototypeFactory factory = fdrms.getQuestionElementsFactory();
		ref = (IDataReference)factory.getNewInstance(factoryName);
		if(ref == null) { 
			throw new UnavailableExternalizerException("A reference prototype could not be found to deserialize a " +
					"reference of the type " + factoryName + ". Please register a Prototype of this type before deserializing " +
					"the data reference " + this.getId());
		}
		ref.readExternal(in);
		
		//don't bother reading relevancy/required/readonly right now; they're only used during parse anyway		
		//this.setRequired(in.readBoolean());
		//condition = (Condition)ExternalizableHelper.readExternalizable(in, new Condition());
	}

	/* (non-Javadoc)
	 * @see org.javarosa.core.services.storage.utilities.Externalizable#writeExternal(java.io.DataOutputStream)
	 */
	public void writeExternal(DataOutputStream out) throws IOException {
		ExternalizableHelper.writeUTF(out, this.getId());
		out.writeInt(this.getDataType());
		ExternalizableHelper.writeUTF(out, this.getPreload());
		ExternalizableHelper.writeUTF(out, this.getPreloadParams());

		out.writeUTF(ref.getClass().getName());
		ref.writeExternal(out);

		//don't bother writing relevancy/required/readonly right now; they're only used during parse anyway
		//out.writeBoolean(this.isRequired());
		//ExternalizableHelper.writeExternalizable(condition, out);
	}
	
	
}

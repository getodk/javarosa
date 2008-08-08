package org.javarosa.core.model;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import org.javarosa.core.JavaRosaServiceProvider;
import org.javarosa.core.model.storage.FormDefRMSUtility;
import org.javarosa.core.model.utils.ExternalizableHelper;
import org.javarosa.core.model.utils.PrototypeFactory;
import org.javarosa.core.util.Externalizable;
import org.javarosa.core.util.UnavailableExternalizerException;

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
	
	public IDataReference getReference() {
		return ref;
	}
	
	public void setReference(IDataReference ref) {
		this.ref = ref;
	}
	
	public int getDataType() {
		return dataType;
	}
	
	public void setDataType(int dataType) {
		this.dataType = dataType;
	}
	
	public String getPreload() {
		return preload;
	}
	
	public void setPreload(String preload) {
		this.preload = preload;
	}
	
	public String getPreloadParams() {
		return preloadParams;
	}
	
	public void setPreloadParams(String preloadParams) {
		this.preloadParams = preloadParams;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
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

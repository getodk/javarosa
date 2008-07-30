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
	private Condition condition;
	private boolean required;
	
	private String preload;
	private String preloadParams;
	
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
	
	public Condition getCondition() {
		return condition;
	}
	
	public void setCondition(Condition condition) {
		this.condition = condition;
	}
	
	public boolean isRequired() {
		return required;
	}
	
	public void setRequired(boolean required) {
		this.required = required;
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
		this.setRequired(in.readBoolean());
		condition = (Condition)ExternalizableHelper.readExternalizable(in, new Condition());
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
	}

	/* (non-Javadoc)
	 * @see org.javarosa.core.services.storage.utilities.Externalizable#writeExternal(java.io.DataOutputStream)
	 */
	public void writeExternal(DataOutputStream out) throws IOException {
		ExternalizableHelper.writeUTF(out, this.getId());
		out.writeInt(this.getDataType());
		ExternalizableHelper.writeUTF(out, this.getPreload());
		ExternalizableHelper.writeUTF(out, this.getPreloadParams());
		out.writeBoolean(this.isRequired());
		ExternalizableHelper.writeExternalizable(condition, out);
		out.writeUTF(ref.getClass().getName());
		ref.writeExternal(out);
	}
	
	
}

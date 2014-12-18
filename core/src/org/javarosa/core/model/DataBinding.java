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

package org.javarosa.core.model;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.javarosa.core.model.condition.Condition;
import org.javarosa.core.model.condition.IConditionExpr;
import org.javarosa.core.model.condition.Recalculate;
import org.javarosa.core.model.instance.TreeElement;
import org.javarosa.core.util.externalizable.DeserializationException;
import org.javarosa.core.util.externalizable.ExtUtil;
import org.javarosa.core.util.externalizable.ExtWrapNullable;
import org.javarosa.core.util.externalizable.ExtWrapTagged;
import org.javarosa.core.util.externalizable.Externalizable;
import org.javarosa.core.util.externalizable.PrototypeFactory;

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

	public Condition relevancyCondition;
	public boolean relevantAbsolute;
	public Condition requiredCondition;
	public boolean requiredAbsolute;
	public Condition readonlyCondition;
	public boolean readonlyAbsolute;
	public IConditionExpr constraint;
	public Recalculate calculate;

	private String preload;
	private String preloadParams;
	public String constraintMessage;

	private List<TreeElement> additionalAttrs = new ArrayList<TreeElement>(0);

	public DataBinding () {
		relevantAbsolute = true;
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

	public void setAdditionalAttribute(String namespace, String name, String value) {
		TreeElement.setAttribute(null, additionalAttrs, namespace, name, value);
	}

	public List<TreeElement> getAdditionalAttributes() {
		return additionalAttrs;
	}

	/* (non-Javadoc)
	 * @see org.javarosa.core.services.storage.utilities.Externalizable#readExternal(java.io.DataInputStream)
	 */
	public void readExternal(DataInputStream in, PrototypeFactory pf) throws IOException, DeserializationException {
		setId((String)ExtUtil.read(in, new ExtWrapNullable(String.class), pf));
		setDataType(ExtUtil.readInt(in));
		setPreload((String)ExtUtil.read(in, new ExtWrapNullable(String.class), pf));
		setPreloadParams((String)ExtUtil.read(in, new ExtWrapNullable(String.class), pf));
		ref = (IDataReference)ExtUtil.read(in, new ExtWrapTagged());

		//don't bother reading relevancy/required/readonly/constraint/calculate/additionalAttrs right now; they're only used during parse anyway
	}

	/* (non-Javadoc)
	 * @see org.javarosa.core.services.storage.utilities.Externalizable#writeExternal(java.io.DataOutputStream)
	 */
	public void writeExternal(DataOutputStream out) throws IOException {
		ExtUtil.write(out, new ExtWrapNullable(getId()));
		ExtUtil.writeNumeric(out, getDataType());
		ExtUtil.write(out, new ExtWrapNullable(getPreload()));
		ExtUtil.write(out, new ExtWrapNullable(getPreloadParams()));
		ExtUtil.write(out, new ExtWrapTagged(ref));

		//don't bother writing relevancy/required/readonly/constraint/calculate/additionalAttrs right now; they're only used during parse anyway
	}


}

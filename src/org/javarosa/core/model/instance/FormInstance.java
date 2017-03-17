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

package org.javarosa.core.model.instance;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Date;
import java.util.HashMap;

import org.javarosa.core.model.FormDef;
import org.javarosa.core.model.data.IAnswerData;
import org.javarosa.core.model.instance.utils.ITreeVisitor;
import org.javarosa.core.model.utils.IInstanceVisitor;
import org.javarosa.core.services.storage.IMetaData;
import org.javarosa.core.services.storage.Persistable;
import org.javarosa.core.util.externalizable.DeserializationException;
import org.javarosa.core.util.externalizable.ExtUtil;
import org.javarosa.core.util.externalizable.ExtWrapMap;
import org.javarosa.core.util.externalizable.ExtWrapNullable;
import org.javarosa.core.util.externalizable.PrototypeFactory;


/**
 * This class represents the xform model instance
 */
public class FormInstance extends DataInstance<TreeElement> implements Persistable, IMetaData {

	public static final String STORAGE_KEY = "FORMDATA";

	/** The date that this model was taken and recorded */
	private Date dateSaved;

	public String schema;
	public String formVersion;
	public String uiVersion;

	private HashMap<String, Object> namespaces = new HashMap<String, Object>();

	/** The root of this tree */
	protected TreeElement root = new TreeElement();

	public FormInstance() {

	}

	public FormInstance(TreeElement root) {
		this(root, null);
	}

	/**
	 * Creates a new data model using the root given.
	 *
	 * @param root
	 *            The root of the tree for this data model.
	 */
	public FormInstance(TreeElement root, String id) {
		super(id);
		setID(-1);
		setFormId(-1);
		setRoot(root);
	}

	public TreeElement getBase() {
		return root;
	}

	public TreeElement getRoot() {

		if (root.getNumChildren() == 0)
			throw new RuntimeException("root node has no children");

		return root.getChildAt(0);
	}



	/**
	 * Sets the root element of this Model's tree
	 *
	 * @param topLevel
	 *            The root of the tree for this data model.
	 */
	public void setRoot(TreeElement topLevel) {
		root = new TreeElement();
		if(this.getName() != null) {
			root.setInstanceName(this.getName());
		}
		if (topLevel != null) {
			root.addChild(topLevel);
		}
	}

	public TreeReference copyNode(TreeReference from, TreeReference to) throws InvalidReferenceException {
		if (!from.isAbsolute()) {
			throw new InvalidReferenceException("Source reference must be absolute for copying", from);
		}

		TreeElement src = resolveReference(from);
		if (src == null) {
			throw new InvalidReferenceException("Null Source reference while attempting to copy node", from);
		}

		return copyNode(src, to).getRef();
	}

	// for making new repeat instances; 'from' and 'to' must be unambiguous
	// references EXCEPT 'to' may be ambiguous at its final step
	// return true is successfully copied, false otherwise
	public TreeElement copyNode(TreeElement src, TreeReference to) throws InvalidReferenceException {
		if (!to.isAbsolute())
			throw new InvalidReferenceException("Destination reference must be absolute for copying", to);

		// strip out dest node info and get dest parent
		String dstName = to.getNameLast();
		int dstMult = to.getMultLast();
		TreeReference toParent = to.getParentRef();

		TreeElement parent = resolveReference(toParent);
		if (parent == null) {
			throw new InvalidReferenceException("Null parent reference whle attempting to copy", toParent);
		}
		if (!parent.isChildable()) {
			throw new InvalidReferenceException("Invalid Parent Node: cannot accept children.", toParent);
		}

		if (dstMult == TreeReference.INDEX_UNBOUND) {
			dstMult = parent.getChildMultiplicity(dstName);
		} else if (parent.getChild(dstName, dstMult) != null) {
			throw new InvalidReferenceException("Destination already exists!", to);
		}

		TreeElement dest = src.deepCopy(false);
		dest.setName(dstName);
		dest.setMult(dstMult);
		parent.addChild(dest);
		return dest;
	}

	public Date getDateSaved() {
		return this.dateSaved;
	}

	public TreeReference addNode(TreeReference ambigRef) {
		TreeReference ref = ambigRef.clone();
		if (createNode(ref) != null) {
			return ref;
		} else {
			return null;
		}
	}

	public TreeReference addNode(TreeReference ambigRef, IAnswerData data, int dataType) {
		TreeReference ref = ambigRef.clone();
		TreeElement node = createNode(ref);
		if (node != null) {
			if (dataType >= 0) {
				node.setDataType(dataType);
			}

			node.setValue(data);
			return ref;
		} else {
			return null;
		}
	}

	/*
	 * create the specified node in the tree, creating all intermediary nodes at
	 * each step, if necessary. if specified node already exists, return null
	 *
	 * creating a duplicate node is only allowed at the final step. it will be
	 * done if the multiplicity of the last step is ALL or equal to the count of
	 * nodes already there
	 *
	 * at intermediate steps, the specified existing node is used; if
	 * multiplicity is ALL: if no nodes exist, a new one is created; if one node
	 * exists, it is used; if multiple nodes exist, it's an error
	 *
	 * return the newly-created node; modify ref so that it's an unambiguous ref
	 * to the node
	 */
	private TreeElement createNode(TreeReference ref) {

		TreeElement node = root;

		for (int k = 0; k < ref.size(); k++) {
			String name = ref.getName(k);
			int count = node.getChildMultiplicity(name);
			int mult = ref.getMultiplicity(k);

			TreeElement child;
			if (k < ref.size() - 1) {
				if (mult == TreeReference.INDEX_UNBOUND) {
					if (count > 1) {
						return null; // don't know which node to use
					} else {
						// will use existing (if one and only one) or create new
						mult = 0;
						ref.setMultiplicity(k, 0);
					}
				}

				// fetch
				child = node.getChild(name, mult);
				if (child == null) {
					if (mult == 0) {
						// create
						child = new TreeElement(name, count);
						node.addChild(child);
						ref.setMultiplicity(k, count);
					} else {
						return null; // intermediate node does not exist
					}
				}
			} else {
				if (mult == TreeReference.INDEX_UNBOUND || mult == count) {
					if (k == 0 && root.getNumChildren() != 0) {
						return null; // can only be one top-level node, and it
						// already exists
					}

					if (!node.isChildable()) {
						return null; // current node can't have children
					}

					// create new
					child = new TreeElement(name, count);
					node.addChild(child);
					ref.setMultiplicity(k, count);
				} else {
					return null; // final node must be a newly-created node
				}
			}

			node = child;
		}

		return node;
	}

	/* (non-Javadoc)
	 * @see org.javarosa.core.model.instance.FormInstanceAdapter#addNamespace(java.lang.String, java.lang.String)
	 */
	public void addNamespace(String prefix, String URI) {
		namespaces.put(prefix, URI);
	}

	/* (non-Javadoc)
	 * @see org.javarosa.core.model.instance.FormInstanceAdapter#getNamespacePrefixes()
	 */
	public String[] getNamespacePrefixes() {
		String[] prefixes = new String[namespaces.size()];
		int i = 0;
    for (String key : namespaces.keySet()) {
			prefixes[i] = key;
			++i;
		}
		return prefixes;
	}

	/* (non-Javadoc)
	 * @see org.javarosa.core.model.instance.FormInstanceAdapter#getNamespaceURI(java.lang.String)
	 */
	public String getNamespaceURI(String prefix) {
		return (String)namespaces.get(prefix);
	}

	public TreeElement processSaved(FormInstance template, FormDef f) {
		TreeElement fixedInstanceRoot = template.getRoot().deepCopy(true);
		TreeElement incomingRoot = root.getChildAt(0);

		if (!fixedInstanceRoot.getName().equals(incomingRoot.getName()) || incomingRoot.getMult() != 0) {
			throw new RuntimeException("Saved form instance to restore does not match form definition");
		}

		fixedInstanceRoot.populate(incomingRoot, f);
		return fixedInstanceRoot;
	}


	public FormInstance clone() {
		FormInstance cloned = new FormInstance(this.getRoot().deepCopy(true));

		cloned.setID(this.getID());
		cloned.setFormId(this.getFormId());
		cloned.setName(this.getName());
		cloned.setDateSaved(this.getDateSaved());
		cloned.schema = this.schema;
		cloned.formVersion = this.formVersion;
		cloned.uiVersion = this.uiVersion;
		cloned.namespaces = new HashMap<String, Object>();
    for (String key : namespaces.keySet()) {
			cloned.namespaces.put(key, this.namespaces.get(key));
		}

		return cloned;
	}

	public void readExternal(DataInputStream in, PrototypeFactory pf) throws IOException, DeserializationException {
		super.readExternal(in, pf);
		schema = (String) ExtUtil.read(in, new ExtWrapNullable(String.class), pf);
		dateSaved = (Date) ExtUtil.read(in, new ExtWrapNullable(Date.class), pf);

		namespaces = (HashMap<String,Object>)ExtUtil.read(in, new ExtWrapMap(String.class, String.class));
		setRoot((TreeElement) ExtUtil.read(in, TreeElement.class, pf));

	}

	public void writeExternal(DataOutputStream out) throws IOException {
		super.writeExternal(out);
		ExtUtil.write(out, new ExtWrapNullable(schema));
		ExtUtil.write(out, new ExtWrapNullable(dateSaved));
		ExtUtil.write(out, new ExtWrapMap(namespaces));

		ExtUtil.write(out, getRoot());
	}


	public void setDateSaved(Date dateSaved) {
		this.dateSaved = dateSaved;
	}

	public void copyItemsetNode(TreeElement copyNode, TreeReference destRef, FormDef f)
			throws InvalidReferenceException {
		TreeElement templateNode = getTemplate(destRef);
		TreeElement newNode = copyNode(templateNode, destRef);
		newNode.populateTemplate(copyNode, f);
	}

	public void accept(IInstanceVisitor visitor) {
		visitor.visit(this);

		if (visitor instanceof ITreeVisitor) {
			root.accept((ITreeVisitor) visitor);
		}

	}


	// determine if nodes are homogeneous, meaning their descendant structure is 'identical' for repeat purposes
	// identical means all children match, and the children's children match, and so on
	// repeatable children are ignored; as they do not have to exist in the same quantity for nodes to be homogeneous
	// however, the child repeatable nodes MUST be verified amongst themselves for homogeneity later
	// this function ignores the names of the two nodes
	public static boolean isHomogeneous(TreeElement a, TreeElement b) {
		if (a.isLeaf() && b.isLeaf()) {
			return true;
		} else if (a.isChildable() && b.isChildable()) {
			// verify that every (non-repeatable) node in a exists in b and vice
			// versa
			for (int k = 0; k < 2; k++) {
				TreeElement n1 = (k == 0 ? a : b);
				TreeElement n2 = (k == 0 ? b : a);

				for (int i = 0; i < n1.getNumChildren(); i++) {
					TreeElement child1 = n1.getChildAt(i);
					if (child1.isRepeatable())
						continue;
					TreeElement child2 = n2.getChild(child1.getName(), 0);
					if (child2 == null)
						return false;
					if (child2.isRepeatable())
						throw new RuntimeException("shouldn't happen");
				}
			}

			// compare children
			for (int i = 0; i < a.getNumChildren(); i++) {
				TreeElement childA = a.getChildAt(i);
				if (childA.isRepeatable())
					continue;
				TreeElement childB = b.getChild(childA.getName(), 0);
				if (!isHomogeneous(childA, childB))
					return false;
			}

			return true;
		} else {
			return false;
		}
	}

	public void initialize(InstanceInitializationFactory initializer, String instanceId) {
		setInstanceId(instanceId);
		root.setInstanceName(instanceId);
	}

	public static final String META_XMLNS = "XMLNS";
	public static final String META_ID = "instance_id";

	public String[] getMetaDataFields() {
		return new String[] { META_XMLNS, META_ID };
	}

	public HashMap<String,Object> getMetaData() {
		HashMap<String,Object> data = new HashMap<String,Object>();
		for(String key : getMetaDataFields()) {
			data.put(key, getMetaData(key));
		}
		return data;
	}

	public Object getMetaData(String fieldName) {
		if(META_XMLNS.equals(fieldName)) {
			return ExtUtil.emptyIfNull(schema);
		} else if(META_ID.equals(fieldName)) {
			return ExtUtil.emptyIfNull(this.getInstanceId());
		}
		throw new IllegalArgumentException("No metadata field " + fieldName  + " in the form instance storage system");
	}
}

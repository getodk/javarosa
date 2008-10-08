package org.javarosa.core.model;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Enumeration;
import java.util.Vector;

import org.javarosa.core.JavaRosaServiceProvider;
import org.javarosa.core.model.data.IAnswerData;
import org.javarosa.core.model.storage.FormDefRMSUtility;
import org.javarosa.core.model.utils.Localizable;
import org.javarosa.core.model.utils.Localizer;
import org.javarosa.core.util.OrderedHashtable;
import org.javarosa.core.util.externalizable.DeserializationException;
import org.javarosa.core.util.externalizable.ExtUtil;
import org.javarosa.core.util.externalizable.ExtWrapList;
import org.javarosa.core.util.externalizable.ExtWrapMap;
import org.javarosa.core.util.externalizable.ExtWrapNullable;
import org.javarosa.core.util.externalizable.ExtWrapTagged;
import org.javarosa.core.util.externalizable.ExternalizableHelperDeprecated;
import org.javarosa.core.util.externalizable.PrototypeFactory;
import org.javarosa.core.util.externalizable.PrototypeFactoryDeprecated;

/** 
 * The definition of a Question to be presented to users when
 * filling out a form.
 * 
 * QuestionDef requires that any IDataReferences that are used
 * are contained in the FormDefRMS's PrototypeFactoryDeprecated in order
 * to be properly deserialized. If they aren't, an exception
 * will be thrown at the time of deserialization. 
 * 
 * @author Daniel Kayiwa/Drew Roos
 *
 */
public class QuestionDef implements IFormElement, Localizable {
	private int id;
	private String name;
	private IDataReference binding;	/** reference to a location in the model to store data in */
	
	private int dataType;  	 /* The type of question. eg Numeric,Date,Text etc. */
	private int controlType;  /* The type of widget. eg TextInput,Slider,List etc. */
	private String appearanceAttr;
	
	private String longText;	 /* The prompt text. The text the user sees. */
	private String longTextID;
	private String shortText;	 /* The prompt text. The text the user sees in short modes. */
	private String shortTextID;
	private String helpText;	 /* The help text. */
	private String helpTextID;

	private OrderedHashtable selectItems;  	/** String -> String */
	private OrderedHashtable selectItemIDs;	/** String -> String */
	private Vector selectItemsLocalizable;
	
	private boolean required; 	/** A flag to tell whether the question is to be answered or is optional. */
	//constraints?
	
	private boolean visible;	/** A flag to tell whether the question should be shown or not. */
	private boolean enabled;	/** A flag to tell whether the question should be enabled or disabled. */
	private boolean locked; 	/** A flag to tell whether a question is to be locked or not. A locked question is one which is visible, enabled, but cannot be edited. */

	private IAnswerData defaultValue;	/** this shouldn't be used for default values that are already pre-loaded in the instance */
		
	Vector observers;
	
	public QuestionDef () {
		this(Constants.NULL_ID, null, Constants.DATATYPE_TEXT, Constants.DATATYPE_TEXT);
	}
	
	public QuestionDef (int id, String name, int dataType, int controlType) {
		setID(id);
		setName(name);
		setDataType(dataType);
		setControlType(controlType);
		required = false;
		visible = true;
		enabled = true;
		locked = false;
		observers = new Vector();
	}
		
	public boolean equals (Object o) {
		if (o instanceof QuestionDef) {
			QuestionDef q = (QuestionDef)o;
			return (id == q.id &&
					ExtUtil.equals(name, q.name) &&
					ExtUtil.equals(binding, q.binding) &&
					dataType == q.dataType &&
					controlType == q.controlType &&
					ExtUtil.equals(appearanceAttr, q.appearanceAttr) &&
					ExtUtil.equals(longText, q.longText) &&
					ExtUtil.equals(longTextID, q.longTextID) &&
					ExtUtil.equals(shortText, q.shortText) &&
					ExtUtil.equals(shortTextID, q.shortTextID) &&
					ExtUtil.equals(helpText, q.helpText) &&
					ExtUtil.equals(helpTextID, q.helpTextID) &&
					ExtUtil.equals(ExtUtil.nullIfEmpty(selectItemIDs), ExtUtil.nullIfEmpty(q.selectItemIDs)) &&
					ExtUtil.equals(ExtUtil.nullIfEmpty(selectItemsLocalizable), ExtUtil.nullIfEmpty(q.selectItemsLocalizable)) &&
					required == q.required &&
					visible == q.visible &&
					enabled == q.enabled &&
					locked == q.locked);
				//no defaultValue, selectItems
		} else {
			return false;
		}
	}
	
	public int getID () {
		return id;
	}
	
	public void setID (int id) {
		this.id = id;
	}
	
	public String getName() {
		return name;
	}
	
	public void setName(String name) {
		this.name = name;
	}

	public IDataReference getBind() {
		return binding;
	}
	
	public void setBind(IDataReference binding) {
		this.binding = binding;
	}
	
	public int getDataType() {
		return dataType;
	}
	
	public void setDataType(int dataType) {
		this.dataType = dataType;
	}
	
	public int getControlType() {
		return controlType;
	}
	
	public void setControlType(int controlType) {
		this.controlType = controlType;
	}

	public String getAppearanceAttr () {
		return appearanceAttr;
	}
	
	public void setAppearanceAttr (String appearanceAttr) {
		this.appearanceAttr = appearanceAttr;
	}	
	
	public String getLongText () {
		return longText;
	}
	
	public void setLongText (String longText) {
		this.longText = longText;
	}

    public String getLongTextID () {
        return longTextID;
    }
    
    public void setLongTextID (String textID, Localizer localizer) {
    	this.longTextID = textID;
    	if (localizer != null) {
    		longText = localizer.getLocalizedText(longTextID);
    	}
    }
	
	public String getShortText () {
		return shortText;
	}
	
	public void setShortText (String shortText) {
		this.shortText = shortText;
	}

    public String getShortTextID () {
        return shortTextID;
    }

    public void setShortTextID (String textID, Localizer localizer) {
    	this.shortTextID = textID;
    	if (localizer != null) {
    		shortText = localizer.getLocalizedText(shortTextID);        
    	}
    } 

	public String getHelpText () {
		return helpText;
	}
	
	public void setHelpText (String helpText) {
		this.helpText = helpText;
	}

    public String getHelpTextID () {
        return helpTextID;
    }
    
    public void setHelpTextID (String textID, Localizer localizer) {
        this.helpTextID = textID;
        if (localizer != null) {
            helpText = localizer.getLocalizedText(helpTextID);
        }
    }

	public OrderedHashtable getSelectItems () {
		return selectItems;
	}

	//this function is dangerous: QuestionDef will not serialize properly unless selectItemIDs is set as well
	public void setSelectItems (OrderedHashtable selectItems) {
		this.selectItems = selectItems;
	}
	
	//this function is dangerous: QuestionDef will not serialize properly unless selectItemIDs is set as well
	public void addSelectItem (String label, String value) {
		if (selectItems == null)
			selectItems = new OrderedHashtable();
		selectItems.put(label, value);
	}
	
	public OrderedHashtable getSelectItemIDs () {
		return selectItemIDs;
	}
	
	public Vector getSelectItemsLocalizable () {
		return selectItemsLocalizable;
	}
	
	public void setSelectItemIDs (OrderedHashtable selectItemIDs, Vector selectItemsLocalizable, Localizer localizer) {
		this.selectItemIDs = selectItemIDs;
		this.selectItemsLocalizable = selectItemsLocalizable;
		if(localizer != null) {
			localizeSelectMap(localizer);
		}
	}
	
	public void addSelectItemID (String labelID, boolean type, String value) {
		if (selectItemIDs == null) {
			selectItemIDs = new OrderedHashtable();
			selectItemsLocalizable = new Vector();
		}
		selectItemIDs.put(labelID, value);
		selectItemsLocalizable.addElement(new Boolean(type));
	}
	
	//calling when localizer == null is meant for when there is no localization data and selectIDMap contains only
	//fixed strings (trans is always false)
	public void localizeSelectMap (Localizer localizer) {
		selectItems = null;
		
		String label;
		for (int i = 0; i < selectItemIDs.size(); i++) {
			String key = (String)selectItemIDs.keyAt(i);
			boolean translate = ((Boolean)selectItemsLocalizable.elementAt(i)).booleanValue();
			if (translate) {
				label = (localizer == null ? "[itext]" : localizer.getLocalizedText(key));
			} else {
				label = key;
			}
			addSelectItem(label, (String)selectItemIDs.get(key));
		}
	}

	public boolean isRequired() {
		return required;
	}
	
	public void setRequired(boolean required) {
		if (this.required != required) {		
			this.required = required;
	    	alertStateObservers(QuestionStateListener.CHANGE_REQUIRED);
		}
	}

	public boolean isVisible() {
		return visible;
	}
	
	public void setVisible(boolean visible) {
		if (this.visible != visible) {		
			this.visible = visible;
	    	alertStateObservers(QuestionStateListener.CHANGE_VISIBLE);
		}
	}
	
	public boolean isEnabled() {
		return enabled;
	}
	
	public void setEnabled(boolean enabled) {
		if (this.enabled != enabled) {		
			this.enabled = enabled;
	    	alertStateObservers(QuestionStateListener.CHANGE_ENABLED);
		}
	}
	
	public boolean isLocked() {
		return locked;
	}
	
	public void setLocked(boolean locked) {
		if (this.locked != locked) {		
			this.locked = locked;
	    	alertStateObservers(QuestionStateListener.CHANGE_LOCKED);
		}
	}
		
	public IAnswerData getDefaultValue() {
		return defaultValue;
	}
	
	public void setDefaultValue(IAnswerData defaultValue) {
		this.defaultValue = defaultValue;
	}

    public void localeChanged(String locale, Localizer localizer) {
    	if(longTextID != null) {
    		longText = localizer.getLocalizedText(longTextID);
    	}

    	if(shortTextID != null) {
    		shortText = localizer.getLocalizedText(shortTextID);
    	}

    	if(helpTextID != null) {
    		helpText = localizer.getLocalizedText(helpTextID);
    	}
    	
    	if (selectItemIDs != null) {
    		localizeSelectMap(localizer);
    	}
    	
    	alertStateObservers(QuestionStateListener.CHANGE_LOCALE);
    }
	
	public Vector getChildren () {
		return null;
	}
	
	public void setChildren (Vector v) {
		throw new IllegalStateException();
	}
	
	public void addChild (IFormElement fe) {
		throw new IllegalStateException();
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.javarosa.core.util.Externalizable#readExternal(java.io.DataInputStream)
	 */
	public void readExternal(DataInputStream dis, PrototypeFactory pf) throws IOException, DeserializationException {
		setID(ExtUtil.readInt(dis));
		setName((String)ExtUtil.read(dis, new ExtWrapNullable(String.class)));
		setAppearanceAttr((String)ExtUtil.read(dis, new ExtWrapNullable(String.class)));
		setLongText((String)ExtUtil.read(dis, new ExtWrapNullable(String.class)));
		setShortText((String)ExtUtil.read(dis, new ExtWrapNullable(String.class)));
		setHelpText((String)ExtUtil.read(dis, new ExtWrapNullable(String.class)));
		setLongTextID((String)ExtUtil.read(dis, new ExtWrapNullable(String.class)), null);
		setShortTextID((String)ExtUtil.read(dis, new ExtWrapNullable(String.class)), null);
		setHelpTextID((String)ExtUtil.read(dis, new ExtWrapNullable(String.class)), null);

		setDataType(ExtUtil.readInt(dis));
		setControlType(ExtUtil.readInt(dis));

		setRequired(ExtUtil.readBool(dis));
		setVisible(ExtUtil.readBool(dis));
		setEnabled(ExtUtil.readBool(dis));
		setLocked(ExtUtil.readBool(dis));

		setSelectItemIDs(
				(OrderedHashtable)ExtUtil.nullIfEmpty((OrderedHashtable)ExtUtil.read(dis, new ExtWrapMap(String.class, String.class, true))),
				ExtUtil.nullIfEmpty((Vector)ExtUtil.read(dis, new ExtWrapList(Boolean.class))),
				null);
		if (getSelectItemIDs() != null && (controlType == Constants.CONTROL_SELECT_MULTI || controlType == Constants.CONTROL_SELECT_ONE)) {
			localizeSelectMap(null); //even for non-multilingual forms, text must be initially 'localized'
		}

		binding = (IDataReference)ExtUtil.read(dis, new ExtWrapNullable(new ExtWrapTagged()));
	}

	/*
	 * (non-Javadoc)
	 * @see org.javarosa.core.util.Externalizable#writeExternal(java.io.DataOutputStream)
	 */
	public void writeExternal(DataOutputStream dos) throws IOException {
		ExtUtil.writeNumeric(dos, getID());
		ExtUtil.write(dos, new ExtWrapNullable(getName()));
		ExtUtil.write(dos, new ExtWrapNullable(getAppearanceAttr()));
		ExtUtil.write(dos, new ExtWrapNullable(getLongText()));
		ExtUtil.write(dos, new ExtWrapNullable(getShortText()));
		ExtUtil.write(dos, new ExtWrapNullable(getHelpText()));
		ExtUtil.write(dos, new ExtWrapNullable(getLongTextID()));
		ExtUtil.write(dos, new ExtWrapNullable(getShortTextID()));
		ExtUtil.write(dos, new ExtWrapNullable(getHelpTextID()));
				
		ExtUtil.writeNumeric(dos, getDataType());
		ExtUtil.writeNumeric(dos, getControlType());
		
		ExtUtil.writeBool(dos, isRequired());
		ExtUtil.writeBool(dos, isVisible());
		ExtUtil.writeBool(dos, isEnabled());
		ExtUtil.writeBool(dos, isLocked());
		
		//selectItems should not be serialized
		ExtUtil.write(dos, new ExtWrapMap(ExtUtil.emptyIfNull(getSelectItemIDs())));
		ExtUtil.write(dos, new ExtWrapList(ExtUtil.emptyIfNull(selectItemsLocalizable)));

		ExtUtil.write(dos, new ExtWrapNullable(binding == null ? null : new ExtWrapTagged(binding)));
	}

	/* === MANAGING OBSERVERS === */
	
	public void registerStateObserver (QuestionStateListener qsl) {
		if (!observers.contains(qsl)) {
			observers.addElement(qsl);
		}
	}
	
	public void unregisterStateObserver (QuestionStateListener qsl) {
		observers.removeElement(qsl);
	}
	
	public void unregisterAll () {
		observers.removeAllElements();
	}
	
	public void alertStateObservers (int changeFlags) {
		for (Enumeration e = observers.elements(); e.hasMoreElements(); )
			((QuestionStateListener)e.nextElement()).questionStateChanged(this, changeFlags);
	}
	
}
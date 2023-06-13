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

import org.javarosa.core.model.actions.ActionController;
import org.javarosa.core.model.instance.TreeElement;
import org.javarosa.core.model.osm.OSMTag;
import org.javarosa.core.services.locale.Localizable;
import org.javarosa.core.services.locale.Localizer;
import org.javarosa.core.util.externalizable.DeserializationException;
import org.javarosa.core.util.externalizable.ExtUtil;
import org.javarosa.core.util.externalizable.ExtWrapList;
import org.javarosa.core.util.externalizable.ExtWrapNullable;
import org.javarosa.core.util.externalizable.ExtWrapTagged;
import org.javarosa.core.util.externalizable.PrototypeFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.javarosa.core.model.IFormElement.stringContains;


/**
 * The definition of a Question to be presented to users when
 * filling out a form.
 *
 * QuestionDef requires that any IDataReferences that are used
 * are registered with PrototypeManager in order
 * to be properly deserialized. If they aren't, an exception
 * will be thrown at the time of deserialization.
 *
 * @author Daniel Kayiwa/Drew Roos
 *
 */
public class QuestionDef implements IFormElement, Localizable {
    private static final Logger logger = LoggerFactory.getLogger(QuestionDef.class);

    private int id;
    private IDataReference binding;    /** reference to a location in the model to store data in */

    private int controlType;  /* The type of widget. eg TextInput,Slider,List etc. */
    private List<OSMTag> osmTags; // If it's an OSM Question, it might have tags.
    private String appearanceAttr;
    private String helpTextID;
    private String labelInnerText;
    private String helpText;
    private String textID; /* The id (ref) pointing to the localized values of (pic-URIs,audio-URIs,text) */
    private String helpInnerText;

    private List<TreeElement> additionalAttributes = new ArrayList<>(0);

    private List<SelectChoice> choices;
    private ItemsetBinding dynamicChoices;

    List<FormElementStateListener> observers;

    private ActionController actionController;

    public QuestionDef () {
        this(Constants.NULL_ID, Constants.DATATYPE_TEXT);
    }

    public QuestionDef (int id, int controlType) {
        setID(id);
        setControlType(controlType);
        observers = new ArrayList<>(0);
        actionController = new ActionController();
    }

    @Override
    public int getID () {
        return id;
    }

    @Override
    public void setID (int id) {
        this.id = id;
    }

    @Override
    public IDataReference getBind() {
        return binding;
    }

    public void setBind(IDataReference binding) {
        this.binding = binding;
    }

    public int getControlType() {
        return controlType;
    }

    public void setControlType(int controlType) {
        this.controlType = controlType;
    }

    public void setOsmTags(List<OSMTag> tags) {
        osmTags = tags;
    }

    @Override
    public String getAppearanceAttr () {
        return appearanceAttr;
    }

    @Override
    public void setAppearanceAttr (String appearanceAttr) {
        this.appearanceAttr = appearanceAttr;
    }

    /**
     * Only if there is no localizable version of the &lt;hint&gt; available should this method be used
     */
    public String getHelpText () {
        return helpText;
    }

    /**
     * Only if there is no localizable version of the &lthint&gtavailable should this method be used
     */
    public void setHelpText (String helpText) {
        this.helpText = helpText;
    }


    @Override
    public void setAdditionalAttribute(String namespace, String name, String value) {
        TreeElement.setAttribute(null, additionalAttributes, namespace, name, value);
    }

    @Override
    public String getAdditionalAttribute(String namespace, String name) {
        TreeElement e = TreeElement.getAttribute(additionalAttributes, namespace, name);
        if ( e != null ) {
            return e.getAttributeValue();
        }
        return null;
    }

    @Override
    public List<TreeElement> getAdditionalAttributes() {
        return additionalAttributes;
    }

    @Override
    public ActionController getActionController() {
        return actionController;
    }

    public String getHelpTextID () {
        return helpTextID;
    }

    public void setHelpTextID (String textID) {
        this.helpTextID = textID;

    }


    public void addSelectChoice (SelectChoice choice) {
        if (choices == null) {
            choices = new ArrayList<>(1);
        }
        choice.setIndex(choices.size());
        choices.add(choice);
    }

    public void removeSelectChoice(SelectChoice choice){
        if(choices == null) {
            choice.setIndex(0);
            return;
        }

        choices.remove(choice);
    }

    public List<SelectChoice> getChoices () {
        return choices;
    }

    public SelectChoice getChoice (int i) {
        return choices.get(i);
    }

    public int getNumChoices () {
        return (choices != null ? choices.size() : 0);
    }

    public SelectChoice getChoiceForValue (String value) {
        for (int i = 0; i < getNumChoices(); i++) {
            if (getChoice(i).getValue().equals(value)) {
                return getChoice(i);
            }
        }
        return null;
    }

    public ItemsetBinding getDynamicChoices () {
        return dynamicChoices;
    }

    public void setDynamicChoices (ItemsetBinding ib) {
        // the call to dynamicChoices.setDestRef(this)
        //
        // is now done later in the load sequence, within:
        //
        // dynamicChoices.initReferences(QuestionDef q)
        this.dynamicChoices = ib;
    }

    /**
     * true if the answer to this question yields xml tree data, not a simple string value
     */
    public boolean isComplex () {
        return (dynamicChoices != null && dynamicChoices.copyMode);
    }

    //Deprecated
    @Override
    public void localeChanged(String locale, Localizer localizer) {
        if (choices != null) {
            for (SelectChoice choice : choices) {
                choice.localeChanged(null, localizer);
            }
        }

        if (dynamicChoices != null) {
            dynamicChoices.localeChanged(locale, localizer);
        }

        alertStateObservers(FormElementStateListener.CHANGE_LOCALE);
    }

    @Override
    public List<IFormElement> getChildren () {
        return null;
    }

    @Override
    public void setChildren (List<IFormElement> v) {
        throw new IllegalStateException("Can't set children on question def");
    }

    @Override
    public void addChild (IFormElement fe) {
        throw new IllegalStateException("Can't add children to question def");
    }

    @Override
    public IFormElement getChild (int i) {
        return null;
    }

    @Override
    public void readExternal(DataInputStream dis, PrototypeFactory pf) throws IOException, DeserializationException {
        try {
            setID(ExtUtil.readInt(dis));
            binding = (IDataReference)ExtUtil.read(dis, new ExtWrapNullable(new ExtWrapTagged()), pf);
            setAppearanceAttr((String)ExtUtil.read(dis, new ExtWrapNullable(String.class), pf));
            setTextID((String)ExtUtil.read(dis, new ExtWrapNullable(String.class), pf));
            setLabelInnerText((String)ExtUtil.read(dis, new ExtWrapNullable(String.class), pf));
            setHelpText((String)ExtUtil.read(dis, new ExtWrapNullable(String.class), pf));
            setHelpTextID((String)ExtUtil.read(dis, new ExtWrapNullable(String.class), pf));
            setHelpInnerText((String)ExtUtil.read(dis, new ExtWrapNullable(String.class), pf));

            setControlType(ExtUtil.readInt(dis));

            additionalAttributes = ExtUtil.readAttributes(dis, null);

            choices = (List<SelectChoice>) ExtUtil.nullIfEmpty((List<SelectChoice>)ExtUtil.read(dis, new ExtWrapList(SelectChoice.class), pf));
            for (int i = 0; i < getNumChoices(); i++) {
                choices.get(i).setIndex(i);
            }
            setDynamicChoices((ItemsetBinding)ExtUtil.read(dis, new ExtWrapNullable(ItemsetBinding.class)));
            actionController = (ActionController) ExtUtil.read(dis, new ExtWrapNullable(ActionController.class), pf);

            osmTags = (List<OSMTag>) ExtUtil.nullIfEmpty((List<OSMTag>)ExtUtil.read(dis, new ExtWrapList(OSMTag.class), pf));
        } catch ( OutOfMemoryError e ) {
            throw new DeserializationException("serialization format change caused misalignment and out-of-memory error");
        }
    }

    @Override
    public void writeExternal(DataOutputStream dos) throws IOException {
        ExtUtil.writeNumeric(dos, getID());
        ExtUtil.write(dos, new ExtWrapNullable(binding == null ? null : new ExtWrapTagged(binding)));
        ExtUtil.write(dos, new ExtWrapNullable(getAppearanceAttr()));
        ExtUtil.write(dos, new ExtWrapNullable(getTextID()));
        ExtUtil.write(dos, new ExtWrapNullable(getLabelInnerText()));
        ExtUtil.write(dos, new ExtWrapNullable(getHelpText()));
        ExtUtil.write(dos, new ExtWrapNullable(getHelpTextID()));
        ExtUtil.write(dos, new ExtWrapNullable(getHelpInnerText()));

        ExtUtil.writeNumeric(dos, getControlType());

        ExtUtil.writeAttributes(dos, additionalAttributes);

        ExtUtil.write(dos, new ExtWrapList(ExtUtil.emptyIfNull(choices)));
        ExtUtil.write(dos, new ExtWrapNullable(dynamicChoices));
        ExtUtil.write(dos, new ExtWrapNullable(actionController));

        ExtUtil.write(dos, new ExtWrapList(ExtUtil.emptyIfNull(osmTags)));
    }

    /* === MANAGING OBSERVERS === */

    @Override
    public void registerStateObserver (FormElementStateListener qsl) {
        if (!observers.contains(qsl)) {
            observers.add(qsl);
        }
    }

    @Override
    public void unregisterStateObserver (FormElementStateListener qsl) {
        observers.remove(qsl);
    }

    public void unregisterAll () {
        observers.clear();
    }

    public void alertStateObservers (int changeFlags) {
      for (FormElementStateListener observer : observers) {
         observer.formElementStateChanged(this, changeFlags);
      }
    }

    @Override
    public int getDeepChildCount() {
        return 1;
    }

    public void setLabelInnerText(String labelInnerText) {
        this.labelInnerText = labelInnerText;
    }

    @Override
    public String getLabelInnerText() {
        return labelInnerText;
    }

    public void setHelpInnerText(String helpInnerText) {
        this.helpInnerText = helpInnerText;
    }

    public String getHelpInnerText() {
        return helpInnerText;
    }

    @Override
    public String getTextID() {
        return textID;
    }

    @Override
    public void setTextID(String textID) {
        if(stringContains(textID,";")){
            logger.warn("TextID contains ;form modifier:: \"{}\"... will be stripped.", textID.substring(textID.indexOf(";")));
            textID=textID.substring(0, textID.indexOf(";")); //trim away the form specifier
        }
        this.textID = textID;
    }
}

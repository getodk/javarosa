package org.javarosa.core.model;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import org.javarosa.core.model.data.helper.Selection;
import org.javarosa.core.model.instance.TreeElement;
import org.javarosa.core.services.locale.Localizable;
import org.javarosa.core.services.locale.Localizer;
import org.javarosa.core.util.externalizable.DeserializationException;
import org.javarosa.core.util.externalizable.ExtUtil;
import org.javarosa.core.util.externalizable.Externalizable;
import org.javarosa.core.util.externalizable.PrototypeFactory;
import org.javarosa.xform.parse.XFormParseException;

public class SelectChoice implements Externalizable, Localizable {
    /** The literal value from the child that is used as a value. For inline selects ("static"), this child always has
     * the name "value". For selects from itemsets ("dynamic"), the child node to use is specified in the form definition
     * (e.g. country_code)
     */
    private String value;

    /**
     * If this choice is from a non-localizable item, the literal value from the child that is used as a label.
     * For inline selects ("static"), this child always has the name "label". For selects from itemsets ("dynamic"), the
     * child node to use is specified in the form definition (e.g. the_human_friendly_name)
     */
    private String labelInnerText;

    /**
     * If this choice is from a localizable item, the literal text from the child that is used as a label. This will be
     * used to look up the localized label based on the current language.
     */
    private String textID;

    private boolean isLocalizable;

    private int index = -1;

    /**
     * if this choice represents part of an <itemset>, and the itemset uses
     * 'copy' answer mode, this points to the node to be copied if this
     * selection is chosen this field only has meaning for dynamic choices, thus
     * is unserialized
     */
    public TreeElement copyNode;

    /**
     * For selects from itemsets ("dynamic"), the node that this choice represents. Not serialized.
     */
    private TreeElement item;

    /**
     * for deserialization only
     */
    public SelectChoice() {

    }

    public SelectChoice(String labelID, String value) {
        this(labelID, null, value, true, null);
    }

    public SelectChoice(String labelID, String labelInnerText, boolean isLocalizable) {
        this(labelID, labelInnerText, isLocalizable, null);
    }

    public SelectChoice(String labelID, String labelInnerText, String value, boolean isLocalizable) {
        this(labelID, labelInnerText, value, isLocalizable, null);
    }

    public SelectChoice(String labelOrID, String Value, boolean isLocalizable, TreeElement item) {
        this(isLocalizable ? labelOrID : null,
            isLocalizable ? null : labelOrID,
            Value, isLocalizable, item);
    }

    /**
     * @param labelID        can be null
     * @param labelInnerText can be null
     * @param value          should not be null
     * @throws XFormParseException if value is null
     */
    public SelectChoice(String labelID, String labelInnerText, String value, boolean isLocalizable, TreeElement item) {
        this.isLocalizable = isLocalizable;
        this.textID = labelID;
        this.labelInnerText = labelInnerText;
        this.item = item;
        if (value != null) {
            this.value = value;
        } else {
            throw new XFormParseException("SelectChoice{id,innerText}:{" + labelID + "," + labelInnerText + "}, has null Value!");
        }
    }

    public void setIndex(int index) {
        this.index = index;
    }

    public String getLabelInnerText() {
        return labelInnerText;
    }

    public String getValue() {
        return value;
    }

    public String getChild(String childName) {
        if (item != null) {
            TreeElement child = item.getChild(childName, 0);
            if (child != null) {
                return child.getValue().getDisplayText();
            }
        }
        return null;
    }

    public int getIndex() {
        if (index == -1) {
            throw new RuntimeException("trying to access choice index before it has been set!");
        }

        return index;
    }


    public void localeChanged(String locale, Localizer localizer) {
        // TODO Review this commented block
        // if (captionLocalizable) {
        //     caption = localizer.getLocalizedText(captionID);
        // }
    }

    public void readExternal(DataInputStream in, PrototypeFactory pf) throws IOException, DeserializationException {
        isLocalizable = ExtUtil.readBool(in);
        setLabelInnerText(ExtUtil.nullIfEmpty(ExtUtil.readString(in)));
        setTextID(ExtUtil.nullIfEmpty(ExtUtil.readString(in)));
        value = ExtUtil.readString(in);
        //index will be set by questiondef
    }

    public void writeExternal(DataOutputStream out) throws IOException {
        ExtUtil.writeBool(out, isLocalizable);
        ExtUtil.writeString(out, ExtUtil.emptyIfNull(labelInnerText));
        ExtUtil.writeString(out, ExtUtil.emptyIfNull(textID));
        ExtUtil.writeString(out, ExtUtil.emptyIfNull(value));
        //don't serialize index; it will be restored from questiondef
    }

    private void setLabelInnerText(String labelInnerText) {
        this.labelInnerText = labelInnerText;
    }

    public Selection selection() {
        return new Selection(this);
    }

    public boolean isLocalizable() {
        return isLocalizable;
    }

    public void setLocalizable(boolean localizable) {
        this.isLocalizable = localizable;
    }

    public String toString() {
        return ((textID != null && textID.length() > 0) ? "{" + textID + "}" : "") + (labelInnerText != null ? labelInnerText : "") + " => " + value;
    }

    public String getTextID() {
        return textID;
    }

    public void setTextID(String textID) {
        this.textID = textID;
    }
}

package org.celllife.clforms;

import org.celllife.clforms.api.IForm;
import org.celllife.clforms.api.ResponseEvent;
import org.celllife.clforms.storage.RMSManager;
import org.celllife.clforms.view.FormView;
import org.celllife.clforms.view.IPrompter;

public interface IController {

    public static final String XFORM_RMS = "XFORM_RMS_NEW";

    public static final String MODEL_RMS = "MODEL_RMS_NEW";

    public abstract void cancelForm();

    public abstract void registerPrompter();

    public abstract void completeForm();

    /**
     * @param formId
     */
    public abstract void loadForm(int recordId);

    public abstract void deleteForm(int recordId);

    public abstract void deleteModel(int recordId);

    public abstract void processEvent(ResponseEvent event);

    public abstract void saveFormModel();

    public abstract void updateModel();

    public abstract IForm getForm();

    public abstract void setForm(IForm form);

    public abstract IPrompter getPrompter();

    public abstract void setPrompter(IPrompter prompter);

    public abstract FormView getFormview();

    public abstract void setFormview(FormView formview);

    public abstract RMSManager getRMSManager();

}
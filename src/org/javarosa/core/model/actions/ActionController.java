package org.javarosa.core.model.actions;


import org.javarosa.core.model.FormDef;
import org.javarosa.core.model.instance.TreeReference;
import org.javarosa.core.util.externalizable.DeserializationException;
import org.javarosa.core.util.externalizable.ExtUtil;
import org.javarosa.core.util.externalizable.ExtWrapListPoly;
import org.javarosa.core.util.externalizable.ExtWrapMap;
import org.javarosa.core.util.externalizable.Externalizable;
import org.javarosa.core.util.externalizable.PrototypeFactory;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Registers actions that should be triggered by certain events, and handles the triggering
 * of those actions when an event occurs.
 *
 * @author Aliza Stone
 */
public class ActionController implements Externalizable {

    // map from an event to the actions it should trigger
    private HashMap<String, List<Action>> eventListeners;

    public ActionController() {
        this.eventListeners = new HashMap<String, List<Action>>();
    }

    public List<Action> getListenersForEvent(String event) {
        if (this.eventListeners.containsKey(event)) {
            return eventListeners.get(event);
        }
        return new ArrayList<Action>();
    }

    public void registerEventListener(String event, Action action) {
        List<Action> actions;
        if (eventListeners.containsKey(event)) {
            actions = eventListeners.get(event);
        } else {
            actions = new ArrayList<Action>();
            eventListeners.put(event, actions);
        }
        actions.add(action);
    }

    public void triggerActionsFromEvent(String event, FormDef model) {
        triggerActionsFromEvent(event, model, null, null);
    }

    public void triggerActionsFromEvent(String event, FormDef model, TreeReference contextForAction,
                                        ActionResultProcessor resultProcessor) {
        for (Action action : getListenersForEvent(event)) {
            TreeReference refSetByAction = action.processAction(model, contextForAction);
            if (resultProcessor != null && refSetByAction != null) {
                resultProcessor.processResultOfAction(refSetByAction, event);
            }
        }
    }

    @Override
    public void readExternal(DataInputStream inStream, PrototypeFactory pf) throws IOException, DeserializationException {
        eventListeners = (HashMap<String, List<Action>>) ExtUtil.read(inStream,
                new ExtWrapMap(String.class, new ExtWrapListPoly()), pf);
    }

    @Override
    public void writeExternal(DataOutputStream outStream) throws IOException {
        ExtUtil.write(outStream, new ExtWrapMap(eventListeners, new ExtWrapListPoly()));
    }

    // Allows defining of a custom callback to execute on a result of processAction()
    public interface ActionResultProcessor {
        /**
         * @param targetRef - the ref that this action targeted
         * @param event - the event that triggered this action
         */
        void processResultOfAction(TreeReference targetRef, String event);
    }

}
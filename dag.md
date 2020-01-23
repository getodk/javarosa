# DAG

## DAG lifecycle

### 1 - The form is parsed

`XFormsParser.parseDoc` parses the blank form's XML contents and returns a `FormDef`

- `parseElement(_xmldoc.getRootElement(), _f, topLevelHandlers)` triggers a recursive parsing process
- Eventually, `StandardBindAttributesProcessor` objects parse the `<bind>` elements. Here condition and computation triggerables are parsed and:
  - They are added to the DAG by calling to `TriggerableDag.addTriggerable(...)`.
  
    This also builds an index of triggerables per trigger reference.
    
  - They're assigned as members of the output `DataBinding` objects.
- `FormInstance fi = instanceParser.parseInstance(...)` triggers calling `FormInstanceParser.applyInstanceProperties(...)`, which iterates the parsed bindings and sets a two-way relationship between the `DataBinding` objects and the `TreeElement` object corresponding to their `nodeset` references.
  - Here's when references are actually declared as targets of triggerable objects. So far, any triggerable object would only know about its trigger references only (which is parsed from the xpath expressions)
  - All triggerables get one target corresponding to `nodeset` reference of the binding where they are defined.
  - Aditionally, relevance conditions declared in group fields get a target reference per each (recursive) descendant element found in them.
- `addMainInstanceToFormDef(mainInstanceNode, fi)` triggers calling `TriggerableDag.finalizeTriggerables`, which effectively finished the DAG building process and leaves everything ready for evaluation at runtime while filling forms.

### 2 - A new form instance is initialized

`FormDef.initialize` prepares the main instance to receive new answers. The last step of this preparation involves initializing the triggerables of the form.

**Set of evaluated triggerables**: all triggerables. 
(see Evaluation of a set of triggerables)

- `Collection<QuickTriggerable> qts = initializeTriggerables(TreeReference.rootRef())` eventually triggers calling `TriggerableDag.initializeTriggerables(...)`.

- `TriggerableDag.initializeTriggerables(...)` receives the form's root reference, which resolves to all triggerables declared in the form, because the root reference is always an ancestor of any target reference in the form.

### 3 - Something changes while answering a form
 
#### a - A value changes

`FormEntryController.answerQuestion(...)` deals with new values coming from the user's interaction with the form instance. Eventually, `FormDef.setValue(...)` gets called which, in turn, starts a new chain of triggerable evaluations.

**Set of evaluated triggerables**: those triggered by the reference of the element that has changed its value.
(see Evaluation of a set of triggerables)

- Before committing the new value, any constraint condition defined for the field is evaluated at `} else if (!complexQuestion && !model.getForm().evaluateConstraint(index.getReference(), data)) {`. 

  If this constraint is not satisfied, no value is committed, which won't start triggerable evaluation.
  
- `commitAnswer(element, index, data, midSurvey)` calls `FormDef.setValue(...)` which, in turn, calls `Collection<QuickTriggerable> qts = triggerTriggerables(ref)`, a wrapper method of `TriggerableDag.triggerTriggerables(...)`.

- `TriggerableDag.triggerTriggerables(...)` receives the reference of the element that has changed, which is used to get the set of triggerables to be evaluated using the index (triggerables per trigger reference) built while parsing the form.

#### b - A repeat instance is created

A new repeat instance can be created under two circumstances:
- When a user is asked to create a new repeat and they answer yes. This is driven by `FormEntryController.newRepeat(...)`.
- When the form has to satisfy a certain number of repeat instances determined by a `jr:count` expression e.g. when jumping to the controller jumps to the index of such a group. This is driven by `FormEntryModel.createModelIfNecessary`.

In both cases, `FormDef.createNewRepeat(...)` is eventually called and a chain of triggerable evaluations is triggered on the new node that's added to the main instance. 

**Set of evaluated triggerables**: 
- First phase (value change), those triggered by the repeat group's reference
- Second phase (initialization), those triggerables that target a descendant of the repeat group's reference
- Third phase (children), those triggered by the reference of any children of the new repeat group 
(see Evaluation of a set of triggerables)

We have to remember that the DAG uses generic references as keys to index the set of triggerables triggered by them. This is the only viable option if we want to have a static DAG (one that gets build while parsing the form and then it remains unchanged) but it forces us to follow alternative strategies to evaluate triggerables belonging to repeat groups, because references for specific multiplicities wouldn't match any set of triggerables.

For this reason, triggerables related to repeat groups are evaluated in three phases, ensuring that no triggerable is triggered more than once, should it be matched in more than one phase.

**Phase one (value change)**

`Set<QuickTriggerable> qtSet1 = triggerTriggerables(mainInstance, evalContext, createRef, new HashSet<>(0))` starts the evaluation of the first set of triggerables as if a simple value would have changed (see 3a). 

This is done to let any other part of the form to react to the creation of a new repeat group instance e.g. to compute the count of instances in the repeat group.

**Phase two (initialization)**

Adding new elements to the main instance requires their initialization the same way we prepare the main instance to receive new answers the first time. `Set<QuickTriggerable> qtSet2 = initializeTriggerables(mainInstance, evalContext, createRef, new HashSet<>(0));` starts the evaluation of all triggerables targetting a descendant of the repeat group's reference. 

This is done to prepare the new elements in case they have computed values. 

**Phase three (children)**

Now the children elements in the new repeat group instance have been created and initialized, which means that we should evaluate any triggerable triggered by their references. `evaluateChildrenTriggerables(mainInstance, evalContext, createdElement, true, alreadyEvaluated);` starts the chain of evaluations of these triggerables as if a value would have changed in each of them. 

#### c - A repeat instance is deleted

`FormEntryController.deleteRepeat(...)` is called when a user deletes a repeat group. After removing the corresponding elements from the main instance, `TriggerableDag.deleteRepeatGroup(...)` gets called. When this happens, we iterate the all the repeat group instances starting from the position that the one that has been deleted had before deleting it.

**Set of evaluated triggerables**: 
- those triggered by the repeat group's reference, once per repeat group sibling starting from the position that belonged to the deleted group.
- those triggered by the references of the children of the deleted repeat group. 
(see Evaluation of a set of triggerables)

Deleting a repeat group instance will update the group's instance count and the position of all the instances that follow the deleted one. This means that we need to evaluate all triggerables triggered by the repeat group's reference. 

We need to remember that we need to follow an alternative strategy to evaluate the triggerables belonging to repeat groups, because the DAG uses genericized trigger references to index sets of triggerables. In this case, calling `Set<QuickTriggerable> alreadyEvaluated = triggerTriggerables(mainInstance, evalContext, repeatGroup.getRef(), new HashSet<>(0))` once per sibling (starting from the deleted group's position) deals with that.

We also have to evaluate any triggerable triggered by children references of the repeat group, which is done by `evaluateChildrenTriggerables(mainInstance, evalContext, repeatGroup, false, alreadyEvaluated)`. Even though this is done inside the loop, the `if` check ensures this is only done once, coinciding with the first iteration (corresponding to the sibling that takes the place of the deleted one). Presumably, this is to have a nice ordering of published events in the `EventNotifier`, although there's no apparent functional requirement for this, and the call could be done outside the loop in `TriggerableDag.deleteRepeatGroup(...)`.

#### d - A complex itemset value is copied to the main instance

This happens when a `select1` or `select` field in a form uses an `itemset` with complex values (trees of elements instead of simple values, like numbers or texts).

**Set of evaluated triggerables**: 
- First phase (value change), those triggered by the new element references
- Second phase (initialization), those triggerables that target a descendant of the new element references

When a user answers one of these questions under the described scenario, new elements have to be created in the main instance which, technically, behave as groups. For this reason, triggerables related to this event are evaluated in two phases, ensuring that no triggerable is triggered more than once, should it be matched in more than one phase.

**Phase one (value change)**

`Set<QuickTriggerable> qtSet1 = triggerTriggerables(mainInstance, evalContext, copyRef, new HashSet<>(0))` starts the evaluation of the first set of triggerables as if a simple value would have changed (see 3a). 

This is done to let any other part of the form to react to the creation of new elements (techincally groups) in the main instance.

**Phase two (initialization)**

Adding new elements to the main instance requires their initialization the same way we prepare the main instance to receive new answers the first time. `Set<QuickTriggerable> qtSet2 = initializeTriggerables(mainInstance, evalContext, createRef, new HashSet<>(0));` starts the evaluation of all triggerables targetting a descendant of the new element's reference. 

This is done to prepare the new elements in case they have computed values. 

## Evaluation of a set of triggerables

Once the DAG has configured a set of triggerables affected by the user's action, their evaluation starts in `TriggerableDag.doEvaluateTriggerables(...)`

To ensure the ordered evaluation of triggerables, the whole DAG is iterated and only the triggerables inside the selected set are evaluated, and if they are not present in the set of triggerables already evaluated.

It's uncertain how and why the `firedAnchors` map is used, so I'll completely ignore it for this explanation.

Each time we iterate over a triggerable, a list of expanded affected references is computed by an `EvaluationContext` using the triggerable to contextualize the provided `anchorRef`. Then the triggerable is applied to each one of these expanded affected references.
 



 

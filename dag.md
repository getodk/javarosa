# DAG

Forms used by JavaRosa can use expressions to compute dynamic values as users answer questions in forms. These values can be used in a range of applications, including, among others, setting values for other fields, validation, or conditionally hiding non-relevant fields.

`calculate`, `relevant`, `readonly`, and `required` expressions used in `<bind>` attributes are stored in a directed acyclic graph (DAG from now on) of `Triggerable` objects.

The `Triggerable` abstraction represents an expression, references updated by it, and references that trigger its evaluation when their values change.

## Deviations from specs

### Recalculation Sequence Algorithm from XForms 1.1

- https://www.w3.org/TR/xforms11/#recalc

  > "The XForms recalculation algorithm considers model items and model item properties to be vertices in a directed graph. Edges between the vertices represent computational dependencies between vertices."

  JavaRosa's underlying DAG tracks `Triggerable` instances (vertices) and cascading evaluations of triggerables when one of their trigger refs is changed by another (edges).
  
  Nevertheless, further reading of the specs reveals that the DAG should have computations as vertices and their computational dependencies as edges, which is similar to what JavaRosa does.

- https://www.w3.org/TR/xforms11/#model-prop-calculate

  > "An XForms Model may include model items whose string values are computed from other values. For example, the sum over line items for quantity times unit price, or the amount of tax to be paid on an order. The formula for such a computed value can be expressed with a calculate property, whose XPath expression is evaluated, converted to a string with the XPath string() function, and stored as the value content of the calculated data node. Chapter 4 Processing Model contains details of when and how the calculation is performed."

  JavaRosa casts expression output values to types other than strings before committing them to the instance.

- https://www.w3.org/TR/xforms11/#rpm-event-seq-vc

  > "3. xforms-refresh performs reevaluation of UI binding expressions then dispatches these events according to value changes, model item property changes and validity changes:"

  JavaRosa doesn't emit `xforms-valid`, `xforms-valid`, `xforms-invalid`, `xforms-enabled`, `xforms-disabled`, `xforms-optional`, `xforms-required`, `xforms-readonly`, `xforms-readwrite`, `xforms-out-of-range`, or `xforms-in-range` events.
  
- https://www.w3.org/TR/xforms11/#rpm-processing-recalc-mddg

  > "Specifically, the depList for a vertex v is assigned to be the vertices other than v whose computational expressions reference v (described below). Vertex v is excluded from its own depList to allow self-references to occur without causing a circular reference exception."

  JavaRosa only allows for self-references in `readonly`, `required`, and `constraint` conditions.

- https://www.w3.org/TR/xforms11/#rpm-processing-recalc-compute

  > "2.b. relevant, readonly, required, constraint: If any or all of these computed properties change, the new settings are placed into effect for associated form controls."

  JavaRosa doesn't include `constraint` condition expressions in the DAG, nor it keeps track of its result in the node's internal state. 
  
  JavaRosa doesn't update the internal state of form controls associated with nodes when evaluating these expressions. 

- https://www.w3.org/TR/xforms11/#rpm-processing-recalc-example

  > "... When x is removed, its neighbor y drops to in-degree zero. The fourth and fifth iterations of this process recalculate the validity of w and y, both of which change to false."

  The specs describe an evaluation sequence that would evaluate constraints on fields changed during the cascade of evaluations and flagging them as invalid as a result, which JavaRosa doesn't do.
  
  JavaRosa only evaluates constraints to prevent committing to the instance values that don't match their constraint expressions.
   
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
  - Additionally, relevance conditions declared in group fields get a target reference per each (recursive) descendant element found in them.
- `addMainInstanceToFormDef(mainInstanceNode, fi)` triggers calling `TriggerableDag.finalizeTriggerables`, which effectively finished the DAG building process and leaves everything ready for evaluation at runtime while filling forms.

### 2 - A new form instance is initialized

`FormDef.initialize` prepares the main instance to receive new answers. The last step of this preparation involves initializing the triggerables of the form.

**Set of evaluated triggerables**: all triggerables. 
(see Evaluation of a set of triggerables)

- `Collection<QuickTriggerable> qts = initializeTriggerables(TreeReference.rootRef())` eventually triggers calling `TriggerableDag.initializeTriggerables(...)`.

- `TriggerableDag.initializeTriggerables(...)` receives the form's root reference, which resolves to all triggerables declared in the form because the root reference is always an ancestor of any target reference in the form.

### 3 - Something changes while answering a form
 
#### a - A value changes

`FormEntryController.answerQuestion(...)` deals with new values coming from the user's interaction with the form instance. Eventually, `FormDef.setValue(...)` gets called, which, in turn, starts a new chain of triggerable evaluations.

**Set of evaluated triggerables**: those triggered by the reference of the element that has changed its value.
(see Evaluation of a set of triggerables)

- Before committing the new value, any constraint condition defined for the field is evaluated at `} else if (!complexQuestion && !model.getForm().evaluateConstraint(index.getReference(), data)) {`. 

  If this constraint is not satisfied, no value is committed, which won't start triggerable evaluation.
  
- `commitAnswer(element, index, data, midSurvey)` calls `FormDef.setValue(...)` which, in turn, calls `Collection<QuickTriggerable> qts = triggerTriggerables(ref)`, a wrapper method of `TriggerableDag.triggerTriggerables(...)`.

- `TriggerableDag.triggerTriggerables(...)` receives the reference of the element that has changed, which is used to get the set of triggerables to be evaluated using the index (triggerables per trigger reference) built while parsing the form.

#### b - A repeat instance is created

A new repeat instance can be created under two circumstances:
- When a user is asked to create a new repeat, and they answer yes. This is driven by `FormEntryController.newRepeat(...)`.
- When the form has to satisfy a certain number of repeat instances determined by a `jr:count` expression e.g., when jumping to the controller jumps to the index of such a group. This is driven by `FormEntryModel.createModelIfNecessary`.

In both cases, `FormDef.createNewRepeat(...)` is eventually called, and a chain of triggerable evaluations is triggered on the new node that's added to the main instance. 

**Set of evaluated triggerables**: 
- First phase (value change), those triggered by the repeat group's reference
- Second phase (initialization), those triggerables that target a descendant of the repeat group's reference
- Third phase (children), those triggered by the reference of any children of the new repeat group 
(see Evaluation of a set of triggerables)

We have to remember that the DAG uses generic references as keys to index the set of triggerables triggered by them. This is the only viable option if we want to have a static DAG (one that gets build while parsing the form and then it remains unchanged), but it forces us to follow alternative strategies to evaluate triggerables belonging to repeat groups because references for specific multiplicities wouldn't match any set of triggerables.

For this reason, triggerables related to repeat groups are evaluated in three phases, ensuring that no triggerable is triggered more than once, should it be matched in more than one phase.

**Phase one (value change)**

`Set<QuickTriggerable> qtSet1 = triggerTriggerables(mainInstance, evalContext, createRef, new HashSet<>(0))` starts the evaluation of the first set of triggerables as if a simple value would have changed (see 3a). 

This is done to let any other part of the form react to the creation of a new repeat group instance e.g., to compute the count of instances in the repeat group.

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

We need to remember that we need to follow an alternative strategy to evaluate the triggerables belonging to repeat groups because the DAG uses genericized trigger references to index sets of triggerables. In this case, calling `Set<QuickTriggerable> alreadyEvaluated = triggerTriggerables(mainInstance, evalContext, repeatGroup.getRef(), new HashSet<>(0))` once per sibling (starting from the deleted group's position) deals with that.

We also have to evaluate any triggerable triggered by children references of the repeat group, which is done by `evaluateChildrenTriggerables(mainInstance, evalContext, repeatGroup, false, alreadyEvaluated)`. Even though this is done inside the loop, the `if` check ensures this is only done once, coinciding with the first iteration (corresponding to the sibling that takes the place of the deleted one). Presumably, this is to have a nice ordering of published events in the `EventNotifier`, although there's no apparent functional requirement for this, and the call could be done outside the loop in `TriggerableDag.deleteRepeatGroup(...)`.

#### d - A complex itemset value is copied to the main instance

This happens when a `select1` or `select` field in a form uses an `itemset` with complex values (trees of elements instead of simple values, like numbers or texts).

**Set of evaluated triggerables**: 
- First phase (value change), those triggered by the new element references
- Second phase (initialization), those triggerables that target a descendant of the new element references

When a user answers one of these questions under the described scenario, new elements have to be created in the main instance, which, technically, behave as groups. For this reason, triggerables related to this event are evaluated in two phases, ensuring that no triggerable is triggered more than once, should it be matched in more than one phase.

**Phase one (value change)**

`Set<QuickTriggerable> qtSet1 = triggerTriggerables(mainInstance, evalContext, copyRef, new HashSet<>(0))` starts the evaluation of the first set of triggerables as if a simple value would have changed (see 3a). 

This is done to let any other part of the form to react to the creation of new elements (technically groups) in the main instance.

**Phase two (initialization)**

Adding new elements to the main instance requires their initialization the same way we prepare the main instance to receive new answers the first time. `Set<QuickTriggerable> qtSet2 = initializeTriggerables(mainInstance, evalContext, createRef, new HashSet<>(0));` starts the evaluation of all triggerables targetting a descendant of the new element's reference. 

This is done to prepare the new elements in case they have computed values. 

## Evaluation of a set of triggerables

Once the DAG has configured a set of triggerables affected by the user's action, their evaluation starts in `TriggerableDag.doEvaluateTriggerables(...)`

To ensure the ordered evaluation of triggerables, the whole DAG is iterated, and only the triggerables inside the selected set are evaluated (if they haven't been already evaluated).

It's uncertain how and why the `firedAnchors` map is used, so I'll completely ignore it for this explanation (see my analysis below).

Each time we iterate over a triggerable, a list of expanded affected references is computed by an `EvaluationContext` using the triggerable to contextualize the provided `anchorRef`. Then the triggerable is applied to each one of these expanded affected references.

### Analysis of the `firedAnchors` map
 
Let's focus on a couple of blocks in `doEvaluateTriggerables` where this firedAnchors map is used:

Lines 125-128:
```java
List<TreeReference> affectedTriggers = qt.findAffectedTriggers(firedAnchors);
if (affectedTriggers.isEmpty()) {
    affectedTriggers.add(anchorRef);
}
```

In this block, we use the `firedAnchors` map to build the `affectedTriggers` reference list, which is used by `evaluateTriggerable` to know where to apply a triggerable.

Given a populated `firedAnchors` map, the resulting list would include all the values that have the triggerable's trigger generic references as keys.

At this point, I'm not sure why we are mixing trigger references with what I would expect to be target references (where the triggerables are applied). My understanding is that triggers are used by the callers of `doEvaluateTriggerables` to get the set of triggerables that must be evaluated. Once this is done, I don't expect to use triggers anymore because I would be interested only in knowing the targets where those must be applied.

Putting aside this confusing detail, we have to continue with the second block and what `evaluateTriggerable` does with these references to get the big picture.

Lines 135-145:
```java
for (EvaluationResult evaluationResult : evaluationResults) {
    TreeReference affectedRef = evaluationResult.getAffectedRef();

    TreeReference key = affectedRef.genericize();
    List<TreeReference> values = firedAnchors.get(key);
    if (values == null) {
        values = new ArrayList<>();
        firedAnchors.put(key, values);
    }
    values.add(affectedRef);
}
```

In this block, we see how we add new entries to the `firedAnchors`  map using the affected reference inside each of the evaluation results.

The affected refs are fully qualified expanded references that `evaluateTriggerable` gets from calling `EvaluationContext.expandReference` with the incoming `affectedTriggers` list of references.

This is where we see that the map uses generic references as keys and their corresponding set of fully qualified expanded references as values, which means that `firedAnchors` is basically a cache of `expandReference` output.

This is unexpected because, given a populated `firedAnchors` map, (fully qualified) expanded references would be fed into `evaluateTriggerable` where they are expanded again with the `EvaluationContext` IMO defeating its purpose.

If this was done to get performance gains, it looks like it's doing just the opposite of that, because `evaluateTriggerable` will have more work (not less) as the map is populated.

This is not helping me understand why we're adding fully qualified expanded trigger references into the mix in the first block, either.

I think we should review the naming/language to try to be more explicit and mix less concepts and, if you agree with my analysis above, we should change it to whether not use any cache or use it in a way that we get the performance advantages from it.

I tested removing the cache, and I've confirmed that no tests break, and I'd wager that now JR is faster because we will have to expand fewer references.

We could explore using a cache  and using it too, but that would come with the tradeoff of having to iterate triggerables twice, and we would get benefits only in case more than one triggerable in the provided set produces the same fully qualified reference with the incoming `anchorRef`. I'm not sure what are the odds of that, though.

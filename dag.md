# DAG

Forms used by JavaRosa can use expressions to compute dynamic values as users answer questions. These values can be used in a range of applications, including, among others, setting values for other fields, validation, or conditionally hiding non-relevant fields.

`calculate`, `relevant`, `readonly`, and `required` expressions used in `<bind>` attributes are stored in a directed acyclic graph (DAG from now on) of `Triggerable` objects.

The `Triggerable` abstraction represents an expression, references updated by it, and references that trigger the expression's evaluation when their values change.

## Relationships to formal specs

### Recalculation Sequence Algorithm from XForms 1.1

- https://www.w3.org/TR/xforms11/#recalc

  > "The XForms recalculation algorithm considers model items and model item properties to be vertices in a directed graph. Edges between the vertices represent computational dependencies between vertices."

  In JavaRosa, the vertices are `Triggerable` instances. The DAG is represented as a map from trigger references to the `Triggerable`s they each directly trigger.
  
- https://www.w3.org/TR/xforms11/#model-prop-calculate

  > "An XForms Model may include model items whose string values are computed from other values. For example, the sum over line items for quantity times unit price, or the amount of tax to be paid on an order. The formula for such a computed value can be expressed with a calculate property, whose XPath expression is evaluated, converted to a string with the XPath string() function, and stored as the value content of the calculated data node. Chapter 4 Processing Model contains details of when and how the calculation is performed."

  JavaRosa's internal representation of expression results is typed. The conversion to string values happens on serialization to XML.

- https://www.w3.org/TR/xforms11/#rpm-event-seq-vc

  > "3. xforms-refresh performs reevaluation of UI binding expressions then dispatches these events according to value changes, model item property changes and validity changes:"

  JavaRosa doesn't emit `xforms-valid`, `xforms-valid`, `xforms-invalid`, `xforms-enabled`, `xforms-disabled`, `xforms-optional`, `xforms-required`, `xforms-readonly`, `xforms-readwrite`, `xforms-out-of-range`, or `xforms-in-range` events.
  
- https://www.w3.org/TR/xforms11/#rpm-processing-recalc-mddg

  > "Specifically, the depList for a vertex v is assigned to be the vertices other than v whose computational expressions reference v (described below). Vertex v is excluded from its own depList to allow self-references to occur without causing a circular reference exception."

  JavaRosa only allows for self-references in `constraint` conditions implicitly due to not storing them in the DAG. `TriggerableDag.getDagEdges` prevents self-references between other expressions. This is presumably because self-references are generally a form design error. However, this deviation precludes possibly useful expressions like `coalesce(., "default")`. `TriggerableDagTest` has several ignored tests that illustrate this deviation.

- https://www.w3.org/TR/xforms11/#rpm-processing-recalc-compute

  > "2.b. relevant, readonly, required, constraint: If any or all of these computed properties change, the new settings are placed into effect for associated form controls."

  JavaRosa doesn't include `constraint` condition expressions in the DAG or keep track of its result in the node's internal state.
  
  JavaRosa keeps a series of flags representing `relevant`, `readonly`, `required` status in `TreeElement`. One of the major challenges of working with JavaRosa code is that there are several tree structures to keep track of. `FormDef.mainInstance` is a reference to a `FormInstance` object which contains the tree of `TreeElement`s corresponding to the primary form instance currently being filled in by the user. `FormDef` is the root of a parallel tree of `IFormElement` representing the blank form structure. In that tree, `QuestionDef` objects represent each question displayed to the user. In general, the mental model is that anything that is dynamic based on how a user has filled in a form instance so far is kept track of in `TreeElement` whereas `IFormElement` represents static properties from the form definition. For example, the tree of `IFormElement` will only have one `GroupDef` object representing an abstract repeat whereas the tree of `TreeElement` will have one subtree for each repeat instance. `relevant`, `readonly`, `required` are all dynamic based on how a user has filled in a form instance so far so they are kept track of in `TreeElement`. However, this mental model breaks down with dynamic select choices which are kept track of in `QuestionDef`. This is likely because JavaRosa initially only supported static selects which made sense to store in `QuestionDef`. When dynamic selects were added, it's possible that the implementers didn't have a clear mental model of the split between the form definition and the filled form instance. Alternately, because choice lists can be long, they may have not wanted to store redundant lists across repeat instances. It's not clear that the latter is a valid concern since only references would be duplicated.

  Dynamic selects are also not included in the DAG which may or may not have been an explicit design decisions. That is, if a value that is used in an expression for a dynamic select is updated, this does not trigger a recomputation of the select choices. Instead, clients of JavaRosa must call `FormEntryPrompt.getSelectChoices` to get choices to display for the current question. This recomputes the choice list. `FormEntryPrompt`/`FormEntryCaption` are convenience wrappers that tie together the trees of `IFormElement`s (the form definition) and `TreeElement`s (the current filled instance). In general, dynamic selects are not very well integrated and are hard to reason about.

- https://www.w3.org/TR/xforms11/#rpm-processing-recalc-example

  > "... When x is removed, its neighbor y drops to in-degree zero. The fourth and fifth iterations of this process recalculate the validity of w and y, both of which change to false."

  The specs describe an evaluation sequence that would evaluate constraints on fields changed during the cascade of evaluations and flagging them as invalid as a result, which JavaRosa doesn't do.
  
  JavaRosa evaluates constraints on a particular instance node before committing a new value to that node. It does not evaluate constraints on dependant values. `TriggerableDag.validate` must be run to verify all constraints.
   
## DAG lifecycle

### 1 - The form is parsed

`XFormsParser.parseDoc` parses the blank form's XML contents and returns a `FormDef`

- `parseElement(_xmldoc.getRootElement(), _f, topLevelHandlers)` triggers a recursive parsing process
- `StandardBindAttributesProcessor` objects parse the `<bind>` elements. Here `Triggerable` objects are built for each expression and:
  - They are added to the DAG by calling to `TriggerableDag.addTriggerable(...)` which builds an index of triggerables per trigger reference.
  - They're assigned as members of the returned `DataBinding` objects.
- In `XFormParser`, `FormInstance fi = instanceParser.parseInstance(...)` results in calling `FormInstanceParser.applyInstanceProperties(...)`, which iterates the parsed bindings and sets a two-way relationship between the `DataBinding` objects and the `TreeElement` object corresponding to their `nodeset` references.
  - Here's when references are actually declared as targets of triggerable objects. So far, any triggerable object would only know about its trigger references only (which is parsed from the xpath expressions)
  - All triggerables get one target corresponding to `nodeset` reference of the binding where they are defined.
  - Additionally, relevance conditions declared in group fields get a target reference per each (recursive) descendant element found in them.
- `addMainInstanceToFormDef(mainInstanceNode, fi)` results in calling `TriggerableDag.finalizeTriggerables`, which effectively finishes the DAG building process and leaves everything ready for evaluation at runtime while filling forms.

### 2 - A new form instance is initialized

`FormDef.initialize` prepares the main instance to receive new answers. The last step of this preparation involves initializing the triggerables of the form.

**Set of evaluated triggerables**: all triggerables. 
(see Evaluation of a set of triggerables)

- `Collection<QuickTriggerable> qts = initializeTriggerables(TreeReference.rootRef())` eventually triggers calling `TriggerableDag.initializeTriggerables(...)`.

- `TriggerableDag.initializeTriggerables(...)` receives the form's root reference, which resolves to all triggerables declared in the form because the root reference is always an ancestor of any target reference in the form.

### 3 - Something changes while answering a form

#### Note about repeats
To ensure correctness, JavaRosa could evaluate every triggerable with a target inside the repeat for every repeat instance. In fact, this has been tried before but it slows repeat addition, deletion, and value updates too much to be practical. It's most common to have expressions in repeats only refer to other nodes in the repeat in which case there's no need to update every instance.

In general, expressions inside a repeat are only evaluated when a new instance of that repeat is added and only for that new instance. However, if an expression in the repeat references the generic repeat (e.g. `count(../../repeat)` or `position(..)`) or a node outside the repeat which itself references the generic repeat, then that expression is evaluated for all repeat instances. See `TriggerableDag.getTriggerablesAffectingAllInstances`.

`TriggerableDagTest` has several ignored tests that highlight cases that aren't correctly updated. A more correct but still performant solution would likely require static analysis on expressions to identify things like predicates or `sum` function calls. `CreateRepeatDagBenchmark` and `DeleteRepeatDagBenchmark` measure different cases with different performance characteristics.
 
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

`Set<QuickTriggerable> qtSet1 = triggerTriggerables(...)` starts the evaluation of the first set of triggerables as if a simple value would have changed (see 3a).

This is done to let any other part of the form react to the creation of a new repeat group instance e.g., to compute the count of instances in the repeat group. This is where some expressions may be evaluated for all repeat instances (see above and `TriggerableDag.getTriggerablesAffectingAllInstances`).

**Phase two (initialization)**

Adding new elements to the main instance requires their initialization the same way we prepare the main instance to receive new answers the first time. `Set<QuickTriggerable> qtSet2 = initializeTriggerables(...);` starts the evaluation of all triggerables targetting a descendant of the repeat group's reference. 

This is done to prepare the new elements in case they have computed values. 

**Phase three (children)**

Now the children elements in the new repeat group instance have been created and initialized, which means that we should evaluate any triggerable triggered by their references. `evaluateChildrenTriggerables(mainInstance, evalContext, createdElement, true, alreadyEvaluated);` starts the chain of evaluations of these triggerables as if a value would have changed in each of them. 

#### c - A repeat instance is deleted

`FormEntryController.deleteRepeat(...)` is called when a user deletes a repeat group. After removing the corresponding elements from the main instance, `TriggerableDag.deleteRepeatGroup(...)` gets called.

**Set of evaluated triggerables**: 
- those triggered by the generic repeat reference
- those triggered by the references of the children of the deleted repeat group. 
(see Evaluation of a set of triggerables)

Deletion uses the same strategy as addition. First, the call on `triggerTriggerables` evaluates cascades with a reference to the generic repeat reference. `getTriggerablesAffectingAllInstances` ensures that references to such calculations inside the repeat are evaluated for all repeat instances.

We then have to evaluate any triggerables triggered by references to children of the repeat group, which is done by `evaluateChildrenTriggerables`.

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

To ensure the ordered evaluation of triggerables, triggerables are triggered in the order they appear in the ordered collection of DAG nodes.

Generally when evaluating a specific triggerable, the context saved in that triggerable can be used to contextualize references in the expressions and targets where the expression result needs to be saved. However, in some cases in repeats (see a note about repeats above), the generic repeat reference is expanded so that the triggerable is evaluated for every repeat instance.

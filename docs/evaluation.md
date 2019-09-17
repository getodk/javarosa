# ODK: Optimisation of Form Evaluation and Rendering



# Table of Contents
1. [Overview](#overview)
2. [Important Classes](#importantclasses)
3. [JavaRosa Expression Evaluation](#jee)
4. [Solutions](#solutions)
	1. [Prior Work done](#priorwork)
    1. [Solution 1](#solution1)
    2. [Solution 2](#solution2)
    3. [Solution 3](#solution3)
5. [Next Steps](#nextsteps) 
4. [Seconday instance XML example](#sec_instance_xml)

Introduction:

Evaluating xpath expressions that reference secondary instances with a large dataset is less efficient than it could be. Our objective is to reduce the evaluation time from _O(n)_ to _O(1)_, where _n_ can be thought of as the number of elements in the XML dataset.

## Overview<a name="overview"></a>:

With the comprehensive structure of an XML document comes its verbosity. By verbosity, we specifically mean the larger number of nodes to traverse through when evaluating XPath queries (searching for a particular node/text or nodes that match the XPath expression) in the XML document. This causes a decrease in efficiency which becomes more noticeable when querying large XML documents.

[XForms](https://opendatakit.github.io/xforms-spec/) in [ODK](https://docs.opendatakit.org/form-design-intro) are represented as XML documents. This means that they come with the benefits of referencing and querying the entities and attributes of the XML documents using XPath expressions; the entities and attributes of the XML Document can be evaluated when filling the form in order to present the user with dynamic data based on inputted or other non-static data.

XForms basically have two sections, the Head and the Body. The Head consists of the main instance which holds the form structure and, optionally, secondary instances which hold data. Secondary instances are mostly used for preloading read-only data that is used in the form.

The data contained in secondary instances is queried using XPath expressions, The flexibility of XPath makes it possible to dynamically query the data contained in the secondary instance, returning an intended result that can be used in the form.

Evaluation Results can be:

- [Nodesets](https://github.com/opendatakit/javarosa/blob/bad552bbecfd75d2d12ba2ff8cc8c028ecea77ab/src/main/java/org/javarosa/xpath/XPathConditional.java#L91) (A set of XML nodes),
- [Readable](https://github.com/opendatakit/javarosa/blob/bad552bbecfd75d2d12ba2ff8cc8c028ecea77ab/src/main/java/org/javarosa/xpath/XPathConditional.java#L87) values (Strings that can be read),
- [Boolean](https://github.com/opendatakit/javarosa/blob/bad552bbecfd75d2d12ba2ff8cc8c028ecea77ab/src/main/java/org/javarosa/xpath/XPathConditional.java#L83) values (to specify conditions),
- [Raw](https://github.com/opendatakit/javarosa/blob/bad552bbecfd75d2d12ba2ff8cc8c028ecea77ab/src/main/java/org/javarosa/xpath/XPathConditional.java#L69) values (which are values that haven&#39;t been cast)

In [ODK Collect](https://docs.opendatakit.org/collect-intro), JavaRosa is the main library used for parsing XForms, secondary instances and also evaluating XPath expressions that reference the secondary instances contained in the form.

In order to understand the JavaRosa parsing and evaluation, some important classes need to be understood.



## Important Classes <a name="importantclasses"></a>

1. [TreeElement](https://github.com/opendatakit/javarosa/blob/develop/src/main/java/org/javarosa/core/model/instance/TreeElement.java):
  - Represents a node or element in the form instance XML tree.
  - Keeps information about the _node&#39;s [name](https://github.com/opendatakit/javarosa/blob/2acd222e43586e092dc13f76bb1d1a873564a32b/src/main/java/org/javarosa/core/model/instance/TreeElement.java#L70)_,  [_children_](https://github.com/opendatakit/javarosa/blob/2acd222e43586e092dc13f76bb1d1a873564a32b/src/main/java/org/javarosa/core/model/instance/TreeElement.java#L78)(which are also TreeElements), [_parent_](https://github.com/opendatakit/javarosa/blob/2acd222e43586e092dc13f76bb1d1a873564a32b/src/main/java/org/javarosa/core/model/instance/TreeElement.java#L72), [_position_](https://github.com/opendatakit/javarosa/blob/2acd222e43586e092dc13f76bb1d1a873564a32b/src/main/java/org/javarosa/core/model/instance/TreeElement.java#L71) (Location within its siblings - see [multiplicity](https://github.com/opendatakit/javarosa/blob/bad552bbecfd75d2d12ba2ff8cc8c028ecea77ab/src/main/java/org/javarosa/core/model/instance/TreeElement.java#L71)) and text element content (IAnswerData)
  - All TreeElements have a parent (except the root node) and have children (except leaf nodes, which contain data - see [IAnswerData](https://github.com/opendatakit/javarosa/blob/develop/src/main/java/org/javarosa/core/model/data/IAnswerData.java)).
  - A TreeElement node with a [IAnswerData](https://github.com/opendatakit/javarosa/blob/develop/src/main/java/org/javarosa/core/model/data/IAnswerData.java) is a leaf node.
  - The `<cars>...</cars>` TreeElement is the root TreeElement with 10000 children
  - The first `<car>...</car>` TreeElements has `<cars>` as it's Parent and a multiplicity of 1 since it's the first among it's siblings

2. [TreeReference](https://github.com/opendatakit/javarosa/blob/develop/src/main/java/org/javarosa/core/model/instance/TreeReference.java):
  - Represents a reference (or pointer) to one or more nodes in a TreeElement
  - Keeps information on how to navigate to the particular node it references without navigating serially from the root node
  - The navigation information is represented as [TreeReferenceLevel](https://github.com/opendatakit/javarosa/blob/develop/src/main/java/org/javarosa/core/model/instance/TreeReferenceLevel.java)
  - Consists of the steps to take to get to the TreeElement
  - Can be a relative, absolute or instance-based reference
  - Absolute TreeReferences are evaluated in the main instance
  - Could be ambiguous or specific; ambiguous (generic) TreeReferences do not have multiplicity values
  - Specific TreeReferences `/cars/car[4]/brand[0]`(Matrix) are evaluated faster than generic TreeReferences `/cars/car/name`(All the name nodes).

3. [XPathExpression](https://github.com/opendatakit/javarosa/blob/develop/src/main/java/org/javarosa/xpath/expr/XPathExpression.java) :
  - This class serves as the base class of all types of XPathExpressions
  - An XPathExpression is how an XPath is represented in JavaRosa
  - It is synonymous with an equation which is intended to return a result from an XML document; in this case the main or a secondary instance.
  - All XPathExpression classes can be found in _org.javarosa.xpath.expr_ package

  - Subclasses of the class XPathExpression include :
    1. [XPathStringLiteral](https://github.com/opendatakit/javarosa/blob/develop/src/main/java/org/javarosa/xpath/expr/XPathStringLiteral.java): Represents a string variable value, for instance, `'John'`
    2. [XPathNumericLiteral](https://github.com/opendatakit/javarosa/blob/develop/src/main/java/org/javarosa/xpath/expr/XPathNumericLiteral.java): Represents a numeric variable value for instance, `10`
    3. [XPathEqExpr](https://github.com/opendatakit/javarosa/blob/develop/src/main/java/org/javarosa/xpath/expr/XPathEqExpr.java): Represents an expression which compares two sides of an equalizer, for instance, It is normally used as a predicate to filter nodeset that meet a condition
    4. [XPathArithExpr](https://github.com/opendatakit/javarosa/blob/develop/src/main/java/org/javarosa/xpath/expr/XPathArithExpr.java): Represents an arithmetic expression, used for basic arithmetic operations, for instance _/root/state/size | /root/state/area_
    5. [XPathPathExpr](https://github.com/opendatakit/javarosa/blob/develop/src/main/java/org/javarosa/xpath/expr/XPathPathExpr.java): Represents a reference to an XML path; usually generic paths when converted to TreeReferences
      1. [XPathStep](https://github.com/opendatakit/javarosa/blob/develop/src/main/java/org/javarosa/xpath/expr/XPathStep.java) represents each path of an [XPathPathExpr](https://github.com/opendatakit/javarosa/blob/develop/src/main/java/org/javarosa/xpath/expr/XPathStep.java), it can also represent a TreeRef erenceLevel
      2.  Each step could have [Predicates](https://github.com/opendatakit/javarosa/blob/bad552bbecfd75d2d12ba2ff8cc8c028ecea77ab/src/main/java/org/javarosa/xpath/expr/XPathStep.java#L76) which are also XPathExpressions used to subcategorize nodeSets.
    6. [XPathFuncExpr](https://github.com/opendatakit/javarosa/blob/develop/src/main/java/org/javarosa/xpath/expr/XPathFuncExpr.java): Used to represent XForm functions
    7. [XPathVariableReference](https://github.com/opendatakit/javarosa/blob/develop/src/main/java/org/javarosa/xpath/expr/XPathVariableReference.java) : Refers to a referenced element in the XML document.

 - These child classes can also be part of an XPathExpression.

4. [EvaluationContext](https://github.com/opendatakit/javarosa/blob/develop/src/main/java/org/javarosa/core/model/condition/EvaluationContext.java) :

  - XPathPathExpression(s) and TreeReference(s) are not always absolute, The EvaluationContext helps to keep track of context, dependent references of an expression and how it is being evaluated,
  - XPathPathExpression(s) can consist of other expressions, the EvaluationContext keeps track of the flow of evaluations.
  - Keeps information of the instances available in order to evaluate paths or references which aren&#39;t absolute.
  - Keeps information on the current context of an XPathExpression
  - Helps to keep information about where the current expression is
  - expandReferenceAccumulator : traverses the entire references of the instance to accumulate nodes that match a supposedly ambiguous reference

5. [XPathConditional](https://github.com/opendatakit/javarosa/blob/develop/src/main/java/org/javarosa/xpath/XPathConditional.java) :
  - Represents a part of an XForm where an Expression is used to fetch data
  - Wraps XPathExpressions and used to cast the result to the appropriate usable value
  - Can be triggered when dependent fields change
  - Has three kinds of Result
    1. evalRaw: returns the uncast result of an XPathExpression evaluation
    2. eval: returns the result of the expression as a boolean value
    3. evalNodeset: returns the result of the expression evaluation as a list of TreeReferences
    4. evalReadable: returns the result of the expression evaluation as a string
  - The method used in this class depends on where the expression is used in the form.
6. FormDef
  - Referesents a Form
6. [QuestionDef](https://github.com/opendatakit/javarosa/blob/develop/src/main/java/org/javarosa/core/model/QuestionDef.java):  Represent control elements used to get information or display information to the user
  - An ItemSet is used to query a FormInstance for elements that match an XPathPathExpression
  - Not all QuestionDefs have ItemSet Expression

## JavaRosa Expression Evaluation <a name="jee"/>

XPath expressions are used in various parts of an XForm in other to dynamically reference data contained in the form instances. control and binding elements are one of the main parts there they are used. The XFormParser class has registered handlers that handle the interpretation of each of the elements contained in the XForm to the various parts of the FormDef.

During the parsing of the XForm, these handlers carry out the interpretation of the different elements contained in the XForm. XPathExpressions often occur as attributes of these elements as Strings.

The class [XPathParseTool](https://github.com/opendatakit/javarosa/blob/develop/src/main/java/org/javarosa/xpath/XPathParseTool.java) is then used to parse the XPathExpression string into an instance of a subclass of XPathExpression. The AST (Abstract Syntax Tree) structure parsing flow is used to parse the expression string depending on the type which could comprise of one or more XPathExpression subcomponents (just like an equation which could consists of equations - `(5+3)/(3-1)*(1+1)`). Predicates are also parsed if they exist in the expressions.

After being parsed from strings to XPathExpression, the XPathExpression class is represented in JavaRosa Form components as XPathConditional. XPathConditional is used to hold, evaluate and convert the evaluation result to the appropriate types. When an XPathExpression is used in the FormDef, it is usually evaluated to get a result which can be used in a control. This result can be dynamic depending on the type of expression and the current state of the form.

The XPathExpression is an abstract class that has an eval abstract method that returns an Object when called, the type of object returned depends on the type of XPathExpression.

Evaluation of other XPath expressions are efficient except for the XPathPathExpression type. This is because evaluation of this type of expression most often traverses the XML Document it references in order to return the nodes of the XML document that match it.

An XPathPathExpression can be converted to a TreeReference which, when matched with the elements of the XML documents it represents, returns matching node(s) or value(s). In order for this to happen all the nodes of the document need to be traversed and matched with the TreeReference.

The time to evaluate XPathPathExpression, therefore, depends on the number of elements in the XML dataset of the instance.

Major use cases where reduced efficiency is detected:

1. XPathPathExpression used to query ItemSet used for the choices of a select type question: in this case, the XPathPathExpression are generic paths which take a time of O(n) to evaluate since the whole XML tree of the instance tree has to be traversed in order to accumulate matches of the XPathPathExpression.
2. XPathPathExpression in the calculate attribute of binding elements used to query values that match a specific condition used to prefill input values.

The main reason for this O(n) time used to evaluate the queries is because all elements are traversed in order to match each element and node to the XPath expression.

For example, let&#39;s consider this secondary instance below

Interpreting the simple XPathPathExpressions

1. instance(cars)/cars/car : Returns all car nodes
2. instance(cars)/cars/car[name=/form/car\_name] : returns all the car nodes that have the specified brand
3. instance(cars)/cars/car/brand : return all the car brands
4. instance(cars)/cars/car[name=/form/car\_name]/brand : returns all brands with the specified car name

During evaluation, JavaRosa evaluates like so

- When the Form is being parsed, the expression is anchored by an XPathConditional to the Form Control.
- At the time the expression is to be evaluated, the XPathConditional **eval** method is called (evalRaw, evalNodeset, evalRaw, eval) depending on the expected result of the expression.
- Converts the xpath string to a XPathPathExpression.
- Converts the XPathPathExpresion to a generic TreeReference.
- Traverses through the TreeElement of the Cars secondary instance.
- Returns all matching nodes as a list of specific TreeReferences.
- If the TreeReference is a leaf node on the TreeElement, it can be converted to a readable value.


## Proposed Solutions:

The key concept being used for these solutions is based on analysing the patterns of the XPathPathExpr XPath expressions. And then creating appropriate dictionaries for the possible results when parsing and creating each node of the Secondary Instance, so that when the expression is evaluated during form initialization or loading, the result of the expression is fetched from the dictionary of the indexer instead of evaluating the expression by traversing the whole form. ** XPaths don't currently support attribute querying this means, Predicates can only be applied to the parents of leaf nodes **

- When the XForm is being parsed, the analysis and indexing is hooked unto the respective element parsing handlers, see the handlers [here](https://github.com/opendatakit/javarosa/blob/2acd222e43586e092dc13f76bb1d1a873564a32b/src/main/java/org/javarosa/xform/parse/XFormParser.java#L212).
- The Question elements are [first handled](https://github.com/opendatakit/javarosa/blob/2acd222e43586e092dc13f76bb1d1a873564a32b/src/main/java/org/javarosa/xform/parse/XFormParser.java#L477) before the [secondary instances](https://github.com/opendatakit/javarosa/blob/2acd222e43586e092dc13f76bb1d1a873564a32b/src/main/java/org/javarosa/xform/parse/XFormParser.java#L488) in the [parseDoc](https://github.com/opendatakit/javarosa/blob/develop/src/main/java/org/javarosa/xform/parse/XFormParser.java) method.
- When a question element is [handled](https://github.com/opendatakit/javarosa/blob/2acd222e43586e092dc13f76bb1d1a873564a32b/src/main/java/org/javarosa/xform/parse/XFormParser.java#L1000), if it&#39;s a MultipleChoice type and also has an Itemset attached to it:
  - The nodeset expression of the ItemSet is analysed to see the kind of pattern it is, like the steps and the Predicates that occur in the expression.
  - An Indexer is then created for the expression
  - Appropriate indexers are created for appropriate expressions this way
  - When the instance elements are being interpreted and each node is created, each node is matched to the appropriate indexers to see if it belongs to that Indexer.
  - If it does, then it is stored by the Indexer
  - When expressions are later being evaluated, they are checked in each Indexer if they have been indexed
  - If they have, the result is fetched from the indexer rather than normal evaluation of traversing the XML document again.

### Prior Work <a name="priorwork" />:

This is based on what has been done before the solutions below in other to solve same problem

See [@ggalmazor&#39;s](https://github.com/ggalmazor) PR [here](https://github.com/opendatakit/javarosa/pull/438)

- Description:
  - Basically, the approach the solution takes is Instead of traversing the whole document, The TreeElement is [first filtered based on the node names](https://github.com/opendatakit/javarosa/blob/112ed0bc6581b528dbadc4903b854f54c7b82d72/src/main/java/org/javarosa/core/model/instance/utils/TreeElementChildrenList.java#L123) and feild that match the node and predicate.

- GitHub branch : [Find Here](https://github.com/ggalmazor/javarosa/tree/itemset_filtering)

### Solution 1 <a name="solution1" />:

Description:
  - Works only for ItemSet nodeset expressions
  - After the Form has been parsed, We get the XPathPathExpr of the Itemset of all select controls
  - We traverse the nodes of the XML documents and create possible Select Choices so that the Select choices are eagerly created instead of lazy evaluation.
  - GitHub branch:[Find here](https://github.com/opendatakit/javarosa/tree/eval_solution_1)
- Benchmarks : [Find here](https://docs.google.com/spreadsheets/d/1ynrhOSOPuKCY3lb_9DJbo6MVqJgfJWLXiH8kztiLysQ/edit#gid=926327088)
- Profiling numbers: [Find here](https://docs.google.com/spreadsheets/d/17n42p_mceECRv4WsRuTpkDSL0hAg3sw0IF_WWPdaBxg/edit#gid=415988908&amp;range=4:4)
- Possible Improvements: Create the select choices of the question when the respective TreeElement of the secondary instance is being built. This will reduce the loading time because currently the extra time comes from the time it takes to traverse the secondary instance again to create the SelectChoices.

### Solution 2 <a name="solution2" />:

- Description:
  - Can be applied for every type of evaluation
  - Currently implemented for the nodeset attribute of ItemSet elements and the calculate attribute of binding elements
  - An Indexer is created for each expression to be indexed.
  - During parsing of the secondary instance, Each node is analysed to see if it matches the result of the pre-evaluated expression of each indexer
  - The results of the pre-evaluated expressions are stored in memory
  - The key of the in-memory is the pre-evaluated expression (converted to a generic TreeReference)
  
- GitHub branch: [See Here](https://github.com/opendatakit/javarosa/tree/eval_solution_3)
  - Main Indexer class: [See Here](https://github.com/opendatakit/javarosa/blob/eval_solution_3/src/main/java/org/javarosa/xpath/eval/MemoryIndexerImpl.java)
- Benchmarks: [See Here](https://docs.google.com/spreadsheets/d/1ynrhOSOPuKCY3lb_9DJbo6MVqJgfJWLXiH8kztiLysQ/edit#gid=1664056377&amp;range=5:5)
- Profiling numbers: [See Here](https://docs.google.com/spreadsheets/d/1ynrhOSOPuKCY3lb_9DJbo6MVqJgfJWLXiH8kztiLysQ/edit#gid=1664056377&amp;range=5:5)
- Possible Improvements: Use a String for the Key of the in-memory Map instead of a TreeReference, This is because the **put** and **get** operations are expensive calls due to the complex algorithm used for **.equals** and **.hashcode** of the TreeReference class

### Solution 3 <a name="solution3" />:

- Description:
  - Can be applied for every type of evaluation
  - Currently implemented for the calculate attribute of binding elements
- GitHub branch: [JavaRosa](https://github.com/opendatakit/javarosa/tree/eval_solution_3)  [Collect](https://github.com/eHealthAfrica/collect/tree/eval_solution_3)
- Benchmarks: None because the Database Indexer is implemented in the Collect source code
- Profiling numbers: [See Here](https://docs.google.com/spreadsheets/d/17n42p_mceECRv4WsRuTpkDSL0hAg3sw0IF_WWPdaBxg/edit#gid=415988908&amp;range=6:6)
- Possible Improvements: The nodeset expressions of the SelectChoice ItemSets are currently not being evaluated, it is a work in progress. To implement this, the code in populateDynamicChoices has to be changed entirely because we would need to get the label and value in a single select query instead of first evaluating the nodeset before each label and value.
- Schema:

| xconditional\_expr | generic\_tree\_ref | specific\_tree\_ref | leaf\_node\_1 | leaf\_node\_2 | leaf\_node\_n |
| --- | --- | --- | --- | --- | --- |
|   |   |   |   |   |   |
|   |   |   |   |   |   |

## Next Steps <a name="nextsteps" />:

- Keep looking at the structure of database solution though load time is discouraging
  - There might be other ways to batch the insert statements to reduce load time
- See whether, in solution 2, the index could be a string rather than a TreeReference
  - Cache serialized values rather than objects.

- Making code compatible with Internal secondary instances



<a name="sec_instance_xml" />
The sample XML document used in the document is found below

```
<instance id="cars">
    <car>
      <name>Toyota</name>
      <brand>Corolla</brand>
      <engine>
        <size>3L</size>
        <block>4</block>
      </engine>
    </car>
    <car>
      <name>Toyota</name>
      <brand>Matrix</brand>
      <engine>
        <size>3L</size>
        <block>4</block>
      </engine>
    </car>
    <car>
      <name>Toyota</name>
      <brand>Camry</brand>
      <engine>
        <size>3L</size>
        <block>4</block>
      </engine>
    </car>
    ...10000n (assume to the 10000th <car>)
</instance>
```


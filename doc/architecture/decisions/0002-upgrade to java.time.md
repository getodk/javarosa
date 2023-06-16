# 2. Update time and date functions to use java.time

Date: 2023-06-11

## Status

Partially Accepted

## Context
The codebase leans on JodaTime for Date and Time processing, and uses java.util.Date for capturing Date and Time information. As of Java 8, java.util.Date has been superseded in the language by the java.time package. 

The project should upgrade the codebase to take advantage of the java.time approach to dates and time.  

## Decision
Making the wholesale change has potentially wide ranging impacts that are not fully understood by this developer, who is new to the codebase and the project as whole. 

Therefore, the decision is to replace the functionality of jodatime with the java.time classes, as a reasonably meaty yet self contained piece of work. Full push to remove java.util.Date and supporting classes will be a separate exercise. 

## Consequences
The codebase still depends on deprecated classes and approach. This change can be done in isolation to the broader change, while making the broader change simpler when the time is right. 

It also means that any reliance on java.util.Date by other applications using JavaRosa as a library are not impacted by this change. 

Finally, by removing joda-time library as a dependency, it's one less thing for maintainers to worry about in an otherwise stable codebase.
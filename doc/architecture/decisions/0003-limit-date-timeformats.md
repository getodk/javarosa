# 3. Limit Date/TimeFormats [Migration Blocker]

Date: 2023-06-11

## Status

Accepted

## Context

DateFormat and DateTimeFormats are referenced by magic numbers in the [DateFormatter](../../../src/main/java/org/javarosa/core/model/utils/DateFormatter.java) class:

`public static final int FORMAT_ISO8601 = 1;`

`public static final int FORMAT_HUMAN_READABLE_SHORT = 2;`

`public static final int FORMAT_HUMAN_READABLE_DAYS_FROM_TODAY = 5;`

`public static final int FORMAT_TIMESTAMP_SUFFIX = 7;`

`public static final int FORMAT_TIMESTAMP_HTTP = 9;`


## Decision

Four of the formats are easily understood and accessible in the codebase. The `FORMAT_HUMAN_READABLE_DAYS_FROM_TODAY` is an unknown format, and not referenced anywhere else. 

For the purposes of migration, this format will no longer be supported.

## Consequences

Users migrating from the original JavaRosa library will need to change existing functionality that refers to this format to use another one.

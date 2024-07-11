# Plugins

Various use cases of JavaRosa can be extended with plugins.

## Parse

Intercept parts of the process and attach data to the created `FormDef` through its extras.

### API
- `XFormParser#addProcessor`
- `FormDef#getExtras`

The default `XFormParser` can be overridden by creating an implementation of `IXFormParserFactory` (`IXFormParserFactory.Wrapper` if you want to chain multiple plugins) and calling `XFormUtils.setXFormParserFactory` with it.

## Finalization

Inspect the `FormEntryModel` after finalization (or "post processing") and attach data via its extras.

### API
- `FormEntryController#addPostProcessor`
- `FormEntryModel#getExtras`

## Instance

Inspect external instances (their ID and parsed XML) after parsing or provide custom parsers for specific instances or file types.

### API
- `ExternalInstanceParser#addFileInstanceParser`

The default `ExternalInstanceParser` can be overridden by creating an implementation of `ExternalInstanceParserFactory` and calling `XFormUtils.setExternalInstanceParserFactory` with it.

## Function

Add custom functions that can be called from XPath.

### API
- `FormEntryController#addFunctionHandler`

## Predicate evaluation

Add custom strategies for filtering nodes for predicate expressions.

### API
- `FormEntryController#addFilterStrategy`
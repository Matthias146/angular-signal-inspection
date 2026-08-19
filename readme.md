# Angular Signal Inspection

An IntelliJ Platform plugin that detects Angular signals used without invocation
inside Angular templates.

## Why?

Angular signals must be invoked when their value is read:

```html
{{ pageNumber() }}

However, some uninvoked signal usages are not currently reported directly by
JetBrains IDEs.
This plugin adds an inspection for those cases.
Examples
Interpolation

{{ pageNumber }}

Angular signal should be invoked

Quick fix:
{{ pageNumber() }}

Angular control flow:
@if (isLoading) {
  Loading...
}

should be:
@if (isLoading()) {
  Loading...
}

The inspection also supports @for expressions:
@for (product of products(); track product.id) {
  ...
}

Supported IDEs
IntelliJ IDEA
WebStorm
Angular plugin required.
Status
Early development / 0.1.0
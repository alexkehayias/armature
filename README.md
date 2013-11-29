# About

(WIP) Armature is a ClojureScript library for building event driven web applications. 

This branch explores declaring dom events using Backbone style scoping and processing them in event loops using core.async channels.

# Example

Load the armature.dev namespace. This will start a browser connected repl. Follow the instructions printed in the repl to open it in the browser. Load the armature.example.core namespace in the new ClojureScript repl. 

By default, a global event loop is declared `global-event-loop` and clicking on the logo will show a message in the js console (assuming you're on a modern browser). Logging is enabled on all events declared/emitted/matched so you can see exactly what it's doing.

## Adding an event
You can add an event to an existing event-loop by calling the `bind-event!` function in the running repl without the need to reload the page.

```
  (bind-event! global-event-loop
               {:selector "#logo"
                :event "click"
                :callback #(debug "Callback #logo click" %)}
               :to-dom? true
               :parent-el (.-body js/document))
```

## Removing an event
You can remove an event from an existing event-loop by calling `unbind-event!`.

```
(unbind-event! global-event-loop "click" "#logo")
```

## License

Copyright © 2013 Alex Kehayias

Distributed under the Eclipse Public License either version 1.0 or (at
your option) any later version.
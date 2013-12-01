# About

(WIP) Armature is a ClojureScript library for building event driven web applications. 

This branch explores declaring dom events and siphoning them into core.async channels. The view code then processes these messages asynchronously through queues rather than callbacks.

# Running the Example

1. run `lein cljsbuild once`. To enable source map support, change the last line of app.js to `//@ sourceMappingURL=/static/scripts/app.js.map`
2. In a repl, evaluate the armature.server namespace. This will start a server on localhost 9000 and automatically enter you into a browser connected repl.
3. Wait until the browser repl finishes loading then, in your browser, navigate to http://127.0.0.1:9000/app to load the example and connect the browser repl.

By default, a global event loop is declared `global-event-loop` and clicking on the logo will show a message in the js console (assuming you're on a modern browser). Logging is enabled on all events declared/emitted/matched so you can see exactly what it's doing.

## Adding an event
You can add an event to an existing event-loop by calling the `bind-event!` function in the running repl without the need to reload the page.

```
  (bind-event! global-event-loop
               {:selector "#logo"
                :event "click"}
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
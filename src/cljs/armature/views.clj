(ns armature.views
  (:use [armature.events :only [bind-event]]))

(defn view
  "Takes a function, f, and an optional hashmap of event callback pairs.
   The view function is called when the view is created.
   
   Optional args:
   - events: a hashmap of events and callback functions

   Example:
   (let [f #(.log js/console \"Initialized view with args\" %1 %2)
         events {:click.item #(.log js/console \"Clicked!\")}]
     (view f :events events :args [1 2]))

  "
  
  [f & {:keys [events args]
        :or {events {} args []}}]
  (.log js/console "Events:" events "Args:" args "More:" more)
  ;; TODO allow an alternate syntax where you can arbitrarily
  ;; pass args with the trigger, handler
  ;; Bind all events
  (doseq [[k v] (seq events)]
    (bind-event k v))
  ;; Call the view function with any optional args
  ;; TODO do we need to provide context here?
  ;; Should the view be able to know what it el it is?
  (apply f args))

(let [f #(.log js/console "Initialized view with args" %1 %2 )
      events {:click.item #(.log js/console "Clicked!")}]
  (view f :events events :args ["yo" "dawg"]))

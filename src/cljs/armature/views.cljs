(ns armature.views
  (:use [armature.events :only [bind-event]]
        [armature.logging :only [debug]]))

(defn view
  "Takes a function, f, and an optional hashmap of event callback pairs.
   The view function is called when the view is created.

   Function f should return the HTML to be rendered by the caller.
   
   Optional args:
   - events: a hashmap of events and callback functions

   Example:
   (let [f #(.log js/console \"Initialized view with args\" %1 %2)
         events {:click.item #(.log js/console \"Clicked!\")}]
     (view f :events events :args [1 2]))

  "
  ;; TODO allow an alternate syntax where you can arbitrarily
  ;; pass args with the trigger, handler  
  [f & {:keys [events args el]
        :or {events {} args [] el "div"}}]
  (debug (str "View called with :events " events ":args" args))
  ;; Bind all events
  (doseq [[k v] (seq events)] (bind-event k v))
  ;; Call the view function with any optional args
  (apply f args))

(let [f #(.log js/console "Initialized view with args" %1 %2 )
      events {"click.item" #(.log js/console "Clicked!")}]
  (view f :events events :args ["yo" "dawg"]))

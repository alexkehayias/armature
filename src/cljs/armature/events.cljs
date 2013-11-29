(ns armature.events
  (:use [armature.logging :only [debug]])
  (:require [cljs.core.async :refer [chan close! <! >!]])
  (:require-macros [cljs.core.async.macros :refer [go]]))


(defn dom-event->chan-callback
  "Return a fn that takes an argument for the event payload
   that enqueues onto the channel ch"
  [ch event-id selector]
  #(go (>! ch {:selector selector :trigger event-id :payload %})))

(defn emit-event
  "Emit the event to the channel"
  [ch event-id selector payload]
  (let [callback (dom-event->chan-callback ch event-id selector)]
    (callback payload)))

(defn dom-event->chan!
  "Add an event listener of type event-id to the element el
   target.addEventListener (type, listener [, useCapture])"
  [el event-id callback]
  (debug "dom-event->chan! el:" el "event-id:" event-id)
  (.addEventListener el event-id callback false))

(defn remove-dom-event!
  "In order to deregister a listener we need a reference to the callback 
   that was registered"
  [el event-id callback]
  (.removeEventListener el event-id callback false))

(defn trigger-events [event bindings]
  (doseq [b bindings]
    (let [callback (:callback b)
          event-name (:event b)
          selector (:selector b)]
      (when (and (= (:trigger event) event-name)
                 (= (:selector event) selector))
        (do (debug "Matched event" event-name "from" selector)
            (callback event))))))

(defn mk-loop
  "Make an event loop that call callbacks when an event trigger 
   triggers is emitted to the loop. Block until any dependent 
   event loops emit.

   Returns a channel"
  [name bindings & deps]
  (debug "Creating event loop" name "with bindings" bindings)
  (let [ch (chan)]
    (go (loop [event (<! ch)]
          (debug "Emitted" event)
          (trigger-events event @bindings)
          (recur (<! ch))))
    ch))

(defn scoped-sel
  "Get all elements that match the selector in scope of the element el"
  [el selector]
  (let [query (.querySelectorAll el selector)
        length (. query -length)]
    (for [i (range length)]
      (.item query i))))

(def dom-event-list
  ["click"
   "dblclick"
   "mousedown"
   "mousemove"
   "mouseover"
   "mouseout"
   "keydown"
   "keypress"
   "keyup"
   "abort"
   "error"
   "load"
   "resize"
   "scroll"
   "unload"
   "blur"
   "change"
   "focus"
   "reset"
   "select"
   "submit"])

(defn bind-dom-events!
  "Bind a collection of events to the dom in scope of node parent-el. 
   Returns a list of event hashmaps with additional :remove-fn key for 
   removing the dom listener later.

   Arg bindings must be a list of hashmaps with the following keys:
   :ch - A channel
   :bindings - A vector of dicts describing an event binding
   :parent-el - An HTML element in which to scope each event binding
  "
  [ch bindings parent-el]
  (for [b bindings
        el (scoped-sel parent-el (:selector b))]
    (let [{:keys [event selector]} b
          ;; adds a callback to enqueue the event to ch
          callback (dom-event->chan-callback ch event selector)]
      (debug "binding event" event "to" selector)
      ;; Check the event to see if it matches
      ;; listenable dom events
      (when (some #{event} dom-event-list)
        (dom-event->chan! el event callback))
      ;; Deregister function
      (assoc b :remove-fn #(remove-dom-event! el event callback)))))

(defn mk-event-loop
  "Create an event loop scoped to the given element with
   a hashmap of events and it's dependencies. Add dom listeners
   to elements that match the selector in scope of the element parent-el"
  [name bindings parent-el & deps]
  (debug "mk-event-loop" "name:" name "bindings" bindings "parent" parent-el)
  (let [bindings-atom (atom bindings)
        ch (mk-loop name bindings-atom)
        updated-bindings (bind-dom-events! ch bindings parent-el)]
    (reset! bindings-atom updated-bindings)
    {:name name :channel ch :events bindings-atom}))

(defn close-event-loop
  "Close an event loop and clean up dom listeners"
  [event-loop bindings])

(defn bind-event!
  "Mutate the event bindings for the given channel hashmap by adding the 
   trigger and handler

   pass :to-dom? true if you want to also bind to the dom
  "
  [event-loop binding
   & {:keys [to-dom? parent-el] :or [to-dom? false parent-el nil]}]
  (debug "Binding event to" (:name event-loop) "\n"
         "Args:\n"
         :event-loop event-loop "\n"
         :binding binding "\n"
         :to-dom? to-dom? "\n"
         :parent-el parent-el)
  (swap! (:events event-loop)
         #(into % (if (and to-dom? (boolean parent-el))
                    (bind-dom-events! (:channel event-loop) [binding] parent-el)
                    [binding]))))

(defn unbind-event!
  "Remove an event currently bound to an event loop. 
   Calls the :remove-fn if it is present"
  [event-loop event-id selector]
  (debug "Un-binding" event-id
         "from" selector
         "in" (:name event-loop) "event loop")
  (let [all-events (:events event-loop)
        match? #(and (= (:event %) event-id)
                     (= (:selector %) selector))
        not-match? #(or (not= (:event %) event-id)
                        (not= (:selector %) selector))        
        events (filter match? @all-events)]
    (doseq [{:keys [remove-fn]} events]
      (when remove-fn (remove-fn)))
    (swap! all-events #(filter not-match? %))))

(def global-event-loop
  (mk-event-loop
   "global"
   []
   (.-body js/document)))

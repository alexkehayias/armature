(ns armature.events
  (:use [armature.logging :only [debug]])
  (:require [cljs.core.async :refer [chan close! <! >!]])
  (:require-macros [cljs.core.async.macros :refer [go]]))


(defn dom-event->chan-callback
  "Return a fn that takes an argument for the event payload
   that enqueues onto the channel ch"
  [ch-name ch event-id selector]
  #(go
    (debug "Emitted" event-id "on" selector "in channel" ch-name)
    (>! ch {:selector selector :event-id event-id :payload %})))

(defn emit-event
  "Emit the event to the channel"
  [ch-name ch event-id selector payload]
  (let [callback (dom-event->chan-callback ch-name ch event-id selector)]
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
  [ch-name ch bindings parent-el]
  (for [b bindings
        el (scoped-sel parent-el (:selector b))]
    (let [{:keys [event selector]} b
          ;; adds a callback to enqueue the event to ch
          callback (dom-event->chan-callback ch-name ch event selector)]
      (debug "Binding event" event "to" selector "on" ch-name)
      ;; Check the event to see if it matches
      ;; listenable dom events
      (when (some #{event} dom-event-list)
        (dom-event->chan! el event callback))
      ;; Deregister function
      (assoc b :remove-fn #(remove-dom-event! el event callback)))))

(defn mk-event-chan
  "Create an event loop scoped to the given element with
   a hashmap of events and it's dependencies. Add dom listeners
   to elements that match the selector in scope of the element parent-el"
  [name bindings parent-el & deps]
  (debug "mk-event-chan" "name:" name "bindings" bindings "parent" parent-el)
  (let [bindings-atom (atom bindings)
        ch (chan)
        updated-bindings (bind-dom-events! name ch bindings parent-el)]
    (reset! bindings-atom updated-bindings)
    {:name name :channel ch :events bindings-atom}))

(defn close-event-chan
  "Close an event loop and clean up dom listeners"
  [event-chan bindings])

(defn bind-event!
  "Mutate the event bindings for the given channel hashmap by adding the 
   trigger and handler

   pass :to-dom? true if you want to also bind to the dom
  "
  [event-chan binding
   & {:keys [to-dom? parent-el] :or [to-dom? false parent-el nil]}]
  (debug "Binding event to" (:name event-chan) "\n"
         "Args:\n"
         :event-chan event-chan "\n"
         :binding binding "\n"
         :to-dom? to-dom? "\n"
         :parent-el parent-el)
  (swap! (:events event-chan)
         #(into % (if (and to-dom? (boolean parent-el))
                    (bind-dom-events! (:name event-chan)
                                      (:channel event-chan)
                                      [binding]
                                      parent-el)
                    [binding]))))

(defn unbind-event!
  "Remove an event currently bound to an event loop. 
   Calls the :remove-fn if it is present"
  [event-chan event-id selector]
  (debug "Un-binding" event-id
         "from" selector
         "in" (:name event-chan) "event loop")
  (let [all-events (:events event-chan)
        match? #(and (= (:event %) event-id)
                     (= (:selector %) selector))
        not-match? #(or (not= (:event %) event-id)
                        (not= (:selector %) selector))        
        events (filter match? @all-events)]
    (doseq [{:keys [remove-fn]} events]
      (when remove-fn (remove-fn)))
    (swap! all-events #(filter not-match? %))))

(def global-event-chan
  (mk-event-chan
   "global"
   []
   (.-body js/document)))

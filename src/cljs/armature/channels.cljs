(ns armature.channels
  (:use [armature.logging :only [debug]])
  (:require [cljs.core.async :refer [chan close! <! >!]])
  (:require-macros [cljs.core.async.macros :refer [go]]))

;; TODO a fan out channel that takes a mutable list of channels to
;; send messages to before removing the messsage



(defn mk-loop
  "Create a loop over a channel calling fn on every message"
  [ch f]
  (go (loop [msg (<! ch)]
        (f msg)
        (recur (<! ch))))
  ch)

(defn fan-out-channel
  "Returns a hashmap channel and subscribers where messages are also enqueued 
   to all channels in the subscribers atom"
  [subscribers]
  (mk-loop (chan) #(doseq [sub @subscribers] (go (>! (:channel sub) %)))))

(defn mk-chan []
  (let [subscribers (atom [])
        ch (fan-out-channel subscribers)]
    {:channel ch :subscribers subscribers :id (gensym "chan__")}))

(defn subscribe
  "Subscribe ch1 to ch2 such that each message that goes to ch1 goes to ch2
   Optionally pass in a function that takes one arg used to filter messages."
  [pub-ch sub-ch & {:keys [f] :or {f nil}}]
  ;; TODO allow a filter function to control what goes in to the chan
  (swap! (:subscribers pub-ch) conj sub-ch))

(defn unsubscribe
  [pub-chan sub-chan]
  ;; TODO filter for the id of the chan and remove it
  ;;(swap! (:subscribers chan) sub-chan)
  )

(defn publish
  "Asnychronously publish a message, msg, to channel ch"
  [ch msg]
  (go (>! (:channel ch) msg)))

(defn consume-every 
  "Consume every message from channel ch that matches event-id selector
   and call fn with the message as the argument."
  [ch-name ch event-id selector f]
  (debug "Consuming every" event-id selector "on" ch-name)
  (mk-loop ch #(when (and (= (:event-id %) event-id)
                          (= (:selector %) selector))
                 (do (debug "Matched event" event-id
                            "from" selector
                            "on" ch-name)
                     (f %)))))



;;Test for fan out
(def pub1 (mk-chan))
(def ch1 (mk-chan))
(def ch2 (mk-chan))
(subscribe pub1 ch1)
(subscribe pub1 ch2)
(mk-loop (:channel ch1) #(debug "ch1 recieved msg" %))
(mk-loop (:channel ch2) #(debug "ch2 recieved msg" %))
(publish pub1 "test")

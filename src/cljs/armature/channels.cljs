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
  "Returns a channel where messages are also enqueued to all channels
   in the subscribers atom"
  [subscribers]
  (mk-loop (chan) #(doseq [sub @subscribers] (go (>! sub %)))))

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

;; Test for fan out
;; (def ch1 (chan))
;; (def ch2 (chan))
;; (def test-fan-out
;;   (fan-out-channel (atom [ch1 ch2])))
;; (mk-loop ch1 #(debug "ch1 recieved msg" %))
;; (mk-loop ch2 #(debug "ch2 recieved msg" %))
;; (go (>! test-fan-out "test"))

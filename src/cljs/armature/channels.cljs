(ns armature.channels
  (:use [armature.logging :only [debug]])
  (:require [cljs.core.async :refer [chan close! <! >!]])
  (:require-macros [cljs.core.async.macros :refer [go]]))


(defn mk-loop
  "Create a loop over a channel calling fn on every message"
  [ch f]
  (go (loop [msg (<! ch)]
        (f msg)
        (recur (<! ch))))
  ch)

(defn fan-out-channel
  "Returns a channel where messages are also enqueued 
   to all channels in the subscribers atom"
  [subscribers]
  (let [in-ch (chan)
        out-ch (chan)]
    (mk-loop in-ch #(do (when-not (empty? @subscribers)
                          (doseq [sub @subscribers]
                            (debug "enqueuing" % "to" sub)
                            (publish sub %)))
                        ;; We always publish to the out channel
                        (publish out-ch %)))
    [in-ch out-ch]))

(defn mk-chan [& {:keys [subscribers] :or {subscribers nil}}]
  (let [subs (atom subscribers)
        [in-ch out-ch] (fan-out-channel subs)]
    {:in in-ch :out out-ch :subscribers subs :id (gensym "chan__")}))

(defn subscribe
  "Subscribe ch1 to ch2 such that each message that goes to ch1 goes to ch2
   Optionally pass in a function that takes one arg used to filter messages."
  [pub-ch sub-ch]
  ;; TODO allow a filter function to control what goes in to the chan
  (swap! (:subscribers pub-ch) conj sub-ch))

(defn unsubscribe
  [pub-ch sub-ch]
  (swap! (:subscribers pub-ch)
         (fn [x]
           (filter #(not= (:id %) (:id sub-ch)) x)))
  sub-ch)

(defn publish
  "Asnychronously publish a message, msg, to channel ch"
  [ch msg]
  (if (:in ch)
    (go (>! (:in ch) msg))
    (go (>! ch msg))))

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



;;Test for pub sub

(def ch1 (mk-chan))
(def ch2 (mk-chan))
(def ch3 (mk-chan))

(def pub1 (mk-chan :subscribers [ch1]))
(publish pub1 "test2")
;; This channel already has a function called in a loop on it's input,
;; we need to embed our function to the subscribe
;; (consume-every "testing"
;;                ch1
;;                :click "#testing"
;;                #(debug "ch1 recieved msg" %))


(mk-loop (:out ch1) #(debug "ch1 recieved msg" %))
;;(mk-loop (:channel ch2) #(debug "ch2 recieved msg" %))
;;(mk-loop (:channel ch3) #(debug "ch3 recieved msg" %))

;;(unsubscribe pub1 ch1)
;;(publish pub1 "test")


(ns armature.example.core
  (:use [armature.logging :only [debug info warn error]])
  (:require [dommy.core :as dom]
            [armature.events :as ev])
  (:use-macros [dommy.macros :only [node sel sel1]]))


(def top-nav
  [:div#top-nav [:h1#logo "Armature"]])

(def app-main
  [:div#main "hello this is the main"])

(defn init-html! []
  (info "Initializing base html")
  (dom/append! (sel1 :body) top-nav)
  (dom/append! (sel1 :body) app-main))

(defn reset-html!
  "Reload dom first then bind events"
  []
  (info "Resetting html")
  (try (do (dom/remove! (sel1 :#top-nav))
           (dom/remove! (sel1 :#main)))
       (catch js/Error e nil))
  (init-html!)
  (reset! (:events ev/global-event-loop) [])
  (ev/bind-event! ev/global-event-loop
                  {:selector "#logo"
                   :event "click"
                   :callback #(debug "Callback #logo click" %)}
                  :to-dom? true
                  :parent-el (.-body js/document)))

(reset-html!)


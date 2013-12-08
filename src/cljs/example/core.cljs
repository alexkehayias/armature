(ns armature.example.core
  (:use [armature.logging :only [debug info warn error]])
  (:use-macros [dommy.macros :only [node sel sel1]])
  (:require [clojure.browser.repl :as repl]
            [dommy.core :as dom]
            [armature.channels :as ch]
            [armature.events :as ev]))


(def top-nav
  [:div#top-nav [:h1#logo "Armature"]])

(def app-main
  [:div#main "hello this is the main"])

(defn init-html! []
  (info "Initializing base html")
  (dom/append! (sel1 :body) top-nav)
  (dom/append! (sel1 :body) app-main))

(defn reset-app!
  "Reload dom first then bind events"
  []
  (info "Resetting html")
  (try (do (dom/remove! (sel1 :#top-nav))
           (dom/remove! (sel1 :#main)))
       (catch js/Error e (error e)))
  (init-html!)
  (reset! (:events ev/global-event-chan) [])
  (ev/bind-event! ev/global-event-chan
                  {:selector "h1#logo"
                   :event "click"}
                  :to-dom? true
                  :parent-el (.-body js/document))
  (ch/consume-every (:name ev/global-event-chan)
                    (:channel ev/global-event-chan)
                    "click" "h1#logo"
                    #(debug %)))

(reset-app!)

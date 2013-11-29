(ns armature.server
  (:require [cemerick.austin.repls :refer (browser-connected-repl-js)]
            [net.cgrand.enlive-html :as enlive]
            [compojure.route :refer (resources)]
            [compojure.core :refer (GET defroutes)]  
            ring.adapter.jetty
            [clojure.java.io :as io])) 

(enlive/deftemplate homepage
  (io/resource "homepage.html")
  [])

(enlive/deftemplate app
  (io/resource "app.html")
  []
  [:body] (enlive/append
            (enlive/html [:script (browser-connected-repl-js)])))

(defroutes site
  (resources "/")
  (GET "/app" req (app))
  (GET "/" req (homepage)))

(defn run
  []
  (defonce server
    (ring.adapter.jetty/run-jetty #'site {:port 9000 :join? false}))
  #(.stop server))

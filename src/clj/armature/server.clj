(ns armature.server
  (:use [armature.dev :only [reset-brepl-env! connect-to-brepl]])
  (:require [cemerick.austin.repls :refer (browser-connected-repl-js)]
            [net.cgrand.enlive-html :as enlive]
            [compojure.route :refer (resources)]
            [compojure.core :refer (GET defroutes)]  
            ring.adapter.jetty
            [clojure.java.io :as io])) 



(enlive/deftemplate homepage "homepage.html" [])

(enlive/deftemplate app
  (io/resource "app.html")
  []
  [:body] (enlive/append
           (enlive/html [:script (browser-connected-repl-js)]
                        [:link {:src "/css/master.css"}])))

(defroutes site
  (resources "/")
  (GET "/app" req (app))
  (GET "/*" req (homepage)))

(defonce server
  (let [server (ring.adapter.jetty/run-jetty #'site {:port 9000 :join? false})]
    (connect-to-brepl (reset-brepl-env!))
    #(.stop server)))

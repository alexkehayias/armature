(ns armature.server
  (:use [armature.dev :only [reset-brepl-env! connect-to-brepl]])
  (:require [cemerick.austin.repls :refer (browser-connected-repl-js)]
            [net.cgrand.enlive-html :as enlive]
            [compojure.route :refer (resources files)]
            [compojure.core :refer (GET defroutes)]  
            ring.adapter.jetty)) 

(def project-root
  (str (System/getProperty "user.dir") "/public"))

(enlive/deftemplate homepage "homepage.html" []
  [:head] (enlive/append
           (enlive/html [:link {:rel "stylesheet"
                                :type "text/css"
                                :href "/static/styles/master.css"}])) )

(enlive/deftemplate app "app.html"
  []
  [:head] (enlive/append
           (enlive/html [:link {:rel "stylesheet"
                                :type "text/css"
                                :href "/static/styles/master.css"}]))
  [:body] (enlive/append
           (enlive/html [:script (browser-connected-repl-js)])))

(defroutes site
  (files "/static" {:root project-root})
  (GET "/app" req (app))
  (GET "/*" req (homepage)))

(defonce server
  (let [server (ring.adapter.jetty/run-jetty #'site {:port 9000 :join? false})]
    ;;(connect-to-brepl (reset-brepl-env!))
    #(.stop server)))

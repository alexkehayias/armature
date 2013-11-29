(ns armature.dev
  (:use armature.server)
  (:require
   [cemerick.austin :as brepl-env]
   [cemerick.austin.repls :as brepl]))


(defn bootstrap-browser-repl! []
  (run)
  (def repl-env (reset! brepl/browser-repl-env
                        (brepl-env/repl-env)))
  (brepl/cljs-repl repl-env))

(bootstrap-browser-repl!)

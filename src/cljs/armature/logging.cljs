(ns armature.logging)

(def log-level (atom :info))

;; TODO make sure that the client supports console.log
;; TODO make a macro that takes any number of args and applies it to
;; .log js/console
;; TODO respect the current logging level
(defn log [level & args]
  (let [level-str (-> (str level)
                      (.replace ":" "")
                      .toUpperCase)]
    (.log js/console
          (str level-str ":")
          (-> args .-arr (.join " ")))))

(defn debug [& args]
  (apply log (conj args :debug)))

(defn info [& args]
  (apply log (conj args :info)))

(defn warn [& args]
  (apply log (conj args :warn)))

(defn error [& args]
  (apply log (conj args :error)))

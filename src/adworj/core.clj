(ns adworj.core)

;; TODO: We seriously need something that can catch and retry random AdWords API errors.

(defn jtoc-name
  "Convert a Java name to a Clojure name"
  [jname]
  (let [x (apply str (map #(if (Character/isUpperCase %) (str \- %) %) (seq jname)))]
    (clojure.string/lower-case (if (.startsWith x "-") (subs 1 x) x))))

(defn jtoc-keyword-name
  [jname]
  (str ":" (jtoc-name jname)))

(defn jtoc-predicate-name
  [jname]
  (str (jtoc-name jname) "?"))

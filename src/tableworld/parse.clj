(ns tableworld.parse
  (:require [clojure.string :as str]))

(defn pull-sections [s]
  (into {} (map (juxt (comp keyword str/lower-case second)
                      #(nth % 2))
                (re-seq #"(?m)^#\s*(.*?)\s*\n((?:.*\n)*?)(?=\n#|$)"
                        s))))

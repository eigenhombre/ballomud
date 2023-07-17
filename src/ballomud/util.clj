(ns ballomud.util
  (:require [clojure.string :as str]))

(defn remove-edge-quotes [s]
  (-> s
      (str/replace #"^(?:\'|\")" "")
      (str/replace #"(?:\'|\")$" "")))

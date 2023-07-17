(ns ballomud.reader-test
  (:require [ballomud.reader :as reader]
            [clojure.test :refer [deftest is]]
            [clojure.java.io :as io]))

(deftest read-from-string-test
  (is (->> "world.yml"
           io/resource
           slurp
           reader/read-from-string)))

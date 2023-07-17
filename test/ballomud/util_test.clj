(ns ballomud.util-test
  (:require [ballomud.util :refer :all]
            [clojure.test :refer [deftest testing are is]]))

(deftest remove-edge-quotes-test
  (are [s want]
    (= want (remove-edge-quotes s))
    "" ""
    "a" "a"
    "'a'" "a"
    "'a" "a"
    "a'" "a"
    "a'a" "a'a"))


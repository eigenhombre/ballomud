(ns ballomud.core-test
  (:require [ballomud.parse :as parse]
            [clojure.java.io :as io]
            [clojure.test :refer [deftest testing is]]
            [clojure.set :as set]))

(deftest basic-parse
  (let [txt (slurp (io/resource "world.tw"))
        result (parse/pull-sections txt)]
    (is (set/subset? #{:rooms :map} (set (keys result))))))

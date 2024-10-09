(ns ballomud.event-test
  (:require [clojure.test :refer [deftest testing is]]
            [ballomud.event :refer :all])
  (:import [java.util.concurrent LinkedBlockingQueue]))

(deftest events-simple
  (let [q (make-event-queue "game events")
        test-event (->room-change-event "Muad'dib" :caladan :arrakis)]
    (is (nil? (next-event! q)))
    (add-event! q test-event)
    (is (= test-event (next-event! q)))
    (is (nil? (next-event! q)))))

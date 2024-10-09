(ns ballomud.event
  (:import [java.util.concurrent LinkedBlockingQueue]))

(defn make-event-queue [name]
  (LinkedBlockingQueue.))

(defn has-events? [q]
  (not= 0 (.size q)))

(defn add-event! [q e]
  (.add q e))

(defn next-event! [q]
  (.poll q))

(defrecord room-change-event [player-name oldroom newroom])

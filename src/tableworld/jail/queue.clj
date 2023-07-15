(ns tableworld.jail.queue
  (:import [java.util.concurrent LinkedBlockingQueue]))

(def event-queue (LinkedBlockingQueue.))

(defn enqueue [item]
  (.put event-queue item))

(defn dequeue []
  (.poll event-queue 100 java.util.concurrent.TimeUnit/MILLISECONDS)
  ;; (.take event-queue)
  )

(def stop (atom false))

(def event-loop (atom nil))

(defn start-event-loop []
  (reset! event-loop
          (future
            (while (not @stop)
              (let [{:keys [player action content]} (dequeue)])
              (Thread/sleep 100)))))

(defn stop-event-loop []
  (when @event-loop
    (while (not (realized? @event-loop))
      (reset! stop true)
      (Thread/sleep 100))))

(comment
  (.size event-queue)
  (def zz (future (dequeue)))
  (realized? zz)
  (enqueue :hello))

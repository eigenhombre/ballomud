(ns ballomud.integration-test
  (:require [ballomud.core :as b]
            [clojure.test :refer [deftest is are testing]]
            [clojure.core.server :as server]))

(defn configure-socket! [socket]
  (.setSoTimeout socket 100))

(defn send-output-to-socket! [socket bytes]
  (doto (.getOutputStream socket)
    (.write bytes)
    (.flush)))

(defn recv-socket-output! [socket]
  (with-open [reader (-> socket
                         .getInputStream
                         java.io.InputStreamReader.)]
    (let [sb (StringBuilder.)]
      (loop []
        (let [c (try
                  (.read reader)
                  (catch java.net.SocketTimeoutException _
                    -1))]
          (when (not (neg? c))
            (.append sb (char c))
            (recur))))
      (.toString sb))))

(deftest integration
  (with-out-str
    (b/main "localhost" 8888 true true nil))
  ;; Establish socket connection to server:
  (with-open [socket (java.net.Socket. "localhost" 8888)]
    (configure-socket! socket)
    (is (.isConnected socket) "Socket should be connected")
    ;; send CR ...:
    (send-output-to-socket! socket (byte-array [13]))
    (is (.contains (recv-socket-output! socket) ">>> ")))
  (server/stop-server "ballomud"))

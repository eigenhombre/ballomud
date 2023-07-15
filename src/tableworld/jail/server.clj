(ns tableworld.server
  (:require [clojure.java.io :as io])
  (:import [java.net ServerSocket]
           [java.nio.channels SocketChannel]))

;; Prototype of another server

(defn handle-connection [server-socket accept-socket]
  ;; Send a short message to the client:
  (let [out (io/writer accept-socket)]
    (.write out ">>> ")
    (.flush out))
  ;; ;; See if we can read a line from the client:
  ;; (let [in (io/reader accept-socket)]
  ;;   (println (.readLine in)))
  ;; See if we can read a single keypress from the client, without
  ;; waiting for a newline... using NIO:
  (let [in (.open (SocketChannel/open))]
    (.configureBlocking in false)
    (.connect in (.getRemoteSocketAddress accept-socket))
    ;; read a single keystroke within 5 seconds, or give up
    ;; and close the connection:
    (let [buffer (java.nio.ByteBuffer/allocate 1)]
      (loop [i 0]
        (when (and (.isConnected in) (< i 10))
          (println "Got a keypress!")
          (.read in buffer)
          (Thread/sleep 1000)
          (recur (dec i))))
      (println (String. (.array buffer)))))
  (let [out (io/writer accept-socket)]
    (.write out "OK\n")
    (.flush out))

  (println "Got connection!" server-socket accept-socket))

(defn make-server [port]
  (let [sock (ServerSocket. port)
        stop (atom false)]
    (future
      (while (not @stop)
        (handle-connection sock (.accept sock))))
    (fn []
      (println "Shutting down.")
      (.close sock)
      (reset! stop true))))

(comment

  (stop-fn)
  (def stop-fn (make-server 8887)))

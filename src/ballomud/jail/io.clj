(ns ballomud.io
  (:import [java.io OutputStream InputStream]
           [java.net ServerSocket])
  (:require [clojure.string :as str]))

(def IAC 255) ;; Interpret As Command
(def DONT 254)
(def DO 253)
(def WONT 252)
(def WILL 251)
(def ECHO 1)
(def SUPPRESS_GO_AHEAD 3)

(def ok-to-write (atom true))

(defn- to-byte [n]
  (byte (if (> n 127)
          (- n 256)
          n)))

(def EOF -1)

(defn negotiate-telnet-options [^OutputStream out]
  (.write out (byte-array (map to-byte [IAC DO SUPPRESS_GO_AHEAD])))
  (.write out (byte-array (map to-byte [IAC WILL SUPPRESS_GO_AHEAD])))
  (.write out (byte-array (map to-byte [IAC WONT ECHO])))
  (.flush out))
(defn negotiate-telnet-options [^OutputStream out]
  (.write out (byte-array (map to-byte [IAC DO SUPPRESS_GO_AHEAD])))
  (.write out (byte-array (map to-byte [IAC WILL SUPPRESS_GO_AHEAD])))
  (.write out (byte-array (map to-byte [IAC WONT ECHO])))
  (.flush out))

(defn handle-telnet-command [^InputStream in]
  (let [command (.read in)]
    (when (not= EOF command)
      (let [option (.read in)]
        (when (not= EOF option)
          (println (str "Received Telnet command: " command " " option)))))))

(defn- pr [out s]
  (.write out (.getBytes s)))

(defn get-input-line [client-socket ok-to-write]
  (let [in (.getInputStream client-socket)
        out (.getOutputStream client-socket)]
    (negotiate-telnet-options out)
    (loop [input-char (.read in)
           out ""]
      (when-not (= EOF input-char)
        (if (= input-char IAC)
          (do
            (handle-telnet-command in)
            (recur (.read in) out))
          (let [ch (char input-char)]
            (if (or (= ch "\n") (zero? input-char))
              (do
                (reset! ok-to-write true)
                out)
              (do
                (reset! ok-to-write false)
                (recur (.read in) (str out ch))))))))))

(defn test-telnet [port]
  (with-open [server-socket (ServerSocket. port)]
    (println (str "Telnet server listening on port " port))
    (with-open [client-socket (.accept server-socket)]
      (println (str "Connection from " (.getInetAddress client-socket)))
      (let [in (.getInputStream client-socket)
            out (.getOutputStream client-socket)]
        (negotiate-telnet-options out)
        (future
          (dotimes [_ 100]
            (when @ok-to-write
              (pr out (str (str/join " "
                                     (repeat (inc (rand-int 5))
                                             "Boo."))
                           "\n")))
            (Thread/sleep (rand-int 1000))))
        (pr out "Welcome to the Telnet Server. Press 'q' to quit.\r\n")
        (loop []
          (let [line-in (get-input-line client-socket ok-to-write)]
            (pr out (format "Got: '%s'\r\n" line-in))
            (if (.startsWith line-in "q")
              (pr out "Ok, bye!")
              (recur))))))))

#_(defn test-telnet [port]
    (with-open [server-socket (ServerSocket. port)]
      (println (str "Telnet server listening on port " port))
      (with-open [client-socket (.accept server-socket)]
        (println (str "Connection from " (.getInetAddress client-socket)))
        (let [in (.getInputStream client-socket)
              out (.getOutputStream client-socket)]
          ;; Negotiate Telnet options
          (negotiate-telnet-options out)
          (.write
           out
           (.getBytes "Welcome to the Telnet Server. Press 'q' to quit.\r\n"))
          (.flush out)
          ;; Handle input
          (loop [input-char (.read in)]
            (if (not= -1 input-char)
              (if (= input-char IAC)
                (do
                  ;; Handle Telnet command
                  (handle-telnet-command in)
                  (recur (.read in)))
                (do
                  (let [ch (char input-char)]
                    ;; Avoid processing echo characters
                    (when (not= ch \newline)
                      (let [response (str "You pressed: " ch "\r\n")]
                        (.write out (.getBytes response))
                        (.flush out)))
                    ;; Exit on 'q'
                    (if (= ch \q)
                      (do
                        (println "Client disconnected")
                        (.write out (.getBytes "Goodbye!\r\n"))
                        (.flush out))
                      (recur (.read in))))))))
          (println "Client disconnected")))))

(def f
  (future (test-telnet 8080)))

(println f)
(realized? f)

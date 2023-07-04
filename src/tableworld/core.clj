(ns tableworld.core
  (:gen-class)
  (:require [clojure.java.io :as io]
            [clojure.core.server :as server]
            [clojure.string :as str]))

(defn handle-command [command]
  (cond
    (= command "hello") "Hello, world!"
    (= command "time") (str "Current time is: " (java.util.Date.))
    :else "Unknown command"))

(defn is-quit? [cmd]
  (= cmd "quit"))

(defn accept
  []
  (loop []
    (print ">>> ")
    (flush)
    (let [command (str/trim (str (read-line)))]
      (if (seq command)
        (when-not (is-quit? command)
          (let [response (handle-command command)]
            (println response)
            (recur)))
        (recur)))))

(defn start-server [name port daemon?]
  (server/start-server {:name name
                        :port port
                        :accept 'tableworld.core/accept
                        :server-daemon daemon?}))

(defn -main
  [& {:keys [name port]
      :or   {name "default"
             port 9999}}]
  (print (format "Server name: '%s'  port: %d.  Accepting connections...." name port))
  (flush)
  (start-server name port false))

(comment
  (server/stop-server "foo")
  (start-server "foo" 9999 true))

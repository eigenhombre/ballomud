(ns tableworld.core
  (:gen-class)
  (:require [clojure.core.server :as server]
            [clojure.java.io :as io]
            [clojure.pprint :refer [pprint]]
            [clojure.string :as str]
            [tableworld.parse :as parse]))

(defn handle-command [command]
  (cond
    (= command "hello") "Hello, world!"
    (= command "time") (str "Current time is: " (java.util.Date.))
    :else (format "Unknown command: '%s'." command)))

(defn is-quit? [cmd]
  (= cmd "quit"))

(defn iseof [s]
  (and (= (first s) (char 4))
       (= 1 (count s))))

(defn accept [world]
  (pprint @world)
  (loop []
    (print ">>> ")
    (flush)
    (let [command (str/trim (str (read-line)))]
      (cond
        (iseof command) nil  ;; done
        (seq command) (when-not (is-quit? command)
                        (let [response (handle-command command)]
                          (println response)
                          (recur)))
        :t (recur)))))

(defn start-server [name port daemon? world]
  (server/start-server {:name name
                        :port port
                        :accept 'tableworld.core/accept
                        :args [world]
                        :server-daemon daemon?}))

(defn -main
  [& {:keys [name port]
      :or   {name "default"
             port 9999}}]
  (let [world (atom
               (parse/parse-world (slurp (io/resource "world.tw"))))]
    (print
     (format "Server name: '%s'  port: %d.  Accepting connections...."
             name port))
    (flush)
    (start-server name port false world)))

(comment
  (server/stop-server "foo")
  (pprint @(atom
            (parse/parse-world (slurp (io/resource "world.tw")))))
  (start-server "foo" 9999 true
                (atom
                 (parse/parse-world (slurp (io/resource "world.tw")))))

  (slurp (io/resource "world.tw")))

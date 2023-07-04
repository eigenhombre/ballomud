(ns tableworld.core
  (:gen-class)
  (:require [clojure.core.server :as server]
            [clojure.java.io :as io]
            [clojure.pprint :refer [pprint]]
            [clojure.string :as str]
            [tableworld.parse :as parse]))

;; "accessors" for world:
(defn location [world]
  (get-in @world [:player :location]))

(defn current-room [world]
  (let [loc-id (location world)]
    (get-in @world [:rooms loc-id])))

(defn shortdesc [world]
  (:shortdesc (current-room world)))

(defn describe-location [world]
  (:desc (current-room world)))

(defn handle-command [command world]
  (cond
    (= command "help") "Available: look help hello time"
    (= command "hello") "Hello!"
    (= command "look") (describe-location world)
    (= command "time") (str "Current time is: " (java.util.Date.))
    :else (format "I don't understand '%s'." command)))

(defn is-quit? [cmd]
  (= cmd "quit"))

(defn iseof [s]
  (and (= (first s) (char 4))
       (= 1 (count s))))

(defn accept [world]
  (loop []
    (print ">>> ")
    (flush)
    (let [command (str/trim (str (read-line)))]
      (cond
        (iseof command) nil  ;; done
        (seq command) (when-not (is-quit? command)
                        (let [response (handle-command command world)]
                          (println response)
                          (recur)))
        :else (recur)))))

(defn start-server [name port daemon? world]
  (server/start-server {:name name
                        :port port
                        :accept 'tableworld.core/accept
                        :args [world]
                        :server-daemon daemon?}))

(defn- main [name port daemon?]
  (let [world (atom
               (assoc (parse/parse-world (slurp (io/resource "world.tw")))
                      :player {:location "hearth"}))]
    (print
     (format "Server name: '%s'  port: %d.  Accepting connections...."
             name port))
    (flush)
    (start-server name port daemon? world)))

(defn -main
  [& {:keys [name port]
      :or   {name "default"
             port 9999}}]
  (main name port false))

(comment
  (pprint @(atom
            (parse/parse-world (slurp (io/resource "world.tw")))))
  (server/stop-server "foo")
  (main "foo" 9999 true))

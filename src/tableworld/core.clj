(ns tableworld.core
  (:gen-class)
  (:require [clojure.core.server :as server]
            [clojure.java.io :as io]
            [clojure.pprint :refer [pprint]]
            [clojure.string :as str]
            [tableworld.parse :as parse]))

;; For REPL hot reloading:
(defonce live (atom false))
(comment (reset! live true))


;; "accessors" for world:
(defn location [player-name world]
  (get-in @world [:players player-name :location]))

(defn current-room [player-name world]
  (let [loc-id (location player-name world)]
    (get-in @world [:rooms loc-id])))

(defn shortdesc [player-name world]
  (:shortdesc (current-room player-name world)))

(defn describe-location [player-name world]
  (:desc (current-room player-name world)))

(defn handle-command [player-name command world]
  (cond
    (= command "help") "Available: look help hello time quit"
    (= command "hello") (format "Hello, %s!" player-name)
    (= command "dump") (pprint @world)  ;; hidden
    (= command "look") (describe-location player-name world)
    (= command "time") (str "Current time is: " (java.util.Date.))
    :else (format "Sorry, %s, I don't understand '%s'." player-name command)))

(defn is-quit? [cmd]
  (= cmd "quit"))

(defn iseof [s]
  (and (= (first s) (char 4))
       (= 1 (count s))))


(defn disconnect [world player-name]
  (swap! world update :players dissoc player-name))

(defn accept [world]
  (loop []
    (print "What is your name? ")
    (flush)
    (let [player-name (read-line)]
      (if (get-in @world [:players player-name])
        (do
          (printf "Player name %s is taken!\n" player-name)
          (flush)
          (recur))
        (do
          (swap! world update :players assoc player-name {:location "hearth"
                                                          :name player-name})
          (printf "Welcome to Table World, %s.\n" player-name)
          (loop []
            (print ">>> ")
            (flush)
            (let [command (str/trim (str (read-line)))]
              (cond
                (iseof command) (disconnect world player-name)
                (seq command) (if (is-quit? command)
                                (disconnect world player-name)
                                (let [response (handle-command player-name
                                                               command
                                                               world)]
                                  (println response)
                                  (recur)))
                :else (recur)))))))))

(defn start-server [name port daemon? world]
  (server/start-server {:name name
                        :port port
                        :accept 'tableworld.core/accept
                        :args [world]
                        :server-daemon daemon?}))

(defn- main [name port daemon?]
  (let [world (atom
               (parse/parse-world (slurp (io/resource "world.tw"))))]
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

(when @live
  (pprint @(atom
            (parse/parse-world (slurp (io/resource "world.tw")))))
  (server/stop-server "foo")
  (main "foo" 9999 true))

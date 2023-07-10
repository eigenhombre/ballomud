(ns tableworld.core
  (:gen-class)
  (:require [clj-wrap-indent.core :as wrap]
            [clojure.core.server :as server]
            [clojure.java.io :as io]
            [clojure.pprint :refer [pprint]]
            [clojure.string :as str]
            [tableworld.model :as m]
            [tableworld.parse :as parse]))

;; For REPL hot reloading:
(defonce live (atom false))
(comment (reset! live true))

(defn- remove-edge-quotes [s]
  (-> s
      (str/replace #"^(?:\'|\")" "")
      (str/replace #"(?:\'|\")$" "")))

(defn say [player-name args]
  (let [content (->> args
                     (map remove-edge-quotes)
                     (str/join " "))]
    ;; (enqueue {:player player-name
    ;;           :action :say
    ;;           :content content})
    (format "You say: '%s'.\n" content)))

(defn handle-command [player-name command world]
  (let [[command & args] (str/split command #"\s")]
    (cond
      (= command "help") "Available:
    dump
    hello
    help
    look
    quit
    say
    time
    n     or north
    s     or south
    e     or east
    w     or west
"
      (= command "hello") (format "Hello, %s!" player-name)
      (= command "dump") (pprint @world)
      (= command "look") (m/describe-player-location player-name
                                                     @world
                                                     :detailed)
      (= command "time") (str "Current time is: " (java.util.Date.))
      (= command "say") (say player-name args)
      (#{"e" "east"
         "n" "north"
         "s" "south"
         "w" "west"} command)
      (let [{:keys [error status]}
            (m/try-to-move-player!
             player-name
             (keyword (get {"east" "e"
                            "north" "n"
                            "south" "s"
                            "west" "w"} command command))
             world)]
        (if (= status :fail)
          (get m/ttmp-error-descriptions error "Unknown error")
          (m/describe-player-location player-name @world)))
      :else (format "Sorry, %s, I don't understand '%s'. Type 'help' for help."
                    player-name
                    command))))

(defn is-quit? [cmd]
  (= cmd "quit"))

(defn iseof [s]
  (and (= (first s) (char 4))
       (= 1 (count s))))

(defn disconnect [world player-name]
  (m/del-player! player-name world))

(defn splash []
  (println (slurp (io/resource "art.txt"))))

(defn wrap [s]
  (wrap/wrap-indent s 50 2))

(defn do-something-random-later []
  (future
    (Thread/sleep (rand-int 10000))
    (when (zero? (rand-int 3))
      (println
       (str "\n"
            (wrap (rand-nth ["You hear a soft breeze blowing."
                             "Your stomach grumbles."
                             "A fly buzzes unseen nearby."
                             "Someone coughs."]))))
      (print ">>> ")
      (flush))))

(defn do-player-loop [player-name world]
  (printf "Welcome to Table World, %s.\n" player-name)
  (m/add-player! player-name "hearth" world)
  (println (wrap (m/describe-player-location player-name
                                             @world
                                             :detailed)))
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
                          (println (wrap response))
                          (do-something-random-later)
                          (recur)))
        :else (recur)))))

(defn accept [skip-intro? world]
  (splash)
  (when-not skip-intro?
    (print "Are you a bot?  Type 'n' or 'no' if not... ")
    (flush))
  (when (or skip-intro?
            (#{"n" "no"} (-> (read-line)
                             str/trim
                             str/lower-case)))
    (loop []
      (when-not skip-intro?
        (print "What is your name? ")
        (flush))
      (let [player-name (if skip-intro?
                          "Mr.Tester"
                          (str/trim (read-line)))]
        (cond
          (re-find #"\s" player-name)
          (do
            (println "Sorry, no whitespace in player names (for now).")
            (recur))

          (m/player-exists player-name world)
          (do
            (printf "Player name %s is taken!\n" player-name)
            (flush)
            (recur))

          (empty? player-name) (recur)

          :else
          (do-player-loop player-name world))))))

(defn start-server [host port daemon? skip-intro? world]
  (server/start-server {:name "tableworld"
                        :address host
                        :port port
                        :accept 'tableworld.core/accept
                        :args [skip-intro? world]
                        :server-daemon daemon?}))

(defn- main [host port daemon? skip-intro?]
  (let [world (atom
               (parse/parse-world (slurp (io/resource "world.tw"))))]
    (print
     (format "Server name: '%s'  port: %d.  Accepting connections...."
             host port))
    (flush)
    (start-server host port daemon? skip-intro? world)))

(defn -main [& [host port skip-intro?]]
  (let [host (or host "localhost")
        port (Integer. (or port "9999"))]
    (main host port false false)))

(when @live
  (pprint @(atom
            (parse/parse-world (slurp (io/resource "world.tw")))))
  (server/stop-server "tableworld")
  (main "localhost" 9999 true true))

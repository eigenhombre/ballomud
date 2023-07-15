(ns tableworld.core
  (:gen-class)
  (:require [clj-wrap-indent.core :as wrap]
            [clojure.core.match :refer [match]]
            [clojure.core.server :as server]
            [clojure.java.io :as io]
            [clojure.pprint :as pprint]
            [clojure.string :as str]
            [tableworld.model :as m]
            [tableworld.parse :as parse]
            [tableworld.yml :refer [world-src]]))

;; For REPL hot reloading:
(defonce live (atom false))
(comment (reset! live true))

(defn- remove-edge-quotes [s]
  (-> s
      (str/replace #"^(?:\'|\")" "")
      (str/replace #"(?:\'|\")$" "")))

(def stdouts (atom {}))

(defn wrap [s]
  (wrap/wrap-indent s 50 2))

(defn async-println [content]
  (println (str "\n" (wrap content)))
  (print ">>> ")
  (flush))

(comment
  (doseq [[pl out] @stdouts]
    (binding [*out* out]
      (async-println "Test of broadcasting from the REPL"))))

(defn broadcast [player-name content]
  (doseq [[pl out] @stdouts]
    (binding [*out* out]
      (async-println
       (format "%s says: '%s'." player-name content)))))

(defn say [player-name args]
  (let [content (->> args
                     (map remove-edge-quotes)
                     (str/join " "))]
    (broadcast player-name content)
    ""))

(def help "Available:
    dump  -- show entire game state
    hello
    help
    look  -- describe where you are
    quit
    say   -- tell something to everyone
    time  -- get current time
    n     or north
    s     or south
    e     or east
    w     or west
")

(defn dump-state [world]
  (pprint/pprint [@stdouts @world])
  "Done.")

(defn get-time []
  (str "Current time is: " (java.util.Date.)))

(defn try-to-move [player-name direction-str world]
  (let [{:keys [error status]}
        (m/try-to-move-player!
         player-name
         (keyword (get {"east" "e"
                        "north" "n"
                        "south" "s"
                        "west" "w"
                        "up" "u"
                        "down" "d"
                        "dwn" "d"}
                       direction-str
                       direction-str))
         world)]
    (if (= status :fail)
      (get m/ttmp-error-descriptions error "Unknown error")
      (m/describe-player-location player-name @world))))

(defn handle-command [player-name command world]
  (let [command-words (->> (str/split command #"\s")
                           (remove #{"the"})
                           (map str/lower-case))]
    ;; Workaround for https://clojure.atlassian.net/browse/CLJ-1852:
    (or (match [command-words]
          [(["help"] :seq)] help
          [(["help" "me"] :seq)] help
          [(["i" "need" "help"] :seq)] help
          [(["hi"] :seq)] (format "Hello, %s!" player-name)
          [(["hello"] :seq)] (format "Hello, %s!" player-name)
          [(["hi" "there"] :seq)] (format "Hello, %s!" player-name)
          [(["hello" "there"] :seq)] (format "Hello, %s!" player-name)
          [(["hi"] :seq)] (format "Hello, %s!" player-name)
          [(["hello"] :seq)] (format "Hello, %s!" player-name)
          [(["look"] :seq)] (m/describe-player-location player-name
                                                        @world
                                                        :detailed)
          [(["look" "around"] :seq)] (m/describe-player-location player-name
                                                                 @world
                                                                 :detailed)
          [(["dump" "world"] :seq)] (dump-state world)
          [(["show" "game" "state"] :seq)] (dump-state world)
          [(["dump" "game" "state"] :seq)] (dump-state world)
          [(["dump" "world"] :seq)] (dump-state world)
          [(["dump"] :seq)] (dump-state world)
          :else nil)
        (match [command-words]
          [(["time"] :seq)] (get-time)
          [(["current" "time"] :seq)] (get-time)
          [(["show" "current" "time"] :seq)] (get-time)
          [(["what" "is" "current" "time"] :seq)] (get-time)
          [(["say" & something] :seq)] (say player-name something)
          [(["tell" "everyone" & something] :seq)] (say player-name something)
          [(["time"] :seq)] (get-time)
          [(["go" direction] :seq)] (try-to-move player-name direction world)
          [(["go" "to" direction] :seq)]
          (try-to-move player-name direction world)
          ;; FIXME: :or clause?
          [(["n"] :seq)] (try-to-move player-name "n" world)
          [(["s"] :seq)] (try-to-move player-name "s" world)
          [(["e"] :seq)] (try-to-move player-name "e" world)
          [(["w"] :seq)] (try-to-move player-name "w" world)
          [(["u"] :seq)] (try-to-move player-name "u" world)
          [(["d"] :seq)] (try-to-move player-name "d" world)
          [(["north"] :seq)] (try-to-move player-name "n" world)
          [(["south"] :seq)] (try-to-move player-name "s" world)
          [(["east"] :seq)] (try-to-move player-name "e" world)
          [(["west"] :seq)] (try-to-move player-name "w" world)
          [(["up"] :seq)] (try-to-move player-name "u" world)
          [(["down"] :seq)] (try-to-move player-name "d" world)
          :else
          (format "Sorry, %s, I don't understand '%s'. Type 'help' for help."
                  player-name
                  command)))))

(defn is-quit? [cmd]
  (= cmd "quit"))

(defn iseof [s]
  (and (= (first s) (char 4))
       (= 1 (count s))))

(defn disconnect [world player-name]
  (m/del-player! player-name world)
  (swap! stdouts (fn [m]
                   (if (m player-name)
                     (dissoc m player-name)
                     m))))

(defn splash []
  (println (slurp (io/resource "art.txt"))))

(defn do-something-random-later []
  (future
    (Thread/sleep (rand-int 10000))
    (when (zero? (rand-int 3))
      (async-println
       (rand-nth ["You hear a soft breeze blowing."
                  "Your stomach grumbles."
                  "A fly buzzes unseen nearby."
                  "Someone coughs."])))))

(defn do-player-loop [out player-name world]
  (swap! stdouts assoc player-name out)
  (printf "Welcome to BalloMUD, %s.\n" player-name)
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
                          (str "Tester" (rand-int 1000))
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
          (do-player-loop *out* player-name world))))))

(defn start-server [host port daemon? skip-intro? world]
  (server/start-server {:name "ballomud"
                        :address host
                        :port port
                        :accept 'tableworld.core/accept
                        :args [skip-intro? world]
                        :server-daemon daemon?}))

(defn check-all-directions
  "
  Run this after all definitions to look for consistency errors.
  "
  [world]
  (doseq [[place m] (:map world)
          [dir dest] (:neighbors m)]
    (assert (get (:rooms world) dest)
            (format "%s is not a known room!" dest))))

(defn- main [host port daemon? skip-intro?]
  (let [world (atom (world-src))
        #_basta/world
        #_(atom
           (parse/parse-world (slurp (io/resource "world.tw"))))]
    (check-all-directions @world)
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
  #_(pprint @(atom
              (parse/parse-world (slurp (io/resource "world.tw")))))
  (check-all-directions (world-src))
  (server/stop-server "ballomud")
  (main "localhost" 9999 true true))

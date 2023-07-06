(ns tableworld.core
  (:gen-class)
  (:require [clj-wrap-indent.core :as wrap]
            [clojure.core.server :as server]
            [clojure.java.io :as io]
            [clojure.pprint :refer [pprint]]
            [clojure.string :as str]
            [tableworld.parse :as parse]))

;; For REPL hot reloading:
(defonce live (atom false))
(comment (reset! live true))


;; "accessors" for world:
;; FIXME: refactor this so that @ operations are done separately from
;; accessors, etc.
(defn location [player-name world]
  (get-in @world [:players player-name :location]))

(defn current-room [player-name world]
  (let [loc-id (location player-name world)]
    (get-in @world [:rooms loc-id])))

(defn shortdesc [player-name world]
  (:shortdesc (current-room player-name world)))

(defn location-description [player-name world long?]
  ((if long? :desc :shortdesc) (current-room player-name world)))

(defn add-player! [player-name room-id world]
  (swap! world update :players assoc player-name {:location room-id
                                                  :name player-name
                                                  :visited #{room-id}})
  (println (location-description player-name world true)))

(defn set-player-location! [player-name new-room-id world]
  (let [lengthy-description?
        (not (get-in @world [:players player-name :visited new-room-id]))]
    (println "Lengthy?" lengthy-description?)
    (swap! world (fn [world]
                   (when-let [player (get-in world [:players player-name])]
                     (-> world
                         (assoc-in [:players player-name :location] new-room-id)
                         (update-in [:players player-name :visited] conj new-room-id)))))
    (println (location-description player-name world lengthy-description?))))

(comment
  (def world (atom {:rooms {"hearth" {:shortdesc "The hearth."
                                      :desc "The Lorem Hearth, home of the many."}
                            "ship" {:shortdesc "The ship."
                                    :desc "A place of seasickness and spray."}}}))
  (current-room "John" world)
  (location-description "John" world true)
  (add-player! "John" "hearth" world)
  (location "John" world)
  (current-room "John" world)
  (add-player! "Ben" "ship" world)
  (set-player-location! "John" "ship" world)
  (set-player-location! "John" "ship" world))

(defn handle-command [player-name command world]
  (cond
    (= command "help") (str "Available: look help hello time dump quit
n s e w north south east west")
    (= command "hello") (format "Hello, %s!" player-name)
    (= command "dump") (pprint @world)
    (= command "look") (location-description player-name world true)
    (= command "time") (str "Current time is: " (java.util.Date.))
    :else (format "Sorry, %s, I don't understand '%s'. Type 'help' for help."
                  player-name
                  command)))

(defn is-quit? [cmd]
  (= cmd "quit"))

(defn iseof [s]
  (and (= (first s) (char 4))
       (= 1 (count s))))


(defn disconnect [world player-name]
  (swap! world update :players dissoc player-name))

(defn splash []
  (println (slurp (io/resource "art.txt"))))

(defn accept [world]
  (splash)
  (loop []
    (print "What is your name? ")
    (flush)
    (let [player-name (str/trim (read-line))]
      (cond
        (get-in @world [:players player-name])
        (do
          (printf "Player name %s is taken!\n" player-name)
          (flush)
          (recur))

        (empty? player-name) (recur)

        :else
        (do
          (printf "Welcome to Table World, %s.\n" player-name)
          (add-player! player-name "hearth" world)
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
                                  (println (wrap/wrap-indent response 50 2))
                                  (recur)))
                :else (recur)))))))))

(defn start-server [host port daemon? world]
  (server/start-server {:name "tableworld"
                        :address host
                        :port port
                        :accept 'tableworld.core/accept
                        :args [world]
                        :server-daemon daemon?}))

(defn- main [host port daemon?]
  (let [world (atom
               (parse/parse-world (slurp (io/resource "world.tw"))))]
    (print
     (format "Server name: '%s'  port: %d.  Accepting connections...."
             host port))
    (flush)
    (start-server host port daemon? world)))

(defn -main [& [host port]]
  (let [host (or host "localhost")
        port (Integer. (or port "9999"))]
    (main host port false)))

(when @live
  (pprint @(atom
            (parse/parse-world (slurp (io/resource "world.tw")))))
  (server/stop-server "tableworld")
  (main "localhost" 9999 true))

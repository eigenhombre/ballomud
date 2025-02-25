(ns ballomud.session
  (:require [clojure.java.io :as io]
            [clojure.string :as str])
  (:import (java.nio.charset StandardCharsets)))

(defonce session (atom {:socket nil :out nil :in nil :log []}))

(defn open-session [host port]
  "
  Opens a Telnet session and stores it in an atom.
  "
  (when-not (:socket @session)
    (let [socket (java.net.Socket. host port)
          out (io/writer (.getOutputStream socket) :append true)
          in (io/reader (.getInputStream socket))
          raw-in (.getInputStream socket)]
      (.setSoTimeout socket 5000)
      (swap! session assoc
             :socket socket
             :out out
             :in in
             :raw-in raw-in)))
  @session)

(defn read-response []
  "
  Reads available data from the socket's input stream without
  blocking.
  "
  (when-let [{:keys [raw-in]} @session]
    ;; Any data to available to read?
    (let [available (.available raw-in)]
      (if (pos? available)
        (let [buffer (byte-array available)]
          ;; Read only the available bytes
          (.read raw-in buffer)
          ;; Convert bytes to string
          (String. buffer StandardCharsets/UTF_8))
        ""))))  ;; Return empty string if no data

(defn send-command [command]
  "
  Sends a command in the open session and logs the response.
  "
  (when-let [{:keys [socket out]} @session]
    (when (and socket (.isConnected socket))
      ;; Send the command
      (.write out (str command "\n"))
      (.flush out)

      ;; Wait for response; this should be improved
      ;; by waiting/checking in read-response:
      (Thread/sleep 100)

      ;; Read only available data
      (let [response (read-response)]
        (swap! session
               update :log
               conj {:command command
                     :response response})
        response))))

(defn close-session []
  "
  Closes the Telnet session but retains the log.
  "
  (when-let [{:keys [socket out in]} @session]
    (try
      (when socket (.close socket))
      (catch Exception e
        (println "Error closing session:" e))))
  ;; Retain the log but remove socket and streams:
  (swap! session dissoc :socket :out :in :raw-in))

(defn session-transcript []
  "
  Returns the session log as a string.
  "
  (str/join
   (for [{:keys [command response]} (:log @session)]
     (format "%s\n%s" command response))))

(comment
  (swap! session assoc :log [])
  (open-session "localhost" 9999)
  (send-command "n") ;; not a bot
  (send-command (str "Joe" (rand-int 1000)))
  (send-command "look")
  (send-command "pick up flashlight")
  (send-command "south")
  (send-command "inventory")
  (send-command "quit")
  (close-session)
  (println (session-transcript)))

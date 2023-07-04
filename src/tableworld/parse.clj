(ns tableworld.parse
  (:require [clojure.string :as str]
            [clojure.java.io :as io]
            [clojure.walk :as walk]))

(defn pull-sections [s]
  (into {} (map (juxt (comp keyword str/lower-case second)
                      #(nth % 2))
                (re-seq #"#\s*(.*?)\s*\n((?:.*\n)*?)(?=\n#|$)"
                        s))))

(defn- room-block [[id shortdesc-line & desclines]]
  {:id id
   :shortdesc (str/trim shortdesc-line)
   :desc (str/join " " (map str/trim desclines))})

(defn- parse-rooms [rooms]
  (->> (str/split rooms #"\n")
       (partition-by empty?)
       (remove (partial = [""]))
       (map room-block)))

(defn- map-block [[line & lines]]
  (let [[room & neighbors] (str/split line #"\s+")
        others (map #(str/split (str/trim %) #"\s+") lines)]
    {:id room
     :neighbors (walk/keywordize-keys
                 (into {}
                       (map (comp vec reverse) (cons (rest neighbors)
                                                     (map rest others)))))}))

(defn- parse-map [map-string]
  (->> (str/split map-string #"\n")
       (partition-by empty?)
       (remove (partial = [""]))
       (map map-block)))

(defn parse-world [world-string]
  (let [secs (pull-sections world-string)]
    (-> secs
        (update :rooms parse-rooms)
        (update :map parse-map))))

(comment
  (parse-world (slurp (io/resource "world.tw"))))

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
  [id
   {:id id
    :shortdesc (str/trim shortdesc-line)
    :desc (str/join " " (map str/trim desclines))}])

(defn- parse-rooms [rooms]
  (->> (str/split rooms #"\n")
       (partition-by empty?)
       (remove (partial = [""]))
       (map room-block)
       (into {})))

(defn- map-block [[line & lines]]
  (let [[room & neighbors] (str/split line #"\s+")
        others (map #(str/split (str/trim %) #"\s+") lines)]
    [room
     {:id room
      :neighbors (->> others
                      (map rest)
                      (cons (rest neighbors))
                      (map (comp vec reverse))
                      (into {})
                      walk/keywordize-keys)}]))

(defn- parse-map [map-string]
  (->> (str/split map-string #"\n")
       (partition-by empty?)
       (remove (partial = [""]))
       (map map-block)
       (into {})))

(defn parse-world [world-string]
  (let [secs (pull-sections world-string)]
    (-> secs
        (update :rooms parse-rooms)
        (update :map parse-map))))

(comment
  (parse-world (slurp (io/resource "world.tw"))))

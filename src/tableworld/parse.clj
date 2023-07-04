(ns tableworld.parse
  (:require [clojure.string :as str]
            [clojure.java.io :as io]))

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
       (remove #{'("")})
       (map room-block)))

(comment
  (let [secs (pull-sections (slurp (io/resource "world.tw")))]
    (update secs :rooms parse-rooms)))


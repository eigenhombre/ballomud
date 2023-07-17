(ns ballomud.reader
  (:require [clj-yaml.core :as yaml]
            [clojure.java.io :as io]
            [clojure.walk :as walk])
  (:import [flatland.ordered.map OrderedMap]))

(defn read-world-source [world-str]
  (walk/postwalk
   (fn [x]
     (if (instance? flatland.ordered.map.OrderedMap x)
       (into {} x)
       x))
   (yaml/parse-string world-str)))

;; Massage into format we want:
(defn add-map-map [world]
  (assoc world
         :map
         (into {}
               (for [[k v] (:rooms world)]
                 [(name k) {:id (name k)
                            :neighbors (:leads_to v)}]))))

(defn keywordize-room-names [world]
  (update world
          :rooms
          (fn [rooms]
            (into {}
                  (for [[k v] rooms]
                    [(name k) (assoc v :id (name k))])))))

(defn read-from-string [s]
  (->> s
       read-world-source
       keywordize-room-names
       add-map-map))

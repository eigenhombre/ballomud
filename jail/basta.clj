(ns ballomud.jail.basta
  (:require [clojure.string :as str]))

;; Experiment in using Hosftadter-style expressions:
;; http://johnj.com/posts/oodles/

(def world (atom {:rooms {} :map {}}))

(defn terminal? [s]
  (.endsWith (name s) "."))

(defn roomname? [x]
  (and (symbol? x)
       (= (name x)
          (.toUpperCase (name x)))))

(defn sentences [l]
  (loop [l l, cur (), ret ()]
    (if (empty? l)
      (reverse ret)
      (let [[l1 & ln] l]
        (if (terminal? l1)
          (if (empty? ln)
            (reverse (cons (reverse (cons l1 cur)) ret))
            (recur ln
                   ()
                   (cons (reverse (cons l1 cur)) ret)))
          (recur ln
                 (cons l1 cur)
                 ret))))))

(comment
  (sentences
   '(You. You are in the woods.  Just you.  You.  Trunks and heavy
          overgrowth obscure your view in all directions.  :NORTH Just
          to the north is a small HUT with door ajar.  Wow.))
  ;;=>
  ((You.)
   (You are in the woods.)
   (Just you.)
   (You.)
   (Trunks
    and heavy
    overgrowth obscure your view in all directions.)
   (:NORTH Just to the north is a small HUT with door ajar.)
   (Wow.)))

(defn no-punctuation [s]
  (str/replace s #"\." ""))

(defn find-direction-map [sentences]
  (->> sentences
       (map (partial filter #(or (keyword? %) (roomname? %))))
       (filter (comp (partial = 2) count))
       (map vec)
       (map (juxt (comp keyword no-punctuation str/lower-case name first)
                  (comp no-punctuation name second)))
       (into {})))

(find-direction-map
 (sentences
  '(You. You are in the woods.  Just you.  You.  Trunks and
         heavy overgrowth obscure your view in all
         directions.  Just to the NORTH is a small HUT
         with door ajar.  Wow.)))

(defn all-caps? [s]
  (= (name s)
     (str/upper-case (name s))))

(defn normalize-name [x]
  (if (all-caps? x)
    (map symbol
         (-> x
             name
             str/lower-case
             (str/split #"_")))
    [x]))

(defn sentence->str [l]
  (str/join " " (map name l)))

(defn add-room! [name-str shortdesc desc directions]
  (swap! world
         (fn [w]
           (-> w
               (assoc-in [:rooms name-str]
                         {:id name-str
                          :shortdesc shortdesc
                          :desc desc})
               (assoc-in [:map name-str]
                         {:id name-str
                          :neighbors directions})))))

(defn addroom [name-str expr-list]
  (let [sentences-list (sentences expr-list)]
    (add-room! name-str
               (sentence->str (first sentences-list))
               (->> sentences-list
                    rest
                    (apply concat)
                    (mapcat normalize-name)
                    sentence->str)
               (find-direction-map sentences-list))))

(defmacro defroom [name_ & expr]
  `(do
     (defn ~(with-meta name_ {:room true}) []
       '~@expr)
     (addroom ~(name name_) '~@expr)))

(defroom FOREST
  (The great forest.
       You are in the woods.  Trunks and heavy overgrowth obscure
       your view in all directions.  Just to the NORTH is a small
       hut MUD_ROOM, with door ajar.  To the SOUTH lies A_DEEPER_FOREST.))

(defroom HEARTH
  (The hearth.
       You are in the hearth room.  There is a heavy stone table here
       near a great fireplace lined with glazed red tile.  To the
       SOUTH lies the MUD_ROOM.))

(defroom MUD_ROOM
  (The mud room.
       You are in the mud room.  Through the outside door to the
       SOUTH you see a great FOREST.  An opening on the NORTH side
       leads to the HEARTH.))

(defroom A_DEEPER_FOREST
  (The deep forest.
       You are in the deep forest.  Brush and vines grow and dangle
       all about you.  To the NORTH the FOREST is somewhat less dense.))

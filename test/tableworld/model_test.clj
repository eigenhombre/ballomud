(ns tableworld.model-test
  (:require [clojure.test :refer [deftest is]]))

(def example-world
  (atom {:rooms
         {"hearth" {:shortdesc "The hearth."
                    :desc "The Lorem Hearth, home of the many."}
          "ship" {:shortdesc "The ship."
                  :desc "A place of seasickness and spray."}}
         :map {"hearth" {:id "hearth"
                         :neighbors {:n "ship"}}
               "ship" {:id "ship"
                       :neighbors {:s "hearth"}}}}))

(defn player-location-id [player-name world]
  (get-in world [:players player-name :location]))

(defn player-room [player-name world]
  (let [loc-id (player-location-id player-name world)]
    (get-in world [:rooms loc-id])))

(defn assoc-player-room [player-name room-id world-map]
  (let [room (get-in world-map [:rooms room-id])]
    (assert room)
    (-> world-map
        (update :players assoc player-name {:location room-id
                                            :name player-name
                                            :visited #{room-id}}))))


(defn describe-player-location [player-name world-map full-desc?]
  ((if full-desc? :desc :shortdesc) (player-room "John" world-map)))

(defn add-player! [player-name room-id world-atom]
  (swap! world-atom (partial assoc-player-room player-name room-id)))

(defn try-to-move-player! [player-name direction world-atom]
  (let [oops (fn [k]
               (throw (ex-info "movement error" {:error k})))]
    (try
      (swap!
       world-atom
       (fn [world]
         (let [old-loc (player-location-id player-name world)
               room (get-in world [:map old-loc])
               new-loc (get-in world [:map old-loc :neighbors direction])]
           (println new-loc)
           (cond
             (not old-loc) (oops :no-player-loc)
             (not room) (oops :no-room-found)
             (not new-loc) (oops :cannot-go-that-way)
             :else (-> world
                       (assoc-in [:players player-name :location] new-loc)
                       (update-in [:players player-name :visited] conj new-loc))))))
      nil
      (catch Exception e
        (let [err (-> e ex-data :error)]
          (if-not err
            (throw e)
            err))))))

(deftest model
  (is (nil? (player-room "John" @example-world)))
  (add-player! "John" "ship" example-world)
  (is (= "The ship." (describe-player-location "John" @example-world false)))
  (is (= "A place of seasickness and spray."
         (describe-player-location "John" @example-world true)))
  (is (= :no-player-loc (try-to-move-player! "Jabba" :to-hut example-world)))
  (is (nil? (try-to-move-player! "John" :s example-world)))
  (is (= "The hearth." (describe-player-location "John" @example-world false)))
  (is (= :cannot-go-that-way (try-to-move-player! "John" :s example-world)))
  )

(ns tableworld.model)

(defn player-location-id [player-name world]
  (get-in world [:players player-name :location]))

(defn player-room [player-name world]
  (let [loc-id (player-location-id player-name world)]
    (get-in world [:rooms loc-id])))

(defn assoc-player-room [player-name room-id world-map]
  (let [room (get-in world-map [:rooms room-id])]
    (assert room)
    (assoc-in world-map [:players player-name] {:location room-id
                                                :name player-name
                                                :visited #{room-id}})))

(defn describe-player-location [player-name world-map full-desc?]
  ((if full-desc? :desc :shortdesc) (player-room player-name world-map)))

(defn player-exists [player-name world-atom]
  (get-in @world-atom [:players player-name]))

(defn add-player! [player-name room-id world-atom]
  (swap! world-atom (partial assoc-player-room player-name room-id)))

(defn del-player! [player-name world-atom]
  (swap! world-atom update :players dissoc player-name))

(defn try-to-move-player! [player-name direction world-atom]
  (let [oops (fn [k]
               (throw (ex-info "movement error" {:error k
                                                 :status :fail})))]
    (try
      (swap!
       world-atom
       (fn [world]
         (let [old-loc (player-location-id player-name world)
               room (get-in world [:map old-loc])
               new-loc (get-in world [:map old-loc :neighbors direction])]
           (cond
             (not old-loc) (oops :no-player-loc)
             (not room) (oops :no-room-found)
             (not new-loc) (oops :cannot-go-that-way)
             :else (-> world
                       (assoc-in [:players player-name :location] new-loc)
                       (update-in [:players player-name :visited]
                                  conj new-loc))))))
      {:status :ok
       :error nil}
      (catch Exception e
        (let [edata (ex-data e)]
          (if-not edata
            (throw e)
            edata))))))

(def ttmp-error-descriptions
  {:no-player-loc "No player found... who the heck are you?!"
   :no-room-found "I don't know where you are!!!"
   :cannot-go-that-way "You cannot go that way."})

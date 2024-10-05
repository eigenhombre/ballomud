(ns ballomud.model
  (:require [clojure.string :as str]))

(defn player-location-id [player-name world-state]
  (get-in world-state [:players player-name :location]))

(defn player-room [player-name world-state]
  (let [loc-id (player-location-id player-name world-state)]
    (get-in world-state [:rooms loc-id])))

(defn assoc-player-room [player-name room-id world-map]
  (let [room (get-in world-map [:rooms room-id])]
    (assert room)
    (assoc-in world-map [:players player-name] {:location room-id
                                                :name player-name
                                                :visited #{room-id}})))
(defn room-occupants [room-id world-map]
  (->> world-map
       :players
       (map second)
       (filter (comp (partial = room-id) :location))
       (map :name)
       (into #{})))


(defn lookup-thing-by-name [world-map nom]
  (:shortdesc ((:things world-map) (keyword nom))))

(defn describe-player-location-full [player-name world-map]
  (let [room-id (player-location-id player-name world-map)
        occupants (remove #{player-name} (room-occupants room-id world-map))
        room (player-room player-name world-map)
        desc (:desc room)
        things (map (partial lookup-thing-by-name world-map) (:contains room))
        desc2 (if (seq things)
                (str desc "\nYou see here: " (str/join "; " things) ".")
                desc)
        desc3 (if (seq occupants)
                (str desc2 "\n\nAlso here: " (str/join ", " occupants) ".")
                desc2)]

    desc3))

(defn describe-player-location
  ([player-name world-map]
   (describe-player-location player-name world-map nil))
  ([player-name world-map mode]
   (let [is-new? (get-in world-map [:players player-name :is-new-location?])]
     (if (or is-new? (= mode :detailed))
       (describe-player-location-full player-name world-map)
       (:shortdesc (player-room player-name world-map))))))

(defn player-exists [player-name world-state]
  (get-in world-state [:players player-name]))

(defn add-player! [player-name room-id world-atom]
  (swap! world-atom (fn [world]
                      (-> world
                          (->> (assoc-player-room player-name room-id))
                          (assoc-in [:players player-name :is-new-location?]
                                    true)))))

(defn add-npc [npc-name world-map]
  (let [room-id (rand-nth (map first (:rooms world-map)))]
    (-> world-map
        (->> (assoc-player-room npc-name room-id))
        (assoc-in [:players npc-name :is-npc?] true))))

(defn del-player! [player-name world-atom]
  (swap! world-atom update :players dissoc player-name))

(defmacro with-oops-swap
  "
  Define a little syntax around throwing and returning errors if
  an operation within an atomic (i.e. maybe-repeating) operation fails.
  "
  [ex-error-type oops & body]
  `(let [oops# (fn [k#]
                 (throw (ex-info ~ex-error-type {:error k#
                                                 :status :fail})))
         ~oops oops#]
     (try
       ~@body
       {:status :ok
        :error nil}
       (catch Exception e#
         (let [edata# (ex-data e#)]
           (if-not edata#
             (throw e#)
             edata#))))))

(defn try-to-move-player! [player-name direction world-atom]
  (with-oops-swap "movement error" oops
    (swap!
     world-atom
     (fn [world]
       (let [old-loc (player-location-id player-name world)
             room (get-in world [:map old-loc])
             new-loc (get-in world [:map old-loc :neighbors direction])
             visited? (get-in world [:players player-name :visited new-loc])]
         (cond
           (not old-loc) (oops :no-player-loc)
           (not room) (oops :no-room-found)
           (not new-loc) (oops :cannot-go-that-way)
           :else (-> world
                     (assoc-in [:players player-name :location] new-loc)
                     (update-in [:players player-name :visited]
                                conj new-loc)
                     (assoc-in [:players player-name :is-new-location?]
                               (not visited?)))))))))

(defn player-inventory [player-name world-map]
  (let [obj-ids (get-in world-map [:players player-name :inventory] [])]
    (into {} (for [id obj-ids]
               [id (get-in world-map [:things (keyword id) :shortdesc])]))))

(defn room-contents [room-name world-map]
  (get-in world-map [:rooms room-name :contains] []))

(defn try-to-pick-up! [player-name object world-atom]
  (with-oops-swap "pick-up-error" oops
    (swap!
     world-atom
     (fn [world]
       (let [loc (player-location-id player-name world)
             object-there (boolean (some #{object}
                                         (room-contents loc world)))]
         (cond
           (not loc) (oops :player-not-there)
           (not object-there) (oops :object-not-here)
           :else (-> world
                     (update-in [:players player-name :inventory]
                                conj object)
                     (update-in [:rooms loc :contains]
                                (partial remove #{object})))))))))

(defn try-to-drop! [player-name object world-atom]
  (with-oops-swap "pick-up-error" oops
    (swap!
     world-atom
     (fn [world]
       (let [loc (player-location-id player-name world)
             has-object (boolean (some #{object}
                                       (keys
                                        (player-inventory player-name world))))]
         (cond
           (not loc) (oops :player-not-there)
           (not has-object) (oops :player-does-not-have-object)
           :else (-> world
                     (update-in [:players player-name :inventory]
                                (partial remove #{object}))
                     (update-in [:rooms loc :contains]
                                conj object))))))))

(def ttmp-error-descriptions
  {:no-player-loc "No player found... who the heck are you?!"
   :no-room-found "I don't know where you are!!!"
   :cannot-go-that-way "You cannot go that way."
   :object-not-here "I don't see that here."
   :player-does-not-have-object "You don't have that."})

(defn move-npcs!
  "
  Move NPCs around the world.
  Returns a list of events that happened (currently, just strings).
  "
  [player-name world-atom leave-probability]
  (let [events (atom [])]
    (swap! world-atom
           (fn [world]
             (let [npcs (->> world
                             :players
                             (filter (comp :is-npc? second))
                             (map first))]
               (loop [npcs npcs
                      world world]
                 (if-not (seq npcs)
                   world
                   (let [[npc-name] npcs
                         room-id (player-location-id npc-name world)
                         room (get-in world [:rooms room-id])
                         neighbors (:leads_to room)
                         [_ new-room-id] (rand-nth (vec neighbors))
                         player-room (player-location-id player-name world)]
                     (if (or (> (rand) leave-probability)
                             (= room-id new-room-id))
                       (recur (rest npcs) world)
                       (do
                         #_(swap! events conj (format "%s moves from %s to %s."
                                                      npc-name
                                                      room-id
                                                      new-room-id))
                         (when (= new-room-id player-room)
                           (swap! events conj (format "%s appears." npc-name)))
                         (when (and (= room-id player-room)
                                    (not= player-room new-room-id))
                           (swap! events conj (format "%s leaves." npc-name)))
                         (recur (rest npcs)
                                (assoc-in world
                                          [:players npc-name :location]
                                          new-room-id))))))))))
    @events))

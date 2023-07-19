(ns ballomud.model-test
  (:require [clojure.test :refer [deftest is]]
            [ballomud.model :refer :all]))

(def example-world
  (atom {:things {:flashlight
                  {:shortdesc "a small, silver flashlight",
                   :desc
                   "A small, silver flashlight, slightly tarnished."}},
         :rooms
         {"hearth" {:shortdesc "The hearth."
                    :desc "The Lorem Hearth, home of the many."
                    :contains ["flashlight"]}
          "ship" {:shortdesc "The ship."
                  :desc "A place of seasickness and spray."}}
         :map {"hearth" {:id "hearth"
                         :neighbors {:n "ship"}}
               "ship" {:id "ship"
                       :neighbors {:s "hearth"}}}}))


(deftest model
  (is (not (player-exists "John" example-world)))
  (is (nil? (player-room "John" @example-world)))

  (add-player! "John" "ship" example-world)
  (is (player-exists "John" example-world))
  (is (= "A place of seasickness and spray."
         (describe-player-location "John" @example-world)))
  (is (= #{"John"} (room-occupants "ship" @example-world)))
  (is (= {:error :no-player-loc
          :status :fail}
         (try-to-move-player! "Jabba" :to-hut example-world)))
  (is (= {:status :ok, :error nil}
         (try-to-move-player! "John" :s example-world)))
  (is (= (str "The Lorem Hearth, home of the many.\n"
              "You see here: a small, silver flashlight.")
         (describe-player-location "John" @example-world)))
  (is (= {:error :cannot-go-that-way
          :status :fail}
         (try-to-move-player! "John" :s example-world)))
  (is (= {:status :ok, :error nil}
         (try-to-move-player! "John" :n example-world)))
  (is (= "The ship." (describe-player-location "John" @example-world)))
  (is (= "A place of seasickness and spray."
         (describe-player-location "John" @example-world :detailed)))
  (is (= {:error :object-not-here
          :status :fail}
         (try-to-pick-up! "John" "flashlight" example-world)))
  (is (= {:error :player-not-there
          :status :fail}
         (try-to-pick-up! "Frodo" "flashlight" example-world)))

  (add-player! "Mary" "hearth" example-world)
  (is (= #{ "Mary"} (room-occupants "hearth" @example-world)))

  (is (not (empty? (room-contents "hearth" @example-world))))
  (is (= {:error nil
          :status :ok}
         (try-to-pick-up! "Mary" "flashlight" example-world)))
  (is (empty? (room-contents "hearth" @example-world)))
  (is (= {:error nil
          :status :ok}
         (try-to-drop! "Mary" "flashlight" example-world)))

  (del-player! "John" example-world)
  (is (not (player-exists "John" example-world)))

  (del-player! "Mary" example-world))

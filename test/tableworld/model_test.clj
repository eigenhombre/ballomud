(ns tableworld.model-test
  (:require [clojure.test :refer [deftest is]]
            [tableworld.model :refer :all]))

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

(deftest model
  (is (not (player-exists "John" example-world)))
  (is (nil? (player-room "John" @example-world)))
  (add-player! "John" "ship" example-world)
  (is (player-exists "John" example-world))
  (is (= "The ship." (describe-player-location "John" @example-world false)))
  (is (= "A place of seasickness and spray."
         (describe-player-location "John" @example-world true)))
  (is (= {:error :no-player-loc
          :status :fail}
         (try-to-move-player! "Jabba" :to-hut example-world)))
  (is (= {:status :ok, :error nil}
         (try-to-move-player! "John" :s example-world)))
  (is (= "The hearth." (describe-player-location "John" @example-world false)))
  (is (= {:error :cannot-go-that-way
          :status :fail}
         (try-to-move-player! "John" :s example-world)))
  (del-player! "John" example-world)
  (is (not (player-exists "John" example-world))))

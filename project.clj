(defproject ballomud "0.1.0-SNAPSHOT"
  :description "A small MUD-inspired game"
  :url "https://github.com/eigenhombre/ballomud"
  :license {:name "MIT"}
  :dependencies [[org.clojure/clojure "1.11.1"]
                 [clj-wrap-indent "1.0.0"]
                 [clj-commons/clj-yaml "1.0.26"]
                 [org.clojure/core.match "1.0.1"]]
  :main ^:skip-aot ballomud.core
  :target-path "target/%s"
  :profiles {:uberjar {:aot :all
                       :jvm-opts ["-Dclojure.compiler.direct-linking=true"]}
             :dev {:plugins [[lein-kibit "0.1.8"]
                             [lein-cloverage "1.2.2"]]}})

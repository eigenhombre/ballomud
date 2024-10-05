(defproject ballomud "0.1.0-SNAPSHOT"
  :description "A small MUD-inspired game"
  :url "https://github.com/eigenhombre/ballomud"
  :license {:name "MIT"}
  :dependencies [[org.clojure/clojure "1.12.0"]
                 [clj-wrap-indent "1.0.0"]
                 [clj-commons/clj-yaml "1.0.28"]
                 [eigenhombre/namejen "0.1.23"]
                 [org.clojure/core.match "1.1.0"]
                 ;; Plugins: (for lein deps to cache correctly in Docker):
                 [lein-kibit "0.1.11"]
                 [lein-ancient "0.7.0"]
                 [lein-cloverage "1.2.4"]]
  :main ^:skip-aot ballomud.core
  :target-path "target/%s"
  :profiles {:uberjar {:aot :all
                       :jvm-opts ["-Dclojure.compiler.direct-linking=true"]}
             :dev {:plugins [[lein-kibit "0.1.11"]
                             [lein-ancient "0.7.0"]
                             [lein-cloverage "1.2.4"]]}})

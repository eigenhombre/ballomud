(defproject ballomud "0.0.2"
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
                             [lein-cloverage "1.2.4"]]}}
  :release-tasks [["vcs" "assert-committed"]
                  ["change" "version" "leiningen.release/bump-version" "release"]
                  ["vcs" "commit"]
                  ["vcs" "tag" "v" "--no-sign"]
                  ["uberjar"]
                  ;; TODO: push to docker
                  ["change" "version" "leiningen.release/bump-version"]
                  ["vcs" "commit"]
                  ["vcs" "push"]])

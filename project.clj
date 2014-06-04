(defproject lambda-zone "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.5.1"]
                 [org.clojure/math.numeric-tower "0.0.3"]
                 [clj-chess-engine "0.1.0.6"]

                 ;;ring stuff
                 [ring "1.2.0"]
                 [ring-server "0.2.8" :exclusions [[org.clojure/clojure]
                                                   [ring]]]
                 [ring/ring-json "0.2.0"]
                 [ring-basic-authentication "1.0.1"]

                 ;;mongdb
                 [com.novemberain/monger "1.7.0"]

                 ;;auth
                 [com.cemerick/friend "0.2.0"]

                 ;;compojure
                 [compojure "1.1.5" :exclusions [[org.clojure/clojure] [ring/ring-core]]]
                 [org.webjars/foundation "4.0.4"]

                 ;; only used to discover demo app namespaces
                 [bultitude "0.1.7"]

                 [hiccup "1.0.4"]
                 [jarohen/chord "0.3.1"]
                 [jarohen/clidget "0.2.0"]

                 [org.clojure/clojurescript "0.0-2202"]
                 ;;[org.clojure/tools.reader "0.8.4"]

                 [prismatic/dommy "0.1.2"]
                 [org.clojure/tools.trace "0.7.5"]
                 [org.clojure/data.json "0.2.4"]
                 [org.clojure/core.async "0.1.267.0-0d7780-alpha"]

                 [cheshire "5.3.1"]
                 [com.cemerick/drawbridge "0.0.6"]
                 ]
  :min-lein-version "2.0.0"
  :plugins [[lein-pdo "0.1.1"]
            [jarohen/lein-frodo "0.3.0-rc2"]
            [lein-cljsbuild "1.0.3"]]

  :frodo/config-resource "chord-example.edn"

  :aliases {"dev" ["pdo" "cljsbuild" "auto," "frodo"]}

  :resource-paths ["resources" "target/resources"]

  :cljsbuild {:builds [{:source-paths ["src"]
                        :compiler {:output-to "target/resources/js/root/chord-example.js"
                                   :output-dir "target/resources/js"
                                   :optimizations :none
                                   :pretty-print true
                                   :source-map true
                                   }}]}
  ;; :cljsbuild {:builds [{:source-paths ["src"]
  ;;                       :compiler {:output-to "target/resources/js/chord-example.js"
  ;;                                  :optimizations :whitespace
  ;;                                  :pretty-print true}}]}
  :jvm-opts ["-Djava.security.policy=example.policy" "-Xmx512M"]
  :uberjar-name "lambda-zone-standalone.jar"
  :main lambda-zone.rest)

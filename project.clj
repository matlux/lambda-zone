(defproject lambda-zone "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.5.1"]
                 [org.clojure/math.numeric-tower "0.0.3"]

                 ;;ring stuff
                 [ring "1.2.0"]
                 [ring-server "0.2.8" :exclusions [[org.clojure/clojure]
                                                   [ring]]]
                 [ring/ring-json "0.2.0"]

                 ;;mongdb
                 [com.novemberain/monger "1.7.0"]

                 ;;auth
                 [com.cemerick/friend "0.2.0"]

                 ;;compojure
                 [compojure "1.1.5" :exclusions [[org.clojure/clojure] [ring/ring-core]]]
                 [org.webjars/foundation "4.0.4"]

                 ;; only used to discover demo app namespaces
                 [bultitude "0.1.7"]
                 ]
  :main lambda-zone.rest)

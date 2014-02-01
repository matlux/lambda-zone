(ns lambda-zone.rest
  (:use [ring.middleware.reload]
        [ring.util.response])
  (:require [clojure.math.numeric-tower :as math]
            [clj-chess-engine.core :as chess]
            [lambda-zone.backend :as back]
            ;;[clojure.string :as str]
            [compojure.handler :as handler]
            [compojure.core
             :as c-core
             :refer [defroutes GET POST PUT DELETE HEAD OPTIONS PATCH ANY]]
            [compojure.route :as c-route]
            [ring.server.standalone :as server]
            [ring.middleware.json :as ring-json] :reload-all)
  (:import clojure.lang.PersistentVector))


(def user-table (atom [{:login "mathieu", :id "rabbit", :fn "(fn random-f [{board :board am-i-white? :white-turn valid-moves :valid-moves ic :in-check? h :history s :state}]\n  (let [v (into [] valid-moves)\n        iteration (if (nil? s) (+ 1 (if am-i-white? 0 1)) (+ 2 s))]\n\n    (println (if am-i-white? \"white: \" \"black: \"))\n    (println \"valid moves:\" valid-moves)\n    (println \"iteration:\" iteration)\n    (let [move (rand-int (count valid-moves))]\n      (println \"choosen move:\" (get v move))\n      {:move (get v move) :state iteration})) )"}]))

(defroutes api
  (GET "/" [] (clojure.java.io/resource "public/html/index.html"))
  (GET "/entry/:name" [name] (response (filter (fn [{n :name}] (= n name )) @user-table)))
  (PUT "/function" {msg :body} (response (do (println "added" msg) (back/save-function msg) {:return "ok"})))
  (c-route/resources "/"))

;;
(def app
  (->
    (var api)
    (handler/api)
    (wrap-reload '(one-route.core))
    (ring-json/wrap-json-body {:keywords? true})
    (ring-json/wrap-json-response)))

(defn start-server []
  (server/serve (var app) {:port 8070
                           :join? false
                       :open-browser? false}))

(defn -main []
  (start-server))

;;
;;(def server (start-server))


;;@user-table

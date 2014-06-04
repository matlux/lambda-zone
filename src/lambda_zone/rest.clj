(ns lambda-zone.rest
  (:use [ring.middleware.reload]
        ;;[ring.util.response]
        )
  (:require [clojure.math.numeric-tower :as math]
            [clj-chess-engine.core :as chess]
            [lambda-zone.backend :as back]
            ;;[clojure.string :as str]
            [compojure.handler :as handler]
            [compojure.core
             :as c-core
             :refer [defroutes GET POST PUT DELETE HEAD OPTIONS PATCH ANY]]
            [compojure.route :as c-route :refer [resources]]
            [ring.util.response :refer [response]]
            [ring.server.standalone :as server]
            [ring.middleware.json :as ring-json]
            [ring.middleware.params :as ring-params]
            [ring.middleware.keyword-params :as keyword-params]
            [ring.middleware.nested-params :as nested-params]
            [ring.middleware.session :as session]
            [ring.middleware.basic-authentication :as ring-basic]
            [ring.util.response :as resp]
            [monger.core :as mg]
            [monger.collection :as mc]
            [cemerick.drawbridge :as drawbridge]
            [cemerick.friend :as friend]
            (cemerick.friend [workflows :as workflows]
                             [credentials :as creds]
                             [openid :as openid])
            [chord.http-kit :refer [with-channel ;;wrap-websocket-handler
                                    ]]
            [clojure.core.async :refer [<! >! >!! put! take! timeout close! chan mult sliding-buffer go go-loop tap untap alts! alts!! buffer]]
            ;;[clojure.core.async :as a]
            [hiccup.page :as h :refer [html5 include-js]]
            ;;[hiccup.page :refer [html5 include-js]]
            [hiccup.element :as e]
            [lambda-zone.misc :as misc]

            ;;[clojure.data.json :as json]
            [cheshire.core :as json]
            [org.httpkit.server :as http]

            ;;:reload-all
            )
  (:import clojure.lang.PersistentVector))

(def user-table (atom [{:login "mathieu", :id "rabbit", :fn "(fn random-f [{board :board am-i-white? :white-turn valid-moves :valid-moves ic :in-check? h :history s :state}]\n  (let [v (into [] valid-moves)\n        iteration (if (nil? s) (+ 1 (if am-i-white? 0 1)) (+ 2 s))]\n\n    (println (if am-i-white? \"white: \" \"black: \"))\n    (println \"valid moves:\" valid-moves)\n    (println \"iteration:\" iteration)\n    (let [move (rand-int (count valid-moves))]\n      (println \"choosen move:\" (get v move))\n      {:move (get v move) :state iteration})) )"}]))



(def providers [{:name "Google" :url "https://www.google.com/accounts/o8/id"}
                {:name "Yahoo" :url "http://me.yahoo.com/"}
                {:name "AOL" :url "http://openid.aol.com/"}
                {:name "Wordpress.com" :url "http://username.wordpress.com"}
                {:name "MyOpenID" :url "http://username.myopenid.com/"}])


;; (for [[k v] auth
;;       :let [[k v] (if (= :identity k)
;;                     ["Your OpenID identity" (str (subs v 0 (* (count v) 2/3)) "…")]
;;                     [k v])]]
;;   [:li [:strong (str (name k) ": ")] v])

(defn openid-info [req]
  [:div {:class "panel"}
   (let [auth (friend/current-authentication req)]
     [:div (if auth
             (let [{:keys [firstname lastname email]} auth]
               [:p (str "Logged in as: " firstname " " lastname " - " email)]
               )
             [:div
              [:p "You are currently an anonymous user"]
              [:h4 "Authenticate with the service of your choice to be able to submit functions:"]
              [:table {:class "table"}
               [:tr (for [{:keys [name url]} providers
                          :let [base-login-url (misc/context-uri req (str "/login?identifier=" url))
                                dom-id (str (gensym))]]
                      [:td [:form {:method "POST" :action (misc/context-uri req "login")
                                   :onsubmit (when (.contains ^String url "username")
                                               (format "var input = document.getElementById(%s); input.value = input.value.replace('username', prompt('What is your %s username?')); return true;"
                                                       (str \' dom-id \') name))}
                            [:input {:type "hidden" :name "identifier" :value url :id dom-id}]
                            [:input {:type "submit" :class "button" :value name}]]])]]
              ;;[:p "…or, with a user-provided OpenID URL:"]
              ;; [:form {:method "POST" :action (misc/context-uri req "login")}
              ;;  [:input {:type "text" :name "identifier" :style "width:250px;"}]
              ;;  [:input {:type "submit" :class "button" :value "Login"}]]
              ])
      ])
                                        ;[:div {:class "panel"} [:p "req:"] [:pre (str req)]]
   ])

(defn home-page-openid [req]
  (h/html5
   misc/pretty-head
   (misc/pretty-body
    (openid-info req)
    [:h3 "Logging out"]
    [:p [:a {:href (misc/context-uri req "logout")} "Click here to log out"] "."]))
  )


(defn submit-function [req]
  (let [{login :email :as auth} (friend/current-authentication req)
        fn-name (if (nil? login) "anonymous" login)]
   [:div {:class "panel"} [:h2 "Submit a Chess Strategy:" [:a {:href "https://github.com/matlux/lambda-zone/wiki/Chess#submit-a-chess-strategy"} "(help?)"]]
    [:form {:id "addForm" :class "form-inline" :onsubmit "return false;"}
     [:div [:input {:id "addId" :type "text" :class "form-control" :placeholder "Function Name" :value (str fn-name (rand-int 100))}]]
     [:div [:textarea {:id "addFunction" :row "80" :cols "100" :placeholder "Function Code (Clojure)"} (str back/random-f-src)]]
     [:button {:type "submit" :onclick "loadFunction();" :class "btn btn-success"} "Load"]
     [:button {:type "submit" :onclick "addFunctionFunction();" :class "btn btn-success"} "Submit"]
     ;;[:button {:type "submit" :onclick "deleteFunction();" :class "btn btn-failure"} "Delete"]
     ]
    [:hr]
    [:pre {:id "addEntryResult"}]]))

(defn home-page [req] (h/html5
  misc/pretty-head
  (misc/pretty-body

   (openid-info req)

   (submit-function req)

   [:div#content]
   [:div#content2]

   [:h3 "Logging out"]
   [:p (e/link-to (misc/context-uri req "logout") "Click here to log out") "."])))

(defn replay-page [req]
  (h/html5
  misc/pretty-head
  (misc/pretty-body

   [:div#content]
   [:div#content2]


   [:p (e/link-to (misc/context-uri req "/") "homepage") "."])))


;; (defn ws-handler [req]
;;   (with-channel req ws
;;     (println "Opened connection from" (:remote-addr req))
;;     (go-loop []
;;       (when-let [{:keys [message]} (<! ws)]
;;         (println "Message received test+++:" message)
;;         (>! ws (format "You said hiya: '%s' at %s." message (java.util.Date.)))
;;         (recur)))))

(defn page-frame []
  (html5
   [:head
    [:title "Chord Example"]
    (include-js "/js/chord-example.js")]
   [:body [:div#content]]))

(def buf (buffer 1))
;;(.full? buf)

(def src-c (chan buf))
(def mc (mult src-c))

(def at-least-one (tap mc (chan (sliding-buffer 1))))

(defn to-string [board]
  (into []  (map #(.toString %) board)))

;;

(defn route-msg2client [{:keys [board message move score id1 id2 iteration msg-type matches game-id] :as msgbus}]
  (let [in-game-update {:board (to-string board)
                        :iteration iteration
                        :id1 id1 :id2 id2
                        :msg-type msg-type
                        :game-id game-id
                        :time (str (format "at %s." (java.util.Date.)))}
        full-results {:matches matches
                      :msg-type msg-type
                      :time (str (format "at %s." (java.util.Date.)))}
        ]
    (case msg-type
      :in-game-update in-game-update
      :full-results (chess/dbg full-results)
      {:msg-type msg-type :msg "route-msg2client didn't recognised or expect this message type"})))

(defn ws-handler [{:keys [async-channel remote-addr] :as req}]
  (with-channel req ws
    (println "Opened connection from" async-channel)
    (let [sink (chan (sliding-buffer 1))]
      (tap mc sink)
      (println "registered mc sink" mc sink "sending init data")
      (>!! ws (json/generate-string {:msg {:msg-type :full-results :matches (back/load-results)}}))
      (go-loop []
        (println "about to wait for message" async-channel)
        (let [;;[{:keys [board message move score id1 id2 iteration] :as val} c]  (alts! [ws sink])
              {:keys [board iteration msg-type] :as val} (<! sink) ;;(alts! [sink (timeout 10000)])
              ]
          (println "Message received test:" (dissoc val :move-history) )
          (when val
            (println "Message received test+++; about to sent to WS" )
            (>! ws (json/generate-string {:msg (route-msg2client val) }))
            (println "message sent to WS")
            (recur))
          (println "about to untap" async-channel)
          (untap mc sink)
          (println "exiting ws" async-channel " " (alts!! [sink (timeout 1000)]))
          :exiting)))))

(defroutes api
  (GET "/" req (home-page req))
  (GET "/function/:id" [id] (response (back/retrieve-function id)))
  (PUT "/function" req (response (back/save-function req src-c)))
  (GET "/result/:id1/:id2" [id1 id2] (response (back/retrieve-result id1 id2)))
  (GET "/board/:id1/:id2/:move" [id1 id2 move] (response (to-string (back/retrieve-board id1 id2 move))))
  (GET "/results" req (response (back/load-results)))
  (GET "/html/replaygame" req (response (replay-page req)))
  (GET "/ws" [] ws-handler)
  (c-route/resources "/js" {:root "js"})
  (c-route/resources "/")
  )

;;(back/retrieve-board "daredevil" "d" "0")

;; remote repl on heroku

(def drawbridge-handler
  (-> (cemerick.drawbridge/ring-handler)
      (keyword-params/wrap-keyword-params)
      (nested-params/wrap-nested-params)
      (ring-params/wrap-params)
      (session/wrap-session)))

(defn authenticated? [name pass]
  (= [name pass] [(System/getenv "AUTH_USER") (System/getenv "AUTH_PASS")]))

(defn wrap-drawbridge [handler]
  (fn [req]
    (let [handler (if (= "/repl" (:uri req))
                    (ring-basic/wrap-basic-authentication
                     drawbridge-handler authenticated?)
                    handler)]
      (handler req))))

(def app-routes
  (->

   (friend/authenticate
              api
              {:allow-anon? true
               ;;:login-uri "/login"
               :default-landing-uri "/"
               ;; :unauthorized-handler #(-> (h/html5 [:h2 "You do not have sufficient privileges to access " (:uri %)])
               ;;                          resp/response
               ;;                          (resp/status 401))
               ;;:credential-fn #(creds/bcrypt-credential-fn users %)
               :workflows [(openid/workflow
                             :openid-uri "/login"
                             :credential-fn identity)]})
      (handler/site)
      (wrap-reload '(lambda-zone.rest))
      (ring-json/wrap-json-body {:keywords? true})
      (ring-json/wrap-json-response)
      (wrap-drawbridge)
      ;;(wrap-websocket-handler)
      ))


(defroutes app-routes-old
  (GET "/" [] (response (page-frame)))
  (GET "/ws" [] ws-handler)
  (resources "/js" {:root "js"}))

;; (def app-routes
;;   (->
;;    #'api
;;    wrap-websocket-handler
;;       ))

;; (defn start-server []
;;   (server/serve (var app-routes) {:port 8070
;;                            :join? false
;;                        :open-browser? false}))

;; (defn -main []
;;   (start-server))

;;
;;(def server (start-server))


;;@user-table
;; (defonce web-server (http-kit/run-server #'app {:port 3000 :join? false}))



(def app
  #'app-routes)

(defn app-handler []
  (back/init-dao)
  app
  )


(defn to-port [s]
  (when-let [port s] (Long. port)))

(defn start-server [& [port]]
  (back/init-dao)
  (http/run-server #'app
   {:port (or (to-port port)
              (to-port (System/getenv "PORT")
                       ) ;; For deploying to Heroku
              3000)
;;    :session-cookie-attrs {:max-age 600}
    }))

(defn -main [& args] (start-server (first args)))

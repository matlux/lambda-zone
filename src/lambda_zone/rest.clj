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
            [ring.middleware.json :as ring-json]
                        [ring.util.response :as resp]
            [monger.core :as mg]
            [monger.collection :as mc]
            [cemerick.friend :as friend]
            (cemerick.friend [workflows :as workflows]
                             [credentials :as creds]
                             [openid :as openid])
            [hiccup.page :as h]
            [hiccup.element :as e]
            [lambda-zone.misc :as misc]
            :reload-all)
  (:import clojure.lang.PersistentVector))


(def user-table (atom [{:login "mathieu", :id "rabbit", :fn "(fn random-f [{board :board am-i-white? :white-turn valid-moves :valid-moves ic :in-check? h :history s :state}]\n  (let [v (into [] valid-moves)\n        iteration (if (nil? s) (+ 1 (if am-i-white? 0 1)) (+ 2 s))]\n\n    (println (if am-i-white? \"white: \" \"black: \"))\n    (println \"valid moves:\" valid-moves)\n    (println \"iteration:\" iteration)\n    (let [move (rand-int (count valid-moves))]\n      (println \"choosen move:\" (get v move))\n      {:move (get v move) :state iteration})) )"}]))



(def providers [{:name "Google" :url "https://www.google.com/accounts/o8/id"}
                {:name "Yahoo" :url "http://me.yahoo.com/"}
                {:name "AOL" :url "http://openid.aol.com/"}
                {:name "Wordpress.com" :url "http://username.wordpress.com"}
                {:name "MyOpenID" :url "http://username.myopenid.com/"}])


(defn openid-info [req]
  [:div {:class "panel"}
   [:h2 "Authenticating with various services using OpenID"]
    [:h3 "Current Status " [:small "(this will change when you log in/out)"]]
   (let [auth (friend/current-authentication req)]
     [:div (if auth
             (do
               [:p "Some information delivered by your OpenID provider:"
                [:ul (for [[k v] auth
                           :let [[k v] (if (= :identity k)
                                         ["Your OpenID identity" (str (subs v 0 (* (count v) 2/3)) "…")]
                                         [k v])]]
                       [:li [:strong (str (name k) ": ")] v])]]

               )
             [:div
              [:p "anonymous user"]
              [:h3 "Login with…"]
              (for [{:keys [name url]} providers
                    :let [base-login-url (misc/context-uri req (str "/login?identifier=" url))
                          dom-id (str (gensym))]]
                [:form {:method "POST" :action (misc/context-uri req "login")
                 :onsubmit (when (.contains ^String url "username")
                             (format "var input = document.getElementById(%s); input.value = input.value.replace('username', prompt('What is your %s username?')); return true;"
                                     (str \' dom-id \') name))}
          [:input {:type "hidden" :name "identifier" :value url :id dom-id}]
          [:input {:type "submit" :class "button" :value name}]])
       [:p "…or, with a user-provided OpenID URL:"]
       [:form {:method "POST" :action (misc/context-uri req "login")}
        [:input {:type "text" :name "identifier" :style "width:250px;"}]
        [:input {:type "submit" :class "button" :value "Login"}]]])
      ])
   [:div {:class "panel"} [:p "req:"] [:pre (str req)]]])

(defn home-page-openid [req]
  (h/html5
   misc/pretty-head
   (misc/pretty-body
    (openid-info req)
    [:h3 "Logging out"]
    [:p [:a {:href (misc/context-uri req "logout")} "Click here to log out"] "."]))
  )


(defn home-page [req] (h/html5
  misc/pretty-head
  (misc/pretty-body

   (openid-info req)

   [:h1 "Chess Game Engine Strategy Submission page"]


   [:h2 "Function"]
   [:form {:id "addForm" :class "form-inline" :onsubmit "return false;"}
    [:div [:input {:id "addId" :type "text" :class "form-control" :placeholder "Function Name"}]]
    [:div [:textarea {:id "addFunction" :row "80" :cols "100" :placeholder "Function Code (Clojure)"}]]
    [:button {:type "submit" :onclick "loadFunction();" :class "btn btn-success"} "Load"]
    [:button {:type "submit" :onclick "addFunctionFunction();" :class "btn btn-success"} "Upload"]
    [:button {:type "submit" :onclick "deleteFunction();" :class "btn btn-failure"} "Delete"]
    ]
   [:hr]
   [:pre {:id "addEntryResult"}]


   [:p "Each of these links require particular roles (or, any authentication) to access. "
    "If you're not authenticated, you will be redirected to a dedicated login page. "
    "If you're already authenticated, but do not meet the authorization requirements "
    "(e.g. you don't have the proper role), then you'll get an Unauthorized HTTP response."]
   [:ul [:li (e/link-to (misc/context-uri req "role-user") "Requires the `user` role")]
    [:li (e/link-to (misc/context-uri req "role-admin") "Requires the `admin` role")]
    [:li (e/link-to (misc/context-uri req "requires-authentication")
                    "Requires any authentication, no specific role requirement")]]
   [:h3 "Logging out"]
   [:p (e/link-to (misc/context-uri req "logout") "Click here to log out") "."])))

(defn save-function [{msg :body :as req}]
  (response (do (println "added" msg) (back/save-function req) {:return "ok"})))

(defroutes api
  (GET "/" req (home-page req))
  (GET "/entry/:name" [name] (response (filter (fn [{n :name}] (= n name )) @user-table)))
  (PUT "/function" req (save-function req))
  (c-route/resources "/"))

;;
(def app
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
      (wrap-reload '(one-route.core))
      (ring-json/wrap-json-body {:keywords? true})
      (ring-json/wrap-json-response)
      ))

(defn start-server []
  (server/serve (var app) {:port 8070
                           :join? false
                       :open-browser? false}))

(defn -main []
  (start-server))

;;
;;(def server (start-server))


;;@user-table

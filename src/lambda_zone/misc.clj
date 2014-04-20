(ns lambda-zone.misc
  (:require [clojure.string :as str]
            [hiccup.page :refer [include-js]])
  (:import java.net.URI))

;; clojurescript tool
(defmacro dbg[x] `(let [x# ~x] (.log js/console "dbg:" (pr-str '~x) "=" (pr-str x#)) x#))


(def github-base-url
  "https://github.com/cemerick/friend-demo/blob/master/src/clj/")

(defn github-url-for
  [ns-name]
  (str github-base-url
       (-> (name ns-name)
         (.replace \. \/)
         (.replace \- \_))
       ".clj"))

(defn github-link
  [req]
  [:div {:style "float:right; width:50%; margin-top:1em; text-align:right"}
   [:a {:class "button" :href (github-url-for (-> req :demo :ns-name))} "View source"]
   " "
   [:a {:class "button secondary" :href "/"} "All demos"]])

(defn resolve-uri
  [context uri]
  (let [context (if (instance? URI context) context (URI. context))]
    (.resolve context uri)))

(defn context-uri
  "Resolves a [uri] against the :context URI (if found) in the provided
   Ring request.  (Only useful in conjunction with compojure.core/context.)"
  [{:keys [context]} uri]
  (if-let [base (and context (str context "/"))]
    (str (resolve-uri base uri))
    uri))

(defn request-url
  "Returns the full URL that provoked the given Ring request as a string."
  [{:keys [scheme server-name server-port uri query-string]}]
  (let [port (when (or (and (= :http scheme) (not= server-port 80))
                       (and (= :https scheme) (not= server-port 443)))
               (str ":" server-port))]
    (str (name scheme) "://" server-name port uri
         (when query-string (str "?" query-string)))))

(def ns-prefix "cemerick.friend-demo")
(defn ns->context
  [ns]
  (str "/" (-> ns ns-name name (subs (inc (count ns-prefix))))))

(def jquery "
        function loadFunction() {
            $.get('/function/' + $('#addId').val(), function(data) {
                $('#addEntryResult').text(JSON.stringify(data))
            })
        }

        function addFunctionFunction() {
            $.ajax({
                url: '/function',
                data: JSON.stringify({id: $('#addId').val(), fn: $('#addFunction').val() }),
                        contentType: 'application/json',
                type: 'PUT',
                success: function(data) { $('#addEntryResult').text(JSON.stringify(data)) }
            })
        }
        function deleteFunction() {
            $.ajax({
                url: '/entry',
                data: JSON.stringify({id: $('#addId').val() }),
                        contentType: 'application/json',
                type: 'DELETE',
                success: function(data) { $('#addEntryResult').text(JSON.stringify(data)) }
            })
        }
")

(def pretty-head
  [:head [:link {:href "/css/normalize.css" :rel "stylesheet" :type "text/css"}]
         [:link {:href "/css/foundation.min.css" :rel "stylesheet" :type "text/css"}]
         [:style {:type "text/css"} "ul { padding-left: 2em }"]
         [:script {:src "/js/foundation.min.js" :type "text/javascript"}]

         [:meta {:name "viewport" :content "width=device-width, initial-scale=1.0"}]
         [:link {:href "/css/bootstrap.min.css" :rel "stylesheet" :media "screen"}]
         [:script {:src "/js/jquery.min.js" :type "text/javascript"}]
         [:script {:src "/js/bootstrap.min.js" :type "text/javascript"}]
         (include-js "/js/goog/base.js")
         (include-js "/js/root/chord-example.js")
   [:script "goog.require('lambda_zone.client');"]
         [:script jquery]])

(defn pretty-body
  [& content]
  [:body {:class "row"}
   (into [:div {:class "col-lg-6 col-lg-offset-1"}] content)])

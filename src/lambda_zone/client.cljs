(ns lambda-zone.client
  (:require [chord.client :refer [ws-ch]]
            [cljs.core.async :refer [<! >! put! take! chan close! timeout]]
            [dommy.core :as d]
            [goog.string.format :as gformat]
            [clojure.string :as string]
            [goog.net.XhrIo :as xhr]
            )
  (:require-macros [cljs.core.async.macros :refer [go]]
                   [dommy.macros :refer [node sel1]]
                   [lambda-zone.misc :refer [dbg]]))

;;(defmacro dbg[x] `(let [x# ~x] (.log js/console "dbg:" '~x "=" x#) x#))

(defn acc-scores [acc {:keys [score id1 id2]}]
  (-> (assoc acc id1 (+ (get score 0) (get acc id1 0)))
      (assoc id2 (+ (get score 1) (get acc id2 0)))))

;;(sort-by key (group-by #(get % 1) (into [] (reduce acc-scores {} (:matches @database)))))

(defn extract-val-from-vector [[score coll]]
  (letfn [(f [[id  score]] id)]
    [score (map f coll)]))

(defn extract-rank [rank [score coll]]
  (letfn [(f [id] [id score (inc rank)])]
    (map f coll)))

(defn rank [matches]
  (->> (reduce acc-scores {} matches)
       (into [])
       (group-by #(get % 1))
                                        ;(map #(println %))
       (map extract-val-from-vector)
       (into {})
       (sort-by key)
       reverse
       (mapcat extract-rank (range))
       ))

(defn initial-board []
  [\r \n \b \q \k \b \n \r
   \p \p \p \p \p \p \p \p
   \- \- \- \- \- \- \- \-
   \- \- \- \- \- \- \- \-
   \- \- \- \- \- \- \- \-
   \- \- \- \- \- \- \- \-
   \P \P \P \P \P \P \P \P
   \R \N \B \Q \K \B \N \R])

(defn c1dto2d [i]
  (vector (int (/ i 8)) (mod i 8)))

(defn char2state [pieces-list]
  (into {} (filter #(not= \- (second %)) (map #(vector (c1dto2d %1) %2 ) (range 64) pieces-list))))

(def piece2img
  {
   "B" "WB"
   "K" "WK"
   "Q" "WQ"
   "N" "WN"
   "P" "WP"
   "R" "WR"
   "b" "BB"
   "k" "BK"
   "q" "BQ"
   "n" "BN"
   "p" "BP"
   "r" "BR"
   })

(defn render-piece [piece background]
  (let [img (piece2img piece)
        b (if background "white" "gray")]
    (if (nil? img)
      [:img { :width "32" :height "32" :style (str "background-color:" b ";")}]
      [:img {:src (str "/images/" img ".png") :alt "white bishop" :width "32" :height "32" :style (str "background-color:" b ";")}])))

(defn render-separator [background]
  (let [b (if background "white" "gray")
        ]
    [:img { :width "1" :height "32" :style (str "background-color:" b ";")}] ))

(defn render-board [board-state]
  (node
   [:div
    (let [line "+------+------+------+------+------+------+------+------+"
          pieces-pos (char2state board-state)         ;(into {} board-state)
          ]
      (list
       ;[:div line]
       [:div (map #(let [pos (c1dto2d (dec %))
                         [x y] pos
                         c (get pieces-pos pos "-")
                         b (even? (+ x y))]
                       (if (zero? (mod % 8))
                         (list (render-piece c b) [:div])
                         (list (render-piece c b) (render-separator b) )
                         ;;(gformat "| %s |\n%s\n" c line)
                         ;;(gformat "| %s " c)
                         )) (range 1 65))]))]))

(defn render-page [bind-input! bind-list!]
  (node
   (list
    ;; [:div
    ;;  [:h3 "Send a message to the server:"]
    ;;  (doto (node [:input {:type :text :size 50}])
    ;;    bind-input!)]
    [:div

     [:h3 "State:"]
     (doto (node [:div])
       bind-list!)])))

(defn keywordize-map [my-map]
  (into {}
  (for [[k v] my-map]
    [(keyword k) v])))

(defn deserialize-msg [msg]
  ((js->clj (JSON/parse (:message msg)) :keywordize-keys true) :msg))

(defn deserialize-json [json]
  (js->clj (JSON/parse json) :keywordize-keys true))


(defn render-in-game-update [{:keys [board id1 id2 iteration time] :as d-msg}]
  (node
   [:ul

    (let []
      ;;(.log js/console (pr-str iteration))
      (list (render-board board)
            [:div (str iteration " / \"" id1 "\" vs \"" id2 "\"")]
            [:div (str time)]
            ;;[:div (str d-msg)]
            )
      )

                                        ;[:li "None yet."]
    ]))



(defn dispatch [msgs]
  (if (seq msgs)
    (let [msg (first msgs)
          {:keys [msg-type] :as d-msg} (deserialize-msg msg)

          ]
      ;;(.log js/console  (str "dispatching:" (pr-str d-msg )))
      (case msg-type
        "in-game-update" (do ;(.log js/console  (str "in-game-update:" (pr-str d-msg )))
                             (render-in-game-update d-msg))

        (render-board (initial-board)))

      )
    (render-board (initial-board))
                                        ;[:li "None yet."]
    ))

(defn link-to [url body]
  [:a {:href url} body])

(defn to-result-page [id1 id2]
  ;(str "result/" id1 "/" id2)
  (str "html/replaygame?id1=" id1 "&id2=" id2))

(defn render-results [matches]
  (node
   (list
    [:div [:table {:class "table"}
           [:tr [:td "function"] [:td "score"] [:td "rank"]]
           (for [[id score rank] (rank matches)]
             [:tr [:td id] [:td (pr-str score)] [:td (pr-str rank)]])]]
    [:div [:table {:class "table"}
           [:tr [:td "opponents"] [:td "score"] [:td "reason"]]
           (for [{:keys [id1 id2 score result]} matches]
             [:tr [:td (link-to (to-result-page id1 id2) (str id1 " vs " id2))]
              [:td (link-to (to-result-page id1 id2) (pr-str score))]
              [:td (link-to (to-result-page id1 id2) result)]])]]
)))

(defn dispatch2 [msgs]
  (if (seq msgs)
    (let [msg (first msgs)
          {:keys [msg-type matches] :as d-msg} (deserialize-msg msg)

          ]
      (.log js/console  (str "dispatching:" (pr-str d-msg )))
      (case msg-type
        "full-results" (do (.log js/console  (str "full-results:" (pr-str d-msg )))
                           (render-results matches))
        [:div "stats default"])

      )
    (render-board (initial-board))
                                        ;[:li "None yet."]
    ))

;;(render-board (initial-board))

(defn list-binder [msgs]
  (fn [$list]
    (add-watch msgs ::list
               (fn [_ _ _ msgs]
                 ;;(.log js/console  (str "msgs changed:" (pr-str msgs )))
                 (->> (reverse msgs)
                      (take 10)
                      (dispatch)
                      (d/replace-contents! $list))))))
(defn list-binder2 [msgs]
  (fn [$list]
    (add-watch msgs ::list2
               (fn [_ _ _ msgs]
                 (->> (reverse msgs)
                      (take 10)
                      (dispatch2)
                      (d/replace-contents! $list))))))

(defn input-binder [ch]
  (fn [$input]
    (d/listen! $input :keyup
               (fn [e]
                 (when (= 13 (.-keyCode e))
                   (put! ch (d/value $input))
                   (d/set-value! $input ""))))
    (go (<! (timeout 200)) (.focus $input))))

(defn button-input-binder [ch]
  (fn [$input]
    (d/listen! $input :click (fn [e] (.log js/console (str "test input binder="  (d/text $input)))
                               (put! ch (d/text $input))))
    (go (<! (timeout 200)) (.focus $input))))


(defn bind-msgs [ch  in-game-msgs results-msgs]
  (go
   (loop []
     (when-let [msg (<! ch)]
       (let [{:keys [msg-type] :as d-msg} (deserialize-msg msg)]
         ;;(.log js/console  (str "dispatching:" (pr-str d-msg )))
         (case msg-type
           "full-results" (swap! results-msgs conj msg)
           "in-game-update" (swap! in-game-msgs conj msg)
           (.log js/console  (str "no dispatch for message:" (pr-str d-msg ))))
         (recur))
       ))))

(defn get-uri []
  (second (string/split js/window.location.href js/window.location.host)))
(defn get-base-uri []
  (first (string/split (get-uri) "?")))

(defn get-params []
  (let [sparams (second (string/split (get-uri) "?"))]
    (keywordize-map (into {}  (map #(string/split % "=") (string/split sparams "&")))) ))


(def home-page (fn []
        (go
          (let [in-game-msgs (atom [])
                results-msgs (atom [])
               ws (<! (ws-ch (str "ws://" js/window.location.host "/ws")))
                ]
            (.log js/console js/window.location.host)

            (bind-msgs ws in-game-msgs results-msgs)

           (d/replace-contents! (sel1 :#content)
                                (render-page (input-binder ws)
                                             (list-binder in-game-msgs)))
           (d/replace-contents! (sel1 :#content2)
                                (render-page (input-binder ws)
                                             (list-binder2 results-msgs)))
           ;;(dispatch ws msgs)

           (reset! in-game-msgs [])
           (reset! results-msgs [])))))



(defn GET [url]
  (let [ch (chan 1)]
    (xhr/send url
              (fn [event]
                (let [res (-> event .-target .getResponseText deserialize-json)]
                  (go (>! ch res)
                      (close! ch)))))
    ch))

(defn render-replay-game [{:keys [board id1 id2 iteration time] :as d-msg}]
  (node
   [:ul

    (let []
      ;;(.log js/console (pr-str iteration))
      (list (render-board board)
            [:div (str iteration " / \"" id1 "\" vs \"" id2 "\"")]
            [:div (str time)]
            ;;[:div (str d-msg)]
            )
      )

                                        ;[:li "None yet."]
    ]))



(defn update-counter
  ([counter direction]
     (if direction
       (swap! counter inc)
       (swap! counter dec)))
  ([counter button move-count]
     (let [c @counter
           direction (= button "next")
           within-boundary (if direction
                             (< c move-count)
                             (< 0 c))
           ]
       ;;(.log js/console (str c ", move-count=" move-count ", direction=" direction))
       (if within-boundary
         (update-counter counter direction)
         c))))

(defn game-binder [c {:keys [id1 id2 move-count] :as params}]
  (fn [$element]
    (let [counter (atom 0)]
     (go (loop [{board :board} {:board (initial-board) }]
           (let [button (<! c)]
             (when button
               (.log js/console (pr-str params))
               (let [
                     counter-val (update-counter counter button move-count)
                     new-board (<! (GET (str "/board/" id1 "/" id2 "/" counter-val)))]
                 (.log js/console (pr-str button))

                 (d/replace-contents! $element
                                      (render-replay-game
                                       (merge params
                                        {:board new-board
                                         :iteration counter-val})) )
                 (recur {:board new-board})))))))))

(defn render-replay-page [bind-input! bind-game!]
  (node
   (list
    [:div

     [:h3 "Game:"]
     (doto (node [:div])
       bind-game!)
    [:div
     ;;[:h3 "Send a message to the server:"]
     (doto (node [:button {:type :submit :class "btn btn-success"} "prev"])
       bind-input!)
     (doto (node [:button {:type :submit :class "btn btn-success"} "next"])
       bind-input!)]

     ])))

;;[:button {:type "submit" :onclick "loadFunction();" :class "btn btn-success"} "Load"]

(def replay-page (fn []
        (go
          (let [
                ;;ws (<! (ws-ch (str "ws://" js/window.location.host "/ws")))
                {:keys [id1 id2] :as params} (get-params)
                {:keys [history]} (<! (GET (str "/result/" id1 "/" id2)))
                valid-move-nb (count (filter vector? history))
                ch (chan)
                ]
            (.log js/console (str id1 id2 valid-move-nb))

            ;(bind-msgs ws in-game-msgs results-msgs)

           (d/replace-contents! (sel1 :#content)
                                (render-replay-page (button-input-binder ch)
                                                    (game-binder ch (merge params {:move-count valid-move-nb})
                                                          )))
           (d/replace-contents! (sel1 :#content2)
                                (pr-str history))

           ;; (d/replace-contents! (render-replay-game {:board (initial-board)
           ;;                                           :iteration 0}))
           (>! ch {:board (initial-board) :iteration 0} )
           ))))

(set! (.-onload js/window)

      (case (get-base-uri)
        "/" home-page
        "/html/replaygame" replay-page
        home-page
        )
)

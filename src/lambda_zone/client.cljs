(ns lambda-zone.client
  (:require [chord.client :refer [ws-ch]]
            [cljs.core.async :refer [<! >! put! take! close! timeout]]
            [dommy.core :as d]
            [goog.string.format :as gformat])
  (:require-macros [cljs.core.async.macros :refer [go]]
                   [dommy.macros :refer [node sel1]]))


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
    [:div
     [:h3 "Send a message to the server:"]
     (doto (node [:input {:type :text :size 50}])
       bind-input!)]
    [:div

     [:h3 "Messages from the server:"]
     (doto (node [:div])
       bind-list!)])))

(defn keywordize-map [my-map]
  (into {}
  (for [[k v] my-map]
    [(keyword k) v])))

(defn deserialize-msg [msg]
  ((js->clj (JSON/parse (:message msg)) :keywordize-keys true) :msg))

(defn render-board-old [board]
  (let [b (partition 8 board)]
    (for [col b]
      [:li (pr-str col) ]
      )))


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

(defn dispatch-old [msgs]
  (node
   [:ul
    (if (seq msgs)
      (let [msg (first msgs)
            {:keys [msg-type] :as d-msg} (deserialize-msg msg)

            ]
        ;;(.log js/console  (str "dispatching:" (pr-str d-msg )))
        (case msg-type
          "in-game-update" (do (.log js/console  (str "in-game-update:" (pr-str d-msg )))
                               (render-in-game-update d-msg))
          "full-results" (do (.log js/console  (str "full-results:" (pr-str d-msg )))
                            [:div "99999"])
          (render-board (initial-board)))

        )
      ;;(render-board (initial-board))
      ;[:li "None yet."]
      )]))

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

(defn render-results [matches]
  (node
   [:div [:table {:class "table"}
      (for [{:keys [id1 id2 score result]} matches]
        [:tr [:td (str id1 " vs " id2)] [:td (pr-str score)]  [:td result]])]]))

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

(defn render-list-old [msgs]
  (node
   [:ul
    (if (seq msgs)
      (for [msg msgs]
        [:li (pr-str msg)])
      [:li "None yet."])]))

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

(set! (.-onload js/window)
      (fn []
        (go
          (let [in-game-msgs (atom [])
                results-msgs (atom [])
               ws (<! (ws-ch "ws://localhost:3000/ws"))
               ]
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

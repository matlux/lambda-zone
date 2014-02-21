(ns lambda-zone.client
  (:require [chord.client :refer [ws-ch]]
            [cljs.core.async :refer [<! >! put! take! close! timeout]]
            [dommy.core :as d])
  (:require-macros [cljs.core.async.macros :refer [go]]
                   [dommy.macros :refer [node sel1]]))

(defn c1dto2d [i]
  (vector (int (/ i 8)) (mod i 8)))


(defn render-board-old [board-state]
  (let [line "+---+---+---+---+---+---+---+---+"
        pieces-pos board-state ;(into {} board-state)
        ]
    (apply str "\n" line "\n"
           (map #(let [pos (c1dto2d (dec %))
                       c (get pieces-pos pos " ")]
                   (if (zero? (mod % 8))
                           (format "| %s |\n%s\n" c line)
                           (format "| %s " c))) (range 1 65)))))

(defn render-page [bind-input! bind-list!]
  (node
   (list
    [:div
     [:h3 "Send a message to the server:"]
     (doto (node [:input {:type :text :size 50}])
       bind-input!)]
    [:div
     [:h3 "test" (pr-str (js->clj  "{
                     list: [1,2,3,4,5],
                     blah: \"vtha\",
                     o: { answer: 42 }
                   }"))]
     [:h3 "Messages from the server:"]
     (doto (node [:div])
       bind-list!)])))

(defn deserialize-msg [msg]
  ((js->clj (JSON/parse (:message msg))) "msg"))

(defn render-board [d-msg]
  (let [b (partition 8 (d-msg "board"))]
    (for [col b]
      [:li (pr-str col) ]
      )))

(defn render-list [msgs]
  (node
   [:ul
    (if (seq msgs)
      (let [msg (first msgs)
            d-msg (deserialize-msg msg)]
        (render-board d-msg))
      [:li "None yet."])]))

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
                 (->> (reverse msgs)
                      (take 10)
                      (render-list)
                      (d/replace-contents! $list))))))

(defn input-binder [ch]
  (fn [$input]
    (d/listen! $input :keyup
               (fn [e]
                 (when (= 13 (.-keyCode e))
                   (put! ch (d/value $input))
                   (d/set-value! $input ""))))
    (go (<! (timeout 200)) (.focus $input))))

(defn bind-msgs [ch msgs]
  (go
   (loop []
     (when-let [msg (<! ch)]
       (swap! msgs conj msg)
       (recur)))))

(set! (.-onload js/window)
      (fn []
        (go
         (let [msgs (atom [])
               ws (<! (ws-ch "ws://localhost:3000/ws"))]
           (bind-msgs ws msgs)
           (d/replace-contents! (sel1 :#content)
                                (render-page (input-binder ws)
                                             (list-binder msgs)))
           (reset! msgs [])))))

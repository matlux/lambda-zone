(ns lambda-zone.backend
  (:require [clojure.math.numeric-tower :as math]
            [clj-chess-engine.core :refer :all]
            [clojure.string :as str]
            [clojure.set :as se]
            [cemerick.friend :as friend] :reload-all)
  (:import clojure.lang.PersistentVector))




;; ----------------------- stats

(defn write-file
  "Writes a value to a file"
  [value out-file]
  (spit out-file "" :append false)
  (with-open [out-data (clojure.java.io/writer out-file)]
      (.write out-data (str value))))

(defn read-file [in-file]
  (with-open [rdr (clojure.java.io/reader in-file)]
    (reduce conj [] (line-seq rdr))))


(def random-f-src-with-print "(fn random-f [{board :board am-i-white? :white-turn valid-moves :valid-moves ic :in-check? h :history s :state}]
  (let [v (into [] valid-moves)
        iteration (if (nil? s) (+ 1 (if am-i-white? 0 1)) (+ 2 s))]

    (println (if am-i-white? \"white: \" \"black: \"))
    (println \"valid moves:\" valid-moves)
    (println \"iteration:\" iteration)
    (let [move (rand-int (count valid-moves))]
      (println \"choosen move:\" (get v move))
      {:move (get v move) :state iteration})) )")

(def random-f-src "(fn random-f [{board :board am-i-white? :white-turn valid-moves :valid-moves ic :in-check? h :history s :state}]
  (let [v (into [] valid-moves)
        iteration (if (nil? s) (+ 1 (if am-i-white? 0 1)) (+ 2 s))]
    (let [move (rand-int (count valid-moves))]
      {:move (get v move) :state iteration})) )")

(def random-f-form '(fn random-f [{board :board, am-i-white? :white-turn, valid-moves :valid-moves, ic :in-check?, h :history, s :state}]
                      (let [v (into [] valid-moves)
                            iteration (if (nil? s) (+ 1 (if am-i-white? 0 1)) (+ 2 s))]
                        (let [move (rand-int (count valid-moves))]
                          {:move (get v move), :state iteration}))))


(def database (atom (read-string (slurp "./db.clj"))))

(def database-test (atom {:matches [{:id1 "superman" :id2 "daredevil" :score [1 0]}]
                          :contenders [{:login "Philip" :id "daredevil" :fn random-f-src}
                                       {:login "Nicholas" :id "superman" :fn random-f-src}
                                       {:login "Steve" :id "Wonderboy" :fn random-f-src }
                                        ] }))

(defn all-available-functions [db]
  (:contenders db))

(defn all-available-function-matches [db]
  (let [all-fns (all-available-functions db)]
    (for [fn1 all-fns
          fn2 all-fns
          :when (and (not= fn1 fn2)
                     ;(> 0 (compare (.toString fn1) (.toString fn2)))
                     )] [fn1 fn2])))

(defn all-matches [db]
  (:matches db))

(defn fn-pairs-already-comp [db]
  (let [played-matches (all-matches db)]
    (for [[{id1 :id :as f1} {id2 :id :as f2}] (all-available-function-matches db)
          {id1m :id1 id2m :id2} (all-matches db)
          :when (and
                 (= [id1 id2] [id1m id2m])
                 ;;(not= id2 id2m)
                 ;;(> 0 (compare (.toString fn1) (.toString fn2)))
                 )]
      [f1 f2])))
;; (defn remaining-fn2comp [db]
;;   (let [possible-matches (all-available-function-matches db)
;;         played-matches (all-matches db)]
;;     (filter
;;      (fn [[{id1 :id :as f1} {id2 :id :as f2}]]
;;        (some
;;         (fn [{id1m :id1 id2m :id2 :as match}] (= [id1 id2] [id1m id2m])) played-matches))
;;      possible-matches)))

(defn remaining-fn-pairs2comp [db]
  (let [possible-matches (into #{} (all-available-function-matches db))
        already-played (into #{} (fn-pairs-already-comp db))]
    (se/difference possible-matches already-played)))


;;(filter (fn [{id1m :id1 id2m :id2 :as match}] ) played-matches)
(count (all-matches @database))

(count (all-available-function-matches
        @database))
(count (remaining-fn-pairs2comp @database))

(defn select2functions [db]
  (first (remaining-fn-pairs2comp db)))

(defn select-contender-by-id [db id]
  (for [{ i :id :as function} (:contenders db)
        :when (= i id)
        ]
    function
    ))

(select-contender-by-id @database "daredevil")

(count (select2functions @database))

(@database :contenders)

(defn save-result [result]
  (let [{s :score res :result id1 :id1 id2 :id2}  result
        ]
    (swap! database (fn [db]
                      {:matches (conj (:matches db) (dissoc result :history :board) )
                       :contenders (:contenders db)}))
    (write-file   (-> (str @database) (str/replace #"}" "}\n\t") (str/replace #"" "")) "./db.clj")))


;(str @database)

(defn compile-fn-verbosed [f src]
  (if (nil? f)
    (wrapper-display-f (eval (read-string src)))
    f))
(defn compile-fn [f src]
  (if (nil? f)
    (eval (read-string src))
    f))

;;((compile-fn nil random-f-src) {})

(defmacro with-time-assoced
  "Evaluates exprs in a context in which *out* is bound to a fresh
  StringWriter.  Returns the assoced map with :time -> created by any nested printing
  calls."
  [& body]
  `(let [s# (new java.io.StringWriter)]
     (binding [*out* s#]
       (assoc (time ~@body) :time (str s#)))))


(defn tournament []
  (let [match (select2functions @database)]
   (when match
     (let [[{name1 :login fn-src1 :fn id1 :id} {name2 :name fn-src2 :fn id2 :id}] match
          f1 (dbg (compile-fn nil (dbg fn-src1)))
          f2 (dbg (compile-fn nil fn-src2))
          result (with-time-assoced (play-game {:board (initial-board) :id1 id1 :f1 f1 :id2 id2 :f2 f2}))
          res (save-result (merge result {:id1 id1 :id2 id2}))
          ]
       (println result)
       (recur)))))

(def tournament-agent (agent {}))

(defn schedule-recomputation []  (send tournament-agent (fn [_] (tournament))))

(defn save-function [{function :body :as req}]
  (let [{login :identity} (friend/current-authentication req)]
    (swap! database (fn [db]
                     {:matches (:matches db)
                      :contenders (conj (:contenders db) (assoc function :login login))}))
   (write-file   (-> (str @database) (str/replace #"}" "}\n\t") (str/replace #"" "")) "./db.clj")
   (schedule-recomputation))
  )


;; (let [[{name1 :login fn-src1 :fn id1 :id} {name2 :name fn-src2 :fn id2 :id}] ((fn [] nil))]
;;   name1)






(defn -main []
  ;;  (tournament)
  ;;(save-function {:login "a" :id "b" :fn random-f-src })
  (send (agent {}) (do (fn [_] (tournament)) 1))
  (println "finished"))

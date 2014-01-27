(ns lambda-zone.stats
  (:require [clojure.math.numeric-tower :as math]
            [clj-chess-engine.core :refer :all]
            [clojure.string :as str :refer [replace]] :reload-all)
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


(def random-f-src '(fn random-f [{board :board am-i-white? :white-turn valid-moves :valid-moves ic :in-check? h :history s :state}]
  (let [v (into [] valid-moves)
        iteration (if (nil? s) (+ 1 (if am-i-white? 0 1)) (+ 2 s))]

    (println (if am-i-white? "white: " "black: "))
    (println "valid moves:" valid-moves)
    (println "iteration:" iteration)
    (let [move (rand-int (count valid-moves))]
      (println "choosen move:" (get v move))
      {:move (get v move) :state iteration})) ))


(def database (atom (read-string (slurp "./db.clj"))))

(def database-test (atom {:matches [{:id1 "superman" :id2 "daredevil" :score [1 0]}]
                     :contenders [{:login "Philip" :functions [{:id "daredevil" :function-src random-f-src}]}
                                  {:login "Nicholas" :functions [{:id "superman" :function-src random-f-src}]}
                                  {:login "Steve" :functions [{:id "Wonderboy" :function-src random-f-src }]}
                                  {:login "Bob" :functions [{:id "Wonderboy2" :function-src random-f-src }]}] }))

(defn all-available-functions [db]
  (for [{login :login fns :functions} (:contenders db)
        {id :id fn :function fn-src :function-src} fns
        ]
    {:login login :id id :fn fn :fn-src fn-src }
    ))

(defn all-available-function-matches [db]
  (let [all-fns (all-available-functions db)]
    (for [fn1 all-fns
          fn2 all-fns
          :when (and (not= fn1 fn2)
                     ;(> 0 (compare (.toString fn1) (.toString fn2)))
                     )] [fn1 fn2])))



(defn select2functions [db]
  (first (all-available-function-matches db)))

(defn select-contender-by-id [db id]
  (for [{login :login fns :functions} (:contenders db)
        {i :id fn :function fn-src :function-src} fns
        :when (= i id)
        ]
    {:login login :id id :fn fn :fn-src fn-src  }
    ))

(select-contender-by-id @database "daredevil")

(count (select2functions @database))

(@database :contenders)

(defn save [result]
  (let [{s :score res :result id1 :id1 id2 :id2}  result
        ]
    (swap! database (fn [db]
                      {:matches (conj (:matches db) (dissoc result :history :board) )
                       :contenders (:contenders db)}))
    (write-file   (-> (str @database) (str/replace #"}" "}\n\t") (str/replace #"" "")) "./db.clj")))

;(str @database)

(defn compile-fn [f src]
  (if (nil? f)
    (wrapper-display-f (eval src))
    f))

;;((compile-fn nil random-f-src) {})

(defn tournament []
  (let [[{name1 :login f-1 :fn fn-src1 :fn-src id1 :id} {name2 :name f-2 :fn fn-src2 :fn-src id2 :id}] (select2functions @database)
        f1 (dbg (compile-fn f-1 (dbg fn-src1)))
        f2 (dbg (compile-fn f-2 fn-src2))
        result (play-game {:board (initial-board) :id1 id1 :f1 f1 :id2 id2 :f2 f2})]
    (save (merge result {:id1 id1 :id2 id2}))
   (println result)
   (recur)))


(defn -main []
 (tournament))

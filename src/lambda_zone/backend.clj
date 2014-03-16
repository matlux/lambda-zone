(ns lambda-zone.backend
  (:require [clojure.math.numeric-tower :as math]
            [clj-chess-engine.core :as chess]
            [clojure.string :as str]
            [clojure.set :as se]
            [cemerick.friend :as friend]
            [clojure.core.async :refer [<! >! >!! put! take! close! chan go go-loop]]
            ;:reload-all
            )
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



;(str @database)




;; (defn eval-form-safely [form]
;;   (fn [in] ((chess/sb) (list form in))))
(defn eval-form-safely [form]
  (chess/sb form 6000))

(defn compile-fn-verbosed [f src]
  (if (nil? f)
    (chess/wrapper-display-f (eval (read-string src)))
    f))
(defn compile-fn
  ([src]
     compile-fn nil src)
  ([f src]
     (if (nil? f)
       (eval-form-safely (read-string src))
       ;;(fn [in] ((chess/sb) (list (read-string src) in)));; (fn [in] ((sb) (list random-f-form in)))
       f)))

(defn single-param? [param]
  (and (vector? param) (= (count param) 1)))


(defn parse-error? [form]
  (map? form))

(defn validate-form [form]
  (if (coll? form)
    (let [ ;;form (read-string src)
         [fn-exp & sx] form
         [name & sx2] (if (symbol? (first sx)) sx (conj sx nil)  )
         ]
     (cond (and (not= fn-exp 'fn) (not= fn-exp 'fn*)) {:result "submitted code is not a function" :reason "fn of fn* missing at beginning of function"}
           (or
            (and (list? (first sx2)) (every? #(not (single-param? %)) sx2))
            (not (single-param? (first sx2)))) {:result "function should accept one argument"}
           :else {:result :ok})
     )
    {:result :not-valid-function :reason "define an fn form"}))

(defn evaluate-and-catch [form]
  (try
    (eval-form-safely form)
    (catch Throwable e {:result :failed-evaluation :reason (str "caught exception: " (.getMessage e))})))

(defn validate-compile-exec [form]
  (try
    (let [evaled (evaluate-and-catch form)] ;;; it's ok now we use clojail
      (if (:result evaled)
        evaled
        (let [
              eval-result (evaled {:board (chess/initial-board) :white-turn true :valid-moves [["e2" "e4"]] :history []})]
          (cond (not (or
                      (and (map? eval-result) (vector? (:move eval-result)))
                      (vector? eval-result)))
                {:result :failed-test-execution :reason "function compiles, execute but does not return a well formed valid move"}
                :else {:result :ok}
                ))))
    (catch Throwable e {:result (str "caught exception: " (.getMessage e))})))


(defn validate-read-string [src]
  (try
    (read-string src)
    (catch Throwable e {:result :not-valid-clojure-form :reason (str "caught exception: " (.getMessage e))})))

;;(parse-error? {:result :not-valid-clojure-form})

(defn validate-fn [src]
  (let [form (validate-read-string src)]
    (if (parse-error? form)
      form
      (let [{res-validation :result :as val-form} (validate-form form)]
       (if (= res-validation :ok)
         (validate-compile-exec form)
         val-form)
       ))))

;;(validate-fn random-f-src)
;;(validate-fn "#(+ 1 1)")
;;
;;(validate-fn "(+ 1 1)")
;;(validate-fn "(fn [a] (+ 1 1))")
;;(validate-fn "(fn name [a] (+ 1 1))")
;;(validate-fn "(fn name [a] (1 1))")
;;(validate-fn "(fn name [a] (1 1)")
;;(validate-fn "1")
;;(validate-fn "{a b c d}")
;;(eval (read-string "(fn name [a] (1 1)"))

;;(read-string "#(+ 1 1)")
;;(read-string random-f-src)

(defmacro with-time-assoced
  "Evaluates exprs in a context in which *out* is bound to a fresh
  StringWriter.  Returns the assoced map with :time -> created by any nested printing
  calls."
  [& body]
  `(let [s# (new java.io.StringWriter)
         oldout# *out*]
     (binding [*out* s#]
       (assoc (time (binding [*out* oldout#] ~@body)) :time (str s#)))))

(defn parse-int [s]
   (Integer. (re-find  #"\d+" s )))

;;(with-time-assoced (println "hello"))

(defn load-results-from-atom []
  (:matches @database))

(defn load-results []
  (load-results-from-atom))

(defn retrieve-result-from-atom [id1-match id2-match]
  (first (filter (fn [{id1 :id1 id2 :id2}] (and (= id1 id1-match) (= id2 id2-match))
                   ) (:matches @database))))

(defn retrieve-result [id1 id2]
  (retrieve-result-from-atom id1 id2))

(defn retrieve-board [id1 id2 move]
  (let [{moves :history} (retrieve-result id1 id2)]
    (println "number of moves" (count moves))
    (nth (chess/board-seq moves) (parse-int move))
    ))

;;(retrieve-board "daredevil" "d" "0")
;;(retrieve-result "daredevil" "d")
;;(chess/board-seq [["e2" "e4"] ["e7" "e5"] ["d1" "h5"] ["d7" "d6"] ["f1" "c4"] ["b8" "c6"] ["h5" "f7"] ["e8" "e7"]] )


(defn save-result [result]
  (let [{s :score res :result id1 :id1 id2 :id2}  result
        ]
    (swap! database (fn [db]
                      {:matches (conj (:matches db) (dissoc result :board) )
                       :contenders (:contenders db)}))
    (write-file   (-> (str @database) (str/replace #"}" "}\n\t") (str/replace #"" "")) "./db.clj")))

(defn remove-f-id-in-atom [{id :id login :login :as function} contenders]
  (remove (fn [{iddb :id logindb :login}]  (and (= iddb id) (= logindb login))) contenders))

(declare schedule-recomputation)


(defn save-function-in-atom [{id :id login :login c :channel :as function}]
  (let [cleaned-f (dissoc function :channel)]
    (swap! database (fn [db]
                     {:matches (remove (fn [{:keys [id1 id2]}] (or (= id1 id) (= id2 id))) (:matches db))
                      :contenders (conj (remove-f-id-in-atom function (:contenders db)) cleaned-f)}))
    (write-file   (-> (str @database) (str/replace #"}" "}\n\t") (str/replace #"" "")) "./db.clj")
    (schedule-recomputation c))
  )

(defn retrieve-function-in-atom [{id :id login :login :as function}]
  (first (filter (fn [{iddb :id logindb :login}]  (and (= iddb id) (= logindb login))) (:contenders @database)))
  )

(defn duplicate-function-in-atom? [{id :id login :login :as function}]
  (some (fn [{iddb :id logindb :login}] (and (= iddb id) (not= logindb login))) (:contenders @database)))

;;(defn delete (filter (fn [{:keys [id1 id2]}] (not (or (= id1 "a") (= id2 "a")))) (:matches @database)))

(defn delete-result-in-atom [{id :id login :login :as function}]
  (swap! database (fn [db]
                    {:matches (filter (fn [{:keys [id1 id2]}] (not (or (= id1 "a") (= id2 "a")))) (:matches db))
                     :contenders (:contenders db)}))
  (write-file   (-> (str @database) (str/replace #"}" "}\n\t") (str/replace #"" "")) "./db.clj"))

;;(delete-result-in-atom [{:id "a"}])


(defn save-function [{{id :id fn :fn :as function} :body :as req} c]
  (let [{login :email :as auth} (friend/current-authentication req)
        f-with-identity (assoc function :login login :channel c)
        {:keys [result reason] :as validation-result} (validate-fn fn)]
    (println "auth:" auth)
    (cond
     (not= result :ok) validation-result
     ;;(nil? login) {:return "function cannot be added anonymously. Please login with the openId above"}
     (duplicate-function-in-atom? f-with-identity)
     {:return (str "function name " id " is already owned by a different user")}
     :else (do

             (save-function-in-atom f-with-identity)
          (if (nil? login)
            {:return "function added anonymously"}
            {:return "function added ok"}))))
  )
(defn retrieve-function [id]
  (let [{login :email} (friend/current-authentication friend/*identity*)
        f-pk {:login login :id id}]
    (cond
     (nil? login) {:return "function cannot be retrieved anonymously. Please login with the openId above"}
     (duplicate-function-in-atom? f-pk)
     {:return (str "function name " id " is already owned by a different user")}
     :else (let [r-f (retrieve-function-in-atom f-pk)]
             (if (nil? r-f)
               {:result "function not found" :not-found true}
               r-f))
     ))
  )


(defn tournament [c]
  (let [match (select2functions @database)]
   (when match
     (let [[{name1 :login fn-src1 :fn id1 :id} {name2 :name fn-src2 :fn id2 :id}] match
           _  (println "found to new match to play: " id1 "against" id2)
           ;;_ (>!! c match)
           f1 (compile-fn nil fn-src1)
           f2 (compile-fn nil fn-src2)
           result (with-time-assoced (chess/play-game {:board (chess/initial-board) :id1 id1 :f1 f1 :id2 id2 :f2 f2 :channel c}))
           result-with-id (merge result {:id1 id1 :id2 id2})
           res (save-result result-with-id)
           finalres (load-results)
           ]
       (println "game finished. About to write result into channel:" c finalres)
       ;;(>!! c (merge (dissoc result-with-id :history) {:msg-type :publish-game-result}))
       (>!! c  {:msg-type :full-results :matches finalres})
       (println "message was sent to channel" c)
       (recur c)))))

(def tournament-agent (agent {}))

(defn schedule-recomputation [c]  (send tournament-agent (fn [_] (tournament c))))




;;@database
;; (let [[{name1 :login fn-src1 :fn id1 :id} {name2 :name fn-src2 :fn id2 :id}] ((fn [] nil))]
;;   name1)

;;(duplicate-function-in-atom? {:login nil, :id "b", :fn "a"})
;;(remove-f-id-in-atom {:login "https://www.google.com/accounts/o8/id?id=AItOawl4hK6AcG3du1Fu9MeeF-sgFVSxrmpWVOU", :id "a", :fn "a"} (:contenders @database))
;;(save-function-in-atom {:login "https://www.google.com/accounts/o8/id?id=AItOawl4hK6AcG3du1Fu9MeeF-sgFVSxrmpWVOU", :id "a", :fn "(+ 1 1)"})




(defn -main []
  ;;  (tournament)
  ;;(save-function {:login "a" :id "b" :fn random-f-src })
  (send (agent {}) (do (fn [_] (tournament)) 1))
  (println "finished"))


(defn acc-scores [acc {:keys [score id1 id2]}]
  (-> (assoc acc id1 (+ (get score 0) (get acc id1 0)))
      (assoc id2 (+ (get score 1) (get acc id2 0)))))

(sort-by key (group-by #(get % 1) (into [] (reduce acc-scores {} (:matches @database)))))

(defn extract-val-from-vector [[score coll]]
  (letfn [(f [[id  score]] id)]
    [score (map f coll)]))

(defn extract-rank [rank [score coll]]
  (letfn [(f [id] [id (inc rank)])]
    (map f coll)))

(defn rank [matches] (->> (reduce acc-scores {} matches)
       (into [])
       (group-by #(get % 1))
                                        ;(map #(println %))
       (map extract-val-from-vector)
       (into {})
       (sort-by key)
       reverse
       (mapcat extract-rank (range))
       ))

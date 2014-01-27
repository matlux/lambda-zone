(ns clj-chess-engine.core
  (:require [clojure.math.numeric-tower :as math])
  (:import clojure.lang.PersistentVector))

(defmacro dbg[x] `(let [x# ~x] (println "dbg:" '~x "=" x#) x#))
(defmacro display-assert [x & args] `(let [x# ~x] (if x# true (do (println '~x "is wrong because =" x# "and [" '~@args "] = [" ~@args "]") false))))

(macroexpand '(display-assert (+ x 1) x))

(defn initial-board []
  [\r \n \b \q \k \b \n \r
   \p \p \p \p \p \p \p \p
   \- \- \- \- \- \- \- \-
   \- \- \- \- \- \- \- \-
   \- \- \- \- \- \- \- \-
   \- \- \- \- \- \- \- \-
   \P \P \P \P \P \P \P \P
   \R \N \B \Q \K \B \N \R])

(defn test-board1 []
  [\r \n \b \q \k \b \n \r
   \p \p \p \p \p \p \p \p
   \- \- \- \- \- \- \- \-
   \- \- \- \- \- \- \- \-
   \- \- \- \- \- \- \- \-
   \- \- \N \- \- \- \- \-
   \P \P \P \P \P \P \P \P
   \R \- \B \Q \K \B \N \R])

(defn test-board2 []
  [\r \n \- \q \k \b \n \r
   \p \- \p \p \p \p \p \p
   \- \- \- \- \- \- \- \-
   \- \K \- \- \- \- \- \-
   \- \p \- \- \- \- \- \-
   \- \- \- \b \- \- \- \-
   \P \- \P \- \- \P \P \P
   \R \- \B \Q \K \B \N \R])

(defn check-mate-test []  ;; it's blacks turn, king is in check. no move will save him => check mate
  [\r \- \b \q \k \b \n \r
   \p \p \p \p \- \Q \p \p
   \- \- \n \- \- \p \- \-
   \- \- \- \- \p \- \- \-
   \- \- \B \- \P \- \- \-
   \- \- \- \- \- \- \- \-
   \P \P \P \P \- \P \P \P
   \R \N \B \- \K \- \N \R])

(defn in-check-test []  ;; it's blacks turn, king is in check. no move will save him => check mate
  [\r \- \b \q \- \b \n \r
   \p \p \p \p \k \Q \p \p
   \- \- \n \- \- \p \- \-
   \- \- \- \- \p \- \- \-
   \- \- \B \- \P \- \- \-
   \- \- \- \- \- \- \- \-
   \P \P \P \P \- \P \P \P
   \R \N \B \- \K \- \N \R])

(defn could-become-in-check-test []  ;; it's blacks turn, king is in check. no move will save him => check mate
  [\r \- \b \q \k \b \n \r
   \p \p \p \p \- \p \p \p
   \- \- \n \- \- \- \- \-
   \- \- \- \- \p \- \- \Q
   \- \- \B \- \P \- \- \-
   \- \- \- \- \- \- \- \-
   \P \P \P \P \- \P \P \P
   \R \N \B \- \K \- \N \R])

(defn en-passant-check-test [] ;; it's white's turn
  [\r \- \b \q \k \b \n \r
   \p \p \- \- \- \p \p \p
   \- \- \- \- \- \- \- \-
   \n \- \p \P \p \- \- \Q
   \- \- \B \- \- \- \- \-
   \- \- \- \- \- \- \- \N
   \P \P \P \P \- \P \P \P
   \R \N \B \- \K \- \- \R])




(def ^:dynamic *file-key* \a)
(def ^:dynamic *rank-key* \0)

(def white-turn true)
(def black-turn false)
(def black black-turn)
(def white white-turn)
(def last-move-was-invalid true)
(def last-move-was-valid false)
(def check-mate true)


(defn file-component [file]
  ;{:post [(and (< % 8) (>= % 0))]}
  (- (int file) (int *file-key*)))

(defn rank-component [rank]
  {:post [(display-assert (and (< % 64) (>= % 0)) rank)]}
  (->> (int *rank-key*)
       (- (int rank))
       (- 8)
       (* 8)))

(defn- file2coord [file]
  ;{:post [(and (< % 8) (>= % 0))]}
  (file-component file))

;(file-coord \a)

(defn- rank2coord [rank]
  ;{:post [(and (< % 8) (>= % 0))]}
  (->> (int *rank-key*)
       (- (int rank))
       (- 8)))

;(rank-coord \1)

(defn- coord2file [^long x]
  {:pre [(and (< x 8) (>= x 0))]}
  (->> (int *file-key*)
       (+ x)
       char))

(defn- coord2rank [^long y]
  {:pre [(and (< y 8) (>= y 0))]}
  (->> (- (int *rank-key*) y)
       (+ 8)
       char))

;(coord2rank 5)

;(rank-coord \1)
(defn pos2coord [^String pos]
  {:pre [(display-assert (and (string? pos) (= (count pos) 2)) pos)]}
  (let [[file rank] pos
        x (file2coord file)
        y (rank2coord rank)]
    [x y]))


(defn coord2pos [[x y]]
  {:pre [(display-assert (and (number? x)(number? y)))]}
  (let [
        file (coord2file x)
        rank (coord2rank y)]
    (str file rank)))

(defn movemap2move [[from {to :move-to}]]
  [from to])

(defn move-xy2move [move-xy] (let [[from to] move-xy] [(coord2pos from) (coord2pos to)]))
(defn move2move-xy [move] (if (nil? move) nil (let [[from to] move] [(pos2coord from) (pos2coord to)])))
(defn move-xy2move-vec [coll] (map (fn [[from to]] [(coord2pos from) (coord2pos to)]) coll))
(defn move2move-xy-vec [coll] {:pre [(display-assert (and (vector? coll) (string? (ffirst coll))) coll)]} (into [] (map (fn [[from to]] [(pos2coord from) (pos2coord to)]) coll)))

(defn move-xymap2move-vec [coll] (move-xy2move-vec (map movemap2move coll)))

;(coord2pos [4 6])


(move2move-xy ["e5" "e6"])

(defn- index [file rank]
  {:pre [(and (char? file) (char? rank))]}
  (+ (file-component file) (rank-component rank)))

(defn- index-xy [x y]
  (+ x (* y 8)))

;;(index \a \1)
;;(index-xy 7 7)

(defn lookup [^PersistentVector board ^String pos]
  {:pre [(string? pos)]}
  (let [[file rank] pos]
    (board (index file rank))))
(defn lookup-xy [^PersistentVector board ^PersistentVector pos]
  {:pre [(display-assert (and (vector? pos) (number? (first pos))) pos)]}
  (lookup board (coord2pos pos)))

;; (file-component \b)
;;(rank-component \1)
;; (lookup (initial-board) "e5")
;;=> \R
;;(lookup-xy (initial-board) [0 7])

;; ------------- all possible moves

(defn is-white? [^Character piece]
  {:pre [(display-assert (char? piece) piece)]}
  (Character/isUpperCase piece))
(defn is-black? [^Character piece]
  (Character/isLowerCase piece))
(defn is-piece? [^Character piece]
  (Character/isLetter piece))

(defn- is-knight? [ piece]
  (or (= piece \N)
      (= piece \n)))
(defn- is-bishop? [ piece]
  (or (= piece \B)
      (= piece \b)))
(defn- is-queen? [ piece]
  (or (= piece \Q)
      (= piece \q)))
(defn- is-king? [ piece]
  (or (= piece \K)
      (= piece \k)))
(defn- is-rook? [ piece]
  (or (= piece \R)
      (= piece \r)))
(defn- is-pawn? [ piece]
  (or (= piece \P)
      (= piece \p)))



(defprotocol Piece
  (getMoves [this]))

;;(pos2coord "e5")

(defn- pos-xy-within-board? [[x y]]
  (and (< x 8)
       (>= x 0)
       (< y 8)
       (>= y 0)
       ))

;; (defn pos2coord [^String pos]
;;   {:pre [(display-assert (and (string? pos) (= (count pos) 2)) pos)]}
;;   )

(defn- pos-within-board? [^String pos]
  (pos-xy-within-board?
   (let [[file rank] pos
         x (file2coord file)
         y (rank2coord rank)]
     [x y])))

(pos-within-board? "e8")
(pos-within-board? "e9")
(pos-within-board? "q8")

(defn collid-self? [board white-turn? coord]
  (if white-turn?
    (is-white? (lookup board (coord2pos coord)))
    (is-black? (lookup board (coord2pos coord)))))
(defn collid-oposite? [board white-turn? coord]
  (if white-turn?
    (is-black? (lookup board (coord2pos coord)))
    (is-white? (lookup board (coord2pos coord)))
    ))

(collid-oposite? (initial-board) true [2 7])


;(collid-self? (initial-board) true [0 7])

(defn- is-vertical? [[x1 y1] [x2 y2]]
  (zero? (- x1 x2)))



(defn- pos-between-vertical [[x1 y1] [x2 y2]]
  (let [[b1 b2] (if (= (.compareTo y2 y1) 1) [y1 y2] [y2 y1])]
      (for [a (range (inc b1) b2)] [x1 a])))

(defn- pos-between-xy [[x1 y1] [x2 y2]]
  {:pre [(let [absslop (math/abs (/ (- y2 y1) (- x2 x1)))]
           ;(println absslop)
           (or (= absslop 1)
               (= absslop 0)))]}
  (let [forward? (> (- x2 x1) 0)
          slop (/ (- y2 y1) (- x2 x1))
          [step a b] (if forward? [1 x1 y1] [-1 x2 y2])
         f (fn [x] [(+ a (* 1 x)) (+ b (* slop x))])]
      (map f (range 1 (math/abs(- x2 x1))))))

(defn- pos-between [p1 p2]
  (if (is-vertical? p1 p2)
    (pos-between-vertical p1 p2)
    (pos-between-xy p1 p2)))


(pos-between [0 0] [7 7])
(pos-between [0 0] [0 7])
(pos-between [2 2] [7 7])
(pos-between [0 0] [7 0])
(pos-between [7 7] [0 0])
(pos-between [0 7] [0 0])
(pos-between [7 0] [0 0])
(pos-between [7 7] [1 1])



(defn- nothing-between [board p1 p2]
  (not-any? is-piece? (map #(lookup-xy board %) (pos-between p1 p2))))

(nothing-between (initial-board) [0 0] [7 7])
(nothing-between (initial-board) [1 1] [6 6])
(nothing-between (initial-board) [2 0] [6 0])
(nothing-between (initial-board) [0 1] [0 6])
(nothing-between (initial-board) [0 0] [7 7])
(nothing-between (initial-board) [0 0] [7 7])
(nothing-between (initial-board) [0 0] [7 7])
(nothing-between (test-board2) [0 7] [1 7])

(defn collid? [board pos] (not (= (lookup-xy board pos) \-)))

(defn c2dto1d [v]
  (let [[x y] v]
    (clojure.core/+ x (clojure.core/* 8 y))))

(defn c1dto2d [i]
  (vector (int (/ i 8)) (mod i 8)))

(defn c1dto2d-xy [i]
  (vector (mod i 8) (int (/ i 8))))

(defn char2state [pieces-list]
  (into {} (filter #(not= \- (second %)) (map #(vector (c1dto2d %1) %2 ) (range 64) pieces-list))))

(defn board2xy-map-piece [pieces-list]
  (into {} (filter #(not= \- (second %)) (map #(vector (c1dto2d-xy %1) %2 ) (range 64) pieces-list))))


;; ((fn [pieces-list]
;;    (into {} (filter #(not= \- (second %)) (map #(vector (c1dto2d %1) %2 ) (range 64) pieces-list)))) (initial-board))

;;(collid? (initial-board) [1 6])


;;---- pawn moves

(defn- en-passant? [board white? last-move diag x y]
  {:pre [(display-assert (or (and (vector? last-move) (string? (first last-move))) (nil? last-move)) last-move)]}
  (when (not (nil? last-move))
    (let [[op opposite-start-rank] (if white? [+ 1] [- 6])
         [from to] (move2move-xy last-move)
         pawn? (is-pawn? (lookup-xy board to))
         [dx dy] diag
         [fx fy] from
         [tx ty] to
         ]
     (let [ep (and pawn?
                 (= fx tx dx)
                 (= fy opposite-start-rank)
                 (= ty (op fy 2) (op dy 1)))]
       ;(when ep (do (println "possible en-passant move" x y ", last-move" last-move ",board" board)))
       ep))))
;;(en-passant? (en-passant-check-test) white ["c7" "c5"] [2 2] 3 3)
;; => true
;;(en-passant? (en-passant-check-test) white ["c7" "c6"] [2 2] 3 3)
;; => false
;;(en-passant? (en-passant-check-test) white ["c7" "c5"] [2 1] 3 2)
;; => false
;;(en-passant? (en-passant-check-test) white ["c7" "c5"] [2 3] 3 4)
;; => false
;;(en-passant? (en-passant-check-test) white ["c7" "c4"] [2 2] 3 3)
;; => false
;;(en-passant? (en-passant-check-test) white ["e7" "e5"] [4 2] 3 3)
;; => true
;;(en-passant? (en-passant-check-test) white ["c7" "c5"] [4 2] 3 3)
;; => false

;;(en-passant? (en-passant-check-test) white nil [4 2] 3 3)
;; => false


(defn- pawn-moves [board white? last-move x y]
  (let [[op start-rank] (if white? [- 6] [+ 1])
        right-diag [(+ x 1) (op y 1)]
        left-diag [(- x 1) (op y 1)]
        front [x (op y 1)]
        front2 [x (op y 2)]
        moves (vector
               (when (and (pos-xy-within-board? right-diag)
                          (collid-oposite? board white? right-diag))
                 {:move-to right-diag} )
               (when (and (pos-xy-within-board? right-diag)
                          (en-passant? board white? last-move right-diag x y))
                 {:move-to right-diag :en-passant true :taken [(+ x 1) y] :last-move last-move :board board} )
               (when (and (pos-xy-within-board? left-diag)
                          (collid-oposite? board white? left-diag))
                 {:move-to left-diag}  )
               (when (and (pos-xy-within-board? left-diag)
                          (en-passant? board white? last-move left-diag x y))
                 {:move-to left-diag :en-passant true :taken [(- x 1) y] :last-move last-move :board board}  )
               (when (and
                      (pos-xy-within-board? front)
                      (not (collid? board front)))
                 {:move-to front})
               (when (and
                      (pos-xy-within-board? front2)
                      (not (collid? board front))
                      (not (collid? board front2))
                      (= y start-rank))
                 {:move-to front2})

               )
        ]
    (filter (comp not nil?) moves)))

;;(not (= (lookup-xy (initial-board) [2 1]) \-))
;;(pawn-moves (initial-board) false [] 1 1)
;;(map coord2pos (pawn-moves (en-passant-check-test) white ["c7" "c5"] 3 3))
;;(map (fn [a] (meta a)) (pawn-moves (en-passant-check-test) white ["c7" "c5"] 3 3))
;;  (map (fn [a] (meta a)) (pawn-moves (en-passant-check-test) white ["e7" "e5"] 3 3))
;;(->> (pawn-moves (en-passant-check-test) white ["e7" "e5"] 3 3) first meta)

;;(count (pawn-moves (initial-board) white [] 2 2))
;(nothing-between (initial-board) )

(defrecord Pawn [^clojure.lang.PersistentVector board ^String pos ^boolean white? history]
  Piece
  (getMoves [this]
    (let [[x y] (pos2coord pos)
          last-move (last history)
          moves (pawn-moves board white? last-move x y)
          no-self-collision? (comp not (partial collid-self? board white?))]
      (filter #(no-self-collision? (:move-to %)) (filter #(pos-xy-within-board? (:move-to %)) moves)))))


;;(->> (Pawn. (en-passant-check-test) "d5" white [[(pos2coord "e7") (pos2coord "e5")]]) getMoves first meta)

;;(getMoves (Pawn. (test-board2) "b3" false))
;; => ("c2" "a2" "b2")
;;(getMoves (Pawn. (test-board2) "a7" false))
;; =>("a6" "a5")
;;(getMoves (Pawn. (test-board2) "a6" false))
;; =>("b5" "a5")
;;(getMoves (Pawn. (test-board2) "a6" false))
;; =>("b5" "a5")
;;(getMoves (Pawn. (en-passant-check-test) "d5" true  [["c7" "c5"]]))
;; =>("c6" "d6")

(defn map-move-to2map [moves]
  (map (fn [move] {:move-to move}) moves))

;;---- King moves

(defn- king-moves [board x y]
  (for [a (range -1 2)
        b (range -1 2)
        :when (and
               (or (and (= (+ a b) 0)
                        (not (= a 0))
                        (not (= b 0))
                        )
                   (and (= (- a b) 0)
                        (not (= a 0))
                        (not (= b 0))
                        )
                   (and (= a 0) (not (= b 0)))
                   (and (= b 0) (not (= a 0))))
               (pos-xy-within-board? [(+ a x) (+ b y)])
               ;(not (collid-self? board true [x y]))
               (nothing-between board [(+ a x) (+ b y)] [x y])
               )] [(+ a x) (+ b y)]))

(count (king-moves (initial-board) 2 2))
;(nothing-between (initial-board) )

(defrecord King [^PersistentVector board ^String pos ^boolean white?]
  Piece
  (getMoves [this]
    (let [[x y] (pos2coord pos)
          moves (king-moves board x y)
          no-self-collision? (comp not (partial collid-self? board white?))]
      (map-move-to2map (filter no-self-collision? (filter pos-xy-within-board? moves))))))


(getMoves (King. (test-board2) "d3" true))


;; --------- queen moves

(defn- queen-moves [board x y]
  (for [a (range -7 8)
        b (range -7 8)
        :when (and
               (or (and (= (+ a b) 0)
                        (not (= a 0))
                        (not (= b 0))
                        )
                   (and (= (- a b) 0)
                        (not (= a 0))
                        (not (= b 0))
                        )
                   (and (= a 0) (not (= b 0)))
                   (and (= b 0) (not (= a 0))))
               (pos-xy-within-board? [(+ a x) (+ b y)])
               ;(not (collid-self? board true [x y]))
               (nothing-between board [(+ a x) (+ b y)] [x y])
               )] [(+ a x) (+ b y)]))

(count (queen-moves (initial-board) 2 2))
;(nothing-between (initial-board) )

(defrecord Queen [^PersistentVector board ^String pos ^boolean white?]
  Piece
  (getMoves [this]
    (let [[x y] (pos2coord pos)
          moves (queen-moves board x y)
          no-self-collision? (comp not (partial collid-self? board white?))]
      (map-move-to2map (filter no-self-collision? (filter pos-xy-within-board? moves))))))

;;(getMoves (Queen. (test-board2) "d3" true))
;;(getMoves (Queen. (could-become-in-check-test) "h5" true))
;;=> ("a3" "b3" "c4" "c3" "d7" "d6" "d5" "d4" "d2" "e4" "e3" "e2" "f5" "f3" "g6" "g3" "h7" "h3")

;;---- Rook

(defn- rook-moves [board x y]
  (for [a (range -7 8)
        b (range -7 8)
        :when (and
               (or (and (= a 0) (not (= b 0))) (and (= b 0) (not (= a 0))))
               (pos-xy-within-board? [(+ a x) (+ b y)])
               ;(not (collid-self? board true [x y]))
               (nothing-between board [(+ a x) (+ b y)] [x y])
               )] [(+ a x) (+ b y)]))

(count (rook-moves (initial-board) 2 2))
;(nothing-between (initial-board) )

(defrecord Rook [^PersistentVector board ^String pos ^boolean white?]
  Piece
  (getMoves [this]
    (let [[x y] (pos2coord pos)
          moves (rook-moves board x y)
          no-self-collision? (comp not (partial collid-self? board white?))]
      (map-move-to2map (filter no-self-collision? (filter pos-xy-within-board? moves))))))


(getMoves (Rook. (test-board2) "c2" true))

;;---- bishop


(defn- bishop-moves [board x y]
  (for [a (range -7 8)
        b (range -7 8)
        :when (and
               (or (= (+ a b) 0) (= (- a b) 0))
               (not (= a 0))
               (not (= b 0))
               (pos-xy-within-board? [(+ a x) (+ b y)])
               ;(not (collid-self? board true [x y]))
               (nothing-between board [(+ a x) (+ b y)] [x y])
               )] [(+ a x) (+ b y)]))

(count (bishop-moves (initial-board) 1 2))
;(nothing-between (initial-board) )

(defrecord Bishop [^PersistentVector board ^String pos ^boolean white?]
  Piece
  (getMoves [this]
    (let [[x y] (pos2coord pos)
          moves (bishop-moves board x y)
          no-self-collision? (comp not (partial collid-self? board white?))]
      (map-move-to2map (filter no-self-collision? (filter pos-xy-within-board? moves))))))


(getMoves (Bishop. (initial-board) "a3" true))
;;("b4" "c5" "d6" "e7")
(getMoves (Bishop. (test-board2) "c1" true))
;;=> ("a3" "b2" "d2" "e3" "f4" "g5" "h6")
(getMoves (Bishop. (test-board2) "a5" true))
;;=> ("b6" "b4" "c7")
(getMoves (Bishop. (test-board2) "e2" true))
;;("d3" "f3" "g4" "h5")


;; ---- knight stuff
(defn- knight-moves [x y]
  #{[(+ x 2) (+ y 1)]
    [(+ x 1) (+ y 2)]
    [(- x 1) (+ y 2)]
    [(- x 2) (+ y 1)]
    [(- x 2) (- y 1)]
    [(- x 1) (- y 2)]
    [(+ x 1) (- y 2)]
    [(+ x 2) (- y 1)]})

;;(knight-moves 0 0)

(defrecord Knight [^PersistentVector board ^String pos ^boolean white?]
  Piece
  (getMoves [this]
    (let [[x y] (pos2coord pos)
          kmoves (knight-moves x y)
          no-self-collision? (comp not (partial collid-self? board white?))]
      (map-move-to2map (filter no-self-collision? (filter pos-xy-within-board? kmoves))))))

;;(getMoves (Knight. (initial-board) "g1" true))
;; f3 g3

(defn- one-color [^PersistentVector board ^Boolean white?]
  (let [color? (if white? is-white? is-black?)]
    (map #(if (color? %) % \- ) board)))

(one-color (initial-board) false)

(defn convert2obj [^PersistentVector board history pos-xy]
  (let [piece (lookup-xy board pos-xy)
        color (is-white? piece)
        pos (coord2pos pos-xy)]
    (cond
     (is-knight? piece) (Knight. board pos color)
     (is-bishop? piece) (Bishop. board pos color)
     (is-queen? piece) (Queen. board pos color)
     (is-king? piece) (King. board pos color)
     (is-rook? piece) (Rook. board pos color)
     (is-pawn? piece) (Pawn. board pos color history))))

;;(convert2obj (initial-board) [] (pos2coord "h1"))

;;---- move validation

(defn possible-moves [^PersistentVector board history pos-xy]
  (getMoves (convert2obj board history pos-xy)))

(defn all-possible-moves [^PersistentVector board ^Boolean white-turn? history]
  (->> (one-color board white-turn?)
       board2xy-map-piece
       (mapcat
        (fn [[pos-xy _]]
          (map
           (fn [move] [pos-xy move])
           (possible-moves board history pos-xy))))))

;;(map (fn [[a b]] [(meta a) (meta b)]) (all-possible-moves (en-passant-check-test) true false [[[2 1] [2 3]]]))

;;(filter #(is-piece? %))
;;(map (fn [[pos c]] (possible-moves board pos)))
;(all-possible-moves (initial-board) true false [])
;(count (all-possible-moves (initial-board) true false []))

(defn king-pos [board king-white?]
  (let [king (if king-white? \K \k)]
    (->> ((->>  (group-by #(second %) (board2xy-map-piece board)) (into {})) king) ffirst)))

(king-pos (check-mate-test) false)
;; => [4 0]

(defn check? [board white-king? history]
  (let [opposite-moves-xy2map (all-possible-moves board (not white-king?) history)
        king (king-pos board white-king?)]
    (not (not (some #(= % king) (map #(:move-to (second %)) opposite-moves-xy2map))))))

;;(check? (check-mate-test) false false [])
;;(check? (initial-board) false false [])


;; -------------- rendering

;;(def ^:const board (vec (range 8)))


;;(c2dto1d [1 1])
;;(c1dto2d 63)

(defn render-board [board-state]
  (let [line "+---+---+---+---+---+---+---+---+"
        pieces-pos board-state ;(into {} board-state)
        ]
    (apply str "\n" line "\n"
           (map #(let [pos (c1dto2d (dec %))
                       c (get pieces-pos pos " ")]
                   (if (zero? (mod % 8))
                           (format "| %s |\n%s\n" c line)
                           (format "| %s " c))) (range 1 65)))))


(defn display-board [board]
  (print (render-board (char2state board))))


;;----- change state of board (make moves)

(defn apply-move [^PersistentVector board ^PersistentVector move] ;; xy or xymap
  (let [[from to] move
        en-passant (:en-passant to)
        taken (:taken to)
        b (:board to)
        real-to (if en-passant (:move-to to) to)
        piece (lookup-xy board from)
        new-board (-> (assoc board (apply index-xy from) \-)
                      (assoc (apply index-xy real-to) piece))]
    (if en-passant
      (assoc new-board (apply index-xy taken) \-)
      new-board)))

;;(display-board (apply-move (initial-board) ["b2" "b3"]))

(defn all-possible-moves-with-in-check [board white-turn? history]
  (let [possible-moves-xy2map (all-possible-moves board white-turn? history)
        f (fn [[from {to :move-to}]] (let [possible-new-board (apply-move board [from to])]
                      (not (check? possible-new-board white-turn? history))))]
    (filter f possible-moves-xy2map)))



;;(map (fn [[a b]] [(meta a) (meta b)]) (all-possible-moves-with-in-check (en-passant-check-test) true false [[[2 1] [2 3]]]))

;;(all-possible-moves-with-in-check (check-mate-test) false false [])
;;(all-possible-moves-with-in-check (in-check-test) false false [])
;; => ([[4 1] [3 2]])
;(filter (fn [[from to]] (= from "f7")) (all-possible-moves-with-in-check (could-become-in-check-test) false false))


(defn- normalize [move]
  (if (and (vector? move)
           (keyword? (first move))
           (keyword? (second move)))
    (let [[f t] move]
      [(name f) (name t)])
    move))


;; todo: catch any exception
;; todo: check that any none valid input returns nil
(defn is-move-valid? [^PersistentVector board ^Boolean white-turn? ^PersistentVector history ^PersistentVector move]
  {:pre [(display-assert (and (vector? history) (or (nil? (first history)) (string?  (ffirst history)))) history)]}
  (let [norm-move (normalize move)]
    (if (and
         (vector? norm-move)
         (string? (first norm-move))
         (= (count (first norm-move)) 2))
      (let [moves-xy2map (all-possible-moves-with-in-check board white-turn? history)]
        (not (not (some #(= (move2move-xy norm-move) (movemap2move %)) moves-xy2map))))
      (do (println "move" move "is not formatted correctly")  false))))


(defn move-en-passant [^PersistentVector board ^Boolean white-turn? ^Boolean castle? ^PersistentVector history ^PersistentVector move]
  (let [moves-xy-map (all-possible-moves-with-in-check board white-turn? history)]
    (first (filter #(let [[from {to :move-to :as tomap}] %] (and (= move [from to])
                          (:en-passant tomap)))
                   moves-xy-map))))

;;(is-move-valid? (en-passant-check-test) white false (move2move-xy-vec [["c7" "c5"]]) (move2move-xy ["d5" "c6"]))
;;(move-en-passant (en-passant-check-test) white false (move2move-xy-vec [["c7" "c5"]]) (move2move-xy ["d5" "c6"]))

;;(all-possible-moves-with-in-check (en-passant-check-test) white false (move2move-xy-vec [["c7" "c5"]]) )
;;(is-move-valid? (en-passant-check-test) true false [] ["d5" "c4"])
;; => true


(defn check-mate? [^PersistentVector board ^Boolean white-turn? ^PersistentVector history]
  (->> (all-possible-moves-with-in-check board white-turn? history) count zero?))
;(check-mate? (check-mate-test) false false [])
;(check-mate? (in-check-test) false false [])


(defn forfeit [white-turn?]
  (if white-turn? [0 1] [1 0]))
(def opposite-color-wins forfeit)

(defn execute [f game-context]
  (try (f game-context )
       (catch Throwable t (do (println "caught exception inside chess player function" t) {:move :caught-exception :exception t}))))

(defn parse-f-return [ret]
  (if  (map? ret)
    ret
    {:move ret}))

;;(display-board (apply-move-safe (initial-board) true false ["a2" "b3"]))
(defn play-game-rec [{board :board f1 :f1 f2 :f2 white-turn? :white-turn? move-history :move-history state-f1 :state-f1 state-f2 :state-f2 iteration :iteration}]
  (if (check-mate? board white-turn? move-history)
      (do
        (println "check-mate!")
        {:score (opposite-color-wins white-turn?) :history move-history :board board :result :check-mate})
      (let [new-iteration (if (nil? iteration) 1 (inc iteration))
            in-check? (check? board (not white-turn?) move-history)
            valid-moves (move-xymap2move-vec (all-possible-moves-with-in-check board white-turn? move-history))
            f-return (if white-turn?
                       (execute f1 {:board board :white-turn white-turn? :valid-moves valid-moves :in-check? in-check? :history move-history :state state-f1})
                       (execute f2 {:board board :white-turn white-turn? :valid-moves valid-moves :in-check? in-check? :history move-history :state state-f2}))
            {move :move new-state :state exception :exception} (parse-f-return f-return)]
        (cond (> new-iteration 500)         {:score [1/2 1/2] :history (conj move-history new-iteration) :board board :result :draw-by-number-of-iteration}
              (= move :caught-exception) {:score (forfeit white-turn?) :history (conj move-history exception ) :board board :result :caught-exception}
              :else (let [valid? (is-move-valid? board white-turn? move-history move)
                norm-move (normalize move)
                new-history (conj move-history norm-move)]
            (if (not valid?)
              {:score (forfeit white-turn?) :history new-history :board board :result :invalid-move}
             (let [
                   move-xy (move2move-xy  norm-move)
                   en-passant-move-xymap (move-en-passant board white-turn? false move-history move-xy)
                   real-move (if en-passant-move-xymap en-passant-move-xymap move-xy)]
               (if white-turn?
                 (recur {:board (apply-move board real-move) :f1 f1 :f2 f2 :white-turn? (not white-turn?) :move-history new-history :state-f1 new-state :state-f2 state-f2 :iteration new-iteration})
                 (recur {:board (apply-move board real-move) :f1 f1 :f2 f2 :white-turn? (not white-turn?) :move-history new-history :state-f1 state-f1 :state-f2 new-state :iteration new-iteration})))
             ))))))

(defn play-game [{ board :board f1 :f1 f2 :f2}]
  (play-game-rec {:board board :f1 f1 :f2 f2 :white-turn? true :move-history []})
  )
;; => [score move-history last-board invalid-move? check-mate?]
;;example => [[1 0] [["e2" "e4"] ["e7" "e5"]] [\- \- \- \k \- ....]]



(defn- every-nth [coll n]
  (map (fn [[i e]] e) (filter (fn [[i e]] (zero? (mod i n))) (map-indexed (fn [i e] [i e]) coll))))

(defn- create-fn [moves]
  (fn [{b :board c :white-turn ic :in-check? h :history s :state}]
      (let [move-seq (if (nil? s)
                       moves
                       s)]
        {:move (first move-seq) :state (next move-seq)})))

(defn create-fns-from-scenario [moves]
  (let [white-moves (every-nth moves 2)
        black-moves (every-nth (drop 1 moves) 2)]
    [(create-fn white-moves)
     (create-fn black-moves)]))



(defn play-scenario [scenario] (let [[f1 f2] (create-fns-from-scenario scenario)]
                                 (let [result (play-game {:board (initial-board) :f1 f1 :f2 f2})]
                                   result)))


;;(play-scenario  (move2move-xy-vec [["e2" "e4"] ["e7" "e5"] ["d1" "h5"] ["d7" "d6"] ["f1" "c4"] ["b8" "c6"] ["h5" "f7"] ["e8" "e7"]]))
;; => check-mate
;;(play-scenario  [["e2" "e4"] ["e7" "e5"] ["d1" "h5"] ["d7" "d6"] ["f1" "c4"] ["b8" "c6"] ["h5" "g6"] ["e8" "e7"]])
;; => invalid move

(defn interactive-f [{board :board am-i-white? :white-turn valid-moves :valid-moves ic :in-check? h :history s :state}]
  (do
    (display-board board)
    (println (if am-i-white? "white: " "black: "))
    (println "valid moves:" valid-moves)
    (let [move (read-string (read-line))]

     move)))



;; (play-game (initial-board) interactive-f interactive-f)

;; first move is special

;; what should be passed:
;; board state

;; pass a map with all the data
;; {:board :last-move :color :white :avaliable-moves {} }

;; [[:a1 :a3] state]
;; set of avaliable moves

;; returns move + state

;; move is source location, destination


(defn f1 [{board :board am-i-white? :white-turn ic :in-check? h :history s :state}]
                            (let [move-seq (if (nil? s)
                                             (list ["e2" "e4"] ["d1" "h5"] ["f1" "c4"] ["h5" "f7"])
                                             s)]
                              [(first move-seq) (next move-seq)]))

(defn f2 [{board :board am-i-white? :white-turn ic :in-check? h :history option-state :state}]
  (let [b board
        move-seq (if (nil? option-state)
           (list ["e7" "e5"] ["d7" "d6"] ["b8" "c6"] ["e8" "e7"])
           option-state)]
    [(first move-seq) (next move-seq)]))

(defn random-f [{board :board am-i-white? :white-turn valid-moves :valid-moves ic :in-check? h :history s :state}]
  (let [v (into [] valid-moves)
        iteration (if (nil? s) (+ 1 (if am-i-white? 0 1)) (+ 2 s))]
    (display-board board)
    (println (if am-i-white? "white: " "black: "))
    (println "valid moves:" valid-moves)
    (println "iteration:" iteration)
    (let [move (rand-int (count valid-moves))]
      (println "choosen move:" (get v move))
      {:move (get v move) :state iteration})) )


;; (play-game (initial-board) interactive-f f2)
;; (play-game (initial-board) interactive-f random-f)
;;(rand-int 42)


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


(def database (atom {
                     :contenders [{:login "Philip" :functions [{:id "daredevil" :function random-f :past-battles [{:against "superman" :scores [1 0] :color :white}]}]}
                                  {:login "Nicholas" :functions [{:id "superman" :function random-f :past-battles []}]}
                                  {:login "Steve" :functions [{:id "Wonderboy" :function random-f }]}
                                  {:login "Bob" :functions [{:id "Wonderboy2" :function random-f }]}]}))

(defn all-available-functions [db]
  (for [{login :login fns :functions} (:contenders db)
        {id :id fn :function battles :past-battles} fns
        ]
    {:login login :id id :fn fn :past-battles battles }
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

(defn select-contender-by-fn [db f]
  (for [{login :login fns :functions} (:contenders db)
        {id :id fn :function battles :past-battles} fns
        :when (= f fn)
        ]
    {:login login :id id :fn fn :past-battles battles }
    ))



(count (select2functions @database))

(defn save [result]
  (let [{s :score res :result bat :past-battles}  result
        battles (if (nil? bat) [] bat)
        ]
    (swap! database (fn [db]
                     (let [a 1] {
                       :contenders [{:login "Philip" :functions [{:id "daredevil" :function random-f :past-battles (conj battles {:score s :result res})}]}
                                    {:login "Nicholas" :functions [{:id "superman" :function random-f :past-battles []}]}
                                    {:login "Steve" :functions [{:id "Wonderboy" :function random-f }]}
                                    {:login "Bob" :functions [{:id "Wonderboy2" :function random-f }]}]})))
    (write-file   @database "./score.txt")))

(defn tournement []
  (let [[{name1 :login f1 :fn id1 :id} {name2 :name f2 :fn id2 :id}] (select2functions @database)
        result (play-game {:board (initial-board) :id1 id1 :f1 f1 :id2 id2 :f2 f2})]
    (save result)
   (println result)
   (recur)))


(defn -main []
 (tournement))

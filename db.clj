{:matches [{:score [1 0], :id1 "superman", :id2 "daredevil"}
	 {:id2 "superman", :id1 "daredevil", :score [1/2 1/2], :result :draw-by-number-of-iteration}
	 {:id2 "superman", :id1 "daredevil", :score [1/2 1/2], :result :draw-by-number-of-iteration}
	 {:id2 "superman", :id1 "daredevil", :score [1/2 1/2], :result :draw-by-number-of-iteration}
	 {:id2 "superman", :id1 "daredevil", :score [1/2 1/2], :result :draw-by-number-of-iteration}
	 {:id2 "superman", :id1 "daredevil", :score [1/2 1/2], :result :draw-by-number-of-iteration}
	 {:id2 "superman", :id1 "daredevil", :score [1/2 1/2], :result :draw-by-number-of-iteration}
	 {:id2 "superman", :id1 "daredevil", :score [1/2 1/2], :result :draw-by-number-of-iteration}
	 {:id2 "superman", :id1 "daredevil", :score [1/2 1/2], :result :draw-by-number-of-iteration}
	 {:id2 "superman", :id1 "daredevil", :score [1/2 1/2], :result :draw-by-number-of-iteration}
	 {:id2 "superman", :id1 "daredevil", :score [0 1], :result :check-mate}
	 {:id2 "superman", :id1 "daredevil", :score [1/2 1/2], :result :draw-by-number-of-iteration}
	 {:id2 "superman", :id1 "daredevil", :score [1/2 1/2], :result :draw-by-number-of-iteration}
	 {:id2 "superman", :id1 "daredevil", :score [1/2 1/2], :result :draw-by-number-of-iteration}
	 {:id2 "superman", :id1 "daredevil", :score [1 0], :result :check-mate}
	 {:id2 "superman", :id1 "daredevil", :score [1/2 1/2], :result :draw-by-number-of-iteration}
	 {:id2 "superman", :id1 "daredevil", :score [1/2 1/2], :result :draw-by-number-of-iteration}
	 {:id2 "superman", :id1 "daredevil", :score [1/2 1/2], :result :draw-by-number-of-iteration}
	 {:id2 "superman", :id1 "daredevil", :score [1/2 1/2], :result :draw-by-number-of-iteration}
	 {:id2 "superman", :id1 "daredevil", :score [1/2 1/2], :result :draw-by-number-of-iteration}
	 {:id2 "superman", :id1 "daredevil", :score [1/2 1/2], :result :draw-by-number-of-iteration}
	 {:id2 "superman", :id1 "daredevil", :score [1/2 1/2], :result :draw-by-number-of-iteration}
	 {:id2 "superman", :id1 "daredevil", :score [1/2 1/2], :result :draw-by-number-of-iteration}
	 {:id2 "superman", :id1 "daredevil", :score [1/2 1/2], :result :draw-by-number-of-iteration}
	 {:id2 "superman", :id1 "daredevil", :score [1/2 1/2], :result :draw-by-number-of-iteration}
	 {:id2 "superman", :id1 "daredevil", :score [1/2 1/2], :result :draw-by-number-of-iteration}
	 {:id2 "superman", :id1 "daredevil", :score [1 0], :result :check-mate}
	 {:id2 "superman", :id1 "daredevil", :score [0 1], :result :check-mate}
	], :contenders [{:login "Philip", :functions [{:id "daredevil", :function-src (fn random-f [{board :board, am-i-white? :white-turn, valid-moves :valid-moves, ic :in-check?, h :history, s :state}
	] (let [v (into [] valid-moves) iteration (if (nil? s) (+ 1 (if am-i-white? 0 1)) (+ 2 s))] (println (if am-i-white? "white: " "black: ")) (println "valid moves:" valid-moves) (println "iteration:" iteration) (let [move (rand-int (count valid-moves))] (println "choosen move:" (get v move)) {:move (get v move), :state iteration}
	)))}
	]}
	 {:login "Nicholas", :functions [{:id "superman", :function-src (fn random-f [{board :board, am-i-white? :white-turn, valid-moves :valid-moves, ic :in-check?, h :history, s :state}
	] (let [v (into [] valid-moves) iteration (if (nil? s) (+ 1 (if am-i-white? 0 1)) (+ 2 s))] (println (if am-i-white? "white: " "black: ")) (println "valid moves:" valid-moves) (println "iteration:" iteration) (let [move (rand-int (count valid-moves))] (println "choosen move:" (get v move)) {:move (get v move), :state iteration}
	)))}
	]}
	 {:login "Steve", :functions [{:id "Wonderboy", :function-src (fn random-f [{board :board, am-i-white? :white-turn, valid-moves :valid-moves, ic :in-check?, h :history, s :state}
	] (let [v (into [] valid-moves) iteration (if (nil? s) (+ 1 (if am-i-white? 0 1)) (+ 2 s))] (println (if am-i-white? "white: " "black: ")) (println "valid moves:" valid-moves) (println "iteration:" iteration) (let [move (rand-int (count valid-moves))] (println "choosen move:" (get v move)) {:move (get v move), :state iteration}
	)))}
	]}
	 {:login "Bob", :functions [{:id "Wonderboy2", :function-src (fn random-f [{board :board, am-i-white? :white-turn, valid-moves :valid-moves, ic :in-check?, h :history, s :state}
	] (let [v (into [] valid-moves) iteration (if (nil? s) (+ 1 (if am-i-white? 0 1)) (+ 2 s))] (println (if am-i-white? "white: " "black: ")) (println "valid moves:" valid-moves) (println "iteration:" iteration) (let [move (rand-int (count valid-moves))] (println "choosen move:" (get v move)) {:move (get v move), :state iteration}
	)))}
	]}
	]}
	
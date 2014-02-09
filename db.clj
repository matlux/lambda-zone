{:matches [{:score [1 0], :id1 "superman", :id2 "daredevil"}
	 {:id2 "Wonderboy", :id1 "daredevil", :score [1/2 1/2], :result :draw-by-number-of-iteration}
	 {:id2 "Wonderboy", :id1 "superman", :score [1/2 1/2], :result :draw-by-number-of-iteration}
	 {:id2 "superman", :id1 "Wonderboy", :score [1/2 1/2], :result :draw-by-number-of-iteration}
	 {:id2 "superman", :id1 "daredevil", :score [1/2 1/2], :result :draw-by-number-of-iteration}
	 {:id2 "daredevil", :id1 "Wonderboy", :score [1/2 1/2], :result :draw-by-number-of-iteration}
	 {:id2 "b", :id1 "Wonderboy", :time "\"Elapsed time: 47441.767 msecs\"\n", :score [1/2 1/2], :result :draw-by-number-of-iteration}
	 {:id2 "Wonderboy", :id1 "b", :time "\"Elapsed time: 37096.372 msecs\"\n", :score [1/2 1/2], :result :draw-by-number-of-iteration}
	 {:id2 "b", :id1 "daredevil", :time "\"Elapsed time: 43779.729 msecs\"\n", :score [1/2 1/2], :result :draw-by-number-of-iteration}
	 {:id2 "b", :id1 "superman", :time "\"Elapsed time: 36498.461 msecs\"\n", :score [1/2 1/2], :result :draw-by-number-of-iteration}
	 {:id2 "superman", :id1 "b", :time "check-mate!\n\"Elapsed time: 11457.696 msecs\"\n", :score [0 1], :result :check-mate}
	 {:id2 "daredevil", :id1 "b", :time "\"Elapsed time: 39769.805 msecs\"\n", :score [1/2 1/2], :result :draw-by-number-of-iteration}
	], :contenders [{:login "Philip", :id "daredevil", :fn "(fn random-f [{board :board am-i-white? :white-turn valid-moves :valid-moves ic :in-check? h :history s :state}
	\n\t\n\t\n\t\n\t\n\t\n\t\n\t\n\t\n\t\n\t\n\t\n\t\n\t\n\t\n\t]\n  (let [v (into [] valid-moves)\n        iteration (if (nil? s) (+ 1 (if am-i-white? 0 1)) (+ 2 s))]\n\n        (let [move (rand-int (count valid-moves))]\n      {:move (get v move) :state iteration}
	\n\t\n\t\n\t\n\t\n\t\n\t\n\t\n\t\n\t\n\t\n\t\n\t\n\t\n\t\n\t)) )"}
	 {:login "Nicholas", :id "superman", :fn "(fn random-f [{board :board am-i-white? :white-turn valid-moves :valid-moves ic :in-check? h :history s :state}
	\n\t\n\t\n\t\n\t\n\t\n\t\n\t\n\t\n\t\n\t\n\t\n\t\n\t\n\t\n\t]\n  (let [v (into [] valid-moves)\n        iteration (if (nil? s) (+ 1 (if am-i-white? 0 1)) (+ 2 s))]\n\n        (let [move (rand-int (count valid-moves))]\n      {:move (get v move) :state iteration}
	\n\t\n\t\n\t\n\t\n\t\n\t\n\t\n\t\n\t\n\t\n\t\n\t\n\t\n\t\n\t)) )"}
	 {:login "Steve", :id "Wonderboy", :fn "(fn random-f [{board :board am-i-white? :white-turn valid-moves :valid-moves ic :in-check? h :history s :state}
	\n\t\n\t\n\t\n\t\n\t\n\t\n\t\n\t\n\t\n\t\n\t\n\t\n\t\n\t\n\t]\n  (let [v (into [] valid-moves)\n        iteration (if (nil? s) (+ 1 (if am-i-white? 0 1)) (+ 2 s))]\n\n        (let [move (rand-int (count valid-moves))]\n      {:move (get v move) :state iteration}
	\n\t\n\t\n\t\n\t\n\t\n\t\n\t\n\t\n\t\n\t\n\t\n\t\n\t\n\t\n\t)) )"}
	 {:login "a", :id "b", :fn "(fn random-f [{board :board, am-i-white? :white-turn, valid-moves :valid-moves, ic :in-check?, h :history, s :state}
	]\n                      (let [v (into [] valid-moves)\n                            iteration (if (nil? s) (+ 1 (if am-i-white? 0 1)) (+ 2 s))]\n                        (let [move (rand-int (count valid-moves))]\n                          {:move (get v move), :state iteration}
	)))"}
	]}
	
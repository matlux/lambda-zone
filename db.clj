{:matches [{:score [1 0], :id1 "superman", :id2 "daredevil"}
	 {:id2 "superman", :id1 "daredevil", :score [1/2 1/2], :result :draw-by-number-of-iteration}
	 {:id2 "foobar", :id1 "daredevil", :time "\"Elapsed time: 27328.09 msecs\"\n", :score [1/2 1/2], :result :draw-by-number-of-iteration}
	 {:id2 "daredevil", :id1 "foobar", :time "\"Elapsed time: 33065.154 msecs\"\n", :score [1/2 1/2], :result :draw-by-number-of-iteration}
	 {:id2 "foobar", :id1 "superman", :time "\"Elapsed time: 41409.302 msecs\"\n", :score [1/2 1/2], :result :draw-by-number-of-iteration}
	 {:id2 "superman", :id1 "foobar", :time "\"Elapsed time: 41222.163 msecs\"\n", :score [1/2 1/2], :result :draw-by-number-of-iteration}
	], :contenders [{:login "Philip", :id "daredevil", :fn "(fn random-f [{board :board am-i-white? :white-turn valid-moves :valid-moves ic :in-check? h :history s :state}
	\n\t\n\t\n\t]\n  (let [v (into [] valid-moves)\n        iteration (if (nil? s) (+ 1 (if am-i-white? 0 1)) (+ 2 s))]\n\n        (let [move (rand-int (count valid-moves))]\n      {:move (get v move) :state iteration}
	\n\t\n\t\n\t)) )"}
	 {:login "Nicholas", :id "superman", :fn "(fn random-f [{board :board am-i-white? :white-turn valid-moves :valid-moves ic :in-check? h :history s :state}
	\n\t\n\t\n\t]\n  (let [v (into [] valid-moves)\n        iteration (if (nil? s) (+ 1 (if am-i-white? 0 1)) (+ 2 s))]\n\n        (let [move (rand-int (count valid-moves))]\n      {:move (get v move) :state iteration}
	\n\t\n\t\n\t)) )"}
	 {:login "https://www.google.com/accounts/o8/id?id=AItOawl4hK6AcG3du1Fu9MeeF-sgFVSxrmpWVOU", :id "foobar", :fn "(fn random-f [{board :board, am-i-white? :white-turn, valid-moves :valid-moves, ic :in-check?, h :history, s :state}
	]\n                      (let [v (into [] valid-moves)\n                            iteration (if (nil? s) (+ 1 (if am-i-white? 0 1)) (+ 2 s))]\n                        (let [move (rand-int (count valid-moves))]\n                          {:move (get v move), :state iteration}
	)))"}
	]}
	
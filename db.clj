{:matches ({:id2 "d", :id1 "daredevil", :time "\"Elapsed time: 86669.248 msecs\"\n", :score [1/2 1/2], :result :draw-by-number-of-iteration}
	), :contenders ({:login "mathieu.gauthron@gmail.com", :id "d", :fn "(fn random-f [{board :board, am-i-white? :white-turn, valid-moves :valid-moves, ic :in-check?, h :history, s :state}
	]\n                      (let [v (into [] valid-moves)\n                            iteration (if (nil? s) (+ 1 (if am-i-white? 0 1)) (+ 2 s))]\n                        (let [move (rand-int (count valid-moves))]\n                          {:move (get v move), :state iteration}
	)))"}
	 {:login "mathieu.gauthron@gmail.com", :id "daredevil", :fn "(fn random-f [{board :board, am-i-white? :white-turn, valid-moves :valid-moves, ic :in-check?, h :history, s :state}
	\n\t\n\t]\n                      (let [v (into [] valid-moves)\n                            iteration (if (nil? s) (+ 1 (if am-i-white? 0 1)) (+ 2 s))]\n                        (let [move (rand-int (count valid-moves))]\n                          {:move (get v move), :state iteration}
	\n\t\n\t)))"}
	)}
	
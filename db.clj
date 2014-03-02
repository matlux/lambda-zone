{:matches (
	), :contenders ({:login "mathieu.gauthron@gmail.com", :id "daredevil", :fn "(fn random-f [{board :board, am-i-white? :white-turn, valid-moves :valid-moves, ic :in-check?, h :history, s :state}
	]\n                      (let [v (into [] valid-moves)\n                            iteration (if (nil? s) (+ 1 (if am-i-white? 0 1)) (+ 2 s))]\n                        (let [move (rand-int (count valid-moves))]\n                          {:move (get v move), :state iteration}
	)))"}

	)}
	
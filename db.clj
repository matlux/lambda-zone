{:matches [{:score [1 0], :id1 "superman", :id2 "daredevil"}
	 {:id2 "Wonderboy", :id1 "daredevil", :score [1/2 1/2], :result :draw-by-number-of-iteration}
	 {:id2 "Wonderboy", :id1 "daredevil", :score [0 1], :result :check-mate}
	 {:id2 "Wonderboy", :id1 "daredevil", :score [1/2 1/2], :result :draw-by-number-of-iteration}
	], :contenders [{:login "Philip", :id "daredevil", :fn "(fn random-f [{board :board am-i-white? :white-turn valid-moves :valid-moves ic :in-check? h :history s :state}
	]\n  (let [v (into [] valid-moves)\n        iteration (if (nil? s) (+ 1 (if am-i-white? 0 1)) (+ 2 s))]\n\n    (println (if am-i-white? \"white: \" \"black: \"))\n    (println \"valid moves:\" valid-moves)\n    (println \"iteration:\" iteration)\n    (let [move (rand-int (count valid-moves))]\n      (println \"choosen move:\" (get v move))\n      {:move (get v move) :state iteration}
	)) )"}
	 {:login "Nicholas", :id "superman", :fn "(fn random-f [{board :board am-i-white? :white-turn valid-moves :valid-moves ic :in-check? h :history s :state}
	]\n  (let [v (into [] valid-moves)\n        iteration (if (nil? s) (+ 1 (if am-i-white? 0 1)) (+ 2 s))]\n\n    (println (if am-i-white? \"white: \" \"black: \"))\n    (println \"valid moves:\" valid-moves)\n    (println \"iteration:\" iteration)\n    (let [move (rand-int (count valid-moves))]\n      (println \"choosen move:\" (get v move))\n      {:move (get v move) :state iteration}
	)) )"}
	 {:login "Steve", :id "Wonderboy", :fn "(fn random-f [{board :board am-i-white? :white-turn valid-moves :valid-moves ic :in-check? h :history s :state}
	]\n  (let [v (into [] valid-moves)\n        iteration (if (nil? s) (+ 1 (if am-i-white? 0 1)) (+ 2 s))]\n\n    (println (if am-i-white? \"white: \" \"black: \"))\n    (println \"valid moves:\" valid-moves)\n    (println \"iteration:\" iteration)\n    (let [move (rand-int (count valid-moves))]\n      (println \"choosen move:\" (get v move))\n      {:move (get v move) :state iteration}
	)) )"}
	 {:login "Bob", :id "Wonderboy2", :fn "(fn random-f [{board :board am-i-white? :white-turn valid-moves :valid-moves ic :in-check? h :history s :state}
	]\n  (let [v (into [] valid-moves)\n        iteration (if (nil? s) (+ 1 (if am-i-white? 0 1)) (+ 2 s))]\n\n    (println (if am-i-white? \"white: \" \"black: \"))\n    (println \"valid moves:\" valid-moves)\n    (println \"iteration:\" iteration)\n    (let [move (rand-int (count valid-moves))]\n      (println \"choosen move:\" (get v move))\n      {:move (get v move) :state iteration}
	)) )"}
	]}
	
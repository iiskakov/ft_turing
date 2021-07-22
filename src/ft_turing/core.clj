(ns ft-turing.core
  (:gen-class)
  (:require [clojure.data.json :as json])
  )

(defn -main
  [& args]
  (declare transition)
  (defn load-description []
    (def parsed (json/read-str (slurp "/Users/iskanderiskakov/code/42/ft_turing/resources/test.json")
                  :key-fn keyword))
    )

  (load-description)
  (get parsed :blank)

  (def sm {:subone [{:read '1 :write '0 :action 'action1 :to_state 'subtwo}
                    {:read '- :write '2 :action 'action2 :to_state 'subtwo}]
           :subtwo [{:read '0 :write '1 :action 'action3 :to_state 'subone}
                    {:read '- :write '2 :action 'action4 :to_state 'subtwo}]})


  (defn compute [input symbol]
    (some (fn [{:keys [read action to_state write]}]
        (when (= read symbol)
          (println write)
          (transition to_state)))
                            input))

  (defn transition [state]
   (compute (get sm (keyword state)) '1))

  ;; TODO: implement tape and movements (left, right + write)
  ;;
  ;;
  ;;
  ;;can probably start with (transition initial)
  )


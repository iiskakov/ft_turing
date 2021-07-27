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

  (def idx (atom 0))
  (def tape (atom ["1" "0"]))

  (load-description)
  (get parsed :transitions)
  (reset! tape [(get parsed :blank)])

  (def sm {:subone [{:read "1" :write "0" :action "RIGHT" :to_state "subtwo"}
                    {:read "-" :write "2" :action "RIGHT" :to_state "subtwo"}]
           :subtwo [{:read "0" :write "1" :action "RIGHT" :to_state "subone"}
                    {:read "-" :write "2" :action "RIGHT" :to_state "subtwo"}]})



  ;; Write/Read Functions
  (defn insert-start [target addition]
    (apply conj (if (vector? addition) addition [addition]) target))

  (defn insert-end [target addition]
    (conj target addition))

  (defn write-left [symbol]
    (cond
      (= @idx 0) (swap! tape insert-start symbol)
      :otherwise
        (when true
          (swap! idx dec)
          (swap! tape assoc @idx symbol)
          )))

  (defn write-right [symbol]
    (cond
      (= @idx (count @tape))
        (when true
          (swap! tape insert-end symbol)
          (swap! idx inc))
      :otherwise
        (when true
          (swap! idx inc)
          (swap! tape assoc @idx symbol)
          )))

  (defn write-tape [direction symbol]
    (cond
      (= direction "LEFT") (write-left symbol)
      (= direction "RIGHT") (write-right symbol))
    (println @tape)
    (flush))
;; rewrite with if else instead of cond

  (defn read-tape []
    (nth @tape @idx))

  (reset! idx 0)
  ;;can probably start with (transition initial)

  (defn compute [input]
    (some (fn [{:keys [read action to_state write]}]
        (when (= read (read-tape))
          (write-tape action write)
          (transition to_state)))
                            input))

  (defn transition [state]
   (compute (get sm (keyword state))))

  )


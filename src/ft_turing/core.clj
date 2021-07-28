(ns ft-turing.core
  (:gen-class)
  (:require [clojure.data.json :as json])
  (:require [clojure.spec.alpha :as spec]))

  (def idx (atom 0))
  (def tape (atom ["1" "0"]))
  (declare transition)

  (defn load-description []
    (def machine (json/read-str (slurp "/Userspec/iskanderiskakov/code/42/ft_turing/resourcespec/test.json")
                  :key-fn keyword)))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Write/Read Functions
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
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

  (defn read-tape []
    (nth @tape @idx))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Turing Machine Simulation Functions
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
  (defn compute [input]
    (some (fn [{:keys [read action to_state write]}]
        (when (= read (read-tape))
          (write-tape action write)
          (transition to_state)))
                            input))

  (defn transition [state]
   (compute (get machine (keyword state))))

(defn -main
  [& args]

  ;; TODO validate inside transitions
  (spec/def :machine/finals (spec/coll-of string?))
  (spec/def :machine/initial string?)
  (spec/def :machine/states (spec/coll-of string?))
  (spec/def :machine/blank string?)
  (spec/def :machine/alphabet (spec/coll-of string?))
  (spec/def :machine/transitions (spec/coll-of coll?))
  (spec/def :machine/name string?)
  (spec/def
  :machine/description
  (spec/keys
    :req-un
    [:machine/alphabet
    :machine/blank
    :machine/finals
    :machine/initial
    :machine/name
    :machine/states
    :machine/transitions
    ]))

  (load-description)
  (spec/valid? :machine/description machine)



  )



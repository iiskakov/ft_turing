(ns ft-turing.core
  (:gen-class)
  (:require [clojure.data.json :as json])
  (:require [clojure.tools.cli :as cli])
  (:require [clojure.string :as str])
  (:require [clojure.spec.alpha :as spec]))

  (def idx (atom 0))
  (def tape (atom []))
  (declare transition)

  (defn load-description [path]
    (def machine (json/read-str (slurp path)
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


(def VERSION "0.0.1")

  (def cli-opts
    [["-v" "--verbose" "Increase verbosity" :default 0 :update-fn inc]
     [nil  "--version" "Print version and exit"]
     ["-h" "--help" "Print this help information"]])

  (defn main [args {:keys [] :as opts}]
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

    (println args)
    (println opts)
    (reset! tape (str/split (nth args 1) #""))
    (println @tape)

    (load-description (nth args 0))
    (spec/valid? :machine/description machine)
    (println machine)
    )

(defn -main [& args]
  (let [{:keys [errors options arguments summary]} (cli/parse-opts args cli-opts)]
      (cond
        (seq errors)
        (do
          (run! println errors)
          (println summary)
          (System/exit -1)) ;; non-zero exit code if something goes wrong

        (:help options)
        (do
          (println summary)
          (System/exit 0))

        (:version options)
        (do
          (println VERSION)
          (System/exit 0))

        :else
        (main arguments options)))
  ;; TODO validate inside transitions



  )



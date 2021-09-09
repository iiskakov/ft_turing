(ns ft-turing.core
  (:gen-class)
  (:require [clojure.data.json :as json])
  (:require [clojure.tools.cli :as cli])
  (:require [clojure.string :as str])
  (:require [clojure.set :as set])
  (:require [clojure.pprint :as pp])
  (:require [clojure.spec.alpha :as spec]))

(def idx (atom 0))
(def tape (atom []))
(declare transition)

(defn load-description [path]
  (try
    (json/read-str (slurp path) :key-fn keyword)
    (catch Exception e
      (do
        (println "Error: Bad file")
        (println (.getMessage e))
        (System/exit -1)))))

(defn print-state [to-state write action opts]
  (when (= (get opts :verbose) 1)
          (println "(" to-state write action ")")
          (print  @tape " ")
          (print "(" to-state " ")
          (print (nth @tape @idx) ")")
          (print " -> ")))

(defn print-initial [machine opts]
  (when (= (get opts :verbose) 1)
    (println "**************************************************************")
    (println (get machine :name))
    (println "**************************************************************")
    (pp/pprint machine)
    (println "**************************************************************")))

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
      (swap! tape assoc @idx symbol)
      (swap! idx dec))))

(defn write-right [symbol]
  (cond
    (= @idx (count @tape))
    (when true
      (swap! tape insert-end symbol)
      (swap! idx inc))
    :otherwise
    (when true
      (swap! tape assoc @idx symbol)
      (swap! idx inc))))

(defn write-tape [direction symbol]
  (cond
    (= direction "LEFT") (write-left symbol)
    (= direction "RIGHT") (write-right symbol))
    ;; (println symbol)
  )

(defn read-tape []
  ;; (when (not= "ANY" (nth @tape @idx))
  ;; (do
  ;;   (println "Tape is" @tape)
  ;;   (println "symbol is" (nth @tape @idx))))
  (str (nth @tape @idx)))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Turing Machine Simulation Functions
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defn turing-machine [initial-state states opts finals]
  (letfn
   [(compute [transitions]
      (or
      (some (fn [{:keys [read action to_state write]}]
              (when (= (str read) (read-tape))
                (println "just read" (str read) "equals to"  (read-tape))
                (println "switching to state" to_state)
                (print-state to_state write action opts)
                (write-tape action write)
                #(transition to_state)))
            transitions)
      (some (fn [{:keys [read action to_state write]}]
              (when (= read "ANY")
                (print-state to_state write action opts)
                (if (= write "ANY")
                  (write-tape action (read-tape))
                  (write-tape action write)
                  )
                (write-tape action (read-tape))
                #(transition to_state)))
            transitions)
      )
      )
    (transition [to-state]
      (if (contains? (set finals) to-state)
        (println "DONE")
        #(compute (get states (keyword to-state)))))]

    (trampoline transition initial-state)))






(def VERSION "0.0.1")

(def cli-opts
  [["-v" "--verbose" "Increase verbosity" :default 0 :update-fn inc]
   ["-h" "--help" "Print this help information"]])

(defn main [args {:keys [] :as opts}]
  (spec/def :tape/form (spec/coll-of string?))
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
      :machine/transitions]))

  (when (not (= (count args) 2))
    (do
      (println "Wrong number of arguments")
      (println "-h, --help Print help information")
      (System/exit -1)))
  (def machine (load-description (nth args 0)))
  (when (not (spec/valid? :machine/description machine))
    (do
      (println "Machine description is invalid")
      (System/exit -1)))

  (reset! tape (str/split (nth args 1) #""))
  (when (not
         (or
          (set/subset? (set @tape) (set (get machine :alphabet)))
          (= (set @tape) (set (get machine :alphabet)))))
    (do
      (println "Tape is invalid")
      (System/exit -1)))

  (print-initial machine opts)

  (turing-machine
   (get machine :initial)
   (get machine :transitions)
   opts
   (get machine :finals))
  (println @tape)

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
      :else
      (main arguments options)))
  ;; TODO validate inside transitions

  )

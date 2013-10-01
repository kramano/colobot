(ns colobot.core
  (:gen-class)
  (:require [clj-http.client :as client]
            [alter-ego.core :refer :all]))

(defn create-colobot []
  ({}))

(def godville-api-url "http://godville.net/gods/api/")

(defn make-url [god-name]
  (str godville-api-url god-name ".json"))

(defn get-hero-state [god-name]
  (let [resp (client/get
              (make-url god-name)
              {:as :json})
        hero (:body resp)]
    hero))

(defn dead? [hero]
  (= 0 (:health hero)))

(defn maybe-enough-gold? [hero]
  (let [gold-string (:gold_approx hero)
        gld-v (re-find #"(\d+) сотен|(\d+) тысяч" gold-string)
        hundreds (nth gld-v 1 -1)
        thousands (nth gld-v 2 -1)]
    (if hundreds
      (<= 30 (Integer. hundreds))
      (<= 3 (Integer. thousands)))))

(defn low-godpower? [hero]
  (< (:godpower hero) 25))

(defn in-town? [hero]
  (not (clojure.string/blank? (:town_name hero))))

(defn fighting-with-boss? [hero]
  (:arena_fight hero))

(defn ressurect [hero]
  (sequence "Ressurect"
            (action "Is dead?"
                    (dead? hero))
            (action "Ressurect hero"
                    true)))

(defn charge-prana [hero]
  (selector "Charge prana"
            (inverter
             (action "Low prana?"
                     (low-godpower? hero)))
            (action "Charge"
                    true)))

(defn make-brick [hero]
  (sequence "Make brick"
            (action "Maybe enough gold?"
                    (maybe-enough-gold? hero))
            (action "Enough gold?"
                    true)
            (until-success
             (sequence
              (charge-prana hero)
              (action "Make bad"
                      true)
              (action "Done?"
                      true)))))

(defn fight-with-boss [hero]
  (sequence "Fight with boss"
            (action "Is fighting with boss?"
                    (fighting-with-boss? hero))
            (until-success
             (sequence "Fight"
                       (charge-prana hero)
                       (action "Something here" true)
                       (action "Boss dead?" true)))))



(defn behave [hero]
  (selector "Hero"
            (ressurect hero)
            (fight-with-boss hero)
            (make-brick hero)))

(defn -main
  "I don't do a whole lot ... yet."
  [& args]
  ;; work around dangerous default behaviour in Clojure
  (alter-var-root #'*read-eval* (constantly false))
  (println "Hello, World!"))

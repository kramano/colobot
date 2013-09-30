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


(defn tree [hero]
  (selector "Root"
   (sequence "Ressurect if dead"
             (action "Is hero dead?"
                     (dead? hero))
             (action "Ressurect hero"
                     (println "Rssurect")))
   (sequence "Hero is in town"
             (action "Is hero in town?"
                     (in-town? hero))
             (action "Maybe has gold for brick?"
                     (maybe-enough-gold? hero))
             (action "Check actual gold"
                     (println "Check gold"))
             (action "Try to make a brick behavior"
                     (println "Try to make a brick behavior")))
   (sequence "Hero is fighting with boss"
             (action "Is hero fighting?"
                     (fighting-with-boss? hero))
             (action "Fighting behavior"
                     (println "Fighting behavior")))
   (selector "Messing around"
             (sequence "Hero is fighting"
                       (action "Check if fighting"
                               (println "Check if fighting")))
             (sequence "Ok"
                       (action "Try to dig a treasure"
                               (println "Try to dig a treasure"))))))

(defn -main
  "I don't do a whole lot ... yet."
  [& args]
  ;; work around dangerous default behaviour in Clojure
  (alter-var-root #'*read-eval* (constantly false))
  (println "Hello, World!"))

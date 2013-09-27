(ns colobot.core
  (:gen-class)
  (:require [clj-http.client :as client]))

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

(defn posiible-enough-gold [hero]
  (let [gold-string (:gold_approx hero)]
    ()))

(defn low-godpower? [hero]
  (< (:godpower hero) 25))

(defn in-town? [hero]
  (not (clojure.string/blank? (:town_name hero))))

(defn fighting? [hero]
  (:arena_fight hero))

(defn collect-gld-strings [names]
  (let [res []]
    (pmap #(->> (get-hero-state %)
                      (:gold_approx)
                      (conj res))
          names)))

(defn -main
  "I don't do a whole lot ... yet."
  [& args]
  ;; work around dangerous default behaviour in Clojure
  (alter-var-root #'*read-eval* (constantly false))
  (println "Hello, World!"))

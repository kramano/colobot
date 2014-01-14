(ns colobot.core
  (:gen-class)
  (:require [clj-http.client :as client]
            [alter-ego.core :refer :all]
            [colobot.browser :as ui]
            [colobot.observer :as ob]))

(def godville-api-url "http://godville.net/gods/api/")

(def phrases {:digging "Копай! Клад! Золото!"
              :battle "Бей! Ударь! Стукни!"})

(defn make-url [god-name]
  (str godville-api-url god-name ".json"))

(defn get-hero-state [god-name]
  (let [resp (client/get
              (make-url god-name)
              {:as :json})
        hero (:body resp)]
    hero))

(defn log [s] (clojure.pprint/pprint s))

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
                    (log (str "Dead: " (dead? hero)))
                    (dead? hero))
            (action "Ressurect hero"
                    (log "Ressurection")
                    (ui/ressurect))))

(defn charge-prana []
  (selector "Charge prana"
            (inverter
             (action "Low prana?"
                     (log (str "Low prana: " (ui/low-prana?)))
                     (ui/low-prana?)))
            (action "Charge"
                    (log "Charging prana")
                    (ui/charge-prana))))

(defn make-brick [hero]
  (sequence "Make brick"
            (action "Maybe enough gold?"
                    (maybe-enough-gold? hero))
            (action "Enough gold?"
                    true)
            (until-success
             (sequence "Make bad"
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
                       (action "Check health" true)
                       (action "Boss dead?" true)))))

(defn low-health? [hero treshold-ratio]
  (let [{:keys [health max_health]} hero]
    (<= (/ health max_health) treshold-ratio)))

(defn messing-around [hero]
  (selector "Where is hero?"
            (action "In town?"
                    (log (str "In town: " (in-town? hero)))
                    (in-town? hero))
            (sequence "Fighting"
                      (charge-prana)
                      (action "Fighting?"
                              (log (str "Fighting: " (ui/fighting?)))
                              (ui/fighting?))
                      (action "Say battle phrase"
                              (ui/say (:battle phrases))))
            (sequence "Walking"
                      (charge-prana)
                      (action "Say dig phrase"
                              (ui/say (:digging phrases))))))

(defn behave [hero]
  (selector "Hero"
            (ressurect hero)
;            (make-brick hero)
            (messing-around hero)))

(defn loop-forever [f]
  (doall (repeatedly f)))

(defn run [f period]
  (loop-forever
   (fn [] (do
           (try (f)
                (catch Exception e (log (.getStackTrace e)))
                (finally (Thread/sleep period)))))))

(defn main-loop []
  (let [hero (get-hero-state "Баст и Он")]
    (alter-ego.core/exec (behave hero))))

(defn -main
  [& args]
  ;; work around dangerous default behaviour in Clojure
  (alter-var-root #'*read-eval* (constantly false))
  (do
    (ui/start-godville)
    (ui/login "***" "****")
    (run main-loop 60000)))

(ns colobot.browser
  (:use clj-webdriver.taxi)
  (:require [taoensso.timbre :as timbre
              :refer (trace debug info warn error fatal spy with-log-level)]))

(def digging_phrase "Копай! Клад! Золото!")

(def log-file "/home/mnovik/clj/colobot/colobot.log")

(defn configure-logger []
  (do
    (timbre/set-level! :info)
    (timbre/set-config! [:appenders :spit :enabled?] true)
    (timbre/set-config! [:shared-appender-config :spit-filename] log-file)
    (timbre/set-config! [:ns-whitelist] ["colobot.*"])))


(defn start-godville []
  (do
    (configure-logger)
    (info "Starting Godville...")
    (System/setProperty "webdriver.chrome.driver",
                        "/home/mnovik/clj/colobot/chromedriver")
    (set-driver! {:browser :chrome} "http://godville.net/")))

(defn login [email password]
  (info (str "Logging in with " email " " password))
  (do
    (quick-fill-submit {"#username" email}
                       {"#password" password}
                       {"#password" submit})
    (wait-until #(exists? "div#hk_distance"))))

(defn say [phrase]
  (info (str "Saying: " phrase))
  (do
    (quick-fill-submit {"#god_phrase" phrase}
                       {"#god_phrase" submit})
    true))

(defn fighting? []
  (let [monster (text (find-element-under "div#news" {:class "l_val"}))]
    (not (clojure.string/blank? monster))))

(defn click-lnk [elem]
  (when (and
         elem
         (present? elem)
         (enabled? elem))
    (click elem)))

(defn make-bad []
  (let [make-bad-lnk (element {:text "Сделать плохо"})]
    (click-lnk make-bad-lnk)
    true))

(defn make-good []
  (let [make-good-lnk (element {:text "Сделать хорошо"})]
    (click make-good-lnk)
    true))

(defn charge-prana []
  (let [charge-prana-lnk (element {:text "Восстановить"})]
    (click charge-prana-lnk)
    true))

(defn ressurect []
  (let [ressurect-lnk (element {:text "Воскресить"})]
    (click ressurect-lnk)
    true))

(defn in-city? []
  (= (text (find-element-under "div#hk_distance" {:class "l_capt"})) "Город"))

(defn parse-int [s]
  (Integer. (re-find #"\d+" s)))

(defn prana-percent []
  (let [prana-sting (text (element {:class "gp_val"}))]
    (parse-int prana-sting)))

(defn acc-value []
  (let [acc-string (text (element {:class "acc_val"}))]
    (parse-int acc-string)))

(defn money []
  (parse-int (text (find-element-under "div#hk_gold_we" {:class "l_val"}))))

(defn life-percent []
  (let [life-string (text (find-element-under "div#hk_health" {:class "l_val"}))
        splitted (clojure.string/split life-string #"\s+")
        current (parse-int (first splitted))
        total (parse-int (nth splitted 2))]
    (quot (* 100 current) total)))

(defn alive? [] (not= life-percent 0))

(defn ressurect []
  (click-lnk (element {:text "Воскресить"})))

(defn exit []
  (info "Leaving hero alone.")
  (quit))

(defn charge-or-die []
  (if (> (acc-value) 0)
    (do (charge-prana)
        (Thread/sleep 1000))
    (do (warn "No fucking prana!!")
        (exit))))

(defn low-prana? []
  (<= (prana-percent) 25))

(defn check-and-charge-prana []
  (info "Checking prana...")
  (when (<= (prana-percent) 25)
    (charge-or-die)))

(defn dig-behavior []
  (when
      (and (alive?)
           (not (in-city?))
           (not (fighting?))
           (> (prana-percent) 0))
    (say digging_phrase)))

(defn ressurect-if-dead []
  (when-not (alive?)
    (ressurect)))

(defn try-make-brick []
  (let [gld (money)]
    (when (>= gld 3000)
      (do
        (info "Trying to make a brick")
        (check-and-charge-prana)
        (make-bad)
        (Thread/sleep 10000)
        (if (< (money) gld)
          (info "Done!")
          (info "Epic fail"))))))

(defn default-behavior []
  (do
    (ressurect-if-dead)
    (when-not (in-city?)
      (check-and-charge-prana))
    (try-make-brick)
    (when (and (not (in-city?)) (not (fighting?)))
      (say digging_phrase))))

(defn loop-forever [f]
  (doall (repeatedly f)))

(defn behave [behavior period]
  (loop-forever
   (fn [] (do
           (try (behavior)
                (catch Exception e (warn e))
                (finally (Thread/sleep period)))))))

(defn main []
  (do
    (start-godville)
    (login "***" "***")
    (wait-until #(exists? "div#hk_distance"))
    (behave default-behavior 60000)))

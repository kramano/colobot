(ns colobot.system
  (:require [colobot.core :as core]
            [taoensso.timbre :as timbre
              :refer (trace debug info warn error fatal spy with-log-level)]))

(def log-file "/home/mnovik/clj/colobot/colobot.log")

(defn configure-logger []
  (do
    (timbre/set-level! :info)
    (timbre/set-config! [:appenders :spit :enabled?] true)
    (timbre/set-config! [:shared-appender-config :spit-filename] log-file)))

(defn create-dev-system []
  (configure-logger))

(defn start [system]
  ())

(defn stop [system]
  ())

(ns colobot.observer
  (:use (clj-net-pcap core sniffer pcap-data))
  (:require [taoensso.timbre :as timbre
             :refer (trace debug info warn error fatal spy with-log-level)]
            [cheshire.core :as cheshire])
  (:import (org.jnetpcap.packet PcapPacket)
           (org.jnetpcap.nio JBuffer)
           (org.jnetpcap.protocol.tcpip Tcp)
           (org.jnetpcap.protocol.lan Ethernet)
           (org.jnetpcap.protocol.network Ip4)))

(def config {:interface "eth0"
             :src-host "46.4.69.52"
             :src-port 82
             :offset 78})
(def message-parts (atom []))

(defn buffer-to-string [^JBuffer buffer offset]
  (when buffer
    (.getUTF8String buffer offset (- (.size buffer) offset))))

(defn log-message [message]
  (try (info (cheshire/parse-string message true))
       (catch Exception ex (info (str "Unable to parse: " message)))))

(defn is-last-part? [^String message]
  (.endsWith message "\"success\"}"))

(defn handle [packet]
  (let [p (:pcap-packet packet)
        message (buffer-to-string p (:offset config))]
    (swap! message-parts conj message)
    (when (is-last-part? message)
      (->> @message-parts
           (apply str)
           (log-message))
      (reset! message-parts []))))

(defn start-capture []
  (create-and-start-cljnetpcap
   handle (:interface config)
   "((tcp) and ((src host 46.4.69.52) and (src port 82)))"))

(defn stop-capture [sniff]
  (stop-cljnetpcap sniff))

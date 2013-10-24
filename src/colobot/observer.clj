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

(defn get-message-length [^PcapPacket packet]
  (let [headers [(Tcp.) (Ip4.) (Ethernet.)]
        header-sizes (map #(.. packet (getHeader %) (size)) headers)
        full-size (.size packet)
        without-headers (- full-size (reduce + header-sizes))]
    {:packet-size full-size
     :without-headers without-headers}))

(def message-parts (atom []))

(defn buffer-to-string [^JBuffer buffer offset]
  (when buffer
    (.getUTF8String buffer offset (- (.size buffer) offset))))

(defn log-message [message]
  (try (info (cheshire/parse-string message true))
       (catch Exception ex (do (info (str "Unable to parse: " message))
                               (info ex)))))

(defn is-last-part? [^String message]
  (.endsWith message "\"success\"}"))

(defn handle [packet]
  (let [p (:pcap-packet packet)
        full-message (buffer-to-string p 66)
        message (buffer-to-string p (:offset config))]
    (if (empty? @message-parts)
      (swap! message-parts conj message)
      (swap! message-parts conj full-message))
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

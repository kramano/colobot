(defproject colobot "0.1.0-SNAPSHOT"
  :description "Simple AI for godville.net"
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.5.1"]
                 [clj-http "0.7.7"]
                 [clj-webdriver "0.6.0"]
                 [com.taoensso/timbre "2.6.2"]
                 [alter-ego "0.0.5-SNAPSHOT"]
                 [jnetpcap "1.4.r1390-1b"]
                 [clj-net-pcap "1.4.1"]
                 [cheshire "5.2.0"]]
  :profiles {:dev {:source-paths ["dev"]
                   :dependencies [[org.clojure/tools.namespace "0.2.4"]
                                  [com.cemerick/pomegranate "0.2.0"]
                                  [spyscope "0.1.3"]
                                  [criterium "0.4.1"]
                                  [org.clojure/java.classpath "0.2.0"]]}}
  :main colobot.core)

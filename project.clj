(defproject osm "0.1.0-SNAPSHOT"
  :description "Displays a map using SeeSaw and the OpenStreetMaps API."
  :url "https://github.com/netb258/fetch-map"
  :license {:name "EPL-2.0 OR GPL-2.0-or-later WITH Classpath-exception-2.0"
            :url "https://www.eclipse.org/legal/epl-2.0/"}
  :dependencies [[org.clojure/clojure "1.8.0"]
                 [seesaw "1.5.0"]
                 [clj-http "3.9.0"]]
  :main ^:skip-aot osm.core
  :target-path "target/%s"
  :profiles {:uberjar {:aot :all}})

(defproject clunogs "0.1.0"
  :description "A Clojure library to wrap the uNoGS API."
  :url "http://github.com/blevs/clunogs"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.8.0"]
                 [clj-http "3.9.0"]
                 [cheshire "5.8.0"]
                 [org.apache.commons/commons-lang3 "3.7"]]
  :plugins [[lein-codox "0.10.6"]]
  :codox {:output-path  "codox"}
  :profiles {:dev {:dependencies [[clj-http-fake "1.0.3"]]}})

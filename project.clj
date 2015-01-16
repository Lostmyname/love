(defproject love "0.1.0-SNAPSHOT"
  :description "An tiny app that pulls positive Facebook posts into Slack."
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.6.0"]
                 [clj-http "1.0.1"]
                 [com.taoensso/carmine "2.9.0"]
                 [environ "1.0.0"]]
  :plugins [[lein-environ "1.0.0"]]
  :main ^:skip-aot love.core
  :target-path "target/%s"
  :profiles {:uberjar {:aot :all}})

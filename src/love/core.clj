(ns love.core
  (:require
    [clj-http.client :as client]
    [taoensso.carmine :as car :refer (wcar)]
    [clojure.string :as str]))
(require '[environ.core :refer [env]])

(def redis-server-connnection {:pool {} :spec {:uri (env :redis-url)}})
(defmacro wcar* [& body] `(car/wcar redis-server-connnection ~@body))

(def slack-webhook-endpoint
  (env :slack-webhook-endpoint))

(def alchemy-api-key
  (env :alchemy-api-key))

(def facebook-page-id
  (env :facebook-page-id))

(def facebook-access-token
  (env :facebook-access-token))

(defn parse-facebook-timestamp [timestamp]
  (str/replace timestamp "+0000" ""))

(defn latest-facebook-timestamp []
  (or (wcar* (car/get "latest-facebook-timestamp")) "0"))

(defn post-to-slack [post]
  (client/post slack-webhook-endpoint
               {:form-params {"text" (str "*" (:name (:from post)) "*: " (:message post) " " (:picture post))} :content-type :json}))

(defn persist [post]
  (wcar* (car/set "latest-facebook-timestamp" (parse-facebook-timestamp (:created_time post)))))

(defn publish-and-persist [post]
  do((post-to-slack post) (persist post)))

(defn sentiment [post]
  (:docSentiment
    (:body
      (client/get
        "https://access.alchemyapi.com/calls/text/TextGetTextSentiment"
        {:as :json :query-params {"apikey" alchemy-api-key "outputMode" "json" "text" (:message post)}}))))

(defn happy? [post]
  (> (read-string (or (:score (sentiment post)) "0")) 0.6))

(defn unhappy? [post]
  (not (happy? post)))

(defn facebook-posts []
  (reverse
    (remove unhappy?
      (:data
        (:body
          (client/get
            (str "https://graph.facebook.com/v2.2/" facebook-page-id "/tagged")
            {:as :json
             :throw-entire-message? true
             :query-params {"access_token"
                            facebook-access-token
                            "fields"
                            "type,story,message,picture,from,created_time"
                            "limit"
                            30
                            "since"
                            (latest-facebook-timestamp)}}))))))

(defn -main [& args]
  (println (map publish-and-persist (facebook-posts))))

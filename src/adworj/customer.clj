(ns adworj.customer
  (:import [com.google.api.ads.adwords.axis.v201506.mcm CustomerServiceInterface]
           [com.google.api.ads.adwords.axis.factory AdWordsServices]))

(defrecord Customer [auto-tagging-enabled?
                     can-manage-clients?
                     company-name
                     currency-code
                     customer-id
                     date-time-zone
                     descriptive-name
                     tracking-url-template
                     test-account?])

(defn customer-service [adwords-session]
  (let [services (AdWordsServices. )]
    (.get services adwords-session CustomerServiceInterface)))

(defn customer [customer-service]
  (let [c (.get customer-service)]
    (map->Customer {:auto-tagging-enabled? (.getAutoTaggingEnabled c)
                    :can-manage-clients?   (.getCanManageClients c)
                    :company-name          (.getCompanyName c)
                    :currency-code         (.getCurrencyCode c)
                    :customer-id           (.getCustomerId c)
                    :date-time-zone        (.getDateTimeZone c)
                    :descriptive-name      (.getDescriptiveName c)
                    :test-account?         (.getTestAccount c)
                    :tracking-url-template (.getTrackingUrlTemplate c)})))

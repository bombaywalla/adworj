(ns adworj.targeting-idea-example
  (:require [adworj.credentials :as ac]
            [adworj.targeting-idea :as at]))

(defn -main [ads-properties show-all & opts]
  (let [credentials (ac/offline-credentials ads-properties)
        session (apply ac/adwords-session ads-properties credentials opts)
        service (at/targeting-idea-service session)
        langparam (at/language-search-parameter "English")
        locparam (at/location-search-parameter "US")
        netparam (at/network-search-parameter (at/network-setting :target-google-search true))
        volparam (at/search-volume-search-parameter 200 100000)
        filtparam (at/idea-text-filter-search-parameter :included ["chandelier" "chandeliers"]
                                                        :excluded ["wooden" "wood" "industrial" "earring"
                                                                   "decal" "stand" "bulb" "bulbs"
                                                                   "pictures of" "prices" "how to"
                                                                   "fixtures" "fixture" "art" "frame"
                                                                   "floor lamp" "covers" "wholesale"
                                                                   "cheap" "discount" "parts" "earrings"])
        queryparam (at/related-to-query-search-parameter "chandelier" "chandeliers")
        selector (at/targeting-idea-selector [queryparam langparam locparam netparam volparam filtparam]
                                             (at/idea-type :keyword)
                                             (at/request-type :ideas)
                                             (at/attribute-types :average-cpc
                                                                 :category-products-and-services
                                                                 :competition
                                                                 :keyword-text             
                                                                 :search-volume)
                                             (at/locale "English" "US")
                                             (at/currency "US"))
        ideas (at/get-ideas service selector)]
    (doseq [idea ideas]
      (if show-all
        (do (println "idea:")
            (doseq [key (keys idea)]
              (println "  " key ": " (get idea key))))
        (println "keyword: " (get idea (at/attribute-type :keyword-text)))))))

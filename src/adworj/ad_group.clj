(ns adworj.ad-group
  (:import [com.google.api.ads.adwords.axis.v201502.cm AdGroupServiceInterface]
           [com.google.api.ads.adwords.axis.v201502.cm AdGroup]
           [com.google.api.ads.adwords.axis.v201502.cm AdGroupPage]
           [com.google.api.ads.adwords.axis.v201502.cm Selector]
           [com.google.api.ads.adwords.axis.utils.v201502 SelectorBuilder]
           [com.google.api.ads.adwords.lib.selectorfields.v201502.cm AdGroupField]
           [com.google.api.ads.adwords.axis.factory AdWordsServices]))

(defrecord AdWordsAdGroup [
                           id
                           campaign-id
                           campaign-name
                           name
                           status
                           tracking-url-template
                           ])

(defn ad-group-service
  [adwords-session]
  (let [services (AdWordsServices. )]
    (.get services adwords-session AdGroupServiceInterface)))

(defn ad-group-field
  [field-name]
  (case field-name
    :id AdGroupField/Id
    :campaign-id AdGroupField/CampaignId
    :campaign-name AdGroupField/CampaignName
    :name AdGroupField/Name
    :status AdGroupField/Status
    :tracking-url-template AdGroupField/TrackingUrlTemplate
    ))

(defn selector-builder
  [campaign-id fields]
  (doto (SelectorBuilder.)
    (.fields (into-array AdGroupField fields))
    (.offset (int 0))                   ; to make sure a Paging is created
    (.equals (ad-group-field :campaign-id) (str campaign-id))
    (.orderAscBy (first fields))))

(defn selector
  [builder]
  (.build builder))

(defn get-ad-group-page
  [service selector]
  (.get service selector))

(defn ad-group-to-clojure
  [camp]
  (map->AdWordsAdGroup {
                        :id (.getId camp)
                        :name (.getName camp)
                        :campaign-id (.getCampaignId camp)
                        :campaign-name (.getCampaignName camp)
                        :status (.getStatus camp)
                        }))

(defn get-ad-groups
  [service selector]
  (let [page-size 100
        start 0
        paging (doto (.getPaging selector) ;Note side-effects Paging in the selector
                 (.setStartIndex (int start))
                 (.setNumberResults (int page-size)))]
    (loop [more-pages true
           offset start
           result []]
      (if-not more-pages
        result
        (let [page (do (.setStartIndex paging (int offset))
                       (get-ad-group-page service selector))
              total-entries (.getTotalNumEntries page)
              ad-groups (.getEntries page)]
          (recur (< offset total-entries)
                 (+ offset page-size)
                 (into result (map ad-group-to-clojure ad-groups))))))))


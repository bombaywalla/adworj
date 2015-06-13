(ns adworj.campaign
  (:import [com.google.api.ads.adwords.axis.v201502.cm CampaignServiceInterface]
           [com.google.api.ads.adwords.axis.v201502.cm Campaign]
           [com.google.api.ads.adwords.axis.v201502.cm CampaignPage]
           [com.google.api.ads.adwords.axis.v201502.cm Selector]
           [com.google.api.ads.adwords.axis.utils.v201502 SelectorBuilder]
           [com.google.api.ads.adwords.lib.selectorfields.v201502.cm CampaignField]
           [com.google.api.ads.adwords.axis.factory AdWordsServices]))

(defrecord AdWordsCampaign [id
                            name
                            status
                            serving-status
                            start-date
                            end-date])

(defn campaign-service
  [adwords-session]
  (let [services (AdWordsServices. )]
    (.get services adwords-session CampaignServiceInterface)))

(defn campaign-field
  [field-name]
  (case field-name
    :id CampaignField/Id
    :name CampaignField/Name
    :status CampaignField/Status
    :serving-status CampaignField/ServingStatus
    :start-date CampaignField/StartDate
    :end-date CampaignField/EndDate))



(defn selector-builder
  [offset pagesize fields]
  (let [builder (SelectorBuilder.)]
    (.fields builder (into-array CampaignField fields))
    (.orderAscBy builder (first fields))
    (.offset builder offset)
    (.limit builder pagesize)))

(defn selector
  [builder]
  (.build builder))


(defn get-campaign-page
  [service selector]
  (.get service selector))

(defn campaign-to-clojure
  [camp]
  (map->AdWordsCampaign {:id (.getId camp)
                         :name (.getName camp)
                         :status (.getStatus camp)
                         :serving-status (.getServingStatus camp)
                         :start-date (.getStartDate camp)
                         :end-date (.getEndDate camp) } ))

(defn get-campaigns
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
                       (get-campaign-page service selector))
              total-entries (.getTotalNumEntries page)
              campaigns (.getEntries page)]
          (recur (< offset total-entries)
                 (+ offset page-size)
                 (into result (map campaign-to-clojure campaigns))))))))


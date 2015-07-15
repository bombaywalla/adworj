(ns adworj.ad-group
  (:import [com.google.api.ads.adwords.axis.v201506.cm AdGroupServiceInterface]
           [com.google.api.ads.adwords.axis.v201506.cm AdGroup]
           [com.google.api.ads.adwords.axis.v201506.cm AdGroupPage]
           [com.google.api.ads.adwords.axis.v201506.cm AdGroupOperation]
           [com.google.api.ads.adwords.axis.v201506.cm AdGroupReturnValue]
           [com.google.api.ads.adwords.axis.v201506.cm Selector]
           [com.google.api.ads.adwords.axis.v201506.cm Operator]
           [com.google.api.ads.adwords.axis.utils.v201506 SelectorBuilder]
           [com.google.api.ads.adwords.lib.selectorfields.v201506.cm AdGroupField]
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
  [campaign-ids fields]
  (doto (SelectorBuilder.)
    (.fields (into-array AdGroupField fields))
    (.offset (int 0))                   ; to make sure a Paging is created
    (.in (ad-group-field :campaign-id) (into-array String (map str campaign-ids)))
    (.orderAscBy (first fields))))

(defn selector
  [builder]
  (.build builder))

(defn get-ad-group-page
  [service selector]
  (.get service selector))

(defn mutate
  [service & operations]
  (.mutate service (into-array AdGroupOperation operations)))

(defn operator
  [type]
  (case type
    :add Operator/ADD
    :remove Operator/REMOVE
    :set Operator/SET
    ))

(defn operation
  [operator adgroup]
  (doto (AdGroupOperation.)
    (.setOperator operator)
    (.setOperand adgroup)))

(defn set-tracking-url-template
  [service ad-group-id trackingurl]
  (let [adg (doto (AdGroup.)
              (.setId ad-group-id)
              (.setTrackingUrlTemplate trackingurl))
        op (operation (operator :set) adg)
        ret (mutate service op)]
    (if (.getPartialFailureErrors ret) (println "XXX There were errors"))
    ret))

(defn get-ad-group-by-id
  [service adgid]
  (let [fields (map ad-group-field [:id :campaign-id :campaign-name :name :status :tracking-url-template])
        builder (doto (SelectorBuilder.)
                  (.fields (into-array AdGroupField fields))
                  (.offset (int 0))
                  (.limit (int 1))
                  (.equalsId adgid))
        sel (selector builder)
        page (get-ad-group-page service sel)
        adgs (.getEntries page)]
    (if (nil? adgs)
      nil
      (if (= (count adgs) 1)
        (first adgs)
        nil))))

(defn ad-group-to-clojure
  [adg]
  (map->AdWordsAdGroup {
                        :id (.getId adg)
                        :name (.getName adg)
                        :campaign-id (.getCampaignId adg)
                        :campaign-name (.getCampaignName adg)
                        :status (.getStatus adg)
                        :tracking-url-template (.getTrackingUrlTemplate adg)
                        }))

(defn get-ad-groups
  ([service selector]
   (get-ad-groups service selector ad-group-to-clojure))
  ([service selector convert-fn]
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
                 (into result (map convert-fn ad-groups)))))))))

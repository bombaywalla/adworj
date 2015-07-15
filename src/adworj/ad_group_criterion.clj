(ns adworj.ad-group-criterion
  (:import [com.google.api.ads.adwords.axis.v201506.cm AdGroupCriterionServiceInterface]
           [com.google.api.ads.adwords.axis.v201506.cm AdGroupCriterion]
           [com.google.api.ads.adwords.axis.v201506.cm AdGroupCriterionOperation]
           [com.google.api.ads.adwords.axis.v201506.cm AdGroupCriterionReturnValue]
           [com.google.api.ads.adwords.axis.v201506.cm Keyword]
           [com.google.api.ads.adwords.axis.v201506.cm PolicyViolationKey]
           [com.google.api.ads.adwords.axis.v201506.cm ExemptionRequest]
           [com.google.api.ads.adwords.axis.v201506.cm BiddableAdGroupCriterion]
           [com.google.api.ads.adwords.axis.v201506.cm NegativeAdGroupCriterion]
           [com.google.api.ads.adwords.axis.v201506.cm CriterionUse]
           [com.google.api.ads.adwords.axis.v201506.cm UserStatus]
           [com.google.api.ads.adwords.axis.v201506.cm AdGroupCriterionPage]
           [com.google.api.ads.adwords.axis.v201506.cm Selector]
           [com.google.api.ads.adwords.axis.v201506.cm Operator]
           [com.google.api.ads.adwords.axis.utils.v201506 SelectorBuilder]
           [com.google.api.ads.adwords.lib.selectorfields.v201506.cm AdGroupCriterionField]
           [com.google.api.ads.adwords.axis.factory AdWordsServices]))


;; TODO: There must be a better way of automatically generating the names.
(defrecord AdWordsAdGroupCriterion [
                                    ad-group-criterion-type
                                    ad-group-id
                                    approval-status
                                    criterion
                                    criterion-use
                                    destination-url
                                    id
                                    final-urls
                                    system-serving-status
                                    tracking-url-template
                                    user-status
                                    ])

(defn ad-group-criterion-service
  [adwords-session]
  (let [services (AdWordsServices. )]
    (.get services adwords-session AdGroupCriterionServiceInterface)))

;; TODO: Should be in another namespace
;; As should others...
(def operator
  {:add Operator/ADD
   :remove Operator/REMOVE
   :set Operator/SET})

(defn get-value
  "To get the string value of a criterion-use or a user-status"
  [arg]
  (.getValue arg))

(def criterion-use
  {:biddable CriterionUse/BIDDABLE
   :negative CriterionUse/NEGATIVE})

(def user-status
  {:enabled UserStatus/ENABLED
   :removed UserStatus/REMOVED
   :paused UserStatus/PAUSED})

(def ad-group-criterion-field
  {:ad-group-id AdGroupCriterionField/AdGroupId
   :approval-status AdGroupCriterionField/ApprovalStatus
   :criteria-type AdGroupCriterionField/CriteriaType
   :criterion-use AdGroupCriterionField/CriterionUse
   :destination-url AdGroupCriterionField/DestinationUrl
   :final-app-urls AdGroupCriterionField/FinalAppUrls
   :final-mobile-urls AdGroupCriterionField/FinalAppUrls
   :final-urls AdGroupCriterionField/FinalUrls
   :id AdGroupCriterionField/Id
   :keyword-match-type AdGroupCriterionField/KeywordMatchType
   :keyword-text AdGroupCriterionField/KeywordText
   :status AdGroupCriterionField/Status ; corresponds to BiddableAdGroupCriterion.userStatus
   :system-serving-status AdGroupCriterionField/SystemServingStatus
   :tracking-url-template AdGroupCriterionField/TrackingUrlTemplate
   :url-custom-parameters AdGroupCriterionField/UrlCustomParameters})

(defn mutate
  [service & operations]
  (.mutate service (into-array AdGroupCriterionOperation operations)))

(defn exemption-request
  [pvkey]
  (doto (ExemptionRequest.)
    (.setKey pvkey)))

(defn policy-violation-key
  [policy-name violating-text]
  (doto (PolicyViolationKey.)
    (.setPolicyName policy-name)
    (.setViolatingText violating-text)))

(defn operation
  [operator operand & exemption-requests]
  (let [op (doto (AdGroupCriterionOperation.)
             (.setOperator operator)
             (.setOperand operand))]
    (if (empty? exemption-requests)
      op
      (doto op
        (.setExemptionRequests
         (into-array ExemptionRequest exemption-requests))))))

;; TODO: handle other operations on SelectorBuilder
;; Especially the CriterionUse
(defn selector-builder
  [ad-group-id fields]                   ;TODO: handle empty fields
  (doto (SelectorBuilder.)
    (.fields (into-array AdGroupCriterionField fields))
    (.offset (int 0))                   ; to make sure a Paging is created
    (.orderAscBy (first fields))
    (.in (ad-group-criterion-field :ad-group-id) (into-array String [(str ad-group-id)]))))

(defn selector
  [builder]
  (.build builder))

;; function to nullify the destination url for given criteria
(defn set-ad-group-kwds-null-dest-url
  [service adgcrits]
  (let [ops (map (fn [adgcrit]
                   (operation (operator :set)
                              (doto (BiddableAdGroupCriterion.)
                                (.setAdGroupId (.getAdGroupId adgcrit))
                                (.setCriterion (doto (Keyword.)
                                                 (.setId (.getId (.getCriterion adgcrit)))))
                                (.setDestinationUrl ""))))
                 adgcrits)]
    (apply (partial mutate service) ops)))

(defn get-ad-group-criterion-page
  [service selector]
  (.get service selector))

(defn ad-group-criterion-to-clojure
  [criterion]
  (let [criterionmap {
                      :ad-group-criterion-type (.getAdGroupCriterionType criterion)
                      :ad-group-id (.getAdGroupId criterion)
                      :criterion (.getCriterion criterion)
                      :criterion-use (.getCriterionUse criterion)
                      }]
    (map->AdWordsAdGroupCriterion
     (if (= (.getAdGroupCriterionType criterion) "BiddableAdGroupCriterion")
       (merge criterionmap
              {
               :approval-status (.getApprovalStatus criterion)
               :destination-url (.getDestinationUrl criterion)
               :final-urls (.getFinalUrls criterion) ; TODO: do better here
               :system-serving-status (.getSystemServingStatus criterion)
               :tracking-url-template (.getTrackingUrlTemplate criterion)
               :user-status (.getUserStatus criterion)
               })
       criterionmap))))

;; TODO: refactor to pass in the page getter as a param
(defn get-ad-group-criteria
  [service selector]
  (let [page-size 100
        start 0
        paging (doto (.getPaging selector) ; Note side-effects Paging in the selector
                 (.setStartIndex (int start))
                 (.setNumberResults (int page-size)))]
    (loop [more-pages true
           offset start
           result []]
      (if-not more-pages
        result
        (let [page (do (.setStartIndex paging (int offset))
                       (get-ad-group-criterion-page service selector))
              total-entries (.getTotalNumEntries page)
              ad-group-criteria (.getEntries page)]
          (recur (< offset total-entries)
                 (+ offset page-size)
                 (into result ad-group-criteria)))))))


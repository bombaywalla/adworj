(ns adworj.ad-group-criterion
  (:import [com.google.api.ads.adwords.axis.v201502.cm AdGroupCriterionServiceInterface]
           [com.google.api.ads.adwords.axis.v201502.cm AdGroupCriterion]
           [com.google.api.ads.adwords.axis.v201502.cm AdGroupCriterionOperation]
           [com.google.api.ads.adwords.axis.v201502.cm AdGroupCriterionReturnValue]
           [com.google.api.ads.adwords.axis.v201502.cm Keyword]
           [com.google.api.ads.adwords.axis.v201502.cm PolicyViolationKey]
           [com.google.api.ads.adwords.axis.v201502.cm ExemptionRequest]
           [com.google.api.ads.adwords.axis.v201502.cm BiddableAdGroupCriterion]
           [com.google.api.ads.adwords.axis.v201502.cm NegativeAdGroupCriterion]
           [com.google.api.ads.adwords.axis.v201502.cm CriterionUse]
           [com.google.api.ads.adwords.axis.v201502.cm AdGroupCriterionPage]
           [com.google.api.ads.adwords.axis.v201502.cm Selector]
           [com.google.api.ads.adwords.axis.v201502.cm Operator]
           [com.google.api.ads.adwords.axis.utils.v201502 SelectorBuilder]
           [com.google.api.ads.adwords.lib.selectorfields.v201502.cm AdGroupCriterionField]
           [com.google.api.ads.adwords.axis.factory AdWordsServices]))


;; TBD There must be a better way of automatically generating the names.
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

;; TBD Should be in another namespace
;; As should others...
(defn operator
  [type]
  (case type
    :add Operator/ADD
    :remove Operator/REMOVE
    :set Operator/SET
    ))
  
(defn criterion-use
  [type]
  (case type
    :biddable CriterionUse/BIDDABLE
    :negative CriterionUse/NEGATIVE
    ))

(defn ad-group-criterion-field
  [field-name]
  (case field-name
    :ad-group-id AdGroupCriterionField/AdGroupId
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
    :url-custom-parameters AdGroupCriterionField/UrlCustomParameters
    ))

(defn mutate
  [service & operations]
  (.mutate service (into-array AdGroupCriterionOperation operations)))

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

(defn selector-builder
  [ad-group-id fields]                   ;TBD handle empty fields
  (doto (SelectorBuilder.)
    (.fields (into-array AdGroupCriterionField fields))
    (.offset (int 0))                   ; to make sure a Paging is created
    (.orderAscBy (first fields))
    (.in (ad-group-criterion-field :ad-group-id) (into-array String [(str ad-group-id)]))))

(defn selector
  [builder]
  (.build builder))


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
               :final-urls (.getFinalUrls criterion) ; TBD do better here
               :system-serving-status (.getSystemServingStatus criterion)
               :tracking-url-template (.getTrackingUrlTemplate criterion)
               :user-status (.getUserStatus criterion)
               })
       criterionmap))))

;; TBD refactor to pass in the page getter as a param
(defn get-ad-group-criteria
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
                       (get-ad-group-criterion-page service selector))
              total-entries (.getTotalNumEntries page)
              ad-group-criteria (.getEntries page)]
          (recur (< offset total-entries)
                 (+ offset page-size)
                 (into result ad-group-criteria)))))))

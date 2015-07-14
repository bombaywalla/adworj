(ns adworj.ad-group-ad
  (:import [com.google.api.ads.adwords.axis.v201502.cm AdGroupAdServiceInterface]
           [com.google.api.ads.adwords.axis.v201502.cm AdGroupAd]
           [com.google.api.ads.adwords.axis.v201502.cm AdGroupAdOperation]
           [com.google.api.ads.adwords.axis.v201502.cm AdUrlUpgrade]
           [com.google.api.ads.adwords.axis.v201502.cm AdGroupAdApprovalStatus]
           [com.google.api.ads.adwords.axis.v201502.cm AdGroupAdStatus]
           [com.google.api.ads.adwords.axis.v201502.cm AdGroupAdReturnValue]
           [com.google.api.ads.adwords.axis.v201502.cm TextAd]
           [com.google.api.ads.adwords.axis.v201502.cm PolicyViolationKey]
           [com.google.api.ads.adwords.axis.v201502.cm ExemptionRequest]
           [com.google.api.ads.adwords.axis.v201502.cm ApiException]
           [com.google.api.ads.adwords.axis.v201502.cm AdGroupAdPage]
           [com.google.api.ads.adwords.axis.v201502.cm Selector]
           [com.google.api.ads.adwords.axis.v201502.cm Operator]
           [com.google.api.ads.adwords.axis.utils.v201502 SelectorBuilder]
           [com.google.api.ads.adwords.lib.selectorfields.v201502.cm AdGroupAdField]
           [com.google.api.ads.adwords.axis.factory AdWordsServices]))


;; TBD There must be a better way of automatically generating the names.
(defrecord AdWordsAdGroupAd [
                             ad-group-id
                             approval-status
                             ad
                             status
                             ])

(defn ad-group-ad-service
  [adwords-session]
  (let [services (AdWordsServices. )]
    (.get services adwords-session AdGroupAdServiceInterface)))

;; TBD Should be in another namespace
;; As should others...
(defn operator
  [type]
  (case type
    :add Operator/ADD
    :remove Operator/REMOVE
    :set Operator/SET
    ))
  
(defn ad-group-ad-approval-status
  [type]
  (case type
    :approved AdGroupAdApprovalStatus/APPROVED
    :disapproved AdGroupAdApprovalStatus/DISAPPROVED
    :family-safe AdGroupAdApprovalStatus/FAMILY_SAFE
    :non-family-safe AdGroupAdApprovalStatus/NON_FAMILY_SAFE
    :porn AdGroupAdApprovalStatus/PORN
    :unchecked AdGroupAdApprovalStatus/UNCHECKED
    :unknown AdGroupAdApprovalStatus/UNKNOWN
    ))

(defn ad-group-ad-status
  [type]
  (case type
    :enabled AdGroupAdStatus/ENABLED
    :paused AdGroupAdStatus/PAUSED
    :disabled AdGroupAdStatus/DISABLED
    ))

;; TBD really need a way to get all
(defn ad-group-ad-field
  [field-name]
  (case field-name
    :ad-group-id AdGroupAdField/AdGroupId
    :ad-group-creative-approval-status AdGroupAdField/AdGroupCreativeApprovalStatus
    :creative-final-urls AdGroupAdField/CreativeFinalUrls
    :url AdGroupAdField/Url             ;aka DestinationUrl
    :display-url AdGroupAdField/DisplayUrl
    :headline AdGroupAdField/Headline
    :description1 AdGroupAdField/Description1
    :description2 AdGroupAdField/Description2
    :device-preference AdGroupAdField/DevicePreference
    :id AdGroupAdField/Id
    :status AdGroupAdField/Status
    :type AdGroupAdField/Type
    :creative-tracking-url-template AdGroupAdField/CreativeTrackingUrlTemplate
    :creative-url-custom-parameters AdGroupAdField/CreativeUrlCustomParameters
    ))

(defn get-ad-group-ad-page
  [service selector]
  (.get service selector))

(defn mutate
  [service & operations]
  (.mutate service (into-array AdGroupAdOperation operations)))

(defn selector-builder
  [ad-group-ids fields]                   ;TBD handle empty fields
  (doto (SelectorBuilder.)
    (.fields (into-array AdGroupAdField fields))
    (.offset (int 0))                   ; to make sure a Paging is created
    (.orderAscBy (first fields))
    (.in (ad-group-ad-field :ad-group-id) (into-array String (map str ad-group-ids)))))

(defn selector
  [builder]
  (.build builder))

(defn upgrade-url
  [service & adurlupgrades]
  (.upgradeUrl service (into-array AdUrlUpgrade adurlupgrades)))

(defn ad-url-upgrade
  [ad-id final-url & {:keys [final-mobile-url tracking-url-template]}]
  (let [auu (doto (AdUrlUpgrade.)
              (.setAdId ad-id)
              (.setFinalUrl final-url))]
    (if final-mobile-url (.setFinalMobileUrl auu final-mobile-url))
    (if tracking-url-template (.setTrackingUrlTemplate auu tracking-url-template))
    auu))

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
  (let [op (doto (AdGroupAdOperation.)
             (.setOperator operator)
             (.setOperand operand))]
    (if (empty? exemption-requests)
      op
      (doto op
        (.setExemptionRequests
         (into-array ExemptionRequest exemption-requests))))))

(defn shallow-copy-text-ad
  [ad]
  (doto (TextAd.)
    (.setId (.getId ad))
    (.setUrl (.getUrl ad))
    (.setDisplayUrl (.getDisplayUrl ad))
    (.setFinalUrls (.getFinalUrls ad))
    (.setFinalMobileUrls (.getFinalMobileUrls ad))
    (.setFinalAppUrls (.getFinalAppUrls ad))
    (.setTrackingUrlTemplate (.getTrackingUrlTemplate ad))
    (.setUrlCustomParameters (.getUrlCustomParameters ad))
    (.setDevicePreference (.getDevicePreference ad))
    (.setAdType (.getAdType ad))
    (.setHeadline (.getHeadline ad))
    (.setDescription1 (.getDescription1 ad))
    (.setDescription2 (.getDescription2 ad))))

(defn shallow-copy-ad-group-ad
  [adgad]
  (doto (AdGroupAd.)
    (.setAdGroupId (.getAdGroupId adgad))
    (.setAd (.getAd adgad))
    (.setExperimentData (.getExperimentData adgad))
    (.setStatus (.getStatus adgad))
    (.setApprovalStatus (.getApprovalStatus adgad))
    (.setTrademarks (.getTrademarks adgad))
    (.setDisapprovalReasons (.getDisapprovalReasons adgad))
    (.setTrademarkDisapproved (.getTrademarkDisapproved adgad))
    (.setLabels (.getLabels adgad))
    (.setForwardCompatibilityMap (.getForwardCompatibilityMap adgad))))

;; helper helper function
;; Note that this side-effects the addop
(defn add-exemption-to-add-op
  [addop err]
  (let [pvkey (.getKey err)]
    (doto addop
      (.setExemptionRequests
       (into-array ExemptionRequest [(exemption-request pvkey)])))))
  
;; helper function
(defn do-final-url-maybe-retry-exemption
  [service addops delops]
  (try (apply (partial mutate service) (concat addops delops))
       (catch ApiException apiex
         (println "Caught an ApiException: " (.getMessage apiex))
         (println apiex)
         (let [errs (.getErrors apiex)]
           (if (every? #(= (.getApiErrorType %) "UrlError") errs)
             (do (println "Since these are UrlErrors, skipping them.")
                 (AdGroupAdReturnValue.))
             (if (every? #(= (.getApiErrorType %) "PolicyViolationError") errs)
               (if (every? #(.getIsExemptable %) errs)
                 (if (= (count addops) (count errs))
                   (let [exaddops (map add-exemption-to-add-op addops errs)] ;TBD This assumes the addops and the errs are in the same order
                     (println "Retrying the mutate with exemption requests")
                     (apply (partial mutate service) (concat exaddops delops)))
                   (do (println "Error count does not match addOp count"
                                (count errs) (count addops))
                       (println "Re-throwing the exception")
                       (throw apiex)))
                 (do (println "Not all errors are exemptable.")
                     (println "Re-throwing the exception")
                     (throw apiex)))
               (do (println "Not all are PolicyViolationErrors")
                   (println "Re-throwing the exception")
                   (throw apiex))))))))

;; Note that since the Ads cannot be updated, this will make new Ads
(defn set-ads-final-url
  "Set the final URL for all the ads specified"
  [service adg-ads final-url]
  (let [delops (map (fn [adg-ad]
                      (operation (operator :remove)
                                 (doto (AdGroupAd.)
                                   (.setAdGroupId (.getAdGroupId adg-ad))
                                   (.setAd (doto (TextAd.) (.setId (.getId (.getAd adg-ad))))))))
                    adg-ads)
        addops (map (fn [adg-ad]
                      (operation (operator :add)
                                 (doto (shallow-copy-ad-group-ad adg-ad)
                                   (.setAd (doto (shallow-copy-text-ad (.getAd adg-ad))
                                             (.setId nil)
                                             (.setUrl nil)
                                             (.setFinalUrls (into-array String [final-url])))))))
                    adg-ads)
        ret (do-final-url-maybe-retry-exemption service addops delops)]
    (if (not (nil? (.getPartialFailureErrors ret)))
      (println "XXX There were errors in the call to mutate (set-ads-final-url)"))
    ret))

(defn ad-group-ad-to-clojure
  [ad]
  (let [admap {
               :ad-group-id (.getAdGroupId ad)
               :ad (.getAd ad)
               }]
    (map->AdWordsAdGroupAd
     (if (= (.getAdType (.getAd ad) "TextAd"))
       (merge admap
              {
               :approval-status (.getApprovalStatus ad)
               :status (.getStatus ad)
               })
       admap))))

;; TBD refactor to pass in the page getter as a param
;; TBD See if this can be made lazy
(defn get-ad-group-ads
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
                       (get-ad-group-ad-page service selector))
              total-entries (.getTotalNumEntries page)
              ad-group-ads (.getEntries page)]
          (recur (< offset total-entries)
                 (+ offset page-size)
                 (into result ad-group-ads)))))))

;; TBD Find out if we can use a function call in another function  before the first function is defined
;; Answer: seems we have to use declare
(defn get-ad-group-ads-by-ad-group-id
  [service adgid]
  (let [fields (map ad-group-ad-field [:ad-group-id
                                       :ad-group-creative-approval-status
                                       :creative-final-urls
                                       :url
                                       :display-url
                                       :headline
                                       :description1
                                       :description2
                                       :device-preference
                                       :id
                                       :status
                                       :type
                                       :creative-tracking-url-template
                                       :creative-url-custom-parameters])
        builder (doto (SelectorBuilder.)
                  (.fields (into-array AdGroupAdField fields))
                  (.offset (int 0))
                  (.equals (ad-group-ad-field :ad-group-id) (str adgid)))
        sel (selector builder)
        ads (get-ad-group-ads service sel)]
    ads))

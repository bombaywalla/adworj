(ns adworj.account-example
  (:require [adworj.credentials :as acreds]
            [adworj.campaign :as acamp]
            [adworj.ad-group :as aadg]
            [adworj.ad-group-criterion :as aadgcrit]
            [adworj.ad-group-ad :as aadgad])
  (:import [java.lang String]
           ))

(defn walk-account
  [sess adgroup-fn]
  (println ">> Adwords account cid" (acreds/get-client-customer-id sess))
  (let [campsvc (acamp/campaign-service sess)
        adgsvc (aadg/ad-group-service sess)
        adgcritsvc (aadgcrit/ad-group-criterion-service sess)
        adgadsvc (aadgad/ad-group-ad-service sess)
        campselbuilder (acamp/selector-builder
                        (map acamp/campaign-field [:id :name
                                                   :status :serving-status
                                                   :start-date :end-date]))
        campsel (acamp/selector campselbuilder)
        campaigns (do (println "Getting all campaigns...")
                      (acamp/get-campaigns campsvc campsel))
        adgselbuilder (aadg/selector-builder
                       (map :id campaigns)
                       (map aadg/ad-group-field [:id
                                                 :campaign-id
                                                 :campaign-name
                                                 :name
                                                 :status
                                                 :tracking-url-template]))
        adgsel (aadg/selector adgselbuilder)
        adgroups (do (println "Getting all adgroups ..")
                     (aadg/get-ad-groups adgsvc adgsel))]
    (println "There are" (count campaigns) "campaigns")
    (println "There are" (count adgroups) "adgroups")
    (doseq [c campaigns]
      (println "Campaign:" (:name c) "(" (:id c) ")")
      (doseq [adg (filter #(= (:campaign-id %) (:id c)) adgroups)]
        (println "  AdGroup:" (:name adg) "(" (:id adg) ")")
          (adgroup-fn adgsvc adgadsvc adgcritsvc adg)))))

(defn walk-adgroup
  [adgsvc adgadsvc adgcritsvc adg]
  (let [adgcrits (aadgcrit/get-ad-group-criteria-by-ad-group-id adgcritsvc (:id adg))
        adgads (aadgad/get-ad-group-ads-by-ad-group-id adgadsvc (:id adg))]
    (println "    There are" (count adgcrits) "criteria")
    (doseq [adgcrit (map aadgcrit/ad-group-criterion-to-clojure adgcrits)]
      (let [crituse (.toString (:criterion-use adgcrit))
            userstatus (if (= crituse "BIDDABLE")
                         (.toString (:user-status adgcrit))
                         "N/A")
            crit (:criterion adgcrit)
            crittype (.getType crit)]
        (cond (= crittype
                 (aadgcrit/criterion-type :keyword)) (println "    Keyword ["
                                                              (.getId crit)
                                                              "]"
                                                              crituse
                                                              userstatus
                                                              (.toString (.getMatchType crit))
                                                              ":"
                                                              (.getText crit))
              :else (println "    Something other than a Keyword"))))
    (println "    There are" (count adgads) "ads")
    (doseq [adgad (map aadgad/ad-group-ad-to-clojure adgads)]
      (let [approvalstatus (.toString (:approval-status adgad))
            status (.toString (:status adgad))
            ad (:ad adgad)
            adtype (.getAdType ad)]
        (cond (= adtype "TextAd") (println "    TextAd ["
                                           (.getId ad)
                                           "]"
                                           status
                                           approvalstatus
                                           ":"
                                           (.getHeadline ad)
                                           "//"
                                           (.getDescription1 ad)
                                           "//"
                                           (.getDescription2 ad))
              :else (println "    Something other than a TextAd"))))
    ))

(defn -main
  "
Example function that walks through a whole account.

The first parameter is the name of the properties file with credentials.

The adgroup-name-starts-with-str parameter can be used to only show keywords and ads
for adgroups whose name starts with the parameter. So, setting the paramter
to \"\" will show details of all adgroups. Setting it to \"SOmeUnlikelyString\" will
prevent showing the details of all adgroups.

Either have the account id in the ads-properties
  api.adwords.clientCustomerId=123-456-7890
and do not use an optional argument
  (-main \"./ads.properties\" \"\")
Or pass it in as an optional keyword argument
  (-main \"./ads.properties\" \"\" :client-customer-id \"123-456-7890\")
  "
  [ads-properties adgroup-name-starts-with-str & sess-opts]
  (let [credentials (acreds/offline-credentials ads-properties)
        session (apply acreds/adwords-session ads-properties credentials sess-opts)]
    (walk-account session
                  (fn [adgsvc adgadsvc adgcritsvc adg]
                    (if (.startsWith (:name adg) adgroup-name-starts-with-str)
                      (walk-adgroup adgsvc adgadsvc adgcritsvc adg))))))

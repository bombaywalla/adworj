(ns adworj.targeting-idea
  (:require [adworj.credentials :as ac])
  (:import [com.google.api.ads.adwords.axis.factory AdWordsServices]
           [com.google.api.ads.adwords.axis.v201502.cm Keyword]
           [com.google.api.ads.adwords.axis.v201502.cm KeywordMatchType]
           [com.google.api.ads.adwords.axis.v201502.cm Language]
           [com.google.api.ads.adwords.axis.v201502.cm Location]
           [com.google.api.ads.adwords.axis.v201502.cm NetworkSetting]
           [com.google.api.ads.adwords.axis.v201502.cm Paging]
           [com.google.api.ads.adwords.axis.v201502.o Attribute]
           [com.google.api.ads.adwords.axis.v201502.o AttributeType]
           [com.google.api.ads.adwords.axis.v201502.o CategoryProductsAndServicesSearchParameter]
           [com.google.api.ads.adwords.axis.v201502.o CompetitionSearchParameter]
           [com.google.api.ads.adwords.axis.v201502.o CompetitionSearchParameterLevel]
           [com.google.api.ads.adwords.axis.v201502.o ExcludedKeywordSearchParameter]
           [com.google.api.ads.adwords.axis.v201502.o IdeaTextFilterSearchParameter]
           [com.google.api.ads.adwords.axis.v201502.o IdeaType]
           [com.google.api.ads.adwords.axis.v201502.o IncludeAdultContentSearchParameter]
           [com.google.api.ads.adwords.axis.v201502.o IntegerSetAttribute]
           [com.google.api.ads.adwords.axis.v201502.o LanguageSearchParameter]
           [com.google.api.ads.adwords.axis.v201502.o LocationSearchParameter]
           [com.google.api.ads.adwords.axis.v201502.o LongAttribute]
           [com.google.api.ads.adwords.axis.v201502.o LongComparisonOperation]
           [com.google.api.ads.adwords.axis.v201502.o NetworkSearchParameter]
           [com.google.api.ads.adwords.axis.v201502.o RelatedToQuerySearchParameter]
           [com.google.api.ads.adwords.axis.v201502.o RelatedToUrlSearchParameter]
           [com.google.api.ads.adwords.axis.v201502.o RequestType]
           [com.google.api.ads.adwords.axis.v201502.o SearchParameter]
           [com.google.api.ads.adwords.axis.v201502.o SearchVolumeSearchParameter]
           [com.google.api.ads.adwords.axis.v201502.o SeedAdGroupIdSearchParameter]
           [com.google.api.ads.adwords.axis.v201502.o StringAttribute]
           [com.google.api.ads.adwords.axis.v201502.o TargetingIdea]
           [com.google.api.ads.adwords.axis.v201502.o TargetingIdeaPage]
           [com.google.api.ads.adwords.axis.v201502.o TargetingIdeaSelector]
           [com.google.api.ads.adwords.axis.v201502.o TargetingIdeaServiceInterface]
           [com.google.api.ads.common.lib.utils Maps] ))

(defn targeting-idea-service
  "Create a TargetingIdeaService from an AdWords Session."
  [adwords-session]
  (let [services (AdWordsServices. )]
    (.get services adwords-session TargetingIdeaServiceInterface)))

;; The documentation appears to indicate supprot for an IdeaType/PLACEMENT as well,
;; but it does not exist in the library
(defn idea-type
  "Specify the idea type for the targeting idea."
  [type]
  (case type
    :keyword IdeaType/KEYWORD))

;; see also: translate-attribute-value later in this file
(defn attribute-type
  "Specify the attribute type for the targeting idea."
  [type]
  (case type
    :unknown AttributeType/UNKNOWN
    :category-products-and-services AttributeType/CATEGORY_PRODUCTS_AND_SERVICES
    :competition AttributeType/COMPETITION
    :criterion AttributeType/CRITERION
    :extracted-from-webpage AttributeType/EXTRACTED_FROM_WEBPAGE
    :idea-type AttributeType/IDEA_TYPE
    :keyword-text AttributeType/KEYWORD_TEXT
    :search-volume AttributeType/SEARCH_VOLUME
    :average-cpc AttributeType/AVERAGE_CPC
    :targeted-monthly-searches AttributeType/TARGETED_MONTHLY_SEARCHES))

(defn attribute-types
  "Convenience function to get a collection of attribute-type's."
  [type1 & types]
  (map attribute-type (cons type1 types)))

(defn request-type
  "Specify the request type for the targeting idea."
  [type]
  (case type
    :ideas RequestType/IDEAS
    :stats RequestType/STATS))

(defn keyword-match-type
  "Specify the match type for a keyword."
  [type]
  (case type
    :exact KeywordMatchType/EXACT
    :phrase KeywordMatchType/PHRASE
    :broad KeywordMatchType/BROAD))

(defn competition-search-parameter-level
  "Specify the level for the competition search parameter."
  [level]
  (case level
    :low CompetitionSearchParameterLevel/LOW
    :medium CompetitionSearchParameterLevel/MEDIUM
    :high CompetitionSearchParameterLevel/HIGH))

;; keyword conflicts with coljure.core/keyword, so adwords-keyword
(defn adwords-keyword
  "Create an adwords keyword object."
  [text match-type]
  (doto (Keyword.)
    (.setText text)
    (.setMatchType match-type)))

(defn network-setting
  [& {:keys [target-google-search target-search-network target-content-network target-partner-search-network]
      :or {target-google-search false target-search-network false
           target-content-network false target-partner-search-network false}}]
  (doto (NetworkSetting.)
    (.setTargetGoogleSearch target-google-search)
    (.setTargetSearchNetwork target-search-network)
    (.setTargetContentNetwork target-content-network)
    (.setTargetPartnerSearchNetwork target-partner-search-network)))

(defn long-comparison-operation
  [min max]
  (doto (LongComparisonOperation.)
    (.setMinimum min)
    (.setMaximum max)))

;; https://developers.google.com/adwords/api/docs/appendix/languagecodes
(def language-codes
  [{:name "Arabic" :code "ar" :id 1019}
   {:name "Bulgarian" :code "bg" :id 1020}
   {:name "Catalan" :code "ca" :id 1038}
   {:name "Chinese (simplified)" :code "zh_CN" :id 1017}
   {:name "Chinese (traditional)" :code "zh_TW" :id 1018}
   {:name "Croatian" :code "hr" :id 1039}
   {:name "Czech" :code "cs" :id 1021}
   {:name "Danish" :code "da" :id 1009}
   {:name "Dutch" :code "nl" :id 1010}
   {:name "English" :code "en" :id 1000}
   {:name "Estonian" :code "et" :id 1043}
   {:name "Filipino" :code "tl" :id 1042}
   {:name "Finnish" :code "fi" :id 1011}
   {:name "French" :code "fr" :id 1002}
   {:name "German" :code "de" :id 1001}
   {:name "Greek" :code "el" :id 1022}
   {:name "Hebrew" :code "iw" :id 1027}
   {:name "Hindi" :code "hi" :id 1023}
   {:name "Hungarian" :code "hu" :id 1024}
   {:name "Icelandic" :code "is" :id 1026}
   {:name "Indonesian" :code "id" :id 1025}
   {:name "Italian" :code "it" :id 1004}
   {:name "Japanese" :code "ja" :id 1005}
   {:name "Korean" :code "ko" :id 1012}
   {:name "Latvian" :code "lv" :id 1028}
   {:name "Lithuanian" :code "lt" :id 1029}
   {:name "Malay" :code "ms" :id 1102}
   {:name "Norwegian" :code "no" :id 1013}
   {:name "Persian" :code "fa" :id 1064}
   {:name "Polish" :code "pl" :id 1030}
   {:name "Portuguese" :code "pt" :id 1014}
   {:name "Romanian" :code "ro" :id 1032}
   {:name "Russian" :code "ru" :id 1031}
   {:name "Serbian" :code "sr" :id 1035}
   {:name "Slovak" :code "sk" :id 1033}
   {:name "Slovenian" :code "sl" :id 1034}
   {:name "Spanish" :code "es" :id 1003}
   {:name "Swedish" :code "sv" :id 1015}
   {:name "Thai" :code "th" :id 1044}
   {:name "Turkish" :code "tr" :id 1037}
   {:name "Ukrainian" :code "uk" :id 1036}
   {:name "Urdu" :code "ur" :id 1041}
   {:name "Vietnamese" :code "vi" :id 1040}])

;; https://developers.google.com/adwords/api/docs/appendix/geotargeting
;; TBD Do better and read in the csv
;; TBD This only deals with countries, not regions smaller than a country.
(def geo-codes
  [{:name "United States" :country-code "US"' :id 2840}
   {:name "Germany" :country-code "DE" :id 2276}
   {:name "Sweden" :country-code "SE" :id 2752}
   {:name "Italy" :country-code "IT" :id 2380}])

;; ----------- start of SearchParameter subtypes

(defn category-products-and-services-search-parameter
  [category-id]
  (doto (CategoryProductsAndServicesSearchParameter.)
    (.setCategoryId category-id)))

(defn competition-search-parameter
  [level1 & levels]
  (doto (CompetitionSearchParameter.)
    (.setLevels (into-array CompetitionSearchParameterLevel (cons level1 levels)))))

(defn excluded-keyword-search-parameter
  [kwd1 & kwds]
  (doto (ExcludedKeywordSearchParameter.)
    (.setKeywords (into-array Keyword (cons kwd1 kwds)))))

(defn idea-text-filter-search-parameter
  [& {:keys [included excluded]
      :or {included [] excluded []}}]
  (doto (IdeaTextFilterSearchParameter.)
    (.setIncluded (into-array String included))
    (.setExcluded (into-array String excluded))))

(defn include-adult-content-search-parameter
  []
  (IncludeAdultContentSearchParameter.))

(defn language-search-parameter
  [name1 & names]
  (let [langs (for [name (cons name1 names)]
                (doto (Language.)
                  (.setId (:id (or (first (filter #(= name (:name %)) language-codes))
                                   (first (filter #(= name (:code %)) language-codes))
                                   (throw (RuntimeException. "Invalid language:" name)))))))]
    (doto (LanguageSearchParameter.)
      (.setLanguages (into-array Language langs)))))

(defn location-search-parameter
  [name1 & names]
  (let [locns (for [name (cons name1 names)]
                (doto (Location.)
                  (.setId (:id (or (first (filter #(= name (:name %)) geo-codes))
                                   (first (filter #(= name (:country-code %)) geo-codes))
                                   (throw (RuntimeException. "Invalid location: " name)))))))]
    (doto (LocationSearchParameter.)
      (.setLocations (into-array Location locns)))))

(defn network-search-parameter
  [network-setting]
  (doto (NetworkSearchParameter.)
    (.setNetworkSetting network-setting)))

(defn related-to-query-search-parameter
  [query1 & queries]
  (doto (RelatedToQuerySearchParameter.)
    (.setQueries (into-array String (cons query1 queries)))))

;; TBD should we have [url1 & urls] instead?
(defn related-to-url-search-parameter
  [urls & {:keys [include-sub-urls] :or {include-sub-urls false}}]
  (doto (RelatedToUrlSearchParameter.)
    (.setQueries (into-array String urls))
    (.setIncludeSubUrls include-sub-urls)))

(defn search-volume-search-parameter
  [min max]
  (doto (SearchVolumeSearchParameter.)
    (.setOperation (long-comparison-operation min max))))

(defn seed-ad-group-id-search-parameter
  [ad-group-id]
  (doto (SeedAdGroupIdSearchParameter.)
    (.setAdGroupId ad-group-id)))

;; ----------- end of SearchParameter subtypes

(defn targeting-idea-selector
  "Create a TargetingIdeaSelector.
  Note that Paging is not passed in as a parameter."
  [search-parameters idea-type request-type
   requested-attribute-types locale-code currency-code]
  (doto (TargetingIdeaSelector.)
    (.setRequestType request-type)
    (.setIdeaType idea-type)
    (.setRequestedAttributeTypes (into-array AttributeType requested-attribute-types))
    (.setPaging (Paging.))
    (.setSearchParameters (into-array SearchParameter search-parameters))
    (.setLocaleCode locale-code)        ;https://developers.google.com/adwords/api/docs/appendix/locales
    (.setCurrencyCode currency-code))) ;https://developers.google.com/adwords/api/docs/appendix/currencycodes
  
(defn get-targeting-idea-page [service selector]
  (.get service selector))

(defn translate-attribute-value
  "Helper function to translate the value of an attribute in an idea."
  [key value]
  (if (nil? value)
    nil
    (condp =  key
      AttributeType/CATEGORY_PRODUCTS_AND_SERVICES (into [] value)
      AttributeType/AVERAGE_CPC (.getMicroAmount value)
      AttributeType/TARGETED_MONTHLY_SEARCHES (into [] (map (fn [x] {:year (.getYear x)
                                                                     :month (.getMonth x)
                                                                     :count (.getCount x)}) value))
      value)))

(defn translate-idea-to-clojure
  "Helper function to convert an Idea to a Clojure Map."
  [idea]
  (let [attrmap (Maps/toMap (.getData idea))]
    (into {}
          (for [[k v] attrmap]
            [k (translate-attribute-value  k (.getValue v))]))))

(defn get-ideas
  "Returns a vector of ideas. Each idea is a map of attributes and values."
  [service selector]
  (let [page-size 100
        paging (doto (.getPaging selector) ;Note sideeffects Paging in the selector
                 (.setNumberResults (int page-size)))]
    (loop [more-pages true
           offset 0
           result []]
      (if-not more-pages
        result
        (let [page (do (.setStartIndex paging (int offset))
                       (get-targeting-idea-page service selector))
              total-entries (.getTotalNumEntries page)
              ideas (.getEntries page)]
          (recur (< offset total-entries)
                 (+ offset page-size)
                 (into result (map translate-idea-to-clojure ideas))))))))

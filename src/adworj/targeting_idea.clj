(ns adworj.targeting-idea
  (:require [adworj.credentials :as ac])
  (:import [com.google.api.ads.adwords.axis.factory AdWordsServices]
           [com.google.api.ads.adwords.axis.v201502.cm Language]
           [com.google.api.ads.adwords.axis.v201502.cm Paging]
           [com.google.api.ads.adwords.axis.v201502.o Attribute]
           [com.google.api.ads.adwords.axis.v201502.o AttributeType]
           [com.google.api.ads.adwords.axis.v201502.o IdeaType]
           [com.google.api.ads.adwords.axis.v201502.o IntegerSetAttribute]
           [com.google.api.ads.adwords.axis.v201502.o LanguageSearchParameter]
           [com.google.api.ads.adwords.axis.v201502.o LongAttribute]
           [com.google.api.ads.adwords.axis.v201502.o RelatedToQuerySearchParameter]
           [com.google.api.ads.adwords.axis.v201502.o RequestType]
           [com.google.api.ads.adwords.axis.v201502.o SearchParameter]
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

(defn create-targeting-idea-selector
  "Create a TargetingIdeaSelector."
  [] (TargetingIdeaSelector.)
  )

  
(defn create-keyword-query-selector
  "Create a TargetingIdeaSelector for keyword queries."
    [keyphrase language]
  (let [req-attrs (into-array AttributeType
                              [AttributeType/KEYWORD_TEXT
                               AttributeType/SEARCH_VOLUME
                               AttributeType/CATEGORY_PRODUCTS_AND_SERVICES])
        paging (doto (Paging.)
                 (.setStartIndex (int 0))
                 (.setNumberResults (int 40)))
        query-param (doto (RelatedToQuerySearchParameter.)
                      (.setQueries (into-array String [keyphrase])))
        english (doto (Language.)       ;TBD use the language parameter
                  (.setId 1000))
        language-param (doto (LanguageSearchParameter.)
                         (.setLanguages (into-array Language [english])))
        search-params (into-array SearchParameter [query-param language-param]) ]
    
        (doto (TargetingIdeaSelector.)
          (.setRequestType RequestType/IDEAS)
          (.setIdeaType IdeaType/KEYWORD)
          (.setRequestedAttributeTypes req-attrs)
          (.setPaging paging)
          (.setSearchParameters search-params))))

(defn get-targeting-idea-page [service selector]
  (.get service selector))

(defn process-page [page process-idea]
  (for [idea (.getEntries page)]
    (process-idea idea)))

(defn print-idea [idea]
  (let [attrmap (Maps/toMap (.getData idea))
        keyword (.getValue (get attrmap AttributeType/KEYWORD_TEXT))
        searchvol (.getValue (get attrmap AttributeType/SEARCH_VOLUME))
        catidset (set (.getValue (get attrmap AttributeType/CATEGORY_PRODUCTS_AND_SERVICES)))]
    (printf "The keyword is '%s'\n" keyword)
    (printf "The search volume is '%d'\n" searchvol)
    (printf "The category ids are:")
    (doseq [id catidset]
      (printf " %d" id))
    (println)))

(defn get-keyphrase [idea]
  (let [attrmap (Maps/toMap (.getData idea))
        keyword (.getValue (get attrmap AttributeType/KEYWORD_TEXT))
        searchvol (.getValue (get attrmap AttributeType/SEARCH_VOLUME))
        catidset (set (.getValue (get attrmap AttributeType/CATEGORY_PRODUCTS_AND_SERVICES)))]
    keyword))

(ns clunogs.core
  (:require [clj-http.client :as client]
            [cheshire.core :as cheshire]
            [clojure.string :as string])
  (:import [org.apache.commons.lang3 StringEscapeUtils]))

;; Bind the api url and default headers options to be used in api calls.

(def ^:dynamic *api-url*
  "uNoGS api endpoint url.

  Can be rebound for a set of calls using the `with-api-url` macro."
  "https://unogs-unogs-v1.p.rapidapi.com/aaapi.cgi")

(def ^:dynamic *headers*
  "Default headers map sent with all api requests.

  Can be rebound for a set of calls using the `with-headers` macro.
  `set-api-key` can be used to add your api key to the headers."
  {:accept "application/json"
   :x-rapidapi-key nil})

(defn set-api-key [api-key]
  "Binds `api-key` in `*headers*` to be used as the default api key."
  (def ^:dynamic *headers*
    (merge *headers* {:x-rapidapi-key api-key})))

;; helper functions

(defn- unescape-html [s]
  "Parse html escapes (ex. '&amp;' -> '&') in string `s`."
  (StringEscapeUtils/unescapeHtml4 s))

(defn- parse-int [s]
  "Parse the first integer contained in string `s`."
  (Integer/parseInt (re-find #"\A-?\d+" s)))

(defn- string->kebab [s]
  "Transorm string `s` into a lowercase, kebob style keyword."
  (-> s
      string/lower-case
      (string/replace #"\s+" "-")
      keyword))

(defn- map-keywordize-kebab-keys [map]
  (into {} (for [[k v] map] [(string->kebab k) v])))

(defn- parse-response
  "Extract and parse the body and headers of the `response` map."
  [response]
  (-> response
      (update :body #(-> % unescape-html (cheshire/parse-string string->kebab)))
      (update :headers map-keywordize-kebab-keys)))

(defn- filter-nil [map]
  (into {} (filter (comp some? val) map)))

;; Dispatch to format the query string for different request types

(defmulti format-query :type)

(defmethod format-query :title [query-map]
  (:title query-map))

(defmethod format-query :netflixid [query-map]
  (:netflixid query-map))

(defmethod format-query :filmid [query-map]
  (:filmid query-map))

(defmethod format-query :imdbid [query-map]
  (:imdbid query-map))

(defmethod format-query :expiring [query-map]
  (str "get:exp:" (:countryid query-map)))

(defmethod format-query :list-countries [query-map]
  (if (:available query-map) "available" "all"))

(defmethod format-query :new-releases [query-map]
  (str "get:new" (:daysback query-map)
       (when (:countryid query-map)
         (str ":" (:countryid query-map)))))

(defmethod format-query :season-changes [query-map]
  (str "get:seasons" (:daysback query-map)
       (when (:countryid query-map)
         (str ":" (:countryid query-map)))))

(defmethod format-query :advanced-search [query-map]
  (str (if (empty? (:query query-map))
         ""
         (format-query (:query query-map)))
       "-!" (:syear query-map) "," (:eyear query-map )
       "-!" (:snfrate query-map) "," (:enfrate query-map )
       "-!" (:simdbrate query-map) "," (:eimdbrate query-map )
       "-!" (:genreid query-map)
       "-!" (:vtype query-map)
       "-!" (:audio query-map)
       "-!" (:subtitle query-map)
       "-!" (:votes-gt-lt query-map) (:imdbvotes query-map)
       "-!" (:downloadable query-map)))

;; Api call wrapper

(defn api-call
  "Perform the specified `method` call to the api.

  If this function is not provided values for `:headers` or `:url` will use the
  default global values `*headers*` and `*api-url*` respectively."
  [method & {:keys [query-params api-key headers url] :or {headers *headers* url *api-url*}}]
  (let [headers (merge headers (when api-key {:x-rapidapi-key api-key}))]
    (parse-response (client/request {:method method
                                     :url url
                                     :headers headers
                                     :query-params (filter-nil query-params)}))))

;; api interface

(defn deleted
  "Query the api for items deleted during `daysback`.
  Optionally, search for a specific `:title` and/or in a specific
  `:countryid`.

  This request has a special case, where its `:countryid` can either be the
  standard, two letter country ID, or it can be a collection of numerical
  country codes found by calling `list-countries`.

  This query returns every item that matches the criteria, so there is no need
  to supply a `:page` value.

  An `:api-key` can be passed to this function, overwriting the api key
  specified globally via the set-api-key function.

  uNoGS documentation: https://rapidapi.com/unogs/api/unogs?endpoint=5c23d6cee4b067d7d9563e18"
  [daysback & {:keys [countryid title api-key]}]
  (let [params {:t "deleted"
                :st daysback
                :cl (if (coll? countryid) (string/join "," countryid) countryid)
                :q (format-query {:type :title
                                  :title title})}]
    (api-call :get :query-params params :api-key api-key)))

(defn expiring
  "Query the api for items expiring in `countryid`.
  Up to 100 results will be returned. Additional items can be accessed by
  providing `:page` greater than 1.

  An `:api-key` can be passed to this function, overwriting the api key
  specified globally via the set-api-key function.

  uNoGS documentaion: https://rapidapi.com/unogs/api/unogs?endpoint=56757595e4b04bcce8ec15ad"
  [countryid & {:keys [page api-key] :or {page 1}}]
  (let [params {:q (format-query {:type :expiring
                                  :countryid countryid})
                :t "ns"
                :st "adv"
                :p page}]
    (api-call :get :query-params params :api-key api-key)))

(defn list-countries
  "Query the api for a list of countries.
  By default, it will return a list of countries where uNoGS information is
  actively updated. By providing `:available false`, it will return a list of
  countries where uNoGS is actively updated and countries that were previously
  cataloged.

  An `:api-key` can be passed to this function, overwriting the api key
  specified globally via the set-api-key function.

  uNogs documentation: https://rapidapi.com/unogs/api/unogs?endpoint=56770144e4b0c2a9f0e83c8e"
  [& {:keys [available api-key] :or {available true}}]
  (let [params {:t "lc"
                :q (format-query {:type :list-countries
                                  :available available})}]
    (api-call :get :query-params params :api-key api-key)))

(defn new-releases
  "Query the api for new releases during `daysback` in `countryid`.
  Up to 100 results will be returned. Additional items can be accessed by
  providing `:page` greater than 1.

  An `:api-key` can be passed to this function, overwriting the api key
  specified globally via the set-api-key function.

  uNoGS documentation: https://rapidapi.com/unogs/api/unogs?endpoint=5675480ee4b059e645dc3a63"
  [daysback countryid & {:keys [page api-key] :or {page 1}}]
  (let [params {:q (format-query {:type :new-releases
                                  :daysback daysback
                                  :countryid countryid})
                :t "ns"
                :st "adv"
                :p page}]
    (api-call :get :query-params params :api-key api-key)))

(defn season-changes
  "Query the api for changes to TV series seasons during `daysback` in `countryid`.
  Up to 100 results will be returned. Additional items can be accessed by
  providing `:page` greater than 1.

  An `:api-key` can be passed to this function, overwriting the api key
  specified globally via the set-api-key function.

  uNoGS documentation: https://rapidapi.com/unogs/api/unogs?endpoint=56c4b2c5e4b07732012a3315"
  [daysback countryid & {:keys [page api-key] :or {page 1}}]
  (let [params {:q (format-query {:type :season-changes
                                  :daysback daysback
                                  :countryid countryid})
                :t "ns"
                :st "adv"
                :p page}]
    (api-call :get :query-params params :api-key api-key)))

(defn advanced-search
  "Query the api for results that match an advanced search filter.
  The following parameters are accepted:

  * `:query` - An optional map that specifies the type of search. If not
  provided the query will return all items that match the other supplied
  parameters. Each map must contain a `:type` key with one of the following
  values:   

    1. `:title` - Contains `:title` that specifies a title string to search for.
      * Ex. `{:type :title, :title \"Bird Box\"}`
    2. `:new-releases` - Contains `:daysback`.
      * Ex. `{:type :new-releases, :daysback 7}`
    3. `:season-changes` - Contains `:daysback`.
      * Ex. `{:type :season-changes, :daysback 10}`
    4. `:expiring` - Contains `:countryid`.
      * Ex. `{:type :expiring, :countryid \"US\"}`

  * `:clist` - A single numerical country id, a vector of then, or \"all\". Note
  that two letter country codes (ex. US) are not valid here. For a list of
  corresponding numerical and letter ids call `list-countries`.
  * `:genreid` - A numerical genre id. Call `genre-ids` for a list.
  * `:vtype` - \"Any\", \"Movie\" or \"Series\".
  * `:audio` - An audio langugage. Ex. \"English\" or \"Chinese\".
  * `:subtitle` - A subtitle language. Ex. \"English\" or \"Chinese\".
  * `:audio-sub-andor` - \"and\" or \"or\". Determines whether an item must have
  both the audio and subtitle languages match, or either of them match.
  * `:simdbrate` - Minimum imdb rating. An integer in the range of 0 to 10.
  * `:eimdbrate` - Maximum imdb rating. An integer in the range of 0 to 10.
  * `:imdbvotes` - A number of votes for an item to have recieved on IMDB. Can
  either be tested against as a floor or ceiling based on the value of
  `:votes-gt-lt`.
  * `:votes-gt-lt` - \"gt\" or \"lt\". Determines whether the value of
  `:imdbvotes` is tested as a floor or ceiling.
  * `:snfrate` - Minimum netflix rating. An integer in the range of 0 to 5.
  * `:enfrate` - Maximum netflix rating. An integer in the range of 0 to 5.
  * `:syear` - The minimum release year.
  * `:eyear` - The maximum release year.
  * `:downloadable` - An optional value. \"Yes\", \"No\" or unspecified.
  * `:sortby` - Sort results by \"Relevence\", \"Date\", \"Rating\", \"Title\",
  \"VideoType\", \"FilmYear\", or \"Runtime\".
  * `:page` - An integer 1 or greater. Groups of up to 100 results will be
  returned and this value will page through them.
  
  As almost all of the above parameters are required, so the most inclusive values
  are used by default.

  An `:api-key` can be passed to this function, overwriting the api key
  specified globally via the set-api-key function.

  uNoGS documentation: https://rapidapi.com/unogs/api/unogs?endpoint=5690bcdee4b0e203818a6518"
  [& {:keys [query clist genreid vtype audio subtitle audio-sub-andor simdbrate
             eimdbrate imdbvotes votes-gt-lt snfrate enfrate syear eyear
             downloadable sortby page api-key]
      :or {query ""
           clist "all"
           genreid 0
           vtype "Any"
           audio "Any"
           subtitle "Any"
           audio-sub-andor "and"
           simdbrate 0
           eimdbrate 10
           imdbvotes 0
           votes-gt-lt "gt"
           snfrate 0
           enfrate 5
           syear 1900
           eyear 3000 ; Remember to update in 1000 years
           downloadable nil
           sortby "Relevance"
           page 1}}]
  (let [params {:q (format-query {:type :advanced-search
                                  :query query
                                  :genreid genreid
                                  :vtype vtype
                                  :audio audio
                                  :subtitle subtitle
                                  :audio-sub-andor audio-sub-andor
                                  :simdbrate simdbrate
                                  :eimdbrate eimdbrate
                                  :imdbvotes imdbvotes
                                  :votes-gt-lt votes-gt-lt
                                  :snfrate snfrate
                                  :enfrate enfrate
                                  :syear syear
                                  :eyear eyear
                                  :downloadable downloadable})
                :t "ns"
                :cl (if (coll? clist) (string/join "," clist) clist)
                :ob sortby
                :sa audio-sub-andor
                :st "adv"
                :p page}]
    (api-call :get :query-params params :api-key api-key)))

(defn genre-ids
  "Query the api for a map of genres and corresponding ids.

  The output of this function is slightly transformed from the response format.
  The generes are given as a vector where each element is a map containing a
  single pairing of a genre key to a vector of its genre ids. For your
  convenience, this is flattened into a single map of genre keys to their genre
  ids.

  An `:api-key` can be passed to this function, overwriting the api key
  specified globally via the set-api-key function.

  uNoGS documentation: https://rapidapi.com/unogs/api/unogs?endpoint=5676f219e4b04efee9356e43"
  [& {:keys [api-key]}]
  (let [params {:t "genres"}
        response (api-call :get :query-params params :api-key api-key)]
    (assoc-in response [:body :items] (into {} (get-in response [:body :items])))))

(defn images
  "Query the api for images corresponding to `netflixid`.

  An `:api-key` can be passed to this function, overwriting the api key
  specified globally via the set-api-key function.

  uNoGS documentation: https://rapidapi.com/unogs/api/unogs?endpoint=57304268e4b002251bb1798d"
  [netflixid & {:keys [api-key]}]
  (let [params {:t "images"
                :q (format-query {:type :netflixid :netflixid netflixid})}]
    (api-call :get :query-params params :api-key api-key)))

(defn episode-details
  "Query the api for episode details of `netflixid`.

  An `:api-key` can be passed to this function, overwriting the api key
  specified globally via the set-api-key function.

  uNoGS documentation: https://rapidapi.com/unogs/api/unogs?endpoint=56770f5ae4b059e645dc3d57"
  [netflixid & {:keys [api-key]}]
  (let [params {:q (format-query {:type :netflixid :netflixid netflixid})
                :t "episodes"}]
    (api-call :get :query-params params :api-key api-key)))

(defn title-details
  "Query the api for title details of `netflixid`.

  An `:api-key` can be passed to this function, overwriting the api key
  specified globally via the set-api-key function.

  uNoGS documentation: https://rapidapi.com/unogs/api/unogs?endpoint=56770d2de4b04efee9356e62"
  [netflixid & {:keys [api-key]}]
  (let [params {:q (format-query {:type :netflixid :netflixid netflixid})
                :t "loadvideo"}]
    (api-call :get :query-params params :api-key api-key)))

(defn imdb-info
  "Query the api for imdb info of `filmid` (a netflix or imdb id).

  An `:api-key` can be passed to this function, overwriting the api key
  specified globally via the set-api-key function.

  uNoGS documentation: https://rapidapi.com/unogs/api/unogs?endpoint=5ab02664e4b06ec3937b50a2"
  [filmid & {:keys [api-key]}]
  (let [params {:q (format-query {:type :filmid :filmid filmid})
                :t "getimdb"}]
    (api-call :get :query-params params :api-key api-key)))

(defn imdb-update
  "Query the api to update the imdb info of `imdbid`.

  An `:api-key` can be passed to this function, overwriting the api key
  specified globally via the set-api-key function.

  uNoGS documentation: https://rapidapi.com/unogs/api/unogs?endpoint=58179d9ee4b0c681e4af7b08"
  [imdbid & {:keys [api-key]}]
  (let [params {:q (format-query {:type :imdbid :imdbid imdbid})
                :t "imdb"}]
    (api-call :get :query-params params :api-key api-key)))

(defn weekly-episodes
  "Query the api for a list of netflix title id's with weekly added episodes.

  An `:api-key` can be passed to this function, overwriting the api key
  specified globally via the set-api-key function.

  uNoGS documentation: https://rapidapi.com/unogs/api/unogs?endpoint=570212a0e4b028dd93816233"
  [& {:keys [api-key]}]
  (let [params {:t "weekly"}]
    (api-call :get :query-params params :api-key api-key)))

(defn weekly-updates
  "Query the api for for a list of titles with series that update weekly within
  `daysback`.

  Optionally, a specific title or country can be searched by supplying `:title`
  or `:countryid` respectively.

  This request contains a special case, where `:countryid` can either be the
  standard, two letter country ID, or it can be given as a collection of them.

  An `:api-key` can be passed to this function, overwriting the api key
  specified globally via the set-api-key function.

  uNoGS documentation: https://rapidapi.com/unogs/api/unogs?endpoint=5c617d57e4b08cf00f3fd3ea"
  [daysback & {:keys [title countryid api-key]}]
  (let [params {:q (format-query {:type :title :title title})
                :t "weeklynew"
                :st daysback
                :cl (if (coll? countryid) (string/join "," countryid) countryid)}]
    (api-call :get :query-params params :api-key api-key)))

;; Convenience Macros

(defmacro with-api-url 
  "Rebind `*api-url*` with `new-api-url` in `body`."
  [new-api-url & body]
  `(binding [*api-url* ~new-api-url]
     ~@body))

(defmacro with-headers 
  "Rebind `*headers*` with `new-headers` in `body`."
  [new-headers & body]
  `(binding [*headers* ~new-headers]
     ~@body))

(defmacro all-pages
  "Take an api call `f` and call it for each page to aggregate all items.

  This macro does not take into account the possibility of a change in the
  number of items in between api calls effecting the number of pages required to
  fetch all items."
  [f]
  `(let [init-query# (~@f :page 1)
         {init-count# :count init-items# :items} (:body init-query#)
         pages# (range 2 (inc (/ (#'clunogs.core/parse-int init-count#) (count init-items#))))
         total# (-> (reduce (fn [acc# page#]
                              (update-in (~@f :page page#)
                                         [:body :items]
                                         concat
                                         (get-in acc# [:body :items])))
                            init-query#
                            pages#))]
     (assoc-in total# [:body :count] (str (count (get-in total# [:body :items]))))))

(ns clunogs.core-test
  (:require [clojure.test :refer :all]
            [clunogs.core :refer :all]
            [cheshire.core :as cheshire]
            [clj-http.fake :refer [with-fake-routes with-fake-routes-in-isolation]])
  (:import [org.apache.commons.lang3 StringEscapeUtils]))

;; support functions and macros

(defmacro request-query [request query-params]
  `(with-fake-routes-in-isolation
     {{:address #".*" :query-params ~query-params}
      (fn [req#] {:status 200})}
     ~request))

(defmacro test-api-keys [request]
  `(testing (str "Methods of setting api-key for " (first '~request))
     (with-fake-routes-in-isolation
       {#".*" (fn [req#] {:status 200 :api-key (get-in req# [:headers "X-Rapidapi-Key"])})}
       ;; Argument based key
       (is (= "argument" (:api-key (~@request :api-key "argument"))))
       ;; Rebinding key with macro
       (is (= "macro" (with-headers {:x-rapidapi-key "macro"} (:api-key ~request))))
       ;; Setting key globally (and resetting it)
       (let [prev-key# (:x-rapidapi-key *headers*)]
         (set-api-key "global")
         (is (= "global" (:api-key ~request)))
         (set-api-key prev-key#))
       ;; ensure argument takes precidence
       (is (= "argument" (with-headers {:x-rapidapi-key "macro"} (:api-key (~@request :api-key "argument"))))))))

;; tests

(deftest api-call-test
  (testing "Arguments result in correct behavior"
    (is (with-fake-routes-in-isolation
          {{:address "http://example.org" :query-params {:a "b" :c "d"}}
           {:get (fn [req] {:status 200 :body (cheshire/encode req)})}}
          (let [req (:body (api-call :get
                                     :query-params {:a "b" :c "d" :d nil}
                                     :api-key "test-key"
                                     :headers {:f "g" :x-rapidapi-key "overwritten"}
                                     :url "http://example.org"))]
            (is (= (:headers req) {:f "g" :x-rapidapi-key "test-key"
                                   ;; clj-http default
                                   :accept-encoding "gzip, deflate"}))))))
  (testing "Throw on exceptional http status"
    (is (thrown? Exception (with-fake-routes-in-isolation
                            {#".*" (fn [_] {:status 400})}
                            (api-call :get :url "http://example.org")))))
  (testing "Parsing"
    (with-fake-routes-in-isolation
      {#".*" (fn [_] {:status 200
                      :headers {"test" "a" "To Kebab" "not kebabed"}  
                      :body (cheshire/encode {:count "2"
                                              :items {"also kebab" ["a" "b" "c"]
                                                      "1 &amp; 2" "&#60; a&b &#62;"}})})}
      (is (= (select-keys (api-call :get :url "http://example.org") [:body :headers])
              {:body {:count "2", :items {:also-kebab ["a" "b" "c"], :1-&-2 "< a&b >"}}
               :headers {:test "a", :to-kebab "not kebabed"}})))
    (test-api-keys (api-call :get :url "http://example.org"))))

;; request tests

(deftest deleted-test
  (testing "Query strings match expected values."
    (is (request-query (deleted 1) {:t "deleted" :st "1"}))
    (is (request-query (deleted 1 :countryid "US") {:t "deleted" :st "1" :cl "US"}))
    (is (request-query (deleted 1 :countryid [78 46]) {:t "deleted" :st "1" :cl "78,46"}))
    (is (request-query (deleted 1 :title "test movie") {:t "deleted" :st "1" :q "test movie"}))
    (is (thrown? Exception (request-query (deleted 1 :countryid "US") {:t "deleted" :st "1" :cl "wrong"}))))
  (test-api-keys (deleted 1)))


(deftest expiring-test
  (testing "Query strings match expected values."
    (is (request-query (expiring "US") {:q "get:exp:US" :t "ns" :st "adv" :p "1"}))
    (is (request-query (expiring "US" :page 2) {:q "get:exp:US" :t "ns" :st "adv" :p "2"}))
    (is (thrown? Exception (request-query (expiring "UK") {:q "get:exp:US" :t "ns" :st "adv" :p "1"}))))
  (test-api-keys (expiring "US")))

(deftest list-countries-test
  (testing "Query strings match expected values."
    (is (request-query (list-countries) {:q "available" :t "lc"}))
    (is (request-query (list-countries :available false) {:q "all" :t "lc"}))
    (is (thrown? Exception (request-query (list-countries) {:q "available" :t "lc" :p "1"}))))
  (test-api-keys (list-countries)))

(deftest new-releases-test
  (testing "Query strings match expected values."
    (is (request-query (new-releases 10 "US") {:q "get:new10:US" :t "ns" :st "adv" :p "1"}))
    (is (request-query (new-releases 7 "CA" :page 2) {:q "get:new7:CA" :t "ns" :st "adv" :p "2"})))
  (test-api-keys (new-releases 10 "US")))

(deftest season-changes-test
  (testing "Query strings match expected values."
    (is (request-query (season-changes 10 "US") {:q "get:seasons10:US" :t "ns" :st "adv" :p "1"}))
    (is (request-query (season-changes 7 "CA" :page 2) {:q "get:seasons7:CA" :t "ns" :st "adv" :p "2"})))
  (test-api-keys (season-changes 10 "US")))

(deftest advanced-search-test
  (testing "Query strings match expected values."
    ;; test defaults
    (is (request-query (advanced-search)
                       {:q "-!1900,3000-!0,5-!0,10-!0-!Any-!Any-!Any-!gt0-!"
                        :t "ns"
                        :cl "all"
                        :ob "Relevance"
                        :sa "and"
                        :st "adv"
                        :p "1"}))
    ;; test all query types
    (is (request-query (advanced-search :query {:type :title :title "abcd"})
                       {:q "abcd-!1900,3000-!0,5-!0,10-!0-!Any-!Any-!Any-!gt0-!"
                        :t "ns" :cl "all" :ob "Relevance" :sa "and" :st "adv" :p "1"}))
    (is (request-query (advanced-search :query {:type :new-releases :daysback 7})
                       {:q "get:new7-!1900,3000-!0,5-!0,10-!0-!Any-!Any-!Any-!gt0-!"
                        :t "ns" :cl "all" :ob "Relevance" :sa "and" :st "adv" :p "1"}))
    (is (request-query (advanced-search :query {:type :season-changes :daysback 10})
                       {:q "get:seasons10-!1900,3000-!0,5-!0,10-!0-!Any-!Any-!Any-!gt0-!"
                        :t "ns" :cl "all" :ob "Relevance" :sa "and" :st "adv" :p "1"}))
    (is (request-query (advanced-search :query {:type :season-changes :daysback 10})
                       {:q "get:seasons10-!1900,3000-!0,5-!0,10-!0-!Any-!Any-!Any-!gt0-!"
                        :t "ns" :cl "all" :ob "Relevance" :sa "and" :st "adv" :p "1"}))
    (is (request-query (advanced-search :query {:type :expiring :countryid "US"})
                       {:q "get:exp:US-!1900,3000-!0,5-!0,10-!0-!Any-!Any-!Any-!gt0-!"
                        :t "ns" :cl "all" :ob "Relevance" :sa "and" :st "adv" :p "1"}))
    (is (thrown? Exception (request-query (advanced-search :query {:type :doesnotexist})
                        {:q "-!1900,3000-!0,5-!0,10-!0-!Any-!Any-!Any-!gt0-!"
                         :t "ns" :cl "all" :ob "Relevance" :sa "and" :st "adv" :p "1"})))
    ;; test setting all params
    (is (request-query (advanced-search :clist 78
                                        :genreid 11
                                        :vtype "Movie"
                                        :audio "Chinese"
                                        :subtitle "Spanish"
                                        :audio-sub-andor "or"
                                        :simdbrate 1
                                        :eimdbrate 9
                                        :imdbvotes "1000"
                                        :votes-gt-lt "lt"
                                        :snfrate 2
                                        :enfrate "3"
                                        :syear 2000
                                        :eyear "3500"
                                        :downloadable "Yes"
                                        :sortby "Date"
                                        :page 8)
                       {:q "-!2000,3500-!2,3-!1,9-!11-!Movie-!Chinese-!Spanish-!lt1000-!Yes"
                        :t "ns"
                        :cl "78"
                        :ob "Date"
                        :sa "or"
                        :st "adv"
                        :p "8"}))
    ;; test clist
    (is (request-query (advanced-search :clist 78)
                       {:q "-!1900,3000-!0,5-!0,10-!0-!Any-!Any-!Any-!gt0-!"
                        :t "ns" :ob "Relevance" :sa "and" :st "adv" :p "1"
                        :cl "78"}))
    (is (request-query (advanced-search :clist [78 46])
                       {:q "-!1900,3000-!0,5-!0,10-!0-!Any-!Any-!Any-!gt0-!"
                        :t "ns" :ob "Relevance" :sa "and" :st "adv" :p "1"
                        :cl "78,46"}))
    )
  (test-api-keys (advanced-search)))

(deftest genre-ids-test
  (testing "Query strings match expected values."
    (is (request-query (genre-ids) {:t "genres"})))
  (testing "Reformat body into single map"
    (with-fake-routes-in-isolation
      {#".*" (fn [_] {:status 200 :body (cheshire/encode {:items [{:a "1"} {:b "2"} {"c" "3"}]})})}
      (is (= (:body (genre-ids))
             {:items {:a "1" :b "2" :c "3"}}))))
  (test-api-keys (genre-ids)))

(deftest images-test
  (testing "Query strings match expected values."
    (is (request-query (images "80196789") {:t "images" :q "80196789"}))
    (is (request-query (images 80196789) {:t "images" :q "80196789"})))
  (test-api-keys (images 80196789)))

(deftest episode-details-test
  (testing "Query strings match expected values."
    (is (request-query (episode-details  "80117540") {:t "episodes" :q  "80117540"}))
    (is (request-query (episode-details  80117540) {:t "episodes" :q  "80117540"})))
  (test-api-keys (episode-details   "80117540")))

(deftest title-details-test
  (testing "Query strings match expected values."
    (is (request-query (title-details "80196789") {:t "loadvideo" :q "80196789"}))
    (is (request-query (title-details 80196789) {:t "loadvideo" :q "80196789"})))
  (test-api-keys (title-details 80196789)))

(deftest imdb-info-test
  (testing "Query strings match expected values."
    (is (request-query (imdb-info "80196789") {:t "getimdb" :q "80196789"}))
    (is (request-query (imdb-info 80196789) {:t "getimdb" :q "80196789"})))
    (is (request-query (imdb-info "tt2737304") {:t "getimdb" :q "tt2737304"}))
  (test-api-keys (imdb-info 80196789)))

(deftest imdb-update-test
  (testing "Query strings match expected values."
    (is (request-query (imdb-update "tt2737304") {:t "imdb" :q "tt2737304"})))
  (test-api-keys (imdb-update "tt2737304")))

(deftest weekly-episodes-test
  (testing "Query strings match expected values."
    (is (request-query (weekly-episodes) {:t "weekly"})))
  (test-api-keys (weekly-episodes)))

(deftest weekly-updates-test
  (testing "Query strings match expected values."
    (is (request-query (weekly-updates 10) {:t "weeklynew" :st "10"}))
    (is (request-query (weekly-updates 12 :title "test") {:t "weeklynew" :st "12" :q "test"})))
  (test-api-keys (weekly-updates 10)))

(deftest all-pages-test
  (testing "all pages are correctly fetched"
    (with-fake-routes-in-isolation
      {#".*" (fn [req] {:status 200
                        :body (cheshire/encode
                               {:items (list (second (re-find #"p=(\d+)" (:query-string req))))
                                :count "5"})})}
      (is (= (:body (all-pages (expiring "US")))
             {:items (list "5" "4" "3" "2" "1")
              :count "5"})))))

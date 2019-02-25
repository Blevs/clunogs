<a id="org7e3a22c"></a>

# Using clunogs

To begin, you must require the `clunogs.core` namespace:

    (ns example
      (:require [clunogs.core :as clunogs]))

# Table of Contents

1.  [Using clunogs](#org7e3a22c)
    1.  [API key management](#org32fcaa6)
    2.  [Output](#orgfb075f9)
        1.  [Exceptions](#orgfd41b80)
    3.  [Arguments](#org981b34b)
        1.  [Country ID](#org2a5b04f)
        2.  [`clist`](#org0e3a8e0)
    4.  [Request Examples](#org32748be)
        1.  [deleted](#orgda0ae85)
        2.  [expiring](#orgc36180d)
        3.  [list-countries](#org7ba0f58)
        4.  [new-releases](#org68e1862)
        5.  [season-changes](#org0c82481)
        6.  [advanced-search](#org3f514af)
        7.  [genre-ids](#org33aeedf)
        8.  [images](#org6c2e224)
        9.  [episode-details](#orgea7f335)
        10. [title-details](#orgf4b0450)
        11. [imdb-info](#orgdb9489e)
        12. [imdb-update](#orgc93779b)
        13. [weekly-episodes](#orgffd9d63)
        14. [weekly-updates](#orgd9d2bda)
        15. [all-pages](#org4903d9e)

<a id="org32fcaa6"></a>

## API key management

clunogs is not opinionated about how you use and store your API key. There
are three methods of including a key with an API request.

The first, and most straightforward, is to pass it as the keyword argument
`:api-key` to any of the request functions:

    (clunogs/new-releases 1 "US" :api-key "secret-key")

    {:cached nil,
     :request-time 837,
     :repeatable? false,
     :protocol-version {:name "HTTP", :major 1, :minor 1},
     :streaming? true,
     :http-client
     #object[org.apache.http.impl.client.InternalHttpClient 0xd59490a "org.apache.http.impl.client.InternalHttpClient@d59490a"],
     :chunked? false,
     :reason-phrase "OK",
     :headers
     {:access-control-allow-origin "*",
      :content-type "application/json; charset=ISO-8859-1",
      :date "Fri, 22 Feb 2019 23:22:10 GMT",
      :server "RapidAPI-1.0.6",
      :x-ratelimit-requests-limit "100",
      :x-ratelimit-requests-remaining "84",
      :content-length "4189",
      :connection "Close"},
     :orig-content-encoding nil,
     :status 200,
     :length 4189,
     :body
     {:count "1",
      :items
      [{:largeimage "",
        :type "movie",
        :title "Trespass Against Us",
        :released "2016",
        :runtime "1h39m",
        :image
        "https://occ-0-3451-3446.1.nflxso.net/art/8ad00/bc36395deb410871cda447f046ac7c1aed28ad00.jpg",
        :unogsdate "2019-02-22",
        :synopsis
        "A man from a criminal family yearns to break away and find a better life, but his father's staunch opposition puts his dreams of freedom in jeopardy.",
        :download "0",
        :imdbid "tt3305308",
        :rating "5.8",
        :netflixid "80057509"}]},
     :trace-redirects []}

The API can also be set globally to avoid passing it to every request:

    (clunogs/set-api-key "secret-key")
    (clunogs/new-releases 1 "US")

The `set-api-key` function rebinds the `*headers*` variable with the
`:x-rapidapi-key` field as your key.

The third and final method is to use a convenience macro to rebind `*headers*`
for a body of requests:

    (clunogs/with-headers
      {:accept "application/json"
       :x-rapidapi-key "secret-key"}
       (clunogs/new-releases 1 "US"))

Passing the key as an argument will override the other settings, using the
macro will override the global value, and the global value acts as the
default.

For the following examples the API key will be set globally to increase
compactness.


<a id="orgfb075f9"></a>

## Output

Let's examine the output of a request:

    (clunogs/new-releases 1 "US")

    {:cached nil,
     :request-time 837,
     :repeatable? false,
     :protocol-version {:name "HTTP", :major 1, :minor 1},
     :streaming? true,
     :http-client
     #object[org.apache.http.impl.client.InternalHttpClient 0xd59490a "org.apache.http.impl.client.InternalHttpClient@d59490a"],
     :chunked? false,
     :reason-phrase "OK",
     :headers
     {:access-control-allow-origin "*",
      :content-type "application/json; charset=ISO-8859-1",
      :date "Fri, 22 Feb 2019 23:22:10 GMT",
      :server "RapidAPI-1.0.6",
      :x-ratelimit-requests-limit "100",
      :x-ratelimit-requests-remaining "84",
      :content-length "4189",
      :connection "Close"},
     :orig-content-encoding nil,
     :status 200,
     :length 4189,
     :body
     {:count "1",
      :items
      [{:largeimage "",
        :type "movie",
        :title "Trespass Against Us",
        :released "2016",
        :runtime "1h39m",
        :image
        "https://occ-0-3451-3446.1.nflxso.net/art/8ad00/bc36395deb410871cda447f046ac7c1aed28ad00.jpg",
        :unogsdate "2019-02-22",
        :synopsis
        "A man from a criminal family yearns to break away and find a better life, but his father's staunch opposition puts his dreams of freedom in jeopardy.",
        :download "0",
        :imdbid "tt3305308",
        :rating "5.8",
        :netflixid "80057509"}]},
     :trace-redirects []}

All JSON keys are converted to kebabed keywords, and all values are left as
the type given. Any HTML escape codes are parsed into their symbolic
equivalents.

The content returned by the API can be found as a map in `:body`, always
containing `:count` with the number of items that match the query. This is
distinct from the number of items contained in this response, as multiple
requests with increased `:page` values may be required to get all the matching
items.

The contents of the request are identical to that of the response JSON, with
the exception of `genre-ids` where the items, originally in individual maps
with distinct keys, are combined into a single map for convenience.

Note: The above request is abridged, and observant readers may note that the
`:length` value is much larger than the displayed content.


<a id="orgfd41b80"></a>

### Exceptions

The API will almost always return with status code 200. For most malformed
requests is just returns no items.

But, in the case that something with the API or the connection goes horribly
wrong and an exceptional HTTP status code is returned, this library does not
alter the \`clj-http\` default behavior and throws an exception. 

We can simulate one using some magic from the test suite:

    (require [clj-http.fake :refer [with-fake-routes]])
    
    (with-fake-routes {#".*" (fn [_] {:status 400})}
      (try (clunogs/deleted 1)
           (catch clojure.lang.ExceptionInfo e e)))

    #error {
     :cause "clj-http: status 400"
     :data {:status 400,
            :body "",
            :request-time 0,
            :orig-content-encoding nil,
            :type :clj-http.client/unexceptional-status}
     :via
     [{:type clojure.lang.ExceptionInfo
       :message "clj-http: status 400"
       :data {:status 400, :body "", :request-time 0, :orig-content-encoding nil, :type :clj-http.client/unexceptional-status}
       :at [slingshot.support$stack_trace invoke "support.clj" 201]}]
     :trace
     [...]}


<a id="org981b34b"></a>

## Arguments

For all requests, required arguments are taken as positional arguments, and
optional arguments can be supplied with keyword arguments. Some of the same
arguments may be required or optional for different requests. For example,
`countryid` is required by `new-releases`, but not by `deleted`.

All arguments are coerced to strings when making the request, so numerical
arguments may be either numbers or strings. 

The request functions will not check to make sure arguments are logically
valid before making the request (ex. The year ranges for `advanced-search`).


<a id="org2a5b04f"></a>

### Country ID

Many of the request functions require or optionally take a `countryid`. This
is a two letter alphabetical country code, ex. US, DE, CA.

A full list of country ID's can be found by calling `list-countries`.

A single request, `weekly-updates`, can take either a single ID or a
collection of multiple countries.


<a id="org0e3a8e0"></a>

### `clist`

The `advanced-search` request, for some reason, requires a numerical country
codes instead of the two letter country ID's. The value for the `:clist`
argument can be given as a single id, or a collection of them. For a mapping
of string country ID's to numerical ones can be found by calling
`list-countries`.

Additionally, the `deleted` request may take a collection of numerical country
codes instead of a singular alphabetical one.


<a id="org32748be"></a>

## Request Examples


<a id="orgda0ae85"></a>

### deleted

Gets all deleted items in daysback. 

    (clunogs/deleted 1)

    {:cached nil,
     :request-time 1256,
     :repeatable? false,
     :protocol-version {:name "HTTP", :major 1, :minor 1},
     :streaming? true,
     :http-client
     #object[org.apache.http.impl.client.InternalHttpClient 0x64ae5494 "org.apache.http.impl.client.InternalHttpClient@64ae5494"],
     :chunked? false,
     :reason-phrase "OK",
     :headers
     {:access-control-allow-origin "*",
      :content-type "application/json; charset=ISO-8859-1",
      :date "Sat, 23 Feb 2019 01:23:29 GMT",
      :server "RapidAPI-1.0.6",
      :x-ratelimit-requests-limit "100",
      :x-ratelimit-requests-remaining "99",
      :content-length "4225",
      :connection "Close"},
     :orig-content-encoding nil,
     :status 200,
     :length 4225,
     :body
     {:count "44",
      :items
      [{:netflixid "80104127",
        :title "Miss India America",
        :ccode "SK",
        :date "2019-02-22 18:00:51"}
       {:netflixid "80105514",
        :title "Accidental Courtesy",
        :ccode "SK",
        :date "2019-02-22 18:00:51"}
       {:netflixid "80104127",
        :title "Miss India America",
        :ccode "HK",
        :date "2019-02-22 17:20:15"}
       {:netflixid "80104127",
        :title "Miss India America",
        :ccode "GR",
        :date "2019-02-22 16:09:31"}
       {:netflixid "80106762",
        :title "Jack Reacher: Never Go Back",
        :ccode "GR",
        :date "2019-02-22 16:09:31"}
       {:netflixid "60025023",
        :title "Treasure Planet",
        :ccode "GR",
        :date "2019-02-22 16:09:31"}
       {:netflixid "70103760",
        :title "Up",
        :ccode "GR",
        :date "2019-02-22 16:09:31"}
       {:netflixid "80105514",
        :title "Accidental Courtesy",
        :ccode "CA",
        :date "2019-02-22 12:37:12"}
       {:netflixid "80104127",
        :title "Miss India America",
        :ccode "CA",
        :date "2019-02-22 12:37:11"}
       {:netflixid "70251895",
        :title "The Impossible",
        :ccode "CA",
        :date "2019-02-22 12:37:11"}
       {:netflixid "60031747",
        :title "Annie",
        :ccode "GB",
        :date "2019-02-22 12:24:25"}
       {:netflixid "80105514",
        :title "Accidental Courtesy",
        :ccode "GB",
        :date "2019-02-22 12:24:25"}
       {:netflixid "80074163",
        :title "Alpha and Omega: Journey to Bear Kingdom",
        :ccode "GB",
        :date "2019-02-22 12:24:25"}
       {:netflixid "80014865",
        :title "My Old Lady",
        :ccode "GB",
        :date "2019-02-22 12:24:25"}
       {:netflixid "80095314",
        :title "The Secret Life of Pets",
        :ccode "GB",
        :date "2019-02-22 12:24:25"}
       {:netflixid "80104127",
        :title "Miss India America",
        :ccode "GB",
        :date "2019-02-22 12:24:24"}
       {:netflixid "80104127",
        :title "Miss India America",
        :ccode "RO",
        :date "2019-02-22 11:38:30"}
       {:netflixid "80104127",
        :title "Miss India America",
        :ccode "ES",
        :date "2019-02-22 11:05:42"}
       {:netflixid "70059993",
        :title "Atonement",
        :ccode "ES",
        :date "2019-02-22 11:05:42"}
       {:netflixid "80104127",
        :title "Miss India America",
        :ccode "BR",
        :date "2019-02-22 09:08:38"}
       {:netflixid "879522",
        :title "Psycho",
        :ccode "BR",
        :date "2019-02-22 09:08:38"}
       {:netflixid "80104127",
        :title "Miss India America",
        :ccode "LT",
        :date "2019-02-22 08:38:46"}
       {:netflixid "80105514",
        :title "Accidental Courtesy",
        :ccode "LT",
        :date "2019-02-22 08:38:46"}
       {:netflixid "80104127",
        :title "Miss India America",
        :ccode "DE",
        :date "2019-02-22 07:20:54"}
       {:netflixid "80105514",
        :title "Accidental Courtesy",
        :ccode "IN",
        :date "2019-02-22 07:14:34"}
       {:netflixid "80104127",
        :title "Miss India America",
        :ccode "IN",
        :date "2019-02-22 07:14:33"}
       {:netflixid "80104127",
        :title "Miss India America",
        :ccode "HU",
        :date "2019-02-22 05:53:39"}
       {:netflixid "80105514",
        :title "Accidental Courtesy",
        :ccode "HU",
        :date "2019-02-22 05:53:39"}
       {:netflixid "80104127",
        :title "Miss India America",
        :ccode "SE",
        :date "2019-02-22 05:33:24"}
       {:netflixid "80104127",
        :title "Miss India America",
        :ccode "SG",
        :date "2019-02-22 05:04:33"}
       {:netflixid "80104127",
        :title "Miss India America",
        :ccode "PL",
        :date "2019-02-22 04:58:15"}
       {:netflixid "80104127",
        :title "Miss India America",
        :ccode "RU",
        :date "2019-02-22 04:46:22"}
       {:netflixid "80105514",
        :title "Accidental Courtesy",
        :ccode "RU",
        :date "2019-02-22 04:46:22"}
       {:netflixid "80104127",
        :title "Miss India America",
        :ccode "NL",
        :date "2019-02-22 04:04:33"}
       {:netflixid "80104127",
        :title "Miss India America",
        :ccode "AU",
        :date "2019-02-22 04:03:18"}
       {:netflixid "80105514",
        :title "Accidental Courtesy",
        :ccode "AU",
        :date "2019-02-22 04:03:18"}
       {:netflixid "451465",
        :title "Dolores Claiborne",
        :ccode "AU",
        :date "2019-02-22 04:03:18"}
       {:netflixid "60010281",
        :title "Death in Venice",
        :ccode "AU",
        :date "2019-02-22 04:03:18"}
       {:netflixid "80096782",
        :title "American Honey",
        :ccode "AU",
        :date "2019-02-22 04:03:18"}
       {:netflixid "80104127",
        :title "Miss India America",
        :ccode "KR",
        :date "2019-02-22 03:57:43"}
       {:netflixid "80173544",
        :title "Contents League Owarai Selection",
        :ccode "JP",
        :date "2019-02-22 03:52:44"}
       {:netflixid "80104127",
        :title "Miss India America",
        :ccode "JP",
        :date "2019-02-22 03:52:44"}
       {:netflixid "80104127",
        :title "Miss India America",
        :ccode "FR",
        :date "2019-02-22 03:16:21"}]},
     :trace-redirects []}

Can optionally specify the country and title. Note that the title is fuzzy-matched.

    (clunogs/deleted 60 :countryid "US" :title "The Game")

    {:cached nil,
     :request-time 1331,
     :repeatable? false,
     :protocol-version {:name "HTTP", :major 1, :minor 1},
     :streaming? true,
     :http-client
     #object[org.apache.http.impl.client.InternalHttpClient 0x12ef7915 "org.apache.http.impl.client.InternalHttpClient@12ef7915"],
     :chunked? false,
     :reason-phrase "OK",
     :headers
     {:access-control-allow-origin "*",
      :content-type "application/json; charset=ISO-8859-1",
      :date "Sat, 23 Feb 2019 01:36:46 GMT",
      :server "RapidAPI-1.0.6",
      :x-ratelimit-requests-limit "100",
      :x-ratelimit-requests-remaining "87",
      :content-length "113",
      :connection "Close"},
     :orig-content-encoding nil,
     :status 200,
     :length 113,
     :body
     {:count "1",
      :items
      [{:netflixid "80245059",
        :title "The Game 365",
        :ccode "US",
        :date "2019-02-16 00:43:03"}]},
     :trace-redirects []}

This request has a special case, unlike all other requests. It's countryid
can also be given as a collection of numerical country codes, found by
calling list-countries.

    (clojure.pprint/pprint (deleted 60 :countryid [78 46] :title "The Game"))

    {:cached nil,
     :request-time 1341,
     :repeatable? false,
     :protocol-version {:name "HTTP", :major 1, :minor 1},
     :streaming? true,
     :http-client
     #object[org.apache.http.impl.client.InternalHttpClient 0x7e26f885 "org.apache.http.impl.client.InternalHttpClient@7e26f885"],
     :chunked? false,
     :reason-phrase "OK",
     :headers
     {:access-control-allow-origin "*",
      :content-type "application/json; charset=ISO-8859-1",
      :date "Sun, 24 Feb 2019 19:54:13 GMT",
      :server "RapidAPI-1.0.6",
      :x-ratelimit-requests-limit "100",
      :x-ratelimit-requests-remaining "89",
      :content-length "203",
      :connection "Close"},
     :orig-content-encoding nil,
     :status 200,
     :length 203,
     :body
     {:count "2",
      :items
      [{:netflixid "80245059",
        :title "The Game 365",
        :ccode "US",
        :date "2019-02-16 00:43:03"}
       {:netflixid "80245059",
        :title "The Game 365",
        :ccode "GB",
        :date "2019-02-15 12:30:31"}]},
     :trace-redirects []}


<a id="orgc36180d"></a>

### expiring

Get all items expiring soon in a country. 

    (clunogs/expiring "US")

    {:cached nil,
     :request-time 1006,
     :repeatable? false,
     :protocol-version {:name "HTTP", :major 1, :minor 1},
     :streaming? true,
     :http-client
     #object[org.apache.http.impl.client.InternalHttpClient 0x2e574b16 "org.apache.http.impl.client.InternalHttpClient@2e574b16"],
     :chunked? false,
     :reason-phrase "OK",
     :headers
     {:access-control-allow-origin "*",
      :content-type "application/json; charset=ISO-8859-1",
      :date "Sat, 23 Feb 2019 01:40:23 GMT",
      :server "RapidAPI-1.0.6",
      :x-ratelimit-requests-limit "100",
      :x-ratelimit-requests-remaining "84",
      :content-length "49302",
      :connection "Close"},
     :orig-content-encoding nil,
     :status 200,
     :length 49302,
     :body
     {:count "96",
      :items
      [{:largeimage "",
        :type "movie",
        :title "Born to Be Blue",
        :released "2015",
        :runtime "1h37m",
        :image
        "https://occ-0-1007-1361.1.nflxso.net/art/d0927/f0580a09af33450dea5c99003c137e5e8e7d0927.jpg",
        :unogsdate "2019-02-23",
        :synopsis
        "This unconventional biopic tunes into the life of ico25b756b7308a0e67e.jpg",
        :unogsdate "2019-02-23",
        :synopsis
        "World War IV is over, but a bomb has gone off in Newport City, killing a major arms dealer who may have ties with the mysterious 501 Organization.<br><b>Expires on 2019-02-23</b>",
        :download "1",
        :imdbid "tt2636124",
        :rating "7.2",
        :netflixid "80002073"}
       {:largeimage "http://cdn0.nflximg.net/images/6424/21896424.jpg",
        :type "movie",
        :title "Ghost Tears",
        :released "2014",
        :runtime "58m",
        :image
        "https://occ-0-3451-3446.1.nflxso.net/art/d2b2d/5582ade5af978b6ad776e4f64d5ab5d7574d2b2d.jpg",
        :unogsdate "2019-02-23",
        :synopsis
        "As Motoko and Batou attempt to thwart a mysterious terrorist group, Togusa tracks the killer of a man with a prosthetic leg made by Mermaid's Leg.<br><b>Expires on 2019-02-23</b>",
        :download "1",
        :imdbid "tt3579524",
        :rating "7.3",
        :netflixid "80021983"}
       {:largeimage "http://cdn0.nflximg.net/images/6514/21896514.jpg",
        :type "movie",
        :title "Ghost Whispers",
        :released "2013",
        :runtime "56m",
        :image
        "https://occ-0-3451-3446.1.nflxso.net/art/8fae3/f546f54f0f1e0f8b6d3e82e21911e550f018fae3.jpg",
        :unogsdate "2019-02-23",
        :synopsis
        "Freed of her responsibilities for the 501 Organization, Motoko must now learn how to take orders from Aramaki.<br><b>Expires on 2019-02-23</b>",
        :download "1",
        :imdbid "tt3017864",
        :rating "7.3",
        :netflixid "80002074"}
       {:largeimage "http://cdn1.nflximg.net/images/4657/23114657.jpg",
        :type "series",
        :title "Attack on Titan",
        :released "2013",
        :runtime "",
        :image
        "https://occ-0-3451-3446.1.nflxso.net/art/c2492/92ec46dbd1e0b0cba92e594b2499a80e886c2492.jpg",
        :unogsdate "2019-02-23",
        :synopsis
        "With his hometown in ruins, young Eren Yeager becomes determined to fight back against the giant Titans that threaten to destroy the human race.<br><b>Expires on 2019-02-23</b>",
        :download "1",
        :imdbid "tt2560140",
        :rating "8.8",
        :netflixid "70299043"}
       {:largeimage "http://cdn1.nflximg.net/images/9202/11739202.jpg",
        :type "series",
        :title "Hawaii Five-0",
        :released "2010",
        :runtime "",
        :image
        "http://occ-2-3451-3446.1.nflxso.net/art/165f9/ea4595128c9da60ac9bc5bd9d24280eddef165f9.jpg",
        :unogsdate "2019-02-24",
        :synopsis
        "Hawaii's top cops are reborn in this update of an iconic TV show. They may work in paradise, but there's enough crime to keep them working overtime.<br><b>Expires on 2019-02-24</b>",
        :download "0",
        :imdbid "tt1600194",
        :rating "7.4",
        :netflixid "70176866"}
       {:largeimage "",
        :type "movie",
        :title "Pygmies: The Children of the Jungle",
        :released "2011",
        :runtime "52m",
        :image
        "https://occ-0-2219-1361.1.nflxso.net/art/d5f68/9b9baa265125c07029e6c751904ec0e8259d5f68.jpg",
        :unogsdate "2019-02-24",
        :synopsis
        "A filmmaker travels to the Central African Republic to fulfill a childhood dream of meeting the enigmatic Pygmy people.<br><b>Expires on ,
        :download "0",
        :imdbid "tt6767882",
        :rating "7.5",
        :netflixid "80156337"}
       {:largeimage "http://cdn0.nflximg.net/images/2632/21372632.jpg",
        :type "movie",
        :title "Pretend We're Kissing",
        :released "2014",
        :runtime "1h24m",
        :image
        "https://occ-0-3451-3446.1.nflxso.net/art/01175/1238b6bcface21520f0f249b4e36fd82a3f01175.jpg",
        :unogsdate "2019-02-26",
        :synopsis
        "An insecure nebbish meets a woman who sparks his desire for a real romantic connection and an effort to get out of his head and get on with life.<br><b>Expires on 2019-02-26</b>",
        :download "1",
        :imdbid "tt2756910",
        :rating "5.6",
        :netflixid "80057170"}
       {:largeimage "http://cdn0.nflximg.net/images/0692/24890692.jpg",
        :type "movie",
        :title "Finding Vivian Maier",
        :released "2013",
        :runtime "1h23m",
        :image
        "https://occ-0-3451-3446.1.nflxso.net/art/86095/6534f00db9c70109bb4d0f12235246cc06e86095.jpg",
        :unogsdate "2019-02-27",
        :synopsis
        "By all accounts, Vivian Maier was an unassuming nanny. But the photos she took that were found only after her death reveal her artistic genius.<br><b>Expires on 2019-02-27</b>",
        :download "1",
        :imdbid "tt2714900",
        :rating "7.7",
        :netflixid "70291615"}
       {:largeimage "",
        :type "movie",
        :title "Finding Altamira",
        :released "2016",
        :runtime "1h33m",
        :image
        "https://occ-0-3451-3446.1.nflxso.net/art/cdc50/9aa14cf5cd9842f50236fd40fda1fe41c69cdc50.jpg",
        :unogsdate "2019-02-27",
        :synopsis
        "The discovery of prehistoric paintings in Spanish caves leads a 19th-century archaeologist into a lifelong struggle to have them accepted as genuine.<br><b>Expires on 2019-02-27</b>",
        :download "0",
        :imdbid "tt3014910",
        :rating "5.9",
        :netflixid "80131186"}
       {:largeimage "http://cdn1.nflximg.net/images/0925/11940925.jpg",
        :type "series",
        :title "Filthy Riches",
        :released "2014",
        :runtime "",
        :image
        "http://occ-1-3451-3446.1.nflxso.net/art/c5c8f/7ceb1a6506a77671faadf2d334c56e8bdfdc5c8f.jpg",
        :unogsdate "2019-02-28",
        :synopsis
        "Across America, independent entrepreneurs who aren't afraid to get their hands dirty make a living by harvesting eels, worms, wild mushrooms and more.<br><b>Expires on 2019-02-28</b>",
        :download "0",
        :imdbid "tt3654672",
        :rating "7.6",
        :netflixid "80020750"}
       {:largeimage "http://cdn0.nflximg.net/images/6838/11716838.jpg",
        :type "series",
        :title "Cesar 911",
        :released "2014",
        :runtime "",
        :image
        "http://occ-0-3451-3446.1.nflxso.net/art/32360/433a1549937f7de66ed7d9c331a216569cd32360.jpg",
        :unogsdate "2019-02-28",
        :synopsis
        "Canine expert extraordinaire Cesar Milan travels around the nation to help troubled dogs that are threatening the balance of their communities.<br><b>Expires on 2019-02-28</b>",
        :download "0",
        :imdbid "tt3648438",
        :rating "8.2",
        :netflixid "80011086"}
       {:largeimage "http://cdn0.nflximg.net/images/7194/25047194.jpg",
        :type "series",
        :title "Animal Fight Night",
        :released "2015",
        :runtime "",
        :image
        "http://occ-1-3451-3446.1.nflxso.net/art/b6af1/76709933048b3d9e0fbf653257f9f6f8ed3b6af1.jpg",
        :unogsdate "2019-02-28",
        :synopsis
        "Warthogs, octopuses, hippos and other creatures tussle with their own kinds over food, turf and mates in the roughest, meanest battles in the wild.<br><b>Expires on 2019-02-28</b>",
        :download "0",
        :imdbid "notfound",
        :rating "",
        :netflixid "80087905"}
       {:largeimage "http://cdn1.nflximg.net/images/6491/25046491.jpg",
        :type "series",
        :title "Africas Deadliest",
        :released "2011",
        :runtime "",
        :image
        "http://occ-2-3451-3446.1.nflxso.net/art/30007/1acc349c72f5720e0e3babda4153d16fa3b30007.jpg",
        :unogsdate "2019-02-28",
        :synopsis
        "From seals and sharks to hippos and elephants, this series explores the killer tactics and natural weaponry of Africa's most effective predators.<br><b>Expires on 2019-02-28</b>",
        :download "0",
        :imdbid:synopsis
        "Uncover fascinating new information about the 'lost' underwater city of Atlantis, a legend that has long mystified scholars and explorers.<br><b>Expires on 2019-02-28</b>",
        :download "0",
        :imdbid "tt2855296",
        :rating "6.5",
        :netflixid "80014593"}
       {:largeimage "",
        :type "series",
        :title "Escape to the Country Collection",
        :released "2015",
        :runtime "",
        :image
        "http://occ-2-3451-3446.1.nflxso.net/art/fb7ed/61005ce08279dc76bcf167d181a76f9fb25fb7ed.jpg",
        :unogsdate "2019-02-28",
        :synopsis
        "Show hosts help prospective home buyers select country houses from all over Great Britain, highlighting each area's natural beauty and history.<br><b>Expires on 2019-02-28</b>",
        :download "0",
        :imdbid "tt0390700",
        :rating "7.1",
        :netflixid "80160893"}
       {:largeimage "http://cdn0.nflximg.net/images/8574/21308574.jpg",
        :type "movie",
        :title "The Truth Behind: UFOs",
        :released "2011",
        :runtime "45m",
        :image
        "https://occ-0-3451-3446.1.nflxso.net/art/bcd95/f095d280c273d4668c77b459dbb2edbf97abcd95.jpg",
        :unogsdate "2019-02-28",
        :synopsis
        "UFO sightings have been on the rise since the 1940s, with thousands reported every year -- but is alien existence all a figment of our imagination?<br><b>Expires on 2019-02-28</b>",
        :download "0",
        :imdbid "tt2855296",
        :rating "6.5",
        :netflixid "80014591"}
       {:largeimage "http://cdn0.nflximg.net/images/3034/11883034.jpg",
        :type "movie",
        :title "Inside the Hunt for the Boston Bomber",
        :released "2014",
        :runtime "1h27m",
        :image
        "http://occ-0-3451-3446.1.nflxso.net/art/ddcdc/382625a0d02a7bda5fca3b242735d9fc506ddcdc.jpg",
        :unogsdate "2019-02-28",
        :synopsis
        "The gripping inside story behind the terrorist bombings at the 2013 Boston Marathon involves a widespread investigation -- and an intense manhunt.<br><b>Expires on 2019-02-28</b>",
        :download "0",
        :imdbid "tt3660486",
        :rating "7.2",
        :netflixid "80013981"}
       {:largeimage "",
        :type "series",
        :title "Bondi Ink Tattoo Crew",
        :released "2017",
        :runtime "",
        :image
        "http://occ-2-3451-3446.1.nflxso.net/art/b0306/604245616575021bf0adc5d0d153c26ac9fb0306.jpg",
        :unogsdate "2019-02-28",
        :synopsis
        "Joined by industry superstar Megan Massacre, the crew of a premier tattoo shop in Australia must contend with clients, celebs and creative quarrels.<br><b>Expires on 2019-02-28</b>",
        :download "0",
        :imdbid "tt3887992",
        :rating "5.8",
        :netflixid "80159711"}
       {:largeimage "http://cdn1.nflximg.net/images/5349/25055349.jpg",
        :type "movie",
        :title "Brothers in War",
        :released "2014",
        :runtime "1h28m",
        :image
        "https://occ-0-3451-3446.1.nflxso.net/art/0f261/685ed0ea5be9a96b197eef2d67889213b890f261.jpg",
        :unogsdate "2019-02-28",
        :synopsis
        "Home movies, audio tapes and letters from members of Charlie Company, the last U.S. platoon sent to Vietnam, illuminate the conflict as never before.<br><b>Expires on 2019-02-28</b>",
        :download "0",
        :imdbid "tt3595312",
        :rating "8.1",
        :netflixid "80087934"}
       {:largeimage "",
        :type "series",
        :title "Monster Garage",
        :released "2006",
        :runtime "",
        :image
        "http://occ-2-3451-3446.1.nflxso.net/art/34962/6751cd3f6a0ec949e6738930cf9d6d70d3534962.jpg",
        :unogsdate "2019-02-28",
        :synopsis
        "Mechanic extraordinaire Jesse James helms this high-octane series that transforms regular cars into outrageous vehicles in just seven days.<br><b>Expires on 2019-02-28</b>",
        :download "0",
        :imdbid "tt0346343",
        :rating "6",
        :netflixid "70180070"}
       {:largeimage "",
        :type "series",
        :title "Great World Hotels",
        :released "2011",
        :runtime "",
        :image
        "http://occ-2-2430-2433.1.nflxso.net/art/e3ad3/f6804e175db895fe01603a68068ebcd9a16e3ad3.jpg",
        :unogsdate "2019-02-28",
        :synopsis
        "No matter how far-flung the locale, this travel series circles the earth to reveal the planet's most luxurious, adventurous and bination plots that were launched against Hitler's life.<br><b>Expires on 2019-02-28</b>",
        :download "0",
        :imdbid "tt1717321",
        :rating "7.6",
        :netflixid "70128979"}
       {:largeimage "http://cdn0.nflximg.net/images/1558/24131558.jpg",
        :type "series",
        :title "Fresh Meat",
        :released "2011",
        :runtime "",
        :image
        "http://occ-1-3451-3446.1.nflxso.net/art/ebde7/2eb1cbe8fc0b40958c160bd532c637019f3ebde7.jpg",
        :unogsdate "2019-03-01",
        :synopsis
        "This comedy series follows six young individuals embarking on the most exciting journey of their lives so far: university.<br><b>Expires on 2019-03-01</b>",
        :download "1",
        :imdbid "tt2058303",
        :rating "7.9",
        :netflixid "70234672"}
       {:largeimage "",
        :type "movie",
        :title "Adult Beginners",
        :released "2014",
        :runtime "1h32m",
        :image
        "https://occ-0-300-2773.1.nflxso.net/art/bd1ed/cdebcb99781a474f32f292988ea3a223b0dbd1ed.jpg",
        :unogsdate "2019-03-01",
        :synopsis
        "A failed entrepreneur shows up at his sister's door and faces true responsibility for the first time when he's put to work as a nanny to his nephew.<br><b>Expires on 2019-03-01</b>",
        :download "1",
        :imdbid "tt3318750",
        :rating "5.6",
        :netflixid "80018332"}
       {:largeimage "",
        :type "movie",
        :title "Before We Go",
        :released "2014",
        :runtime "1h35m",
        :image
        "https://occ-0-3451-3446.1.nflxso.net/art/1881c/2b620563f65d1a90b82c52304442175784d1881c.jpg",
        :unogsdate "2019-03-01",
        :synopsis
        "When a young wife is robbed before boarding a train to Boston, she meets a free-spirited musician who stays with her on the adventure of a lifetime.<br><b>Expires on 2019-03-01</b>",
        :download "1",
        :imdbid "tt0443465",
        :rating "6.8",
        :netflixid "80017262"}
       {:largeimage "",
        :type "movie",
        :title "Hot Sugar's Cold World",
        :released "2015",
        :runtime "1h26m",
        :image
        "https://occ-0-3451-3446.1.nflxso.net/art/5ddd8/844c37482b0b22860b286568015cce4ab9b5ddd8.jpg",
        :unogsdate "2019-03-01",
        :synopsis
        "Cameras follow musical innovator Nick Koenig, aka Hot Sugar, as he captures the noises of Paris and transforms them into one-of-a-kind soundscapes.<br><b>Expires on 2019-03-01</b>",
        :download "1",
        :imdbid "tt4423066",
        :rating "6.5",
        :netflixid "80061042"}
       {:largeimage "",
        :type "movie",
        :title "Liz in September",
        :released "2014",
        :runtime "1h32m",
        :image
        "http://occ-0-3451-3446.1.nflxso.net/art/c42ce/d94f91c52c2cecf9fcebe42a097d72abe37c42ce.jpg",
        :unogsdate "2019-03-01",
        :synopsis
        "A shocking secret, ex-lovers and the arrival of an outsider shake things up for Liz as she celebrates her birthday at a lesbian-owned resort.<br><b>Expires on 2019-03-01</b>",
        :download "1",
        :imdbid "tt2325833",
        :rating "5.5",
        :netflixid "80048493"}
       {:largeimage "",
        :type "movie",
        :title "World’s Heaviest Man Gets Married",
        :released "2009",
        :runtime "44m",
        :image
        "https://occ-0-2219-1e lover.<br><b>Expires on 2019-03-01</b>",
        :download "1",
        :imdbid "tt3687186",
        :rating "6.6",
        :netflixid "80016426"}
       {:largeimage "",
        :type "series",
        :title "I Am the Ambassador",
        :released "2015",
        :runtime "",
        :image
        "http://occ-2-768-769.1.nflxso.net/art/f48d2/5d3a1f4c842a89480224bd4e5ab8c9f9364f48d2.jpg",
        :unogsdate "2019-03-01",
        :synopsis
        "This documentary series follows Rufus Gifford, the U.S. ambassador to Denmark and an advocate for LGBT rights, in his personal and professional life.<br><b>Expires on 2019-03-01</b>",
        :download "1",
        :imdbid "notfound",
        :rating "",
        :netflixid "80101901"}
       {:largeimage "",
        :type "movie",
        :title "The Man in 3B",
        :released "2015",
        :runtime "1h33m",
        :image
        "https://occ-0-2219-1361.1.nflxso.net/art/1418f/a145f69b425fa87cdb1ab033d11daa97f601418f.jpg",
        :unogsdate "2019-03-01",
        :synopsis
        "A charismatic new tenant quickly makes his mark on a Queens apartment building, but his popularity soon results in a murder with many suspects.<br><b>Expires on 2019-03-01</b>",
        :download "0",
        :imdbid "tt3384180",
        :rating "5.1",
        :netflixid "80088085"}
       {:largeimage "",
        :type "movie",
        :title "Ataud Blanco",
        :released "2016",
        :runtime "1h10m",
        :image
        "https://occ-0-2219-1361.1.nflxso.net/art/51cd1/cd98fd6bc4fb8ebd2d6fbedf805b69d2cd151cd1.jpg",
        :unogsdate "2019-03-01",
        :synopsis
        "After her daughter is abducted by a truck driver on an isolated road, a young mother gives chase, becoming an unwitting pawn in a deadly demonic game.<br><b>Expires on 2019-03-01</b>",
        :download "0",
        :imdbid "tt5767182",
        :rating "4.1",
        :netflixid "80156689"}
       {:largeimage "",
        :type "movie",
        :title "Rolling Stones: Crossfire Hurricane",
        :released "2012",
        :runtime "1h50m",
        :image
        "https://occ-0-784-778.1.nflxso.net/art/378ff/fc1d7a9bfc7429003ddacda71d0b9ba887d378ff.jpg",
        :unogsdate "2019-03-01",
        :synopsis
        "Mixing archival footage and recent interviews, this film tells the story of the Rolling Stones, from blues-obsessed teens to rock 'n' roll legends.<br><b>Expires on 2019-03-01</b>",
        :download "0",
        :imdbid "tt2370140",
        :rating "7.4",
        :netflixid "80166308"}
       {:largeimage "",
        :type "movie",
        :title "Singing with Angels",
        :released "2016",
        :runtime "1h34m",
        :image
        "https://occ-0-2219-1361.1.nflxso.net/art/971ac/a55bec89d0cbf32714005335290a650a158971ac.jpg",
        :unogsdate "2019-03-01",
        :synopsis
        "An LDS woman reflects on how her faith, family and position in the Mormon Tabernacle Choir have all helped her to meet life's many challenges.<br><b>Expires on 2019-03-01</b>",
        :download "0",
        :imdbid "tt4678298",
        :rating "5.9",
        :netflixid "80112409"}
       {:largeimage "",
        :type "series",
        :title "Clangers",
        :released "2016",
        :runtime "",
        :image
        "http://occ-0-3451-3446.1.nflxso.net/art/5e482/5bdb914d3b96567992f7b6380a4d8b1f0a65e482.jpg",
        :unogsdate "2019-03-01",
        :synopsis
        "Teamwork and compassion guide the gentle adventures of a family of slide-whistling, mice-like aliens in this revival of a classic British series.<br><b>Expires on 2019-03-01</b>",
        :download "0",
        :imdbid "tt4771826",
        :rating "8.2",
        :netflixid "80172009"}
       {:largeimage "",
        :type "movie",
        :title "Ice Girls",
        :released "2016",
        :runtime "1h30m",
        :image
        "https://occ-0-3451-3446.1.nflxso.net/art/0c74c/b02eea6054a99caec3fd9ef69ebfd39962d0c74c.jpg",
        :unogsdate "2019-03-01",
        :synopsis
        "Sidelined by an injury and a relocation to a new town, a teen figure skater gets back on the ice thanks to the support of a local skating rink owner.<br><b>Expires on 2019-03-01</b>",
        :download "0",
        :imdbid "tt5198570",
        :rating "6.1",
        :netflixid "80171980"}
       {:largeimage "",
        :type "movie",
        :title "The Preacher's Son",
        :released "2017",
        :runtime "1h39m",
        :image
        "https://occ-0-2219-1361.1.nflxso.net/art/874ec/aeeer and community standing of a respected bishop are threatened.<br><b>Expires on 2019-03-01</b>",
        :download "0",
        :imdbid "tt1397213",
        :rating "0",
        :netflixid "80174094"}
       {:largeimage "",
        :type "movie",
        :title "No Estamos Solos",
        :released "2016",
        :runtime "1h15m",
        :image
        "https://occ-0-2219-1361.1.nflxso.net/art/14a34/b3b6fa48be622a15922abcccc3b774d3c4f14a34.jpg",
        :unogsdate "2019-03-01",
        :synopsis
        "After moving to the outskirts of Lima, a family is terrorized by a dark presence in their new home, forcing them to seek the help of an exorcist.<br><b>Expires on 2019-03-01</b>",
        :download "0",
        :imdbid "tt4696502",
        :rating "4.6",
        :netflixid "80163154"}
       {:largeimage "http://cdn1.nflximg.net/images/6429/9186429.jpg",
        :type "series",
        :title "Bo on the Go!",
        :released "2007",
        :runtime "",
        :image
        "http://occ-1-3451-3446.1.nflxso.net/art/e912c/41ae72b462d4c1ab37f838c0d11ff03e888e912c.jpg",
        :unogsdate "2019-03-01",
        :synopsis
        "Staying at home doesn't mean sitting still for energetic Bo and her little dragon friend Dezzy, who embark on amazing adventures through movement.<br><b>Expires on 2019-03-01</b>",
        :download "1",
        :imdbid "tt1516172",
        :rating "6.1",
        :netflixid "70230412"}
       {:largeimage "",
        :type "movie",
        :title "The Little Rascals",
        :released "1994",
        :runtime "1h22m",
        :image
        "https://occ-0-2219-1361.1.nflxso.net/art/6a2f7/9e10339c8be80e1ed2f4a7e0569c02af3c06a2f7.jpg",
        :unogsdate "2019-03-01",
        :synopsis
        "Inspired by Hal Roach's 'Our Gang' TV series, this delightful family film promises shenanigans from the funniest little mischief makers of all time.<br><b>Expires on 2019-03-01</b>",
        :download "1",
        :imdbid "tt0110366",
        :rating "6.3",
        :netflixid "705761"}
       {:largeimage "http://cdn0.nflximg.net/images/8910/12018910.jpg",
        :type "movie",
        :title "Hostage",
        :released "2005",
        :runtime "1h53m",
        :image
        "https://occ-0-1007-1361.1.nflxso.net/art/1f572/9b4f462f34923f8103e1eff2c153a5845e61f572.jpg",
        :unogsdate "2019-03-01",
        :synopsis
        "A former hostage negotiator finds his newly quiet existence shattered when a family linked to the mob is taken hostage by wayward thieves.<br><b>Expires on 2019-03-01</b>",
        :download "1",
        :imdbid "tt0340163",
        :rating "6.6",
        :netflixid "70018724"}
       {:largeimage "",
        :type "movie",
        :title "Don Verdean",
        :released "2015",
        :runtime "1h36m",
        :image
        "https://occ-0-3451-3446.1.nflxso.net/art/4826c/9c547c871b5b648fe1f8fb28ab856957ff34826c.jpg",
        :unogsdate "2019-03-01",
        :synopsis
        "A questionable character working as a self-styled 'biblical archaeologist' advances his cause by creating phony religious artifacts.<br><b>Expires on 2019-03-01</b>",
        :download "1",
        :imdbid "tt3534282",
        :rating "5.4",
        :netflixid "80037274"}
       {:largeimage "http://cdn0.nflximg.net/images/8211/8668211.jpg",
        :type "movie",
        :title "I Am Number Four",
        :released "2011",
        :runtime "1h50m",
        :image
        "https://occ-0-1007-1361.1.nflxso.net/art/d8dc3/1175d427f69f6a095fa8529142afbcb7588d8dc3.jpg",
        :unogsdate "2019-03-01",
        :synopsis
        "After nine aliens flee their home planet to find peace on Earth, their plans are shattered by pursuers who must kill them in numerical order.<br><b>Expires on 2019-03-01</b>",
        :download "0",
        :imdbid "tt1464540",
        :rating "6.1",
        :netflixid "70153713"}
       {:largeimage "http://cdn1.nflximg.net/images/0455/22510455.jpg",
        :type "movie",
        :title "The Fifth Estate",
        :released "2013",
        :runtime "2h2m",
        :image
        "https://occ-0-3451-3446.1.nflxso.net/art/613e3/d9df92eece0515bbb9f4b66e5945530da91613e3.jpg",
        :unogsdate "2019-03-01",
        :synopsis
        "This fact-based drama recounts the early days of the controversial but revolutionary WikiLeaks website and the inevitable conflict it wrought.<br><b>Expires on 2019-03-01</b>",
        :download "0",
        :imdbid "tt1837703",
        :rating "6.2",
    
        :synopsis
        "In this gripping thriller, a group of men volunteers to take on the roles of guards and inmates at a mock prison as part of a controversial study.<br><b>Expires on 2019-03-01</b>",
        :download "0",
        :imdbid "tt0997152",
        :rating "6.5",
        :netflixid "70129581"}
       {:largeimage "http://cdn1.nflximg.net/images/8419/11618419.jpg",
        :type "movie",
        :title "The Negotiator",
        :released "1998",
        :runtime "2h19m",
        :image
        "https://occ-0-3451-3446.1.nflxso.net/art/3c1dd/a76c75efe03807e36706cff5595f9b4eb813c1dd.jpg",
        :unogsdate "2019-03-01",
        :synopsis
        "The police try to arrest an expert hostage negotiator who insists he's being framed for his partner's murder in what he believes is a conspiracy.<br><b>Expires on 2019-03-01</b>",
        :download "1",
        :imdbid "tt0120768",
        :rating "7.3",
        :netflixid "16915198"}
       {:largeimage "http://cdn0.nflximg.net/images/5616/23185616.jpg",
        :type "movie",
        :title "The Gift",
        :released "2015",
        :runtime "1h47m",
        :image
        "https://occ-0-3451-3446.1.nflxso.net/art/0e2ce/df154da1c71b234245a70a71c470115c3bc0e2ce.jpg",
        :unogsdate "2019-03-01",
        :synopsis
        "Newly relocated to California, a man bumps into a former classmate who seems friendly at first, but is soon revealed to be nursing a murderous grudge.<br><b>Expires on 2019-03-01</b>",
        :download "0",
        :imdbid "tt4178092",
        :rating "7.1",
        :netflixid "80046694"}
       {:largeimage "",
        :type "movie",
        :title "Miles",
        :released "2016",
        :runtime "1h27m",
        :image
        "http://occ-0-3451-3446.1.nflxso.net/art/ff463/29e78919cf53b0aa35f3feaf9468eb1b7c2ff463.jpg",
        :unogsdate "2019-03-01",
        :synopsis
        "With his eye on a scholarship, a gay teen in a small Illinois town joins his high school's girls' volleyball team and dreams of escaping to Chicago.<br><b>Expires on 2019-03-01</b>",
        :download "0",
        :imdbid "tt4066836",
        :rating "5.7",
        :netflixid "80176074"}
       {:largeimage "",
        :type "movie",
        :title "A Little Princess",
        :released "1995",
        :runtime "1h37m",
        :image
        "https://occ-0-3451-3446.1.nflxso.net/art/3141d/ab8925e446b5b221f6b9024ac5107df132f3141d.jpg",
        :unogsdate "2019-03-01",
        :synopsis
        "With her father fighting in World War I, young Sara enrolls in boarding school and clashes with a headmistress who tries to stifle her self-worth.<br><b>Expires on 2019-03-01</b>",
        :download "0",
        :imdbid "tt0113670",
        :rating "7.7",
        :netflixid "705734"}
       {:largeimage "",
        :type "movie",
        :title "LEGO DC Super Hero Girls: Brain Drain",
        :released "2017",
        :runtime "1h15m",
        :image
        "http://occ-1-3451-3446.1.nflxso.net/art/93e79/ba348caa702128ff05cf95f5f7a44ae826293e79.jpg",
        :unogsdate "2019-03-01",
        :synopsis
        "Super Hero High School classmates Wonder Woman, Batgirl and Supergirl are expelled for misdeeds they committed during 24 hours they can't remember.<br><b>Expires on 2019-03-01</b>",
        :download "0",
        :imdbid "tt7158814",
        :rating ":type "movie",
        :title "Bruce Almighty",
        :released "2003",
        :runtime "1h41m",
        :image
        "https://occ-0-3451-3446.1.nflxso.net/art/ec4c3/482a6cba65cde6709bdaa7435d2d51bfb6eec4c3.jpg",
        :unogsdate "2019-03-01",
        :synopsis
        "When TV reporter Bruce Nolan angrily ridicules God, the Almighty responds by giving Bruce all His divine powers. But can Bruce improve on perfection?<br><b>Expires on 2019-03-01</b>",
        :download "0",
        :imdbid "tt0315327",
        :rating "6.7",
        :netflixid "60027700"}
       {:largeimage "http://cdn0.nflximg.net/images/7420/3237420.jpg",
        :type "movie",
        :title "Pearl Harbor",
        :released "2001",
        :runtime "3h3m",
        :image
        "https://occ-0-358-360.1.nflxso.net/art/d734c/777c73697f9a0b4330421d1254e25c1942ad734c.jpg",
        :unogsdate "2019-03-01",
        :synopsis
        "The friendship between two fighter pilots is tested when they become entangled in a love triangle with a nurse amid the attack on Pearl Harbor.<br><b>Expires on 2019-03-01</b>",
        :download "0",
        :imdbid "tt0213149",
        :rating "6.1",
        :netflixid "60004468"}
       {:largeimage "http://cdn0.nflximg.net/images/7876/9747876.jpg",
        :type "movie",
        :title "The Cider House Rules",
        :released "1999",
        :runtime "2h5m",
        :image
        "https://occ-0-3451-3446.1.nflxso.net/art/dcadb/d54120d851231d7fb2a785e7b12fe90cc78dcadb.jpg",
        :unogsdate "2019-03-01",
        :synopsis
        "A protégé to a physician who runs an orphanage sets off to see the world, but reality soon intrudes on his youthful idealism and moral certitude.<br><b>Expires on 2019-03-01</b>",
        :download "1",
        :imdbid "tt0124315",
        :rating "7.4",
        :netflixid "60000410"}
       {:largeimage "http://cdn1.nflximg.net/images/0363/21490363.jpg",
        :type "movie",
        :title "The Breakfast Club",
        :released "1985",
        :runtime "1h37m",
        :image
        "https://occ-0-2219-1361.1.nflxso.net/art/9e07b/e5c55c85cc2829af1d6c685ebb0867060a59e07b.jpg",
        :unogsdate "2019-03-01",
        :synopsis
        "The athlete, the brain, the criminal, the princess and the basket case break through the social barriers of high school during Saturday detention.<br><b>Expires on 2019-03-01</b>",
        :download "1",
        :imdbid "tt0088847",
        :rating "7.9",
        :netflixid "330210"}
       {:largeimage "http://cdn1.nflximg.net/images/6114/8536114.jpg",
        :type "movie",
        :title "King Kong",
        :released "2005",
        :runtime "3h7m",
        :image
        "https://occ-0-1007-1361.1.nflxso.net/art/32cda/91ca4f2b00a311623b51cd6e000a59cf4fe32cda.jpg",
        :unogsdate "2019-03-01",
        :synopsis
        "Peter Jackson's remake of the classic follows a group of adventurous explorers and filmmakers to Skull Island, where they search for a giant gorilla.<br><b>Expires on 2019-03-01</b>",
        :download "1",
        :imdbid "tt0360717",
        :rating "7.2",
        :netflixid "70021664"}
       {:largeimage "http://cdn1.nflximg.net/images/7615/23737615.jpg",
        :type "movie",
        :title "Nacho Libre",
        :released "2006",
        :runtime "1h32m",
        :image
        "http://occ-1-3451-3446.1.nflxso.net/art/8ac3b/7d359c92bb9d42f0093bb96327ab161d7128ac3b.jpg",
        :unogsdate "2019-03-01",
        :synopsis
        "When Ignacio learns of an orphanage's financial woes, he pitches in to help -- by disguising himself and joining the professional wrestling circuit.<br><b>Expires on 2019-03-01</b>",
        :download "0",
        :imdbid "tt0457510",
        :rating "5.7",
        :netflixid "70044883"}
       {:largeimage "http://cdn0.nflximg.net/images/8562/8098562.jpg",
        :type "movie",
        :title "Ghostbusters",
        :released "1984",
        :runtime "1h45m",
        :image
        "https://occ-0-3451-3446.1.nflxso.net/art/6aae4/bfed5d19a915a8029e1a8de5dbf1af6f8946aae4.jpg",
        :unogsdate "2019-03-01",
        :synopsis
        "Parapsychologists create a business exterminating ghouls and hobgoblins -- and end up facing one killer demon in a cellist's apartment.<br><b>Expires on 2019-03-01</b>",
        :download "1",
        :imdbid "tt0087332",
        :rating "7.8",
        :netflixid "541018"}
       {:largeimage "",
        :type "movie",
        :title "Fair Game (2010)"https://occ-0-3451-3446.1.nflxso.net/art/54660/d0e80ced71e97935aced1022c997ddc622354660.jpg",
        :unogsdate "2019-03-01",
        :synopsis
        "Director Doug Liman re-cuts his true-life spy thriller about the politically motivated unmasking of undercover CIA operative Valerie Plame.<br><b>Expires on 2019-03-01</b>",
        :download "0",
        :imdbid "tt0977855",
        :rating "6.8",
        :netflixid "81036150"}
       {:largeimage "http://cdn0.nflximg.net/images/7694/8127694.jpg",
        :type "movie",
        :title "Ghostbusters 2",
        :released "1989",
        :runtime "1h48m",
        :image
        "https://occ-0-3451-3446.1.nflxso.net/art/f5e86/b69ddc6a38c74830f162830775cbed88643f5e86.jpg",
        :unogsdate "2019-03-01",
        :synopsis
        "This engaging sequel finds the ghostbusting trio saving the Big Apple from a massive slime attack and a flood of evil spirits on New Year's Eve.<br><b>Expires on 2019-03-01</b>",
        :download "1",
        :imdbid "tt0097428",
        :rating "6.5",
        :netflixid "541027"}
       {:largeimage "http://cdn0.nflximg.net/images/6440/3586440.jpg",
        :type "movie",
        :title "Cape Fear",
        :released "1991",
        :runtime "2h7m",
        :image
        "https://occ-0-1007-1361.1.nflxso.net/art/2e5d4/a7943b3b271b8b8eb2e323286c83fcbcbbe2e5d4.jpg",
        :unogsdate "2019-03-01",
        :synopsis
        "After serving a hellish 14-year prison sentence for a brutal rape, sadistic Max Cady seeks revenge on the defense attorney from his trial.<br><b>Expires on 2019-03-01</b>",
        :download "1",
        :imdbid "tt0101540",
        :rating "7.3",
        :netflixid "60010202"}
       {:largeimage "http://cdn0.nflximg.net/images/8122/8308122.jpg",
        :type "movie",
        :title "United 93",
        :released "2006",
        :runtime "1h46m",
        :image
        "https://occ-0-3451-3446.1.nflxso.net/art/a520a/13f703f3a37e24a9bf270b826d601694d2ca520a.jpg",
        :unogsdate "2019-03-01",
        :synopsis
        "Re-creating the harrowing events aboard United Airlines Flight 93 in real time, Paul Greengrass presents the devastating drama of Sept. 11, 2001.<br><b>Expires on 2019-03-01</b>",
        :download "1",
        :imdbid "tt0475276",
        :rating "7.6",
        :netflixid "70048592"}
       {:largeimage "",
        :type "movie",
        :title "Christine",
        :released "1983",
        :runtime "1h50m",
        :image
        "https://occ-0-358-360.1.nflxso.net/art/c6db7/3c156462579497aa79de22b5e7b625071b9c6db7.jpg",
        :unogsdate "2019-03-01",
        :synopsis
        "A geeky student restores a classic 1958 Plymouth Fury -- but once he gets behind the wheel, his newfound confidence turns to furious arrogance.<br><b>Expires on 2019-03-01</b>",
        :download "0",
        :imdbid "tt0085333",
        :rating "6.7",
        :netflixid "70007667"}
       {:largeimage "http://cdn0.nflximg.net/images/8132/8208132.jpg",
        :type "movie",
        :title "Friday",
        :released "1995",
        :runtime "1h31m",
        :image
        "https://occ-0-3451-3446.1.nflxso.net/art/9fbb3/d92a613a013409168659495d5c3b9a9db049fbb3.jpg",
        :unogsdate "2019-03-01",
        :synopsis
        "Over the course of one Friday afternoon, two pot-smoking friends get into some crazy trouble in their South Central L.A. neighborhood.<br><b>Expires on 2019-03-01</b>",
        :download "0",
        :imdbid "tt0113118",
        :rating "7.3",
        :netflixid "525057"}
       {:largeimage "http://cdn1.nflximg.net/images/6751/22636751.jpg",
        :type "movie",
        :title "Astro Boy",
        :released "2009",
        :runtime "1h33m",
        :image
        "https://occ-0-1007-1361.1.nflxso.net/art/98e25/14b16027403dc929ab13556c5bd33f3a7e298e25.jpg",
        :unogsdate "2019-03-01",
        :synopsis
        "Astro Boy, a young robot with superpowers, embarks on a dangerous odyssey in search of purpose before returning home to save his loved ones.<br><b>Expires on 2019-03-01</b>",
        :download "1",
        :imdbid "tt0375568",
        :rating "6.3",
        :netflixid "70108989"}
       {:largeimage "http://cdn1.nflximg.net/images/1669/11431669.jpg",
        :type "movie",
        :title "Friday After Next",
        :released "2002",
        :runtime "1h24m",
        :image
        "https://occ-0-3451-3446.1.nflxso.net/art/10bd4/f48bc81d09be454aa3abd8df519113f1ff610bd4.jpg",
        :title "Sniper",
        :released "1993",
        :runtime "1h39m",
        :image
        "https://occ-0-3451-3446.1.nflxso.net/art/3774a/8ae2ec4946b11d36687039bfd162d286d8b3774a.jpg",
        :unogsdate "2019-03-01",
        :synopsis
        "A veteran Marine sniper and an Olympic-medalist marksman team up to assassinate a Noriega-like revolutionary and his crew in the jungles of Panama.<br><b>Expires on 2019-03-01</b>",
        :download "1",
        :imdbid "tt0108171",
        :rating "6.1",
        :netflixid "976522"}
       {:largeimage "http://cdn1.nflximg.net/images/4415/25014415.jpg",
        :type "movie",
        :title "Next Friday",
        :released "2000",
        :runtime "1h38m",
        :image
        "http://occ-1-3451-3446.1.nflxso.net/art/b9b88/312bf529df9415140301ec5185d16b17c04b9b88.jpg",
        :unogsdate "2019-03-01",
        :synopsis
        "Streetwise Craig Jones is still rooming at his parents' house in South Central Los Angeles when he learns that his archnemesis has broken out of jail.<br><b>Expires on 2019-03-01</b>",
        :download "0",
        :imdbid "tt0195945",
        :rating "6.1",
        :netflixid "60000424"}
       {:largeimage "http://cdn0.nflximg.net/images/0704/8070704.jpg",
        :type "movie",
        :title "The Strangers",
        :released "2008",
        :runtime "1h25m",
        :image
        "https://occ-0-3451-3446.1.nflxso.net/art/662c4/36243edb59e52872242473554d1e91c6863662c4.jpg",
        :unogsdate "2019-03-01",
        :synopsis
        "A young couple welcomes the peace and quiet of a secluded vacation home until three masked invaders make them realize how dangerous isolation can be.<br><b>Expires on 2019-03-01</b>",
        :download "0",
        :imdbid "tt0482606",
        :rating "6.2",
        :netflixid "70060008"}
       {:largeimage "",
        :type "movie",
        :title "The Scorpion King 2: Rise of a Warrior",
        :released "2008",
        :runtime "1h48m",
        :image
        "https://occ-0-3451-3446.1.nflxso.net/art/a8c36/57a2ca3643c57f42d61595bf76d274a3c45a8c36.jpg",
        :unogsdate "2019-03-01",
        :synopsis
        "This action-driven prequel tells the heroic tale of young Mathayus and his relentless quest for justice against powerful villain King Sargon.<br><b>Expires on 2019-03-01</b>",
        :download "0",
        :imdbid "tt1483835",
        :rating "5.7",
        :netflixid "70099816"}
       {:largeimage "http://cdn1.nflximg.net/images/7188/8637188.jpg",
        :type "movie",
        :title "The Bone Collector",
        :released "1999",
        :runtime "1h57m",
        :image
        "https://occ-0-3451-3446.1.nflxso.net/art/1cec9/b25ea88ef24dda8e1db55e766d2e5ea134a1cec9.jpg",
        :unogsdate "2019-03-01",
        :synopsis
        "A rookie cop reluctantly teams with a paralyzed ex-detective to catch a grisly serial killer dubbed the Bone Collector in this crime thriller.<br><b>Expires on 2019-03-01</b>",
        :download "1",
        :imdbid "tt0145681",
        :rating "6.7",
        :netflixid "27828200"}
       {:largeimage "http://cdn1.nflximg.net/images/2133/8282133.jpg",
        :type "movie",
        :title "The Dark Knight",
        :released "2008",
        :runtime "2h32m",
        :image
        "https://occ-0-2219-1361.1.nflxso.net/art/8773a/eea0bff6b25b0b100599f785c7a84e7d5618773a.jpg",
        :unogsdate "2019-03-01",
        :synopsis
        "As Batman, Lt. Gordon and the district attorney continue to dismantle Gotham's criminal underground, a new villain threatens to undo their good work.<br><b>Expires on 2019-03-01</b>",
        :download "0",
        :imdbid "tt0468569",
        :rating "9",
        :netflixid "70079583"}
       {:largeimage "http://cdn0.nflximg.net/images/1934/22681934.jpg",
        :type "series",
        :title "Law and Order: Special Victims Unit",
        :released "1999",
        :runtime "",
        :image
        "http://occ-0-3451-3446.1.nflxso.net/art/76ef2/e6d075e9f3f625df0b80d38ddfa7799221576ef2.jpg",
        :unogsdate "2019-03-02",
        :synopsis
        "This edgy police procedural follows members of the Special Victims Unit as they investigate sexual offenses such as rape, incest and pedophilia.<br><b>Expires on 2019-03-02</b>",
        :download "1",
        :imdbid "tt0203259",
        :rating "8",
        :netflixid "70140403"}
       {:largeimage "http://cdn1.nflximg.net/images/0359/11980359.jpg",
        :type "series",
        :title "Houdini",
        :released "2014",
        :runtime "",
        :image
        "http://occ-1-3451-3446.1.nflxso.net/art/c5ae0/1c61799a0780bae7e966261d04f724176b7c5ae0.jpg",
        :unogsdate "2019-03-02",
        :synopsis
        "This miniseries delves into the celebrated feats and complex personal life of Harry Houdini, who captivated the world with his death-defying escapes.<br><b>Expires on 2019-03-02</b>",
        :download "1",
        :imdbid "tt3132738",
        :rating "7.4",
        :netflixid "80024227"}
       {:largeimage "http://cdn0.nflximg.net/images/9758/8319758.jpg",
        :type "series",
        :title "Drop Dead Diva",
        :released "2009",
        :runtime "",
        :image
        "http://occ-2-3451-3446.1.nflxso.net/art/e8ea8/9f037ef2ea0c1633c65e6e1da5358246a82e8ea8.jpg",
        :unogsdate "2019-03-03",
        :synopsis
        "Shallow, stick-thin Deb Dobkins learns lessons about the joys of compassion and intelligence after she's reincarnated as a smart, plus-size lawyer.<br><b>Expires on 2019-03-03</b>",
        :download "1",
        :imdbid "tt1280822"    :rating "7.4",
        :netflixid "70155596"}
       {:largeimage "http://cdn0.nflximg.net/images/2264/12962264.jpg",
        :type "series",
        :title "One Child",
        :released "2014",
        :runtime "",
        :image
        "http://occ-0-3451-3446.1.nflxso.net/art/fd94c/10842500c47036591ca41e4e2d7680b0fbdfd94c.jpg",
        :unogsdate "2019-03-06",
        :synopsis
        "At the request of her birth mother, adoptee Mei leaves her home in Britain to visit China, where she finds her brother faces an unjust death sentence.<br><b>Expires on 2019-03-06</b>",
        :download "0",
        :imdbid "tt3864896",
        :rating "6.6",
        :netflixid "80020404"}
       {:largeimage "",
        :type "series",
        :title "Race of Life",
        :released "2015",
        :runtime "",
        :image
        "http://occ-1-3451-3446.1.nflxso.net/art/1339b/f1441252f075b6681115a419909eff8c30d1339b.jpg",
        :unogsdate "2019-03-07",
        :synopsis
        "This series explores wild animals' continual struggle to survive and the varied strategies used by creatures both big and small to endure and thrive.<br><b>Expires on 2019-03-07</b>",
        :download "0",
        :imdbid "tt6767048",
        :rating "6.1",
        :netflixid "80156476"}
       {:largeimage "",
        :type "series",
        :title "Luxury Travel Show",
        :released "2016",
        :runtime "",
        :image
        "http://occ-1-1490-1489.1.nflxso.net/art/6d2a1/7f550c6cc9970e8066479f9999a2bc90d9e6d2a1.jpg",
        :unogsdate "2019-03-07",
        :synopsis
        "Experience the VIP treatment at some of the world's grandest and most lavish travel destinations, from Barbados to Barcelona.<br><b>Expires on 2019-03-07</b>",
        :download "0",
        :imdbid "notfound",
        :rating "",
        :netflixid "80203249"}
       {:largeimage "http://cdn0.nflximg.net/images/7070/12927070.jpg",
        :type "series",
        :title "The Returned",
        :released "2015",
        :runtime "",
        :image
        "http://occ-0-3451-3446.1.nflxso.net/art/0d6e0/f5171998efbc9ae1ae1f2dbed0b430c299d0d6e0.jpg",
        :unogsdate "2019-03-09",
        :synopsis
        "Several people come back to their home town in the same week after they've been dead for years in this eerie dramatic series.<br><b>Expires on 2019-03-09</b>",
        :download "0",
        :imdbid "tt3230780",
        :rating "7.1",
        :netflixid "80037657"}
       {:largeimage "",
        :type "series",
        :title "Lockup: Extended Stay",
        :released "2017",
        :runtime "",
        :image
        "http://occ-1-3451-3446.1.nflxso.net/art/bd306/78dcb25dfb4890b3093a6638fa8dab52916bd306.jpg",
        :unogsdate "2019-03-09",
        :synopsis
        "This documentary series offers a glimpse into the day-to-day lives of inmates and officers in detention centers all across America.<br><b>Expires on 2019-03-09</b>",
        :download "0",
        :imdbid "tt0446847",
        :rating "7.8",
        :netflixid "80157796"}
       {:largeimage "",
        :type "series",
        :title "Panama Canal: Prized Possession",
        :released "2015",
        :runtime "",
        :image
        "http://occ-2-1490-1489.1.nflxso.net/art/de351/36face21e049cbd4eb385da98f3e6d5b7c9de351.jpg",
        :unogsdate "2019-03-14",
        :synopsis
        "This documentary exa:type "series",
        :title "Monster Fish",
        :released "2014",
        :runtime "",
        :image
        "http://occ-0-3451-3446.1.nflxso.net/art/90abe/25f119af41ee705b1792f28593eab4ce9a890abe.jpg",
        :unogsdate "2019-03-15",
        :synopsis
        "From Australia's river shark to Nicaragua's killer tarpon, biologist Zeb Hogan travels the globe in search of the biggest, fiercest fresh water fish.<br><b>Expires on 2019-03-15</b>",
        :download "0",
        :imdbid "tt2017931",
        :rating "7",
        :netflixid "80088543"}
       {:largeimage "",
        :type "series",
        :title "Underworld, Inc.",
        :released "2015",
        :runtime "",
        :image
        "http://occ-0-3451-3446.1.nflxso.net/art/9d4c5/dd982cf691777f6383fdbd9ff4824aeae269d4c5.jpg",
        :unogsdate "2019-03-15",
        :synopsis
        "Get an insider's access to illegal activities and black markets around the world, where drugs, firearms and even people are mere commodities.<br><b>Expires on 2019-03-15</b>",
        :download "0",
        :imdbid "tt4480608",
        :rating "7.3",
        :netflixid "80088564"}
       {:largeimage "http://cdn1.nflximg.net/images/8141/13028141.jpg",
        :type "series",
        :title "Brain Games",
        :released "2011",
        :runtime "",
        :image
        "https://occ-0-3451-3446.1.nflxso.net/art/3e882/0d3d39671810c105a9a2988eeb0a971799f3e882.jpg",
        :unogsdate "2019-03-15",
        :synopsis
        "This interactive series uses games, illusions and experiments to illustrate how our brains manufacture our reality and often play tricks on us.<br><b>Expires on 2019-03-15</b>",
        :download "0",
        :imdbid "tt2078690",
        :rating "8.4",
        :netflixid "80029103"}
       {:largeimage "http://cdn1.nflximg.net/images/5401/9295401.jpg",
        :type "series",
        :title "Alaska State Troopers",
        :released "2009",
        :runtime "",
        :image
        "http://occ-2-3451-3446.1.nflxso.net/art/62be4/9b45f557dedf6d34848f1ae304e7b3ebdea62be4.jpg",
        :unogsdate "2019-03-15",
        :synopsis
        "Chronicling the adventures of Alaska state troopers, this reality series puts a cold-weather spin on the everyday challenges of law enforcement.<br><b>Expires on 2019-03-15</b>",
        :download "0",
        :imdbid "tt1531068",
        :rating "7.6",
        :netflixid "70179953"}
       {:largeimage "",
        :type "series",
        :title "Hive Alive",
        :released "2014",
        :runtime "",
        :image
        "http://occ-1-3451-3446.1.nflxso.net/art/017bb/53da5ba3a38a773b1b892cdac380826b1e6017bb.jpg",
        :unogsdate "2019-03-15",
        :synopsis
        "Using advanced technology, a team of field experts joins hosts Chris Packham and Martha Kearney as they explore the complex world of honeybees.<br><b>Expires on 2019-03-15</b>",
        :download "0",
        :imdbid "tt3885974",
        :rating "8.8",
        :netflixid "80150137"}
       {:largeimage "",
        :type "series",
        :title "Mega Builders",
        :released "2010",
        :runtime "",
        :image
        "http://occ-0-3451-3446.1.nflxso.net/art/02484/a05d3295f3ddaaa9fc22a996b8dd44a88cc02484.jpg",
        :unogsdate "2019-03-15",
        :synopsis
        "Follow engineering teams as they build the most dangerous, complex and amazing structures in modern history, using the latest scientific techniques.<br><b>Expires on 2019-03-15</b>",
        :download "0",
        :imdbid "tt0891831",
        :rating "7.4",
        :netflixid "70177692"}
       {:largeimage "",
        :type "series",
        :title "Alien Deep with Bob Ballard",
        :released "2012",
        :runtime "",
        :image
        "http://occ-2-3451-3446.1.nflxso.net/art/ca1a3/197b59a41cc6fc692bf916e39c23b788991ca1a3.jpg",
        :unogsdate "2019-03-15",
        :synopsis
        "Renowned ocean explorer Dr. Bob Ballard takes viewers to the deepest parts of the ocean, where priceless treasures both natural and man-made lurk.<br><b>Expires on 2019-03-15</b>",
        :download "0",
        :imdbid "tt2399098",
        :rating "6.8",
        :netflixid "80115278"}
       {:largeimage "http://cdn1.nflximg.net/images/6125/8676125.jpg",
        :type "series",
        :title "Locked Up Abroad",
        :released "2007",
        :runtime "",
        :image
        "http://occ-0-3451-3446.1.nflxso.net/art/673b7/2c073d7cbcdec528980bad65af4dce9bf27673b7.jpg",
        :unogsdate "2019-03-15",
        :synopsis
        "Experience the horrors and challenges facing travelers imprisoned abroad in this gritty documentary series from National Geographic.<br><b>Expires on 2019-03-15</b>",
        :download "",
        :imdbid "tt1020109",
        :rating "8.2",
        :netflixid "70264716"}
       {:largeimage "http://cdn0.nflximg.net/images/7142/8587142.jpg",
        :type "series",
        :title "The Almighty Johnsons",
        :released "2010",
        :runtime "",
        :image
        "http://occ-2-3451-3446.1.nflxso.net/art/22ecd/e1ef9c675109d33d9e0a47647f6bf943b3d22ecd.jpg",
        :unogsdate "2019-03-18",
        :synopsis
        "Inside each of the Johnson brothers beats the heart of a Norse god. But they can't fulfill their destinies until brother Axl finds his soul mate.<br><b>Expires on 2019-03-18</b>",
        :download "1",
        :imdbid "tt1752076",
        :rating "8.2",
        :netflixid "70266182"}
       {:largeimage "",
        :type "series",
        :title "Drugs, Inc.",
        :released "2015",
        :runtime "",
        :image
        "https://occ-0-3451-3446.1.nflxso.net/art/5fab9/8d477e4ab6f7cca06098036a243bedbeb1d5fab9.jpg",
        :unogsdate "2019-03-22",
        :synopsis
        "This absorbing documentary series follows the entire supply chain of the illicit drug trade, tracing its production, export, sale and consumption.<br><b>Expires on 2019-03-22</b>",
        :download "0",
        :imdbid "tt1688779",
        :rating "7.9",
        :netflixid "70251816"}]},
     :trace-redirects []}


<a id="org7ba0f58"></a>

### list-countries

List all countries in which uNoGS is collecting Netflix data. Adding
:available false will override the default, and list all countries with
active and historical data.

    (clunogs/list-countries)

    {:cached nil,
     :request-time 790,
     :repeatable? false,
     :protocol-version {:name "HTTP", :major 1, :minor 1},
     :streaming? true,
     :http-client
     #object[org.apache.http.impl.client.InternalHttpClient 0x2cb2582d "org.apache.http.impl.client.InternalHttpClient@2cb2582d"],
     :chunked? false,
     :reason-phrase "OK",
     :headers
     {:access-control-allow-origin "*",
      :content-type "application/json; charset=ISO-8859-1",
      :date "Sat, 23 Feb 2019 01:50:10 GMT",
      :server "RapidAPI-1.0.6",
      :x-ratelimit-requests-limit "100",
      :x-ratelimit-requests-remaining "78",
      :content-length "2662",
      :connection "Close"},
     :orig-content-encoding nil,
     :status 200,
     :length 2662,
     :body
     {:count "30",
      :items
      [["21"
        "ar"
        "Argentina "
        "24"
        "128"
        "48"
        "4180"
        "1240"
        "2940"
        "ARS"
        "109"
        "149"
        "189"]
       ["23"
        "au"
        "Australia "
        "31"
        "201"
        "87"
        "5393"
        "1712"
        "3681"
        "AUD"
        "8.99"
        "11.99"
        "14.99"]
       ["26"
        "be"
        "Belgium "
        "32"
        "250"
        "50"
        "3993"
        "1256"
        "2737"
        "EUR"
        "7.99"
        "9.99"
        "11.99"]
       ["29"
        "br"
        "Brazil "
        "21"
        "131"
        "52"
        "4071"
        "1189"
        "2882"
        "BRL"
        "19.9"
        "22.9"
        "29.9"]
       ["33"
        "ca"
        "Canada "
        "15"
        "190"
        "112"
        "5700"
        "1659"
        "4041"
        "CAD"
        "7.99"
        "9.99"
        "11.99"]
       ["307"
        "cz"
        "Czech Republic "
        "21"
        "148"
        "45"
        "4710"
        "1496"
        "3214"
        "EUR"
        "7.99"
        "9.99"
        "11.99"]
       ["45"
        "fr"
        "France "
        "21"
        "176"
        "40"
        "3773"
        "1224"
        "2549"
        "EUR"
        "7.99"
        "9.99"
        "11.99"]
       ["39"
        "de"
        "Germany "
        "16"
        "105"
        "38"
        "3790"
        "1102"
        "2688"
        "EUR"
        "7.99"
        "9.99"
        "11.99"]
       ["327"
        "gr"
        "Greece "
        "20"
        "123"
        "37"
        "3969"
        "1221"
        "2748"
        "EUR"
        "7.99"
        "9.99"
        "11.99"]
       ["331"
        "hk"
        "Hong Kong "
        "19"
        "104"
        "34"
        "3497"
        "1143"
        "235
        "mx"
        "Mexico "
        "24"
        "127"
        "48"
        "4162"
        "1231"
        "2931"
        "MXN"
        "99"
        "129"
        "159"]
       ["67"
        "nl"
        "Netherlands "
        "24"
        "182"
        "52"
        "3645"
        "1052"
        "2593"
        "EUR"
        "7.99"
        "9.99"
        "11.99"]
       ["392"
        "pl"
        "Poland "
        "17"
        "102"
        "35"
        "3090"
        "966"
        "2124"
        "PLN"
        "34"
        "43"
        "52"]
       ["400"
        "ro"
        "Romania "
        "26"
        "174"
        "35"
        "4274"
        "1275"
        "2999"
        "EUR"
        "7.99"
        "9.99"
        "11.99"]
       ["402"
        "ru"
        "Russia"
        "23"
        "144"
        "48"
        "4571"
        "1490"
        "3081"
        "EUR"
        "7.99"
        "9.99"
        "11.99"]
       ["408"
        "sg"
        "Singapore "
        "26"
        "145"
        "48"
        "4299"
        "1406"
        "2893"
        "SGD"
        "10.98"
        "13.98"
        "16.98"]
       ["412"
        "sk"
        "Slovakia "
        "26"
        "152"
        "51"
        "4717"
        "1501"
        "3216"
        "EUR"
        "7.99"
        "9.99"
        "11.99"]
       ["348"
        "kr"
        "South Korea"
        "16"
        "96"
        "29"
        "3929"
        "1079"
        "2850"
        "KRW"
        "9500"
        "12000"
        "14500"]
       ["270"
        "es"
        "Spain "
        "16"
        "117"
        "23"
        "3302"
        "1049"
        "2253"
        "EUR"
        "7.99"
        "9.99"
        "11.99"]
       ["73"
        "se"
        "Sweden "
        "10"
        "87"
        "48"
        "3405"
        "1014"
        "2391"
        "SEK"
        "79"
        "99"
        "119"]
       ["34"
        "ch"
        "Switzerland "
        "20"
        "156"
        "46"
        "4230"
        "1308"
        "2922"
        "CHF"
        "11.9"
        "14.9"
        "17.9"]
       ["425"
        "th"
        "Thailand "
        "25"
        "147"
        "46"
        "4437"
        "1408"
        "3029"
        "THB"
        "280"
        "350"
        "420"]
       ["46"
        "gb"
        "United Kingdom"
        "28"
        "190"
        "89"
        "5692"
        "1732"
        "3960"
        "GBP"
        "5.99"
        "7.49"
        "8.99"]
       ["78"
        "us"
        "United States"
        "17"
        "232"
        "95"
        "5902"
        "1811"
        "4091"
        "USD"
        "7.99"
        "9.99"
        "11.99"]]},
     :trace-redirects []}

The most important output here is the first and second items in each vector,
being the numerical and alphabetical country codes, respectively.


<a id="org68e1862"></a>

### new-releases

All items released during daysback in `countryid`.

    (clunogs/new-releases 1 "US")

    {:cached nil,
     :request-time 837,
     :repeatable? false,
     :protocol-version {:name "HTTP", :major 1, :minor 1},
     :streaming? true,
     :http-client
     #object[org.apache.http.impl.client.InternalHttpClient 0xd59490a "org.apache.http.impl.client.InternalHttpClient@d59490a"],
     :chunked? false,
     :reason-phrase "OK",
     :headers
     {:access-control-allow-origin "*",
      :content-type "application/json; charset=ISO-8859-1",
      :date "Fri, 22 Feb 2019 23:22:10 GMT",
      :server "RapidAPI-1.0.6",
      :x-ratelimit-requests-limit "100",
      :x-ratelimit-requests-remaining "84",
      :content-length "4189",
      :connection "Close"},
     :orig-content-encoding nil,
     :status 200,
     :length 4189,
     :body
     {:count "1",
      :items
      [{:largeimage "",
        :type "movie",
        :title "Trespass Against Us",
        :released "2016",
        :runtime "1h39m",
        :image
        "https://occ-0-3451-3446.1.nflxso.net/art/8ad00/bc36395deb410871cda447f046ac7c1aed28ad00.jpg",
        :unogsdate "2019-02-22",
        :synopsis
        "A man from a criminal family yearns to break away and find a better life, but his father's staunch opposition puts his dreams of freedom in jeopardy.",
        :download "0",
        :imdbid "tt3305308",
        :rating "5.8",
        :netflixid "80057509"}]},
     :trace-redirects []}


<a id="org0c82481"></a>

### season-changes

All series that have had a change to their seasons during daysback in `countryid`.

    (clunogs/season-changes 1 "US")

    {:cached nil,
     :request-time 934,
     :repeatable? false,
     :protocol-version {:name "HTTP", :major 1, :minor 1},
     :streaming? true,
     :http-client
     #object[org.apache.http.impl.client.InternalHttpClient 0x63a8436c "org.apache.http.impl.client.InternalHttpClient@63a8436c"],
     :chunked? false,
     :reason-phrase "OK",
     :headers
     {:access-control-allow-origin "*",
      :content-type "application/json; charset=ISO-8859-1",
      :date "Sat, 23 Feb 2019 01:53:45 GMT",
      :server "RapidAPI-1.0.6",
      :x-ratelimit-requests-limit "100",
      :x-ratelimit-requests-remaining "77",
      :content-length "2596",
      :connection "Close"},
     :orig-content-encoding nil,
     :status 200,
     :length 2596,
     :body
     {:count "5",
      :items
      [{:largeimage "",
        :type "series",
        :title "The Big Family Cooking Showdown",
        :released "2017",
        :runtime "",
        :image
        "https://occ-0-3451-3446.1.nflxso.net/art/a3c5b/aa8c75893c6977b1e48e9f640950534ca63a3c5b.jpg",
        :unogsdate "2019-02-22",
        :synopsis
        "In this unscripted series, families passionate about food serve up their most delicious dishes for the chance to be crowned Britain's best home cooks.<br><b>From 1 to 2 Seasons</b><br>2019-02-22 20:51:42",
        :download "0",
        :imdbid "tt7518558",
        :rating "6.9",
        :netflixid "80186090"}
       {:largeimage "",
        :type "series",
        :title "Rebellion",
        :released "2016",
        :runtime "",
        :image
        "https://occ-0-3451-3446.1.nflxso.net/art/6fd24/521c9930f287e831b80fb22bd795dfb96fc6fd24.jpg",
        :unogsdate "2019-02-22",
        :synopsis
        "As World War I rages, three women and their families in Dublin choose sides in the violent Easter Rising revolt against British rule.<br><b>From 1 to 2 Seasons</b><br>2019-02-22 22:05:01",
        :download "1",
        :imdbid "tt4699982",
        :rating "7.1",
        :netflixid "80094273"}
       {:largeimage "",
        :type "series",
        :title "Suburra: Blood on Rome",
        :released "2017",
        :runtime "",
        :image
        "https://occ-0-3451-3446.1.nflxso.net/art/15d61/9a10d5cfbe098358f3b55dbae6fe081349915d61.jpg",
        :unogsdate "2019-02-22",
        :synopsis
        "In 2008, a fight over land in a seaside town near Rome spirals into a deadly battle between organized crime, corrupt politicians and the Vatican.<br><b>From 1 to 2 Seasons</b><br>2019-02-22 23:27:29",
        :download "0",
        :imdbid "notfound",
        :rating "",
        :netflixid "80081537"}
       {:largeimage "http://cdn1.nflximg.net/images/7123/20977123.jpg",
        :type "series",
        :title "Chef's Table",
        :released "2015",
        :runtime "",
        :image
        "https://occ-0-3451-3446.1.nflxso.net/art/3b8ff/4e20da330264e176da683ab34af6ac6d0743b8ff.jpg",
        :unogsdate "2019-02-22",
        :synopsis
        "Find out what’s inside the kitchens and minds of six international culinary stars in this Netflix original six-part docu-series.<br><b>From 5 to 6 Seasons</b><br>2019-02-22 23:36:44",
        :download "1",
        :imdbid "tt4295140",
        :rating "8.6",
        :netflixid "80007945"}
       {:largeimage "",
        :type "series",
        :title "The Kindness Diaries",
        :released "2015",
        :runtime "",
        :image
        "https://occ-0-3451-3446.1.nflxso.net/art/506e8/9605aebb25b0ac59f522c48ae12087bfc69506e8.jpg",
        :unogsdate "2019-02-23",
        :synopsis
        "Host Leon Logothetis travels the world with only a vintage motorbike and the kindness of strangers, which he pays back in unexpected, inspiring ways.<br><b>From 1 to 2 Seasons</b><br>2019-02-23 00:13:20",
        :download "1",
        :imdbid "notfound",
        :rating "",
        :netflixid "80156137"}]},
     :trace-redirects []}


<a id="org3f514af"></a>

### advanced-search

For a full listing of optional parameters, see the namespace documentation
for this function.

In addition to many individual search parameters, other queries can be
embedded in this search. Emulating, new-releases, `season-changes`, `expiring`
and title based search. The syntax for this is to pass a map with a key of
:type and a value of `:new-releases`, `:season-changes`, `:expiring` or `:title`
respectively. The rest of the map contains keys and values corresponding to
the parameters of that query.

For example, search for all newly added movies within the last 7 days in the
US with Spanish subtitles and released in or after 2015.

    (clunogs/advanced-search :query {:type :new-releases :daysback 7}
                             :vtype "Movie"
                             :subtitle "Spanish"
                             :syear 2015)

    {:cached nil,
     :request-time 1990,
     :repeatable? false,
     :protocol-version {:name "HTTP", :major 1, :minor 1},
     :streaming? true,
     :http-client
     #object[org.apache.http.impl.client.InternalHttpClient 0x3717dd30 "org.apache.http.impl.client.InternalHttpClient@3717dd30"],
     :chunked? false,
     :reason-phrase "OK",
     :headers
     {:access-control-allow-origin "*",
      :content-type "application/json; charset=ISO-8859-1",
      :date "Sat, 23 Feb 2019 02:39:04 GMT",
      :server "RapidAPI-1.0.6",
      :x-ratelimit-requests-limit "100",
      :x-ratelimit-requests-remaining "42",
      :content-length "3146",
      :connection "Close"},
     :orig-content-encoding nil,
     :status 200,
     :length 3146,
     :body
     {:count "7",
      :items
      [{:largeimage "",
        :type "movie",
        :title "Firebrand",
        :released "2019",
        :runtime "1h56m",
        :image
        "https://occ-0-1168-1217.1.nflxso.net/art/9106f/c9ade357ae38c836aaaa6b02e5885ceb9d79106f.jpg",
        :unogsdate "2019-02-22",
        :synopsis
        "While she successfully advocates for her female clients in difacts her own marriage.",
        :download "0",
        :imdbid "",
        :rating "",
        :netflixid "81026327"}
       {:largeimage "",
        :type "movie",
        :title "Paddleton",
        :released "2019",
        :runtime "1h29m",
        :image
        "https://occ-0-64-58.1.nflxso.net/art/af8e0/96d1e6c32f1a576decabc74e632779fad60af8e0.jpg",
        :unogsdate "2019-02-22",
        :synopsis
        "After he's diagnosed with terminal cancer, middle-aged Michael asks his neighbor friend Andy to help him end his life before the disease does.",
        :download "0",
        :imdbid "",
        :rating "",
        :netflixid "80224060"}
       {:largeimage "",
        :type "movie",
        :title "Paris Is Us",
        :released "2019",
        :runtime "1h24m",
        :image
        "https://occ-0-768-769.1.nflxso.net/art/81a83/f21b3c841207817f07bdb70ce8ef8ed723581a83.jpg",
        :unogsdate "2019-02-22",
        :synopsis
        "Amid a turbulent romance and rising tensions in Paris, a young woman finds herself caught in a dizzying spiral of dreams, memories and what-ifs.",
        :download "0",
        :imdbid "",
        :rating "",
        :netflixid "81027190"}
       {:largeimage "",
        :type "movie",
        :title "The Photographer Of Mauthausen",
        :released "2018",
        :runtime "1h50m",
        :image
        "https://occ-0-64-58.1.nflxso.net/art/91bcc/3cd13bfcf5444ba33dc52a9deb03b2f1cfd91bcc.jpg",
        :unogsdate "2019-02-22",
        :synopsis
        "A Catalán prisoner at a Nazi concentration camp uses his office job to steal photo negatives of the atrocities committed there. Based on true events.",
        :download "0",
        :imdbid "",
        :rating "",
        :netflixid "80191608"}
       {:largeimage "",
        :type "movie",
        :title "The Drug King",
        :released "2018",
        :runtime "2h18m",
        :image
        "https://occ-0-1007-1361.1.nflxso.net/art/83118/086d89110fdcd619afe1648c5bffdb19e5c83118.jpg",
        :unogsdate "2019-02-21",
        :synopsis
        "A petty smuggler from Busan dives headfirst into illicit drug trafficking in the 1970s and rises to become king of narcotics exports to Japan.",
        :download "0",
        :imdbid "",
        :rating "",
        :netflixid "80236133"}
       {:largeimage "",
        :type "movie",
        :title "El ángel",
        :released "2018",
        :runtime "1h54m",
        :image
        "https://occ-0-2717-360.1.nflxso.net/art/5bd60/691d96f4d9ae7ecae52ee728fe97f7a42225bd60.jpg",
        :unogsdate "2019-02-20",
        :synopsis
        "In 1971 Buenos Aires, cherub-faced teen Carlitos goes from burglary to serial murder after meeting kindred spirit Ramón. Inspired by true events.",
        :download "0",
        :imdbid "tt7204348",
        :rating "7.2",
        :netflixid "80225411"}
       {:largeimage "",
        :type "movie",
        :title "Zero",
        :released "2018",
        :runtime "2h39m",
        :image
        "https://occ-0-979-38.1.nflxso.net/art/342fe/ae7db924f2fc1bdee4cf55a6271c670c585342fe.jpg",
        :unogsdate "2019-02-20",
        :synopsis
        "Through his relationships with two wildly different women, a vertically challenged bachelor with a larger-than-life persona must discover his purpose.",
        :download "0",
        :imdbid "tt6527426",
        :rating "6",
        :netflixid "81005364"}]},
     :trace-redirects []}

Here is a much simpler search for an item with the title "Bird Box". Note
that titles are fuzzy matched.

    (clunogs/advanced-search :query {:type :title :title "Bird Box"})

    {:cached nil,
     :request-time 1850,
     :repeatable? false,
     :protocol-version {:name "HTTP", :major 1, :minor 1},
     :streaming? true,
     :http-client
     #object[org.apache.http.impl.client.InternalHttpClient 0x7c8312e "org.apache.http.impl.client.InternalHttpClient@7c8312e"],
     :chunked? false,
     :reason-phrase "OK",
     :headers
     {:access-control-allow-origin "*",
      :content-type "application/json; charset=ISO-8859-1",
      :date "Sat, 23 Feb 2019 03:05:40 GMT",
      :server "RapidAPI-1.0.6",
      :x-ratelimit-requests-limit "100",
      :x-ratelimit-requests-remaining "33",
      :content-length "9888",
      :connection "Close"},
     :orig-content-encoding nil,
     :status 200,
     :length 9888,
     :body
     {:count "21",
      :items
      [{:largeimage "",
        :type "movie",
        :title "Bird Box",
        :released "2018",
        :runtime "2h4m",
        :image
        "https://occ-0-1223-58.1.nflxso.net/art/05b9a/2086f83c4da613f3e986d8b584a60e5759a05b9a.jpg",
        :unogsdate "2018-12-21",
        :synopsis
        "Five years after an ominous unseen presence drives most of society to suicide, a survivor and her two children make a desperate bid to reach safety.",
        :download "0",
        :imdbid "tt2737304",
        :rating "6.7",
        :netflixid "80196789"}
       {:largeimage "",
        :type "movie",
        :title "The Big Bird Cage",
        :released "1972",
        :runtime "1h35m",
        :image
        "https://occ-0-1007-1361.1.nflxso.net/art/ecdec/3aa7a40e48670b269c4945759ca41e989ddecdec.jpg",
        :unogsdate "2017-12-29",
        :synopsis
        "A buxom bad girl and her radical guerilla boyfriend devise a plan to liberate inmates at a local women's prison to satisfy the mercenary's friends.",
        :download "0",
        :imdbid "tt0068273",
        :rating "6.1",
        :netflixid "60021919"}
       {:largeimage "",
        :type "series",
        :title "Love Bird",
        :released "2013",
        :runtime "",
        :image
        "https://occ-0-2219-1361.1.nflxso.net/art/dbec7/fe1bf1bb8de4e43a8c2cd467bf4d074694bdbec7.jpg",
        :unogsdate "2016-09-30",
        :synopsis
        "Spending school vacations with her aunt, feisty orphan Feride falls for her sophisticated older cousin, Kamran. Love blossoms, but can it flourish?",
        :download "0",
        :imdbid "tt4471694",
        :rating "8.2",
        :netflixid "80106774"}
       {:largeimage "",
        :type "movie",
        :title "A YELLOW BIRD",
        :released "2016",
        :runtime "1h50m",
        :image
        "https://occ-0-38-1501.1.nflxso.net/art/878da/44c365a2fd65a9d165a42d08c86336bf7ba878da.jpg",
        :unogsdate "2017-05-01",
        :synopsis
        "In Singapore, a homeless ex-convict hoping to reunite with his family forms a bond with a Chinese sex worker while serving as her bodyguard.",
        :download "0",
        :imdbid "tt5709164",
        :rating "5.5",
        :netflixid "80115133"}
       {:largeimage "",
        :type "movie",
        :title "National Bird",
        :released "2016",
        :runtime "1h31m",
        :image
        "https://occ-0-768-769.1.nflxso.net/art/7c7a9/81a1f1490c716b1bed868827cf0ba6f953e7c7a9.jpg",
        :unogsdate "2017-05-08",
        :synopsis
        "Three former military operatives offer disturbing, firsthand accounts of the deadly impact th"Sent to live with the Dodo family, Big Bird becomes homesick and lonely, and embarks on a cross-country journey back to his friends on Sesame Street.",
        :download "0",
        :imdbid "tt0089994",
        :rating "6.7",
        :netflixid "60021960"}
       {:largeimage "http://cdn0.nflximg.net/images/7264/13047264.jpg",
        :type "movie",
        :title "White Bird in a Blizzard",
        :released "2014",
        :runtime "1h31m",
        :image
        "https://occ-0-300-2773.1.nflxso.net/art/712b0/4c944a092856ccc513ea916b3cc41addfd4712b0.jpg",
        :unogsdate "2015-04-14",
        :synopsis
        "When 17-year-old Kat Conners's mom, Eve, vanishes, the teen relishes her newfound freedom -- until the reality of her loss sets in.",
        :download "1",
        :imdbid "tt2238050",
        :rating "6.4",
        :netflixid "70299866"}
       {:largeimage "",
        :type "movie",
        :title "High Flying Bird",
        :released "2019",
        :runtime "1h30m",
        :image
        "https://occ-0-1223-58.1.nflxso.net/art/cf779/9a06bfcb39b203fb3303175be0771c9551bcf779.jpg",
        :unogsdate "2019-02-08",
        :synopsis
        "When an NBA lockout sidelines his big rookie client, an agent hatches a bold plan to save their careers -- and disrupt the league's power structure.",
        :download "0",
        :imdbid "tt8128188",
        :rating "6.4",
        :netflixid "80991400"}
       {:largeimage "http://cdn1.nflximg.net/images/8261/21998261.jpg",
        :type "movie",
        :title "Nasu: A Migratory Bird with Suitcase",
        :released "2007",
        :runtime "54m",
        :image
        "http://occ-0-2772-1007.1.nflxso.net/art/4fb5a/077f145f25f54ab1f0057db0bc108c4de7b4fb5a.jpg",
        :unogsdate "2015-09-01",
        :synopsis
        "In the wake of a tragedy, Team Pao Pao goes east for the Japan Cup, even as several teammates face uncertain futures.",
        :download "",
        :imdbid "notfound",
        :rating "",
        :netflixid "80072436"}
       {:largeimage "",
        :type "series",
        :title "Dave Chappelle: Equanimity and The Bird Revelation",
        :released "2017",
        :runtime "",
        :image
        "https://occ-0-300-2773.1.nflxso.net/art/b7790/db074f03e986a21cae0ff1831bef69da5a5b7790.jpg",
        :unogsdate "2017-12-31",
        :synopsis
        "Comedy titan Dave Chappelle caps a wild year with two stand-up specials packed with scorching new material, self-reflection and tough love.",
        :download "0",
        :imdbid "notfound",
        :rating "",
        :netflixid "80230402"}
       {:largeimage "",
        :type "movie",
        :title
        "Detective Conan: The Mystery of the Legendary Monster Bird",
        :released "2011",
        :runtime "1h33m",
        :image
        "https://occ-0-1007-1361.1.nflxso.net/art/6b1ff/b6f02d09de6efe4e40e84eba86d402660996b1ff.jpg",
        :unogsdate "2018-10-01",
        :synopsis
        "While visiting a mountainside village, Shinichi becomes involved in a deadly incident that the locals attribute to a legendary monstrous bird.",
        :download "0",
        :imdbid "notfound",
        :rating "",
        :netflixid "80236576"}
       {:largeimage "",
        :type "movie",
        :title "Healing",
        :released "2014",
        :runtime "1h59m",
        :image
        "https://occ-0-38-1501.1.nflxso.net/art/c7840/81b5c2412c1a275ee9596861d2936f72485c7840.jpg",
        :unogsdate "2017-11-15",
        :synopsis
        "After serving 18 years, a near-broken man is transferred to a low-security prison farm, where a bird-rehabilitation project offers him a glimpse of hope.",
        :download "0",
        :imdbid "tt2310792",
        :rating "6.7",
        :netflixid "80047655"}
       {:largeimage "",
        :type "movie",
        :title "Woody Woodpecker",
        :released "2017",
        :runtime "1h31m",
        :image
        "https://occ-0-3451-3446.1.nflxso.net/art/e22fb/7099e59b9fb2e6ec75fd512f0ef4d786c40e22fb.jpg",
        :unogsdate "2018-02-07",
        :synopsis
        "A rascally bird with a distinctive laugh pecks back with a vengeance when his forest habitat is threatened by a slick lawyer building his dream home.",
        :download "0",
        :imdbid "tt2114504",
        :rating "4.2",
        :netflixid "80225034"}
       {:largeimage "http://cdn0.nflximg.net/images/9246/11209246.jpg",
        :type "movie",
        :title "The Rescuers Down Under",
        :released "1990",
        :runtime "1h17m",
        :image
        "https://occ-0-768-769.1.nflxso.net/art/aaacb/1f742004c1adeab97f9838bfc7a3dec4c66aaacb.jpg",
        :unogsdate "2015-04-14",
        :synopsis
        "A poacher wants to capture a majestic and rare golden eagle, so he kidnaps the boy who knows how to find the bird, and the Rescuers must save the day.",
        :download "0",
        :imdbid "tt0100477",
        :rating "6.9",
        :netflixid "60001198"}
       {:largeimage "",
        :type "movie",
        :title "The Ornithologist",
        :released "2016",
        :runtime "1h53m",
        :image
        "https://occ-0-3451-3446.1.nflxso.net/art/bfd87/842d9947f7cc2467823c79d20a7e94312e0bfd87.jpg",
        :unogsdate "2017-10-04",
        :synopsis
        "While seeking an elusive bird, an ornithologist is swept away by rapids and has to contend with numerous -- and surreal -- perils in the woods.",
        :download "0",
        :imdbid "tt4929038",
        :rating "6.4",
        :netflixid "80186137"}
       {:largeimage "http://cdn1.nflximg.net/images/9063/8959063.jpg",
        :type "movie",
        :title "Attenbor3446.1.nflxso.net/art/d0803/2d772e627956dc55cfd63c64280cc167e83d0803.jpg",
        :unogsdate "2015-08-06",
        :synopsis
        "An orphaned bird tags along with a flock on their long migration to Africa and becomes a hero when his newfound 'family' runs into trouble.",
        :download "0",
        :imdbid "tt3526408",
        :rating "5.5",
        :netflixid "80018296"}
       {:largeimage "",
        :type "movie",
        :title "Beak and Brain: Genius Birds From Down Under",
        :released "2013",
        :runtime "52m",
        :image
        "https://occ-0-979-38.1.nflxso.net/art/22c4f/c51facee98db4bbcf82aab69030a1fc05b322c4f.jpg",
        :unogsdate "2017-03-01",
        :synopsis
        "Whoever came up with the term 'bird brain' never met these feathered thinkers, who use their claws and beaks to solve puzzles, make tools and more.",
        :download "0",
        :imdbid "tt4790094",
        :rating "8.4",
        :netflixid "80135631"}
       {:largeimage "",
        :type "series",
        :title "Chuck Chicken",
        :released "2017",
        :runtime "",
        :image
        "https://occ-0-2219-1361.1.nflxso.net/art/e6ca1/9fcb019ea2284108087753ee543e608c318e6ca1.jpg",
        :unogsdate "2017-11-15",
        :synopsis
        "On the island of Rocky Perch, security company owner Chuck Adoodledoo and his friends provide kung fu-style protection for their fellow bird citizens.",
        :download "0",
        :imdbid "notfound",
        :rating "",
        :netflixid "80202454"}
       {:largeimage "",
        :type "movie",
        :title "Mahabharat",
        :released "2013",
        :runtime "1h59m",
        :image
        "https://occ-0-64-58.1.nflxso.net/art/e9f00/5779c199aa963d2cf0c18a8baddc3cf9287e9f00.jpg",
        :unogsdate "2018-08-01",
        :synopsis
        "Two young brothers encounter a singing bird who treats them to a musical reinterpretation of one of India’s most epic ancient tales.",
        :download "0",
        :imdbid "tt3212600",
        :rating "8.8",
        :netflixid "81002214"}]},
     :trace-redirects []}


<a id="org33aeedf"></a>

### genre-ids

Get a map of all genres and their corresponding numerical ids. This is the
only request whose output is modified from the response JSON. Originally,
the content of :items is a vector of individual maps for each genre and
its ids. For convenience, this vector is merged into a single map containing
all of the genres.

    (clunogs/genre-ids)

    {:cached nil,
     :request-time 959,
     :repeatable? false,
     :protocol-version {:name "HTTP", :major 1, :minor 1},
     :streaming? true,
     :http-client
     #object[org.apache.http.impl.client.InternalHttpClient 0x47d00a60 "org.apache.http.impl.client.InternalHttpClient@47d00a60"],
     :chunked? false,
     :reason-phrase "OK",
     :headers
     {:access-control-allow-origin "*",
      :content-type "application/json; charset=ISO-8859-1",
      :date "Sat, 23 Feb 2019 02:53:56 GMT",
      :server "RapidAPI-1.0.6",
      :x-ratelimit-requests-limit "100",
      :x-ratelimit-requests-remaining "35",
      :content-length "18278",
      :connection "Close"},
     :orig-content-encoding nil,
     :status 200,
     :length 18278,
     :body
     {:count "517",
      :items
      {:foreign-comedies [4426],
       :crime-films [5824],
       :international-sci-fi-&-fantasy [852491],
       :bafta-award-winning-films [69946],
       :police-action-&-adventure [75418],
       :danish-documentaries [60026],
       :canadian-independent-films [56184],
       :political-documentaries [7018],
       :comic-book-and-superhero-movies [10118
        75459
        76507
        78628
        852493
        89804
        9299
        9847
        9873],
       :argentinian-tv-shows [69616],
       :steamy-thrillers [972],
       :canadian-comedies [56174],
       :anime-sci-fi [2729],
       :tv-sci-fi-&-fantasy [1372],
       :tv-animated-comedies [7992],
       :film-noir [7687],
       :all-thrillers
       [10306
        10499
        10504
        10719
        11014
        11140
        1138506
        1321
        1774
        3269
        43048
        46588
        5505
        58798
        65558
        6867
        75390
        78507
        799
        852488
        8933
        89811
        9147
        972],
       :all-anime [10695 11146 2653 2729 3063 413820 452 6721 7424 9302],
       :disco [3493],
       :talk-shows-&-stand-up-comedy [1516534],
       :anime-features [3063],
       :tv-thrillers [89811],
       :quirky-romance [36103],
       :food-&-travel-tv [72436],
       :romantic-swedish-movies [60829],
       :anime [7424],
       :action-sci-fi-&-fantasy [1568],
       :disney [67673],
       :spanish-dramas [58796],
       :romantic-gay-&-lesbian-movies [3329],
       :military-&-war-documentaries [77245],
       :dramas-based-on-real-life [3653],
       :military-&-war-movies [76510],
       :westerns [7700],
       :japanese-period-dramas [1402191],
       :romantic-tv-soaps [26052],
       :国内tv番組・ドラマ [64256],
       :brazilian-comedies [17648],
       :romantic-japanese-films [17241],
       :vampire-horror-movies [75804],
       :gay-&-lesbian-comedies [7120],
       :all-gay-and-lesbian [3329 4720 500 5977 65263 7120],
       :classic-action-&-adventure [46576],
       :australian-tv-programmes [52387],
       :korean-dramas [1989],
       :french-comedies [58905],
       :tearjerkers [6384],
       :nordic-dramas [78628],
       :animal-tales [5507],
       :golden-globe-award-winning-films [82489],
       :contemporary-r&b [7129],
       :deep-sea-horror-movies [45028],
       :asian-movies [78104],
       :anime-action [2653],
       :norwegian-thrillers [78507],
       :documentaries [6839],
       :finnish-movies [62285],
       :courtroom-dramas [528582748],
       :crime-tv-dramas [26009],
       :satires [4922],
       :military-&-war-dramas [76507],
       :music [1701],
       :african-movies [3761],
       :laugh-out-loud-comedies [1333288],
       :african-american-dramas [9847],
       :all-faith-and-spirituality [26835 52804 751423],
       :foreign-action-&-adventure [11828],
       :latino-stand-up-comedy [34157],
       :crime-comedies [4058],
       :punk-rock [8721],
       :family-feature-animation [51058],
       :political-tv-documentaries [55087],
       :dance [8451],
       :canadian-french-language-movies [63151],
       :classic-country-&-western [2994],
       :japanese-movies [10398],
       :canadian-films [56181],
       :dutch-children-&-family-movies [89513],
       :foreign-thrillers [10306],
       :showbiz-dramas [5012],
       :kids-tv-for-ages-2-to-4 [27480],
       :all-music [10032 10741 1701 2222 2856 5096 52843 6031],
       :dance-&-electronica [5080],
       :french-thrillers [58798],
       :tv-action-&-adventure [10673],
       :dutch-tv-shows [89442],
       :classic-horror-films [48303],
       :spanish-horror-films [61546],
       ...}},
     :trace-redirects []}


<a id="org6c2e224"></a>

### images

Get all images associated with a netflixid.

    (clunogs/images "80196789")

    {:cached nil,
     :request-time 847,
     :repeatable? false,
     :protocol-version {:name "HTTP", :major 1, :minor 1},
     :streaming? true,
     :http-client
     #object[org.apache.http.impl.client.InternalHttpClient 0x38a60956 "org.apache.http.impl.client.InternalHttpClient@38a60956"],
     :chunked? false,
     :reason-phrase "OK",
     :headers
     {:access-control-allow-origin "*",
      :content-type "application/json; charset=ISO-8859-1",
      :date "Sat, 23 Feb 2019 03:08:24 GMT",
      :server "RapidAPI-1.0.6",
      :x-ratelimit-requests-limit "100",
      :x-ratelimit-requests-remaining "32",
      :content-length "6135",
      :connection "Close"},
     :orig-content-encoding nil,
     :status 200,
     :length 6135,
     :body
     {:results
      [{:image
        [{:type "background",
          :url
          "https://occ-0-1490-1489.1.nflxso.net/art/44e7e/7bc59ad009ba2c8a48e410dfe8e275ab19c44e7e.jpg",
          :height "477",
          :width "848"}]}
       {:image
        [{:type "background",
          :url
          "https://occ-0-1490-1489.1.nflxso.net/art/3011a/e9c310ae8f925de57360d8b9a489ab85f6e3011a.jpg",
          :height "480",
          :width "853"}]}
       {:image
        [{:type "background",
          :url
          "https://occ-0-1490-1489.1.nflxso.net/art/0e2cb/3860ee541f4677f6ed26a10e8e865e345010e2cb.jpg",
          :height "480",
          :width "853"}]}
       {:image
        [{:type "billboard",
          :url
          "https://occ-0-2218-2794.1.nflxso.net/art/c987e/fe7c44d1ca3d7f81733deb67a68351da098c987e.jpg",
          :height "720",
          :width "1280"}]}
       {:image
        [{:type "billboard",
          :url
          "https://occ-0-1490-1489.1.nflxso.net/art/644c0/34c88f729197d785f35b3815e93f11885b4644c0.jpg",
          :height "720",
          :width "1280"}]}
       {:image
        [{:type "boxart",
          :url
          "https://occ-0-1490-1489.1.nflxso.net/dnm/api/v5/rendition/412e4119fb212e3ca9f1add558e2e7fed42f8fb4/AAAABfbY3I-Q_XSkkUZy4FnU6a-BYLQMrtmzVteDG2CQ-M48DneFRpcdbFC40d2Bmou0gqWeSY5ecrB8idDz8Q9W38C64YFq-hXCEPaHZAtd0870DCMEjyq-Ob4zpadxeplVfb9GYNkK6iU.jpg",
          :height "720",
          :width "1280"}]}
       {:image
        [{:type "boxart",
          :url
          "https://occ-0-2218-2794.1.nflxso.net/dnm/api/v5/rendition/412e4119fb212e3ca9f1add558e2e7fed42f8fb4/AAAABUBjEhTM5sDuZBCbk2MD5Y7_-rxGk91fR2K2ecp3n-qsuS6lhO9CKRWgP9HPhTXim6DsnnvJZDw2yH1xqshLMaYhRxgHOOzDZ_AqRHc2a2g0MH3OppGcxO9hKKh059E8lU6PDOEXaso.jpg",
          :height "720",
          :width "1280"}]}
       {:image
        [{:type "boxart",
          :url
          "https://occ-0-1490-1489.1.nflxso.net/dnm/api/v5/rendition/412e4119fb212e3ca9f1add558e2e7fed42f8fb4/AAAABdh44ox5y14HoxSIVzADho-0SHrHd_WMQF_1CDTWy6dAMvTRfwMKP-ONsqhDqElMzkrhrqvScEN5rqLFDphqpezb2PUgjnC8hCxfre2l4VJtIHN-8qCMx1TwNvv7JTeio3zimyzlxhA.jpg",
          :height "720",
          :width "1280"}]}
       {:image
        [{:type "boxart",
          :url
          "https://occ-0-2219-1361.1.nflxso.net/art/523fe/26bd2c7b5c7e444396b0108946b2c80c533523fe.jpg",
          :height "233",
          :width "166"}]}
       {:image
        [{:type "boxart",
          :url
          "https://occ-0-1490-1489.1.nflxso.net/dnm/api/v5/rendition/412e4119fb212e3ca9f1add558e2e7fed42f8fb4/AAAABUFqDLCNYmS9HSJabnNhgfjzD2hxp27K68tA-OPmcRIq4FJAmr9q8c98",
          :width "284"}]}
       {:image
        [{:type "boxart",
          :url
          "https://occ-0-2218-2794.1.nflxso.net/art/1e734/e7bf04ab74c3648eb94b18ebe23cd4699a01e734.jpg",
          :height "398",
          :width "284"}]}
       {:image
        [{:type "boxart",
          :url
          "https://occ-0-1490-1489.1.nflxso.net/art/6b4b1/4a68d221b10fc5417a6aaa1f4d79856d6f06b4b1.jpg",
          :height "398",
          :width "284"}]}
       {:image
        [{:type "boxart",
          :url
          "https://occ-0-1490-1489.1.nflxso.net/dnm/api/v5/rendition/412e4119fb212e3ca9f1add558e2e7fed42f8fb4/AAAABTcRGDMTp2YCf9YRrhi0XAxfTlzrrD1yPSXShMyohY2N1-d38NIntLTwKptE-n0HqotE7wIs4jrzh_05Rzhh_kZ7JrJgbDFf_gZ5ryrn2UfLD5_lTOKq79Wi7ngHB9-MsYnVuCbLjg.jpg",
          :height "374",
          :width "665"}]}
       {:image
        [{:type "boxart",
          :url
          "https://occ-0-2218-2794.1.nflxso.net/dnm/api/v5/rendition/412e4119fb212e3ca9f1add558e2e7fed42f8fb4/AAAABTrEJ118Aif46a60ga-zzxkZo6g9weyK5dY1Pd1EWOcOeYmphRFegmmbflqe-gs8LzwvIArTT0Cu0wggtVA52huV6rndWVbaLr6WUINSAAhI0ZJ0sGLh9DAHbiyoSLBng-1rsTtW7g.jpg",
          :height "374",
          :width "665"}]}
       {:image
        [{:type "boxart",
          :url
          "https://occ-0-1490-1489.1.nflxso.net/dnm/api/v5/rendition/412e4119fb212e3ca9f1add558e2e7fed42f8fb4/AAAABZrZzY5vYVtwzwMu76rf7hIDIq_MPHnjFvwmM-nsLefWT8niqil5Ac6cavUg1tIWls-Lo_w2UTpnHnxNDbtSCjiX22TXp98AkqhWwB_-RZJFnrOot3zK6bUP8luIQ_9mL4YVztSRfA.jpg",
          :height "374",
          :width "665"}]}
       {:image
        [{:type "boxart",
          :url
          "http://occ-0-1599-1001.1.nflxso.net/art/2b3a2/161aa5c99c36b9153e6a5e9b62567ec9d162b3a2.jpg",
          :height "233",
          :width "166"}]}
       {:image
        [{:type "boxart",
          :url
          "https://occ-0-38-1501.1.nflxso.net/art/05b9a/2086f83c4da613f3e986d8b584a60e5759a05b9a.jpg",
          :height "233",
          :width "166"}]}
       {:image
        [{:type "boxart",
          :url
          "https://occ-0-2218-2794.1.nflxso.net/art/282cf/cbedf562d44e567e4d3c7c0d97d7cbd9f9c282cf.jpg",
          :height "233",
          :width "166"}]}
       {:image
        [{:type "boxart",
          :url
          "https://occ-0-2218-2794.1.nflxso.net/art/b9257/5df87eb389006295bc30c00944b5918313fb9257.jpg",
          :height "233",
          :width "166"}]}
       {:image
        [{:type "boxart",
          :url
          "https://occ-0-32-34.1.nflxso.net/art/dfcb0/43b5ef455fc77ca3038a2f5c4fcd4b7047edfcb0.jpg",
          :height "233",
          :width "166"}]}
       {:image
        [{:type "boxart",
          :url
          "https://occ-0-2218-2794.1.nflxso.net/art/fdedc/9dafa64d6cc2c7d99acf36617309a3a8150fdedc.jpg",
          :height "233",
          :width "166"}]}
       {:image
        [{:type "boxart",
          :url
          "https://occ-0-32-34.1.nflxso.net/art/06717/782030ace38695b93d26f8ecd81c414a1b606717.jpg",
          :height "233",
          :width "166"}]}
       {:image
        [{:type "boxart",
          :url
          "https://occ-0-2218-2794.1.nflxso.net/art/b91bc/c24daec55cb725860c07535f385e22aa666b91bc.jpg",
          :height "233",
          :width "166"}]}
       {:image
        [{:type "boxart",
          :url
          "https://occ-0-2218-2794.1.nflxso.net/art/8ac70/1e2bf3aeda43c1a31d23d0a1c1ad05168d18ac70.jpg",
          :height "233",
          :width "166"}]}
       {:image
        [{:type "logo",
          :url
          "https://occ-0-1490-1489.1.nflxso.net/dnm/api/v5/rendition/febbc512601e6d10fbc3b42d8e6ea359b2abf88d/AAAABdXhNl2WjcDT1ZpAGQwPpqvhvqK-BAWzY4bMHQNvNBJAnoj4J-bdRCnT6Oo3YZfd_q2bK2Wi7wfTOGN8Axq0tQpJBcNTNDKOJGjUWOv_L4StIdeWXnkHNh4ycVmVHX2SsRc_kskE8lY5RxyE7dTCKOA1RzboKn4rTVh2ajyQ8hhwY56EPCPb3hLjpU2MWI6hKIYgmDH1tDhFizYqlPKDJT7KCbV8ZgwpbjTS0Dy4njyJPBWe9z45zkBK9lFoCfxs.png",
          :height "90",
          :width "400"}]}]},
     :trace-redirects []}


<a id="orgea7f335"></a>

### episode-details

Get individual episode details for a series with netflixid.

    (clunogs/episode-details "80117540")

    {:cached nil,
     :request-time 1096,
     :repeatable? false,
     :protocol-version {:name "HTTP", :major 1, :minor 1},
     :streaming? true,
     :http-client
     #object[org.apache.http.impl.client.InternalHttpClient 0x73fb8696 "org.apache.http.impl.client.InternalHttpClient@73fb8696"],
     :chunked? true,
     :reason-phrase "OK",
     :headers
     {:access-control-allow-origin "*",
      :content-type "application/json; charset=ISO-8859-1",
      :date "Sat, 23 Feb 2019 03:09:39 GMT",
      :server "RapidAPI-1.0.6",
      :x-ratelimit-requests-limit "100",
      :x-ratelimit-requests-remaining "30",
      :transfer-encoding "chunked",
      :connection "Close"},
     :orig-content-encoding nil,
     :status 200,
     :length -1,
     :body
     {:results
      [{:seasid "80117541",
        :seasnum "1",
        :country "",
        :episodes
        [{:episode
          {:id "80117462",
           :seasnum "1",
           :epnum "1",
           :title "Antarctica",
           :image
           "https://art-s.nflximg.net/ae722/f3c064beeeb4d251a77ae9d1de85b499252ae722.jpg",
           :synopsis
           "Interested in having a girlfrieoo her. A lost credit card sends Elsa back to the bar. Casey hears rumors about Evan.",
           :available "true"}}
         {:episode
          {:id "80117464",
           :seasnum "1",
           :epnum "3",
           :title "Julia Says",
           :image
           "https://art-s.nflximg.net/e1762/2f2963d1341e2d4ef21457875de823565f5e1762.jpg",
           :synopsis
           "Sam decides to update his look. Casey learns a family secret. Feeling like her family no longer needs her, Elsa seeks attention elsewhere.",
           :available "true"}}
         {:episode
          {:id "80117465",
           :seasnum "1",
           :epnum "4",
           :title "A Nice Neutral Smell",
           :image
           "https://art-s.nflximg.net/39c16/bbdaa29f42b33542ce99f4f344dbfc690cb39c16.jpg",
           :synopsis
           "When a classmate shows interest in him, Sam makes a list of pros and cons to see whether she's girlfriend material. Casey gets a prestigious offer.",
           :available "true"}}
         {:episode
          {:id "80117466",
           :seasnum "1",
           :epnum "5",
           :title "That’s My Sweatshirt",
           :image
           "https://art-s.nflximg.net/16e7b/78ea6e24d493cfc3ef1bab91e32829ba0c016e7b.jpg",
           :synopsis
           "Sam becomes rattled after Paige invades his bedroom. Casey visits Clayton Prep and gets a taste of what life away from her brother would be like.",
           :available "true"}}
         {:episode
          {:id "80117467",
           :seasnum "1",
           :epnum "6",
           :title "The D-Train to Bone Town",
           :image
           "https://art-s.nflximg.net/55a0f/c01c467d93fac5a6188481d3a96856ef06f55a0f.jpg",
           :synopsis
           "Paige proposes having an autism-friendly school dance to the PTA. Sam makes preparations for losing his virginity. Doug and Julia help each other.",
           :available "true"}}
         {:episode
          {:id "80117468",
           :seasnum "1",
           :epnum "7",
           :title "I Lost My Poor Meatball",
           :image
           "https://art-s.nflximg.net/42e5b/fe9da8b5144da1392fc4c69dec1fcd5481742e5b.jpg",
           :synopsis
           "When Paige makes a confession to Sam, he creates a checklist to figure how he feels. Things with Nick suddenly get too real for Elsa.",
           :available "true"}}
         {:episode
          {:id "80117469",
           :seasnum "1",
           :epnum "8",
           :title "The Silencing Properties of Snow",
           :image
           "https://art-s.nflximg.net/20f30/ccd3bf260d4d98708ed37a41eebeb60401420f30.jpg",
           :synopsis
           "After a disastrous dinner with Paige's family, Sam makes amends at the school dance. Casey's fury at her mom affects her relationship with Evan.",
           :available "true"}}]}
       {:seasid "80195939",
        :seasnum "2",
        :country "",
        :episodes
        [{:episode
          {:id "80195739",
           :seasnum "2",
           :epnum "1",
           :title "Juiced!",
           :image
           "https://occ-0-1490-1489.1.nflxso.net/art/dcef5/5e7dd7a20e48434fbeb66082d2385e59da1dcef5.jpg",
           :synopsis
           "Sam's frustration at being unable to find a therapist and Casey's anxiety about going to a new school spark a blowup between the two.",
           :available "true"}}
         {:episode
          {:id "80215119",
           :seasnum "2",
           :epnum "2",
           :title "Penguin Cam and Chill",
           :image
           "https://occ-0-1490-1489.1.nflxso.net/art/1b9a8/0308dcd939fdb6769f593e598865605134d1b9a8.jpg",
           :synopsis
           "Casey feels less than welcome at Clayton. Sam asks Paige to spell out the rules of their 'casual relationship,' while Doug sets boundaries with Elsa.",
           :available "true"}}
         {:episode
          {:id "80215120",
           :seasnum "2",
           :epnum "3",
           :title "Little Dude and the Lion",
           :image
           "https://occ-0-1490-1489.1.nflxso.net/art/9f29d/f8b7350bc696cc9ad6f304157efbce206b09f29d.jpg",
           :synopsis
           "A peer group meeting leads Sam to an important decision. Detention with Izzie shows Casey they have more in common than she thought.",
           :available "true"}}
         {:episode
          {:id "80215121",
           :seasnum "2",
           :epnum "4",
           :title "Pants on Fire",
           :image
           "https://occ-0-1490-1489.1.nflxso.net/art/eab31/13c42253909e5054ae4a69654aaccedb4c2eab31.jpg",
           :synopsis
           "Zahid teaches Sam to lie, while Elsa practices being honest. Evan helps Casey get over an embarrassment. A doctor visit forces Julia out of denial.",
           :available "true"}}
         {:episode
          {:id "80215122",
           :seasnum "2",
           :epnum "5",
           :title "The Egg Is Pipping",
           :image
           "https://occ-0-1490-1489.1.nflxso.net/art/26af0/d4c7cf04939748e0feea2cea826f36f58d326af0.jpg",
           :synopsis
           "In peer group, Sam realizes he needs to manage his own money. Doug gets advice from Megan on dealing with Elsa. Evan meets Casey's new friends.",
           :available "true"}}
         {:episode
          {:id "80215123",
           :seasnum "2",
           :epnum "6",
           :title "In the Dragon's Lair",
           :image
           "https://occ-0-1490-1489.1.nflxso.net/art/900be/8a26aa022642ba3a580990de6699617dd25900be.jpg",
           :synopsis
           "To prepare for college life, Sam sleeps over at Zahid's. Sam's $708d444887c6ed0f44abcfaf1ec7130f4ed286b7c1.jpg",
           :synopsis
           "Casey tells Izzie what Nate did. Sam tries to figure out which colleges to apply to. Doug discovers a need to educate first responders about autism.",
           :available "true"}}
         {:episode
          {:id "80215125",
           :seasnum "2",
           :epnum "8",
           :title "Living at an Angle",
           :image
           "https://occ-0-1490-1489.1.nflxso.net/art/88286/e3994e43ab680a314e98ba770234a8fe95788286.jpg",
           :synopsis
           "Sam loses his portfolio for his art school application and panics. Megan gets flirty with Doug, who makes a decision about Elsa. Julia calls Miles.",
           :available "true"}}
         {:episode
          {:id "80215126",
           :seasnum "2",
           :epnum "9",
           :title "Ritual-licious",
           :image
           "https://occ-0-1490-1489.1.nflxso.net/art/f907f/4d3706f370148903e85864e91206c51dc2af907f.jpg",
           :synopsis
           "Ignoring Casey's wishes, Elsa throws her a big birthday party, while Sam insists on following a decade-old birthday ritual. The bartender haunts Doug.",
           :available "true"}}
         {:episode
          {:id "80215127",
           :seasnum "2",
           :epnum "10",
           :title "Ernest Shackleton's Rules for Survival",
           :image
           "https://occ-0-1490-1489.1.nflxso.net/art/d529b/f22e6637b6f71f5373883b785493ccffa12d529b.jpg",
           :synopsis
           "Cruel yearbook comments show Sam who's got his back, a favor he repays at graduation. Feelings for Izzie confuse Casey. Elsa asks Doug a key question.",
           :available "true"}}]}]},
     :trace-redirects []}


<a id="orgf4b0450"></a>

### title-details

Get the title details for netflixid.

    (clunogs/title-details "80196789")

    {:cached nil,
     :request-time 831,
     :repeatable? false,
     :protocol-version {:name "HTTP", :major 1, :minor 1},
     :streaming? true,
     :http-client
     #object[org.apache.http.impl.client.InternalHttpClient 0x39c461f4 "org.apache.http.impl.client.InternalHttpClient@39c461f4"],
     :chunked? false,
     :reason-phrase "OK",
     :headers
     {:access-control-allow-origin "*",
      :content-type "application/json; charset=ISO-8859-1",
      :date "Sat, 23 Feb 2019 03:10:22 GMT",
      :server "RapidAPI-1.0.6",
      :x-ratelimit-requests-limit "100",
      :x-ratelimit-requests-remaining "29",
      :content-length "12870",
      :connection "Close"},
     :orig-content-encoding nil,
     :status 200,
     :length 12870,
     :body
     {:result
      {:nfinfo
       {:updated "2019-02-23 03:04:15",
        :avgrating "0",
        :image1
        "https://occ-0-2219-1361.1.nflxso.net/art/73306/1016f29ea3a9b26ef7b88582b1512a68c6d73306.jpg",
        :type "movie",
        :title "Bird Box",
        :image2
        "https://occ-0-2219-1361.1.nflxso.net/art/73306/1016f29ea3a9b26ef7b88582b1512a68c6d73306.jpg",
        :matlevel "",
        :released "2018",
        :runtime "2h4m",
        :matlabel "Recommended for ages 16 and up",
        :unogsdate "2018-12-21 08:21:33",
        :synopsis
        "Five years after an ominous unseen presence drives most of society to suicide, a survivor and her two children make a desperate bid to reach safety.",
        :download "0",
        :netflixid "80196789"},
       :imdbinfo
       {:genre "Drama, Horror, Sci-Fi, Thriller",
        :plot
        "Five years after an ominous unseen presence drives most of society to suicide, a mother and her two children make a desperate bid to reach safety.",
        :votes "152930",
        :language "English",
        :runtime "124 min",
        :metascore "51",
        :imdbid "tt2737304",
        :country "USA",
        :awards "N/A",
        :rating "6.7"},
       :mgname
       ["Dramas based on Books"
        "Hollywood Movies"
        "Sci-Fi & Fantasy"
        "Sci-Fi Thrillers"
        "Sci-Fi Dramas"
        "Dramas"
        "Thrillers"
        "Psychological Thrillers"],
       :genreid
       ["4961" "2298875" "1492" "11014" "3916" "5763" "8933" "5505"],
       :people
       [{:actor
         ["Sarah Paulson"
          "Lil Rel Howery"
          "John Malkovich"
          "Sandra Bullock"
          "Danielle Macdonald"
          "Tom Hollander"
          "Jacki Weaver"
          "BD Wong"
          "Rosa Salazar"
          "Trevante Rhodes"]}
        {:creator ["Eric Heisserer" "Josh Malerman"]}
        {:director ["Susanne Bier"]}],
       :country
       [{:cid "21",
         :new "2018-12-21",
         :seasondet [""],
         :expires "",
         :seasons "",
         :islive "yes",
         :subs ["English" "Italian" "Arabic" "German" "Spanish"],
         :audio
         ["English [Original]"
          "Spanish"
          "English - Audio Description"
          "Spanish - Audio Description"
          "Italian"
          "German - Audio Description"
          "German"
          "Italian - Audio Description"],
         :ccode "ar",
         :country "Argentina "}
        {:cid "23",
         :new "2018-12-22",
         :seasondet [""],
         :expires "",
         :seasons "",
         :islive "yes",
         :subs
         ["Greek"
          "English"
          "Simplified Chinese"
          "Italian"
          "Traditional Chinese"],
         :audio
         ["French - Audio Description"
          "Spanish - Audio Description"
          "Italian - Audio Description"
          "Spanish"
          "Italian"
          "German"
          "English - Audio Description"
          "French"
          "German - Audio Description"
          "English [Original]"],
         :ccode "au",
         :country "Australia "}
        {:cid "26",
         :new "2018-12-21",
         :seasondet [""],
         :expires "",
         :seasons "",
         :islive "yes",
         :subs ["English" "French" "Dutch" "German"],
         :audio
         ["German"
          "German - Audio Description"
          "English - Audio Description"
          "French - Audio Description"
          "English [Original]"
          "French"],
         :ccode "be",
         :country "Belgium "}
        {:cid "29",
         :new "2018-12-21",
         :seasondet [""],
         :expires "",
         :seasons "",
         :islive "yes",
         :subs
         ["English" "Brazilian Portuguese" "German" "Italian" "French"],
         :audio
         ["Brazilian Portuguese - Audio Description"
          "German - Audio Description"
          "Italian"
          "German"
          "English - Audio Description"
          "English [Original]"
          "Brazilian Portuguese"
          "French"
          "Italian - Audio Description"
          "French - Audio Description"],
         :ccode "br",
         :country "Brazil "}
        {:cid "33",
         :new "2018-12-21",
         :seasondet [""],
         :expires "",
         :seasons "",
         :islive "yes",
         :subs ["English" "German" "Italian" "French" "Spanish"],
         :audio
         ["English [Original]"
          "French - Audio Description"
          "English - Audio Description"
          "Spanish"
          "French"
          "Italian"
          "German"
          "Italian - Audio Description"
          "German - Audio Description"
          "Spanish - Audio Description"],
         :ccode "ca",
         :country "Canada "}
        {:cid "307",
         :new "2018-12-22",
         :seasondet [""],
         :expires "",
         :seasons "",
         :islive "yes",
         :subs ["English" "German" "Polish"],
         :audio
         ["Polish"
          "English [Original]"
          "German"
          "German - Audio Description"
          "English - Audio :seasondet [""],
         :expires "",
         :seasons "",
         :islive "yes",
         :subs ["Arabic" "German" "European Spanish" "English" "French"],
         :audio
         ["French - Audio Description"
          "European Spanish - Audio Description"
          "European Spanish"
          "English - Audio Description"
          "French"
          "German"
          "German - Audio Description"
          "Brazilian Portuguese - Audio Description"
          "English [Original]"
          "Brazilian Portuguese"],
         :ccode "fr",
         :country "France "}
        {:cid "39",
         :new "2018-12-22",
         :seasondet [""],
         :expires "",
         :seasons "",
         :islive "yes",
         :subs ["German" "Turkish" "French" "English" "Russian"],
         :audio
         ["English - Audio Description"
          "Turkish"
          "German"
          "French"
          "Russian"
          "English [Original]"
          "Russian - Audio Description"
          "French - Audio Description"
          "German - Audio Description"
          "Turkish - Audio Description"],
         :ccode "de",
         :country "Germany "}
        {:cid "327",
         :new "2018-12-21",
         :seasondet [""],
         :expires "",
         :seasons "",
         :islive "yes",
         :subs ["English" "Turkish" "French" "German" "Greek"],
         :audio
         ["English - Audio Description"
          "French - Audio Description"
          "English [Original]"
          "German - Audio Description"
          "German"
          "Turkish"
          "Turkish - Audio Description"
          "French"],
         :ccode "gr",
         :country "Greece "}
        {:cid "331",
         :new "2018-12-21",
         :seasondet [""],
         :expires "",
         :seasons "",
         :islive "yes",
         :subs ["Simplified Chinese" "Traditional Chinese" "English"],
         :audio ["English - Audio Description" "English [Original]"],
         :ccode "hk",
         :country "Hong Kong "}
        {:cid "334",
         :new "2018-12-22",
         :seasondet [""],
         :expires "",
         :seasons "",
         :islive "yes",
         :subs ["French" "German" "English" "Romanian"],
         :audio
         ["German - Audio Description"
          "French - Audio Description"
          "French"
          "English - Audio Description"
          "German"
          "English [Original]"],
         :ccode "hu",
         :country "Hungary "}
        {:cid "337",
         :new "2018-12-22",
         :seasondet [""],
         :expires "",
         :seasons "",
         :islive "yes",
         :subs ["English"],
         :audio ["English [Original]" "English - Audio Description"],
         :ccode "in",
         :country "India "}
        {:cid "336",
         :new "2018-12-21",
         :seasondet [""],
         :expires "",
         :seasons "",
         :islive "yes",
         :subs ["Russian" "Romanian" "Arabic" "Hebrew" "English"],
         :audio
         ["English [Original]"
          "Russian - Audio Description"
          "Russian"
          "English - Audio Description"
          "Polish - Audio Description"
          "Polish"],
         :ccode "il",
         :country "Israel "}
        {:cid "269",
         :new "2018-12-22",
         :seasondet [""],
         :expires "",
         :seasons "",
         :islive "yes",
         :subs ["English" "German" "Italian" "French" "Greek"],
         :audio
         ["French"
          "Italian - Audio Description"
          "English - Audio Description"
          "European Spanish - Audio Description"
          "English [Original]"
          "European Spanish"
          "Italian"
          "German"
          "German - Audio Description"
          "French - Audio Description"],
         :ccode "it",
         :country "Italy "}
        {:cid "267",
         :new "2018-12-22",
         :seasondet [""],
         :expires "",
         :seasons "",
         :islive "yes",
         :subs
         ["Japanese" "English" "Portuguese" "Simplified Chinese" "Korean"],
         :audio
         ["Japanese - Audio Description"
          "Brazilian Portuguese - Audio Description"
          "English [Original]"
          "English - Audio Description"
          "Japanese"
          "Brazilian Portuguese"],
         :ccode "jp",
         :country "Japan "}
        {:cid "357",
         :new "2018-12-21",
         :seasondet [""],
         :expires "",
         :seasons "",
         :islive "yes",
         :subs ["Russian" "German" "English"],
         :audio
         ["German - Audio Description"
          "German"
          "English - Audio Description"
          "Russian - Audio Description"
          "Russian":seasondet [""],
         :expires "",
         :seasons "",
         :islive "yes",
         :subs ["English" "French" "Dutch" "European Spanish" "German"],
         :audio
         ["French"
          "English [Original]"
          "French - Audio Description"
          "English - Audio Description"
          "Turkish - Audio Description"
          "European Spanish - Audio Description"
          "German - Audio Description"
          "Turkish"
          "European Spanish"
          "German"],
         :ccode "nl",
         :country "Netherlands "}
        {:cid "392",
         :new "2018-12-22",
         :seasondet [""],
         :expires "",
         :seasons "",
         :islive "yes",
         :subs ["Polish" "English" "German" "Russian"],
         :audio
         ["German - Audio Description"
          "Polish"
          "English - Audio Description"
          "Russian"
          "Polish - Audio Description"
          "German"
          "English [Original]"
          "Russian - Audio Description"],
         :ccode "pl",
         :country "Poland "}
        {:cid "268",
         :new "2019-01-06",
         :seasondet [""],
         :expires "",
         :seasons "",
         :islive "no",
         :subs ["Portuguese" "English" "French" "European Spanish"],
         :audio
         ["English - Audio Description"
          "English [Original]"
          "Brazilian Portuguese - Audio Description"
          "European Spanish - Audio Description"
          "French"
          "French - Audio Description"
          "European Spanish"
          "Brazilian Portuguese"],
         :ccode "pt",
         :country "Portugal "}
        {:cid "400",
         :new "2018-12-21",
         :seasondet [""],
         :expires "",
         :seasons "",
         :islive "yes",
         :subs ["German" "Romanian" "European Spanish" "English" "French"],
         :audio
         ["German"
          "French - Audio Description"
          "European Spanish - Audio Description"
          "English - Audio Description"
          "German - Audio Description"
          "Turkish - Audio Description"
          "French"
          "European Spanish"
          "English [Original]"
          "Turkish"],
         :ccode "ro",
         :country "Romania "}
        {:cid "402",
         :new "2018-12-22",
         :seasondet [""],
         :expires "",
         :seasons "",
         :islive "yes",
         :subs ["English" "Finnish" "Russian"],
         :audio
         ["English [Original]"
          "Russian"
          "Russian - Audio Description"
          "English - Audio Description"],
         :ccode "ru",
         :country "Russia"}
        {:cid "408",
         :new "2018-12-22",
         :seasondet [""],
         :expires "",
         :seasons "",
         :islive "yes",
         :subs ["Simplified Chinese" "English" "Traditional Chinese"],
         :audio ["English - Audio Description" "English [Original]"],
         :ccode "sg",
         :country "Singapore "}
        {:cid "412",
         :new "2018-12-22",
         :seasondet [""],
         :expires "",
         :seasons "",
         :islive "yes",
         :subs ["Polish" "German" "English"],
         :audio
         ["German - Audio Description"
          "Polish - Audio Description"
          "Polish"
          "German"
          "English [Original]"
          "English - Audio Description"],
         :ccode "sk",
         :country "Slovakia "}
        {:cid "447",
         :new "2019-01-10",
         :seasondet [""],
         :expires "",
         :seasons "",
         :islive "no",
         :subs
         ["French"
          "Spanish"
          "Traditional Chinese"
          "Simplified Chinese"
          "English"],
         :audio
         ["German"
          "Italian - Audio Description"
          "French"
          "Spanish"
          "Italian"
          "English [Original]"
          "English - Audio Description"
          "French - Audio Description"
          "German - Audio Description"
          "Spanish - Audio Description"],
         :ccode "za",
         :country "South Africa"}
        {:cid "348",
         :new "2018-12-22",
         :seasondet [""],
         :expires "",
         :seasons "",
         :islive "yes",
         :subs ["Korean" "English"],
         :audio ["English - Audio Description" "English [Original]"],
         :ccode "kr",
         :country "South Korea"}
        {:cid "270",
         :new "2018-12-21",
         :seasondet [""],
         :expires "",
         :seasons "",
         :islive "yes",
         :subs ["English" "European Spanish" "Arabic" "Romanian" "French"],
         :audio
         ["European Spanish"
          "Italian"
          "Italian - Audio Description"
          "French - Audio Description"
          "English [Original]"
          "European Spanish - Audio Description"
          "French"
          "German"
          "English - Audio Description"
          "German - Audio Description"],
         :ccode "es",
         :country "Spain "}
        {:cid "73",
         :new "2018-12-22",
         :seasondet [""],
         :expires "",
         :seasons "",
         :islive "yes",
         :subs ["English" "Finnish" "German" "Norwegian" "Swedish"],
         :audio
         ["German - Audio Description"
          "English [Original]"
          "English - Audio Description"
          "German"
          "French - Audio Description"
          "French"],
         :ccode "se",
         :country "Sweden "}
        {:cid "34",
         :new "2018-12-21",
         :seasondet [""],
         :expires "",
         :seasons "",
         :islive "yes",
         :subs ["English" "French" "Italian" "Portuguese" "German"],
         :audio
         ["French"
          "German - Audio Description"
          "Italian - Audio Description"
          "French - Audio Description"
          "Brazilian Portuguese - Audio Description"
          "English :audio
         ["French"
          "European Spanish"
          "Brazilian Portuguese - Audio Description"
          "English - Audio Description"
          "Polish"
          "Polish - Audio Description"
          "English [Original]"
          "French - Audio Description"
          "Brazilian Portuguese"
          "European Spanish - Audio Description"],
         :ccode "gb",
         :country "United Kingdom"}
        {:cid "78",
         :new "2018-12-21",
         :seasondet [""],
         :expires "",
         :seasons "",
         :islive "yes",
         :subs
         ["Traditional Chinese"
          "French"
          "Simplified Chinese"
          "Spanish"
          "English"],
         :audio
         ["Spanish - Audio Description"
          "English - Audio Description"
          "German"
          "French - Audio Description"
          "German - Audio Description"
          "Italian - Audio Description"
          "French"
          "English [Original]"
          "Spanish"
          "Italian"],
         :ccode "us",
         :country "United States"}]}},
     :trace-redirects []}


<a id="orgdb9489e"></a>

### imdb-info

Get the IMDB info for filmid, which can be either a Netflix or IMDB id.

    (clunogs/imdb-info "tt2737304") ; equivalent to Netflix id  "80196789"

    {:cached nil,
     :request-time 859,
     :repeatable? false,
     :protocol-version {:name "HTTP", :major 1, :minor 1},
     :streaming? true,
     :http-client
     #object[org.apache.http.impl.client.InternalHttpClient 0x2ed17c72 "org.apache.http.impl.client.InternalHttpClient@2ed17c72"],
     :chunked? false,
     :reason-phrase "OK",
     :headers
     {:access-control-allow-origin "*",
      :content-type "application/json; charset=ISO-8859-1",
      :date "Sat, 23 Feb 2019 03:11:55 GMT",
      :server "RapidAPI-1.0.6",
      :x-ratelimit-requests-limit "100",
      :x-ratelimit-requests-remaining "28",
      :content-length "807",
      :connection "Close"},
     :orig-content-encoding nil,
     :status 200,
     :length 807,
     :body
     {:tomatoreviews "0",
      :genre "Drama, Horror, Sci-Fi, Thriller",
      :imdbvotes "152930",
      :tomatofresh "0",
      :tomatouserreviews "0",
      :plot
      "Five years after an ominous unseen presence drives most of society to suicide, a mother and her two children make a desperate bid to reach safety.",
      :date "2019-02-16 07:36:21",
      :tomatometer "0",
      :tomatorating:localimage "",
      :tomatousermeter "0",
      :type "movie",
      :top250tv "0",
      :rated "R",
      :filmid "80196789",
      :poster
      "https://m.media-amazon.com/images/M/MV5BMjAzMTI1MjMyN15BMl5BanBnXkFtZTgwNzU5MTE2NjM@._V1_SX300.jpg",
      :released "21 Dec 2018",
      :language "English",
      :runtime "124 min",
      :tomatorotten "0",
      :metascore "51",
      :newid "34504",
      :tomatoconsensus "N/A",
      :tomatouserrating "0",
      :imdbid "tt2737304",
      :country "USA",
      :awards "N/A",
      :top250 "0"},
     :trace-redirects []}


<a id="orgc93779b"></a>

### imdb-update

The IMDB info is not as regularly collected as the Netflix data. You can
prompt information to be refreshed by making this call with a IMDB id. 

    (clunogs/imdb-update "tt2737304") 

    {:cached nil,
     :request-time 1455,
     :repeatable? false,
     :protocol-version {:name "HTTP", :major 1, :minor 1},
     :streaming? true,
     :http-client
     #object[org.apache.http.impl.client.InternalHttpClient 0x923b29a "org.apache.http.impl.client.InternalHttpClient@923b29a"],
     :chunked? false,
     :reason-phrase "OK",
     :headers
     {:access-control-allow-origin "*",
      :content-type "application/json; charset=ISO-8859-1",
      :date "Sat, 23 Feb 2019 04:25:02 GMT",
      :server "RapidAPI-1.0.6",
      :x-ratelimit-requests-limit "100",
      :x-ratelimit-requests-remaining "27",
      :content-length "525",
      :connection "Close"},
     :orig-content-encoding nil,
     :status 200,
     :length 525,
     :body
     {:imdbinfo
      {:genre "Drama, Horror, Sci-Fi, Thriller",
       :imdbvotes "186457",
       :plot
       "Five years after an ominous unseen presence drives most of society to suicide, a mother and her two children make a desperate bid to reach safety.",
       :imdbrating "6.7",
       :language "English",
       :runtime "124 min",
       :metascore "51",
       :imdbid "tt2737304",
       :country "USA",
       :awards "N/A"},
      :rtomatoes
      {:consensus "N/A",
       :userreviews "N/A",
       :usermeter "N/A",
       :meter "N/A",
       :reviews "N/A",
       :rotten "N/A",
       :userrating "N/A",
       :fresh "N/A",
       :rating "N/A"}},
     :trace-redirects []}


<a id="orgffd9d63"></a>

### weekly-episodes

Get a list of all Netflix title ids with episodes that are added weekly.

    (clunogs/weekly-episodes) 

    {:cached nil,
     :request-time 1220,
     :repeatable? false,
     :protocol-version {:name "HTTP", :major 1, :minor 1},
     :streaming? true,
     :http-client
     #object[org.apache.http.impl.client.InternalHttpClient 0x1cae767 "org.apache.http.impl.client.InternalHttpClient@1cae767"],
     :chunked? false,
     :reason-phrase "OK",
     :headers
     {:access-control-allow-origin "*",
      :content-type "application/json; charset=ISO-8859-1",
      :date "Sat, 23 Feb 2019 04:26:07 GMT",
      :server "RapidAPI-1.0.6",
      :x-ratelimit-requests-limit "100",
      :x-ratelimit-requests-remaining "26",
      :content-length "1291",
      :connection "Close"},
     :orig-content-encoding nil,
     :status 200,
     :length 1291,
     :body
     {:results
      [70143860
       70180057
       70195800
       70197049
       70197057
       70202589
       70260729
       70266998
       70276033
       70281312
       70283260
       70283261
       70283264
       70285581
       70285785
       70295760
       70298433
       80014749
       80019925
       80020542
       80021955
       80027158
       80027159
       80066080
       80067290
       80077586
       80084447
       80092878
       80103318
       80103331
       80104434
       80109194
       80111373
       80113482
       80113627
       80113641
       80113647
       80113701
       80117781
       80129584
       80131479
       80133124
       80133311
       80135750
       80138688
       80145637
       80145746
       80146480
       80146743
       80156387
       80157728
       80158044
       80158135
       80162053
       80163524
       80165290
       80169271
       80169272
       80169379
       80169404
       80170687
       80174280
       80174918
       80175348
       80175351
       80175495
       80176842
       80176864
       80176866
       80176931
       80176999
       80178543
       80179106
       80182123
       80183434
       80184138
       80187302
       80187572
       80188115
       80188992
       80189619
       80189728
       80191236
       80191369
       80193178
       80193209
       80193247
       80193315
       80195736
       80195839
       80196595
       80199051
       80204451
       80209553
       80210471
       80212301
       80213536
       80214013
       80214405
       80214406
       ...]},
     :trace-redirects []}


<a id="orgd9d2bda"></a>

### weekly-updates

While similar to weekly-episodes, this allows for querying titles with
weekly added episodes within daysback, but with optional filters for title
and countryid.

    (clunogs/weekly-updates 60 :title "Patriot Act") 

    :results
      [{:cc "MX",
        :newdate "2019-02-19 20:24:26",
        :seasdet "1:7,2:2",
        :filmid "80239931",
        :title "Patriot Act with Hasan Minhaj",
        :preseasdet "1:7,2:1"}
       {:preseasdet "1:7,2:1",
        :title "Patriot Act with Hasan Minhaj",
        :filmid "80239931",
        :seasdet "1:7,2:2",
        :newdate "2019-02-19 10:46:53",
        :cc "IL"}
       {:filmid "80239931",
        :seasdet "1:7,2:2",
        :newdate "2019-02-19 07:38:04",
        :cc "TH",
        :preseasdet "1:7,2:1",
        :title "Patriot Act with Hasan Minhaj"}
       {:seasdet "1:7,2:2",
        :filmid "80239931",
        :cc "IT",
        :newdate "2019-02-19 06:36:23",
        :preseasdet "1:7,2:1",
        :title "Patriot Act with Hasan Minhaj"}
       {:preseasdet "1:7,2:1",
        :title "Patriot Act with Hasan Minhaj",
        :filmid "80239931",
        :seasdet "1:7,2:2",
        :newdate "2019-02-18 10:39:56",
        :cc "GR"}
       {:title "Patriot Act with Hasan Minhaj",
        :preseasdet "1:7,2:1",
        :cc "BR",
        :newdate "2019-02-18 07:52:24",
        :seasdet "1:7,2:2",
        :filmid "80239931"}
       {:newdate "2019-02-18 07:34:35",
        :cc "LT",
        :filmid "80239931",
        :seasdet "1:7,2:2",
        :title "Patriot Act with Hasan Minhaj",
        :preseasdet "1:7,2:1"}
       {:title "Patriot Act with Hasan Minhaj",
        :preseasdet "1:7,2:1",
        :cc "DE",
        :newdate "2019-02-18 06:40:54",
        :seasdet "1:7,2:2",
        :filmid "80239931"}
       {:filmid "80239931",
        :seasdet "1:7,2:2",
        :newdate "2019-02-18 05:48:42",
        :cc "IN",
        :preseasdet "1:7,2:1",
        :title "Patriot Act with Hasan Minhaj"}
       {:cc "RO",
        :newdate "2019-02-18 04:46:48",
        :seasdet "1:7,2:2",
        :filmid "80239931",
        :title "Patriot Act with Hasan Minhaj",
        :preseasdet "1:7,2:1"}
       {:preseasdet "1:7,2:1",
        :title "Patriot Act with Hasan Minhaj",
        :seasdet "1:7,2:2",
        :filmid "80239931",
        :cc "HU",
        :newdate "2019-02-18 04:46:16"}
       {:newdate "2019-02-18 04:33:24",
        :cc "PL",
        :filmid "80239931",
        :seasdet "1:7,2:2",
        :title "Patriot Act with Hasan Minhaj",
        :preseasdet "1:7,2:1"}
       {:newdate "2019-02-18 04:01:52",
        :cc "SE",
        :filmid "80239931",
        :seasdet "1:7,2:2",
        :title "Patriot Act with Hasan Minhaj",
        :preseasdet "1:7,2:1"}
       {:newdate "2019-02-18 03:54:58",
        :cc "SG",
        :filmid "80239931",
        :seasdet "1:7,2:2",
        :title "Patriot Act with Hasan Minhaj",
        :preseasdet "1:7,2:1"}
       {:title "Patriot Act with Hasan Minhaj",
        :preseasdet "1:7,2:1",
        :cc "RU",
        :newdate "2019-02-18 03:47:26",
        :seasdet "1:7,2:2",
        :filmid "80239931"}
       {:cc "NL",
        :newdate "2019-02-18 03:42:49",
        :seasdet "1:7,2:2",
        :filmid "80239931",
        :title "Patriot Act with Hasan Minhaj",
        :preseasdet "1:7,2:1"}
       {:newdate "2019-02-18 02:41:34",
        :cc "FR",
        :filmid "80239931",
        :seasdet "1:7,2:2",
        :title "Patriot Act with Hasan Minhaj",
        :preseasdet "1:7,2:1"}
       {:cc "JP",
        :newdate "2019-02-18 02:29:26",
        :seasdet "1:7,2:2",
        :filmid "80239931",
        :title "Patriot Act with Hasan Minhaj",
        :preseasdet "1:7,2:1"}
       {:title "Patriot Act with Hasan Minhaj",
        :preseasdet "1:7,2:1",
        :newdate "2019-02-18 02:14:42",
        :cc "AU",
        :filmid "80239931",
        :seasdet "1:7,2:2"}
       {:newdate "2019-02-17 21:44:48",
        :cc "US",
        :filmid "80239931",
        :seasdet "1:7,2:2",
        :title "Patriot Act with Hasan Minhaj",
        :preseasdet "1:7,2:1"}
       {:title "Patriot Act with Hasan Minhaj",
        :preseasdet "1:7,2:1",
        :newdate "2019-02-17 19:59:11",
        :cc "BE",
        :filmid "80239931",
        :seasdet "1:7,2:2"}
       {:preseasdet "1:7,2:1",
        :title "Patriot Act with Hasan Minhaj",
        :filmid "80239931",
        :seasdet "1:7,2:2",
        :newdate "2019-02-17 17:40:36",
        :cc "AR"}
       {:filmid "80239931",
        :seasdet "1:7,2:2",
        :newdate "2019-02-17 16:21:54",
        :cc "CZ",
        :preseasdet "1:7,2:1",
        :title "Patriot Act with Hasan Minhaj"}
       {:title "Patriot Act with Hasan Minhaj",
        :preseasdet "1:7,2:1",
        :newdate "2019-02-17 15:43:38",
        :cc "HK",
        :filmid "80239931",
        :seasdet "1:7,2:2"}
       {:title "Patriot Act with Hasa:title "Patriot Act with Hasan Minhaj"}
       {:filmid "80239931",
        :seasdet "1:7,2:2",
        :newdate "2019-02-17 09:22:57",
        :cc "SK",
        :preseasdet "1:7,2:1",
        :title "Patriot Act with Hasan Minhaj"}]},
     :trace-redirects []}


<a id="org4903d9e"></a>

### all-pages

This is a macro provided for convenience. It wraps a query and performs it
until all pages of data are retrieved. It calculates the number of pages
required with the first request and is susceptible, no matter how unlikely,
to a change in the amount of data occurring after the first query resulting
in not all data being requested.

The final output is the concatenation of all the items from each query with
the header information of the last query. The count is calculated from the
total number of items.

    (clunogs/all-pages (clunogs/new-releases 30 "US"))

    {:cached nil,
     :request-time 918,
     :repeatable? false,
     :protocol-version {:name "HTTP", :major 1, :minor 1},
     :streaming? true,
     :http-client
     #object[org.apache.http.impl.client.InternalHttpClient 0x1782c736 "org.apache.http.impl.client.InternalHttpClient@1782c736"],
     :chunked? false,
     :reason-phrase "OK",
     :headers
     {:access-control-allow-origin "*",
      :content-type "application/json; charset=ISO-8859-1",
      :date "Sat, 23 Feb 2019 04:46:04 GMT",
      :server "RapidAPI-1.0.6",
      :x-ratelimit-requests-limit "100",
      :x-ratelimit-requests-remaining "10",
      :content-length "18149",
      :connection "Close"},
     :orig-content-encoding nil,
     :status 200,
     :length 18149,
     :body
     {:count "239",
      :items
      ({:largeimage "",
        :type "movie",
        :title "Ugly Aur Pagli",
        :released "2008",
        :runtime "1h54m",
        :image
        "https://occ-0-1168-1217.1.nflxso.net/art/0e8b7/ed41fe12c500d0887344fb28b48f84c8dc60e8b7.jpg",
        :unogsdate "2019-02-15",
        :synopsis
        "Everyday guy Kabir is thrown for a loop when he meets s and donning ladies' shoes at her whim.",
        :download "0",
        :imdbid "tt1132606",
        :rating "3.8",
        :netflixid "70109378"}
       {:largeimage "",
        :type "movie",
        :title "Yucatán",
        :released "2018",
        :runtime "2h10m",
        :image
        "https://occ-0-1223-58.1.nflxso.net/art/b2d0a/1364eac0749a0b78349232a958f39e951c3b2d0a.jpg",
        :unogsdate "2019-02-15",
        :synopsis
        "Competing con artists attempt to creatively and ruthlessly swindle a fatherly lottery winner while on a lively cruise from Spain to Mexico.",
        :download "0",
        :imdbid "tt6502956",
        :rating "5.3",
        :netflixid "80988834"}
       {:largeimage "",
        :type "movie",
        :title "The Detained",
        :released "2017",
        :runtime "1h25m",
        :image
        "https://occ-0-2219-1361.1.nflxso.net/art/4ccdc/6ed757e17a87970c68c29eb78681284c2964ccdc.jpg",
        :unogsdate "2019-02-15",
        :synopsis
        "Five high school students serving Saturday detention in a former corrections facility must find a way to outsmart an unseen menace out to kill them.",
        :download "0",
        :imdbid "tt5810368",
        :rating "4.2",
        :netflixid "81050194"}
       {:largeimage "",
        :type "movie",
        :title "The Breaker Upperers",
        :released "2018",
        :runtime "1h22m",
        :image
        "https://occ-0-1168-1217.1.nflxso.net/art/359bd/94ca0a91b5cdb6342d27e4ad155221dff7d359bd.jpg",
        :unogsdate "2019-02-15",
        :synopsis
        "For the right price, BFFs Jen and Mel will ruthlessly end any romance. But when one of them grows a conscience, their friendship begins to unravel.",
        :download "0",
        :imdbid "tt6728096",
        :rating "6.1",
        :netflixid "80992672"}
       {:largeimage "",
        :type "movie",
        :title "Sur: The Melody of Life",
        :released "2002",
        :runtime "2h16m",
        :image
        "https://occ-0-1223-58.1.nflxso.net/art/4eb83/2561b7a19a979603cb2e2399df00f770f694eb83.jpg",
        :unogsdate "2019-02-15",
        :synopsis
        "A renowned music teacher mentors a promising young singer, but when her fame begins to overshadow his own, he lets jealousy and competition take over.",
        :download "0",
        :imdbid "tt0332766",
        :rating "6.5",
        :netflixid "81067761"}
       {:largeimage "",
        :type "series",
        :title "Larry Charles' Dangerous World of Comedy",
        :released "2019",
        :runtime "",
        :image
        "http://occ-1-358-360.1.nflxso.net/art/ff6fb/0f0ea2ae96a6d20f8a35dd55615382ff49aff6fb.jpg",
        :unogsdate "2019-02-15",
        :synopsis
        "Legendary comedy writer and director Larry Charles travels the world in search of humor in the most unusual, unexpected and dangerous places.",
        :download "0",
        :imdbid "tt9654082",
        :rating "",
        :netflixid "80188051"}
       {:largeimage "",
        :type "series",
        :title "The 43",
        :released "2019",
        :runtime "",
        :image
        "https://occ-0-38-1501.1.nflxso.net/art/bf840/396b7dac59537e3ea4223f5d658217fa958bf840.jpg",
        :unogsdate "2019-02-15",
        :synopsis
        "This docuseries disputes the Mexican government's account of how and why 43 students from Ayotzinapa Rural Teachers' College vanished in Iguala in 2014.",
        :download "0",
        :imdbid "tt9789272",
        :rating "",
        :netflixid "81045551"}
       {:largeimage "",
        :type "series",
        :title "The Kirlian Frequency",
        :released "2017",
        :runtime "",
        :image
        "http://occ-2-358-360.1.nflxso.net/art/bf059/1ac95d7c1cc7787ed6e68624050a848e86dbf059.jpg",
        :unogsdate "2019-02-15",
        :synopsis
        "In the midnight hour, a lone DJ broadcasts the strangest -- and scariest -- tales from the outer edges of Kirlian, a lost city somewhere in Argentina.",
        :download "0",
        :imdbid "tt6843558",
        :rating "8.3",
        :netflixid "81045308"}
       {:largeimage "",
        :type "series",
        :title "The Umbrella Academy",
        :released "2019",
        :runtime "",
        :image
        "https://occ-0-38-1501.1.nflxso.net/art/1d877/701f9c5d834fa42b485b003f3c8becc07eb1d877.jpg",
        :unogsdate "2019-02-15",
        :synopsis
        "Reunited by their father's death, estranged siblings with extraordinary powers uncover shocking family secrets -- and a looming threat to humanity.",
        :download "0",
        :imdbid "tt1312171",
        :rating "0",
        :netflixid "80186863"}
       {:largeimage "",
        :type "movie",
        :title "My Travel Buddy",
        :released "2017",
        :runtime "1h50m",
        :image
        "https://occ-0-53-2774.1.nflxso.net/art/ba35c/e2abdad14b1d54acb50e65e12af3888e3b9ba35c.jpg",
        :unogsdate "2019-02-15",
        :synopsis
        "Days before Eid, a salesman fired from his job drives to Ayvalık to meet his girlfriend's family, but the trip goes astray due to his zany travel buddy.",
        :download "0",
        :imdbid "notfound",
        :rating "",
        :netflixid "81043346"}
       {:largeimage "",
        :type "movie",
        :title "Genius",
        :released "2018",
        :runtime "1h36m",
        :image
        "https://occ-0-1490-1489.1.nflxso.net/art/ca814/6c6ca5f036b138417af8ee9516042bb64c5ca814.jpg",
        :unogsdate "2019-02-16",
        :synopsis
        "Extreme pressure from his father to excel at school during childhood has dangerous psychological effects on a brilliant but suicidal man’se3a841c0287a5b0.jpg",
        :unogsdate "2019-02-16",
        :synopsis
        "A hard-driving submarine captain is hired to steal gold from a sunken Nazi sub, but there's no honor among thieves during this underwater heist.",
        :download "1",
        :imdbid "tt2261331",
        :rating "6.4",
        :netflixid "80013271"}
       {:largeimage "http://cdn1.nflximg.net/images/1848/9161848.jpg",
        :type "movie",
        :title "The 40-Year-Old Virgin",
        :released "2005",
        :runtime "1h56m",
        :image
        "https://occ-0-1168-1217.1.nflxso.net/art/e7f9d/4461ad8c52a8dac211d2d2ba621b8bcffa0e7f9d.jpg",
        :unogsdate "2019-02-16",
        :synopsis
        "At age 40, there's one thing Andy hasn't done, and it's really bothering the sex-obsessed guys at work, who set out to help him get laid.",
        :download "1",
        :imdbid "tt0405422",
        :rating "7.1",
        :netflixid "70028904"}
       {:largeimage "",
        :type "movie",
        :title "Studio 54",
        :released "2018",
        :runtime "1h38m",
        :image
        "https://occ-0-3451-3446.1.nflxso.net/art/63087/aa7b546fdc619b9af770982236fd19243d763087.jpg",
        :unogsdate "2019-02-16",
        :synopsis
        "This documentary follows the rapid rise and fall of the Manhattan discotheque and the glittery debauchery that attracted the city's eccentric and elite.",
        :download "0",
        :imdbid "tt5773986",
        :rating "6.9",
        :netflixid "81004511"}
       {:largeimage "",
        :type "movie",
        :title "The Super",
        :released "2017",
        :runtime "1h28m",
        :image
        "https://occ-0-3451-3446.1.nflxso.net/art/e0331/d9fa1103a37f482c52f6c2baaaad2ebc8c5e0331.jpg",
        :unogsdate "2019-02-16",
        :synopsis
        "When an ex-cop becomes a superintendent of an apartment building, he suspects the sinister janitor is behind the eerie disappearances of its tenants.",
        :download "0",
        :imdbid "tt5884784",
        :rating "6.1",
        :netflixid "80989337"}
       {:largeimage "",
        :type "movie",
        :title "Never Heard",
        :released "2018",
        :runtime "1h29m",
        :image
        "http://occ-0-3451-3446.1.nflxso.net/art/4ef80/2df180cbeb30455179fa3d79e64bbd894bb4ef80.jpg",
        :unogsdate "2019-02-17",
        :synopsis
        "As his father sits behind bars, a young man is lured into Los Angeles' merciless drug world and forced to choose between God and gang life.",
        :download "0",
        :imdbid "tt6529772",
        :rating "4.7",
        :netflixid "80216820"}
       {:largeimage "",
        :type "movie",
        :title "Neevevaro",
        :released "2018",
        :runtime "2h11m",
        :image
        "https://occ-0-1223-58.1.nflxso.net/art/e8943/ea835cbafc7aac4ae5deec9b6244b1b17eee8943.jpg",
        :unogsdate "2019-02-17",
        :synopsis
        "When a blind chef’s girlfriend goes missing, his unnerving search for her leads him to find there's more to her disappearance than what meets the eye.",
        :download "0",
        :imdbid "tt8484586",
        :rating "8.2",
        :netflixid "81045069"}
       {:largeimage "http://cdn1.nflximg.net/images/6337/21896337.jpg",
        :type "movie",
        :title "Ghost Pain",
        :released "2013",
        :runtime "58m",
        :image
        "https://occ-0-784-778.1.nflxso.net/art/0e67e/8ad9ac91f3cb06855cfe33b25b756b7308a0e67e.jpg",
        :unogsdate "2019-02-18",
        :synopsis
        "World War IV is over, but a bomb has gone off in Newport City, killing a major arms dealer who may have ties with the mysterious 501 Organization.",
        :download "1",
        :imdbid "tt2636124",
        :rating "7.2",
        :netflixid "80002073"}
       {:largeimage "http://cdn0.nflximg.net/images/6424/21896424.jpg",
        :type "movie",
        :title "Ghost Tears",
        :released "2014",
        :runtime "58m",
        :image
        "https://occ-0-784-778.1.nflxso.net/art/0354d/254f27bca292b0d99dc46ae9e6834005ee10354d.jpg",
        :unogsdate "2019-02-18",
        :synopsis
        "As Motoko and Batou attempt to thwart a mysterious terrorist group, Togusa tracks the killer of a man with a prosthetic leg made by Mermaid's Leg.",
        :download "1",
        :imdbid "tt3579524",
        :rating "7.3",
        :netflixid "80021983"}
       {:largeimage "http://cdn0.nflximg.net/images/6514/21896514.jpg",
        :type "movie",
        :title "Ghost Whispers",
        :released "2013",
        :runtime "56m",
        :image
        "https://occ-0-784-778.1.nflxso.net/art/f4fbe/825a695cd2b5871b88e86ed70f8b466af97f4fbe.jpg",
        :unogsdate "2019-02-18",
        :synopsis
        "Freed of her responsibilities for the 501 Organization, Motoko must now learn how to take orders from Aramaki.",
        :download "1",
        :imdbid "tt3017864",
        :rating "7.3",
        :netflixid "80002074"}
       {:largeimage "http://cdn1.nflximg.net/images/4657/23114657.jpg",
        :type "series",
        :title "Attack on Titan",
        :released "2013",
        :runtime "",
        :image
        "https://occ-0-784-778.1.nflxso.net/art/64d7e/289e157d6b890b1d2150f92fab46c18ccb864d7e.jpg",
        :unogsdate "2019-02-18",
        :synopsis
        "With his hometown in ruins, young Eren Yeager becomes determined to fight back against the giant Titans that threaten to destroy the human race.",
        :download "1",
        :imdbid "tt2560140",
        :rating "8.8",
        :netflixid "70299043"}
       {:largeimage "",
        :type "movie",
        :title "Postcards from Londo:title "Spy Kids: All the Time in the World",
        :released "2011",
        :runtime "1h27m",
        :image
        "https://occ-0-1168-1217.1.nflxso.net/art/d2a40/10e52034e8007dead3b548ba166968d5822d2a40.jpg",
        :unogsdate "2019-02-19",
        :synopsis
        "Former Spy Kids Carmen and Juni return to help twin siblings Rebecca and Cecil Wilson save the world with their retired secret-agent stepmother.",
        :download "1",
        :imdbid "tt1517489",
        :rating "3.5",
        :netflixid "70176656"}
       {:largeimage "",
        :type "series",
        :title "The 2000s",
        :released "2018",
        :runtime "",
        :image
        "https://occ-0-3451-3446.1.nflxso.net/art/85fae/b1065279edee2927b04b4170b5e544acf2f85fae.jpg",
        :unogsdate "2019-02-19",
        :synopsis
        "Examine the triumphs, tragedies, cultural shifts and technological leaps that occurred during the millennium's first decade.",
        :download "0",
        :imdbid "tt8651972",
        :rating "8.3",
        :netflixid "81027396"}
       {:largeimage "",
        :type "movie",
        :title "Transformer",
        :released "2017",
        :runtime "1h19m",
        :image
        "https://occ-0-3451-3446.1.nflxso.net/art/58340/6811e2b72ca00dd8f5dbb3b15bf666760e358340.jpg",
        :unogsdate "2019-02-20",
        :synopsis
        "Powerlifter Matt Kroczaleski faced his greatest challenge when he came out as transgender. This documentary captures his transition into a woman.",
        :download "0",
        :imdbid "tt7935784",
        :rating "9.8",
        :netflixid "81031652"}
       {:largeimage "",
        :type "movie",
        :title "Kevin James: Sweat the Small Stuff",
        :released "2001",
        :runtime "42m",
        :image
        "http://occ-0-979-38.1.nflxso.net/art/fd24e/12ee03348779b526891687c6e7126dad9dafd24e.jpg",
        :unogsdate "2019-02-21",
        :synopsis
        "The film and television star riffs on life's many royal pains in this hourlong special taped at New York City's Hudson Theatre in 2001.",
        :download "0",
        :imdbid "tt0305727",
        :rating "8",
        :netflixid "60031404"}
       {:largeimage "",
        :type "movie",
        :title "The Intent 2: The Come Up",
        :released "2018",
        :runtime "1h43m",
        :image
        "https://occ-0-38-1501.1.nflxso.net/art/609cc/207ea4817018c6cc2c33b636672bf06ce20609cc.jpg",
        :unogsdate "2019-02-21",
        :synopsis
        "During a trip to Jamaica, a London gang targets a disloyal member who has been meeting with rivals. Meanwhile, an undercover cop stands among them.",
        :download "0",
        :imdbid "",
        :rating "",
        :netflixid "81069254"}
       {:largeimage "",
        :type "movie",
        :title "The Drug King",
        :released "2018",
        :runtime "2h18m",
        :image
        "https://occ-0-53-2774.1.nflxso.net/art/83118/086d89110fdcd619afe1648c5bffdb19e5c83118.jpg",
        :unogsdate "2019-02-21",
        :synopsis
        "A petty smuggler from Busan dives headfirst into illicit drug trafficking in the 1970s and rises to become king of narcotics exports to Japan.",
        :download "0",
        :imdbid "",
        :rating "",
        :netflixid "80236133"}
       {:largeimage "",
        :type "movie",
        :title "Firebrand",
        :released "2019",
        :runtime "1h56m",
        :image
        "https://occ-0-38-1501.1.nflxso.net/art/9106f/c9ade357ae38c836aaaa6b02e5885ceb9d79106f.jpg",
        :unogsdate "2019-02-22",
        :synopsis
        "While she successfully advocates for her female clients in difficult divorce cases, a lawyer’s trauma from sexual assault impacts her own marriage.",
        :download "0",
        :imdbid "",
        :rating "",
        :netflixid "81026327"}
       {:largeimage "",
        :type "movie",
        :title "Trespass Against Us",
        :released "2016",
        :runtime "1h39m",
        :image
        "https://occ-0-3451-3446.1.nflxso.net/art/8ad00/bc36395deb410871cda447f046ac7c1aed28ad00.jpg",
        :unogsdate "2019-02-22",
        :synopsis
        "A man from a criminal family yearns to break away and find a better life, but his father's staunch opposition puts his dreams of freedom in jeopardy.",
        :download "0",
        :imdbid "tt3305308",
        :rating "5.8",
        :netflixid "80057509"}
       {:largeimage "",
        :type "movie",
        :title "Shonar Pahar",
        :released "2018",
        :runtime "2h10m",
        :image
        "https://occ-0-53-2774.1.nflxso.net/art/371e8/8e376dbb2885b1ac91c3f7ecffc866fd541371e8.jpg",
        :unogsdate "2019-02-22",
        :synopsis
        "An older woman's unique friendship with a young boy inspires her to examine the strained relationship she shares with her married son.",
        :download "0",
        :imdbid "tt8347882",
        :rating "8.1",
        :netflixid "81071868"}
       {:largeimage "",
        :type "movie",
        :title "Paddleton",
        :released "2019",
        :runtime "1h29m",
        :image
        "https://occ-0-1168-1217.1.nflxso.net/art/af8e0/96d1e6c32f1a576decabc74e632779fad60af8e0.jpg",
        :unogsdate "2019-02-22",
        :synopsis
        "After he's diagnosed with terminal cancer, middle-aged Michael asks his neighbor friend Andy to help him end his life before the disease does.",
        :download "0",
        :imdbid "",
        :rating "",
        :netflixid "80224060"}
       {:largeimage "http://cdn1.nflximg.net/images/8265/11198265.jpg",
        :type "movie",
        :title "The Unknown Known",
        :released "2013",
        :runtime "1hto his old world after his daughter is abducted and faces an old foe in New Orleans.",
        :download "1",
        :imdbid "tt1085492",
        :rating "4.6",
        :netflixid "80013712"}
       {:largeimage "",
        :type "movie",
        :title "Paris Is Us",
        :released "2019",
        :runtime "1h24m",
        :image
        "https://occ-0-1168-1217.1.nflxso.net/art/81a83/f21b3c841207817f07bdb70ce8ef8ed723581a83.jpg",
        :unogsdate "2019-02-22",
        :synopsis
        "Amid a turbulent romance and rising tensions in Paris, a young woman finds herself caught in a dizzying spiral of dreams, memories and what-ifs.",
        :download "0",
        :imdbid "",
        :rating "",
        :netflixid "81027190"}
       {:largeimage "",
        :type "movie",
        :title "The Photographer Of Mauthausen",
        :released "2018",
        :runtime "1h50m",
        :image
        "https://occ-0-53-2774.1.nflxso.net/art/91bcc/3cd13bfcf5444ba33dc52a9deb03b2f1cfd91bcc.jpg",
        :unogsdate "2019-02-22",
        :synopsis
        "A Catalán prisoner at a Nazi concentration camp uses his office job to steal photo negatives of the atrocities committed there. Based on true events.",
        :download "0",
        :imdbid "",
        :rating "",
        :netflixid "80191608"}
       {:largeimage "",
        :type "series",
        :title "Go! Live Your Way",
        :released "2019",
        :runtime "",
        :image
        "https://occ-0-2506-1432.1.nflxso.net/art/bdb27/e5a4f6041854a3737d728eaad882d5e1d08bdb27.jpg",
        :unogsdate "2019-02-22",
        :synopsis
        "Charismatic Mía gets a scholarship to an elite performing arts school, where she makes close friends but clashes with the owner's popular daughter.",
        :download "0",
        :imdbid "",
        :rating "",
        :netflixid "80220541"}
       {:largeimage "",
        :type "series",
        :title "Workin' Moms",
        :released "2018",
        :runtime "",
        :image
        "https://occ-0-2506-1432.1.nflxso.net/art/aceac/f65cac9a0370deeeb29e277928a0f4a6033aceac.jpg",
        :unogsdate "2019-02-23",
        :synopsis
        "Maternity leave is over and it's time for these four moms to return to work while navigating kids, bosses, love and life in modern-day Toronto.",
        :download "0",
        :imdbid "tt6143796",
        :rating "5.4",
        :netflixid "80198991"}
       {:largeimage "",
        :type "series",
        :title "Z4",
        :released "2018",
        :runtime "",
        :image
        "http://occ-1-358-360.1.nflxso.net/art/e3314/7ae69a22a0c1f940eecb984c1539e9badb9e3314.jpg",
        :unogsdate "2019-02-23",
        :synopsis
        "Fading music biz veteran Zé realizes he has just one more chance at redemption. He must assemble a hit boy band from a ragtag group of pop newbies.",
        :download "0",
        :imdbid "tt8618456",
        :rating "9.2",
        :netflixid "81041002"}
       {:largeimage "",
        :type "series",
        :title "Camp X",
        :released "2014",
        :runtime "",
        :image
        "https://occ-0-3451-3446.1.nflxso.net/art/13d66/501c742089cfdf5342abeacd787f0dda10c13d66.jpg",
        :unogsdate "2019-02-01",
        :synopsis
        "Established during World War II, North America's first secret agent training school -- known as Camp X -- paved the way for present-day espionage.",
        :download "0",
        :imdbid "tt3772982",
        :rating "7.8",
        :netflixid "81063160"}
       {:largeimage "",
        :type "series",
        :title "Botched Up Bodies",
        :released "2013",
        :runtime "",
        :image
        "https://occ-0-768-769.1.nflxso.net/art/30c91/6cc3edd87b4469eaddaa953207ca34a072630c91.jpg",
        :unogsdate "2019-02-01",
        :synopsis
        "From lifting saggy skin to repairing shoddy boob jobs, plastic surgeons tackle extreme fixer-uppers.",
        :download "0",
        :imdbid "tt4883194",
        :rating "6.6",
        :netflixid "81029301"}
       {:largeimage "",
        :type "series",
        :title "Sammy and Bella's Kitchen Rescue",
        :released "2018",
        :runtime "",
        :image
        "https://occ-0-38-1501.1.nflxso.net/art/93919/9838f22c0c35805714ccad9a1c76f4eb06d93919.jpg",
        :unogsdate "2019-02-01",
        :synopsis
        "Sammy and Bella help a guest in need of a kitchen intervention with genius hacks, savory recipes and time-saving skills for a complete culinary rehab.",
        :download "0",
        :imdbid "t:runtime "",
        :image
        "https://occ-0-38-1501.1.nflxso.net/art/9b45a/827ef98b7b198b4934f4eff8a5e2bd248129b45a.jpg",
        :unogsdate "2019-02-01",
        :synopsis
        "Nadia keeps dying and reliving her 36th birthday party. She's trapped in a surreal time loop -- and staring down the barrel of her own mortality.",
        :download "0",
        :imdbid "tt7520794",
        :rating "0",
        :netflixid "80211627"}
       {:largeimage "",
        :type "series",
        :title "Operation Gold Rush",
        :released "2016",
        :runtime "",
        :image
        "https://occ-0-3451-3446.1.nflxso.net/art/1e7d6/5ac26fc042c53c820e3043dafa711dcc1a11e7d6.jpg",
        :unogsdate "2019-02-01",
        :synopsis
        "A team of modern-day adventurers follows the footsteps of the money-hungry explorers who trekked the Klondike in search of Canadian gold.",
        :download "0",
        :imdbid "tt6238500",
        :rating "7.1",
        :netflixid "81063091"}
       {:largeimage "",
        :type "series",
        :title "Sparta",
        :released "2018",
        :runtime "",
        :image
        "https://occ-0-38-1501.1.nflxso.net/art/b4e7f/39f05b64200c04f05b416328a70bf94ccf9b4e7f.jpg",
        :unogsdate "2019-02-01",
        :synopsis
        "While investigating the mysterious death of a teacher, a grizzled detective gets caught up in the world of a high-stakes virtual reality game.",
        :download "0",
        :imdbid "tt9050268",
        :rating "4",
        :netflixid "81041414"}
       {:largeimage "",
        :type "series",
        :title "License To Drill: Louisiana",
        :released "2014",
        :runtime "",
        :image
        "https://occ-0-38-1501.1.nflxso.net/art/5f16e/8e8296e2fd82129daaca1718cf96fa40bf75f16e.jpg",
        :unogsdate "2019-02-01",
        :synopsis
        "A crew of oil riggers work an off-season job and swap the blistering cold of their native Canada for sunny, Southern comfort in the Big Easy.",
        :download "0",
        :imdbid "tt4230936",
        :rating "0",
        :netflixid "81025317"}
       {:largeimage "",
        :type "series",
        :title "Slice of Paradise",
        :released "2017",
        :runtime "",
        :image
        "https://occ-0-3451-3446.1.nflxso.net/art/73bea/5466bda78e96d1c447655d460ac2c98954673bea.jpg",
        :unogsdate "2019-02-01",
        :synopsis
        "From first-home buyers to real estate investors, property hunters tap 'The Block' hosts Peter Wolfkamp and Shelley Ferguson to find their dream spaces.",
        :download "0",
        :imdbid "tt9703220",
        :rating "0",
        :netflixid "81025327"}
       {:largeimage "",
        :type "series",
        :title "Dangerous Roads",
        :released "2012",
        :runtime "",
        :image
        "https://occ-0-3451-3446.1.nflxso.net/art/f9102/ae86efd33941f6e5bb0af5ef059b55ad89cf9102.jpg",
        :unogsdate "2019-02-01",
        :synopsis
        "Using various 4x4 vehicles, celebrities steer their way across some of the most infamous roads and trickiest terrains on earth.",
        :download "0",
        :imdbid "notfound",
        :rating "",
        :netflixid "81059779"}
       {:largeimage "",
        :type "series",
        :title "Bringing Sexy Back",
        :released "2015",
        :runtime "",
        :image
        "https://occ-0-768-769.1.nflxso.net/art/62e93/412969b4b3d01bc835e83ac3358b0b0cb4962e93.jpg",
        :unogsdate "2019-02-01",
        :synopsis
        "In this reality series, everyday individuals adopt healthier lifestyles and transform their bodies to reclaim their confidence -- with dramatic results.",
        :download "0",
        :imdbid "tt9703112",
        :rating "",
        :netflixid "81025218"}
       {:largeimage "",
        :type "series",
        :title "Horror Homes",
        :released "2016",
        :runtime "",
        :image
        "https://occ-0-38-1501.1.nflxso.net/art/ea753/541dfac70a90a293a4b8484ed34f783b4a3ea753.jpg",
        :unogsdate "2019-02-01",
        :synopsis
        "Dream homes turn into property nightmares when mold, maggots, natural disasters and other inconveniences move in.",
        :download "0",
        :imdbid "notfound",
        :rating "",
        :netflixid "81025263"}
       {:largeimage "",
        :type "movie",
        :title "Free Rein: Valentine's Day",
        :released "2019",
        :runtime "50m",
        :image
        "https://occ-0-53-2774.1.nflxso.net/art/8d7b9/c64fdb26e36d14603da2d459896538fdcad8d7b9.jpg",
        :unogsdate "2019-02-01",
        :synopsis
        "Love is in the air as Zoe and friends go on a quest to find a fabled Maid's Stone. But when rivalry blinds them to danger, it's Raven to the rescue!",
        :download "0",
        :imdbid "notfound",
        :rating "",
        :netflixid "81001413"}
       {:largeimage "",
        :type "series",
        :title "Ghost Town Gold",
        :released "2012",
        :runtime "",
        :image
        "https://occ-0-38-1501.1.nflxso.net/art/8da9f/87e3909d37918c8e38d5e7e74836a6c07418da9f.jpg",
        :unogsdate "2019-02-01",
        :synopsis
        "Two history buffs with an eye for valuables traverse through forgotten mines and abandoned landmarks in the wild, wild West to score collectibles.",
        :download "0",
        :imdbid "tt2510302",
        :rating "8",
        :netflixid "81025309"}
       {:largeimage "",
        :type "series",
        :title "Hunters of the South Seas",
        :released "2015",
        :runtime "",
        :image
        "https://occ-0-3451-3446.1.nflxso.net/art/1d83a/fad47c756d00d869cf3d15e36f72563946c1d83a.jpg",
            :imdbid "tt4622718",
        :rating "8",
        :netflixid "81059643"}
       {:largeimage "",
        :type "series",
        :title "Mars",
        :released "2016",
        :runtime "",
        :image
        "https://occ-0-53-2774.1.nflxso.net/art/4500d/8f2b52b26aafdcd02ce08589fdf05bc7a8d4500d.jpg",
        :unogsdate "2019-02-01",
        :synopsis
        "Fact meets fiction in this docudrama chronicling the journey of a spacecraft crew as it embarks in 2033 on a mission to colonize the red planet.",
        :download "0",
        :imdbid "tt4939064",
        :rating "7.5",
        :netflixid "80144355"}
       {:largeimage "",
        :type "series",
        :title "My Tattoo Addiction",
        :released "2013",
        :runtime "",
        :image
        "https://occ-0-3451-3446.1.nflxso.net/art/f8cb8/04203170382b91a9f53b13a8ae99b66d102f8cb8.jpg",
        :unogsdate "2019-02-01",
        :synopsis
        "Ink junkies dig skin deep and share personal stories about their collection of body art.",
        :download "0",
        :imdbid "tt4842406",
        :rating "0",
        :netflixid "81059663"}
       {:largeimage "",
        :type "series",
        :title "Clash of the Collectables",
        :released "2017",
        :runtime "",
        :image
        "https://occ-0-38-1501.1.nflxso.net/art/dfbc7/60a14b88c17452c8f227a90eb541bcdcd84dfbc7.jpg",
        :unogsdate "2019-02-01",
        :synopsis
        "Two antique experts go head-to-head by flipping hidden treasures for maximum profit at auctions in various locations.",
        :download "0",
        :imdbid "tt7799830",
        :rating "0",
        :netflixid "81025239"}
       {:largeimage "",
        :type "series",
        :title "Border Patrol",
        :released "2014",
        :runtime "",
        :image
        "https://occ-0-3451-3446.1.nflxso.net/art/5d8c4/87e8b55d7ccacef5acd05e2858e84f8bef85d8c4.jpg",
        :unogsdate "2019-02-01",
        :synopsis
        "This reality TV series follows the men and women of New Zealand's border patrol in their quest to keep their country safe and secure.",
        :download "0",
        :imdbid "tt4355508",
        :rating "0",
        :netflixid "80222133"}
       {:largeimage "",
        :type "series",
        :title "Beast Legends",
        :released "2010",
        :runtime "",
        :image
        "https://occ-0-3451-3446.1.nflxso.net/art/82d16/a38bd74b8180f8be265101d9068a989b94482d16.jpg",
        :unogsdate "2019-02-01",
        :synopsis
        "A diverse crew of experts dive deep into the origin stories of mythological monsters before building replicas of the creatures using 3D CGI technology.",
        :download "0",
        :imdbid "tt1773132",
        :rating "5.1",
        :netflixid "81063028"}
       {:largeimage "",
        :type "series",
        :title "The Bible's Buried Secrets",
        :released "2011",
        :runtime "",
        :image
        "https://occ-0-3451-3446.1.nflxso.net/art/dad4e/02fbe9801f042bd93d36e8ef0f65d1ee2b8dad4e.jpg",
        :unogsdate "2019-02-01",
        :synopsis
        "Host Francesca Stavrakopoulou travels across the Middle East, offering extraordinary insights into the stories of the Old Testament.",
        :download "0",
        :imdbid "tt9434204",
        :rating "",
        :netflixid "81059684"}
       {:largeimage "",
        :type "series",
        :title "The Big Catch",
        :released "2015",
        :runtime "",
        :image
        "https://occ-0-3451-3446.1.nflxso.net/art/aa415/390f54702120fcdca67f04382277b0e8edfaa415.jpg",
        :unogsdate "2019-02-01",
        :synopsis
        "In a global competition, eight fishing enthusiasts battle extreme conditions and dangerous waters to be crowned the ultimate angler.",
        :download "0",
        :imdbid "notfound",
        :rating "",
        :netflixid "81059816"}
       {:largeimage "",
        :type "series",
        :title "Diva Brides",
        :released "2017",
        :runtime "",
        :image
        "https://occ-0-3451-3446.1.nflxso.net/art/82845/51192d2ea00deb10895c91762a08d0b384882845.jpg",
        :unogsdate "2019-02-01",
        :synopsis
        "In this reality series, brides-to-be stop at nothing to have their dream weddings despite meager funds, fallouts with friends or even absent grooms.",
        :download "0",
        :imdbid "notfound",
        :rating "",
        :netflixid "80224490"}
       {:largeimage "",
        :type "series",
        :title "Kitten Rescuers",
        :released "2017",
        :runtime "",
        :image
    :synopsis
        "This documentary follows British bomb disposal teams in Afghanistan and the dangers they face in their methodical lives on the front lines of war.",
        :download "0",
        :imdbid "tt2074962",
        :rating "8",
        :netflixid "81063049"}
       {:largeimage "",
        :type "series",
        :title "Space Dealers",
        :released "2017",
        :runtime "",
        :image
        "https://occ-0-3451-3446.1.nflxso.net/art/2efe1/f48d827a84299b4c6d02d3377ca1ef1dd4a2efe1.jpg",
        :unogsdate "2019-02-01",
        :synopsis
        "A trio of dealers finds their thrills locating and flipping historic and collectable pieces of space exploration history.",
        :download "0",
        :imdbid "tt9703240",
        :rating "0",
        :netflixid "81025287"}
       {:largeimage "",
        :type "movie",
        :title "World's Weirdest Homes",
        :released "2015",
        :runtime "48m",
        :image
        "https://occ-0-3451-3446.1.nflxso.net/art/e0fb5/677e69f72d9ca15f360700e45c5871e3a3fe0fb5.jpg",
        :unogsdate "2019-02-01",
        :synopsis
        "From a bubble-shaped palace to an island built on plastic bottles, tour the world’s most fantastically bizarre residences.",
        :download "0",
        :imdbid "notfound",
        :rating "",
        :netflixid "81059812"}
       {:largeimage "",
        :type "series",
        :title "World's Busiest Cities",
        :released "2017",
        :runtime "",
        :image
        "https://occ-0-3451-3446.1.nflxso.net/art/a827d/d36cecd5e8f15db573ef824d846f00767f2a827d.jpg",
        :unogsdate "2019-02-01",
        :synopsis
        "From Moscow to Mexico City, three BBC journalists delve into the inner workings of some of the most burgeoning metropolises on Earth.",
        :download "0",
        :imdbid "tt8950426",
        :rating "",
        :netflixid "81059856"}
       {:largeimage "",
        :type "series",
        :title "The Story of God with Morgan Freeman",
        :released "2016",
        :runtime "",
        :image
        "https://occ-0-38-1501.1.nflxso.net/art/d5c1b/6f0ef61dd5a2105a97184ac0a4281effe0bd5c1b.jpg",
        :unogsdate "2019-02-01",
        :synopsis
        "Host Morgan Freeman explores religion's role in human history, how our beliefs connect us and possible answers to life's million-dollar questions.",
        :download "0",
        :imdbid "tt5242220",
        :rating "8",
        :netflixid "80178897"}
       {:largeimage "",
        :type "series",
        :title "Trawlermen Tales",
        :released "2016",
        :runtime "",
        :image
        "https://occ-0-38-1501.1.nflxso.net/art/f7b3e/4448bbde3f3576d150590d1427661a37214f7b3e.jpg",
        :unogsdate "2019-02-01",
        :synopsis
        "While braving torrential waters and other obstacles in the deep, trawler fishermen balance family life with their demanding jobs at sea.",
        :download "0",
        :imdbid "tt6141594",
        :rating "0",
        :netflixid "81025353"}
       {:largeimage "",
        :type "movie",
        :title "True: Happy Hearts Day",
        :released "2019",
        :runtime "23m",
        :image
        "https://occ-0-784-778.1.nflxso.net/art/eed29/c1e37fcf8b3ea1d43459d7a5424068ba78beed29.jpg",
        :unogsdate "2019-02-01",
        :synopsis
        "True and Bartleby try to cheer up the Rainbow Kingdom’s loneliest citizen, but his gloomy mood is contagious! Can a trio of wishes turn things around?",
        :download "0",
        :imdbid "tt9698110",
        :rating "0",
        :netflixid "81035127"}
       {:largeimage "",
        :type "series",
        :title "The Story of Us with Morgan Freeman",
        :released "2017",
        :runtime "",
        :image
        "https://occ-0-2506-1432.1.nflxso.net/art/24095/6b21f4dd2c126a951ed80f4067fe68c677f24095.jpg",
        :unogsdate "2019-02-01",
        :synopsis
        "Explore the forces of love, belief, power, war, peace, rebellion and freedom in this series about the ties that bind -- and destroy -- humanity.",
        :download "0",
        :imdbid "tt7492116",
        :rating "7.1",
        :netflixid "80992963"}
       {:largeimage "",
        :type "series",
        :title "One Strange Rock",
        :released "2018",
        :runtime "",
        :image
        "https://occ-0-3451-3446.1.nflxso.net/art/9e62e/011ba517d6fee7d9baf97f53657b9f125d29e62e.jpg",
        :unogsdate "2019-02-01",
        :synopsis
        "Hosted by Will Smith, this series follows eight astronauts who share their unique perspectives on Earth, the fragile and beautiful planet we call home.",
        :download "0",
        :imdbid "tt7651892",
        :rating "8.8",
        :netflixid "81071666"}
       {:largeimage "http://cdn0.nflximg.net/images/5688/8685688.jpg",
        :type "movie",
        :title "Jaws",
        :released "1975",
        :runtime "2h3m",
        :image
        "https://occ-0-1223-58.1.nflxso.net/art/593bb/0e9f4504401f2131c021015222398766f4a593bb.jpg",
        :unogsdate "2019-02-01",
        :synopsis
        "When an insatiable great white shark terrorizes Amity Island, a police chief, an oceanographer and a grizzled shark hunter seek to destroy the beast.",
        :download "1",
        :imdbid "tt0073195",
        :rating "8",
        :netflixid "60001220"}
       {:largeimage "http://cdn1.nflximg.net/images/7975/22707975.jpg",
        :type "movie",
        :title "Jaws 2",
        :released "1978",
        :runtime "1h56m",
        :image
        "https://occ-0-2506-1432.1.nflxso.net/art/29f94/d8094c5ff51f3e441fe9c7c2b02ba66e86329f94.jpg",
        :unogsdate "2019-02-01",
        :synopsis:download "1",
        :imdbid "tt0077766",
        :rating "5.7",
        :netflixid "60020332"}
       {:largeimage "http://cdn0.nflximg.net/images/9762/11199762.jpg",
        :type "movie",
        :title "Ninja Assassin",
        :released "2009",
        :runtime "1h38m",
        :image
        "https://occ-0-38-1501.1.nflxso.net/art/4320f/3af040e8dba010b36ba39a60d4810e308824320f.jpg",
        :unogsdate "2019-02-01",
        :synopsis
        "When his best friend is murdered by the shadowy Ozunu clan, Raizo, an orphan raised to be an assassin, vows revenge.",
        :download "1",
        :imdbid "tt1186367",
        :rating "6.4",
        :netflixid "70101697"}
       {:largeimage "http://cdn0.nflximg.net/images/5532/8505532.jpg",
        :type "movie",
        :title "Legally Blonde 2: Red, White and Blonde",
        :released "2003",
        :runtime "1h34m",
        :image
        "http://occ-2-3451-3446.1.nflxso.net/art/c7183/a58a04dccfc0017e49f111bf4e90ef9a9dfc7183.jpg",
        :unogsdate "2019-02-01",
        :synopsis
        "After being fired by her law firm because of her opposition to animal testing, perky aspiring attorney Elle Woods takes her fight to Washington, D.C.",
        :download "0",
        :imdbid "tt0333780",
        :rating "4.7",
        :netflixid "60029153"}
       {:largeimage "http://cdn0.nflximg.net/images/9574/10849574.jpg",
        :type "movie",
        :title "Jaws 3",
        :released "1983",
        :runtime "1h38m",
        :image
        "https://occ-0-2506-1432.1.nflxso.net/art/9fb0f/164bb61b359455b8a880be97b5c71f522dc9fb0f.jpg",
        :unogsdate "2019-02-01",
        :synopsis
        "When a baby great white shark finds its way to a theme park, the manager keeps it for a new exhibit -- but soon its angry mother comes to wreak havoc.",
        :download "1",
        :imdbid "tt0085750",
        :rating "3.6",
        :netflixid "60028468"}
       {:largeimage "http://cdn1.nflximg.net/images/9563/22719563.jpg",
        :type "movie",
        :title "Jaws: The Revenge",
        :released "1987",
        :runtime "1h29m",
        :image
        "https://occ-0-2506-1432.1.nflxso.net/art/d05ec/b3325607ddde89dcd3e56939534484d5b9dd05ec.jpg",
        :unogsdate "2019-02-01",
        :synopsis
        "After another deadly shark attack, Ellen Brody has had enough of Amity Island and moves to the Caribbean -- but a great white shark follows her there.",
        :download "1",
        :imdbid "tt0093300",
        :rating "2.9",
        :netflixid "60028469"}
       {:largeimage "",
        :type "movie",
        :title "LEGO Ninjago: Masters of Spinjitzu: Day of the Departed",
        :released "2016",
        :runtime "44m",
        :image
        "http://occ-1-3451-3446.1.nflxso.net/art/221f6/624658e89c5ca3e848d310b180d8dcc4413221f6.jpg",
        :unogsdate "2019-02-01",
        :synopsis
        "Cole finds himself trapped in Yang's temple after accidentally releasing a host of villainous spirits that the Ninja must defeat.",
        :download "0",
        :imdbid "tt5953880",
        :rating "7.7",
        :netflixid "81048910"}
       {:largeimage "",
        :type "movie",
        :title "Live and Let Die",
        :released "1973",
        :runtime "2h1m",
        :image
        "http://occ-2-3451-3446.1.nflxso.net/art/0b1f3/c1d3e7f7389d16b8a73afaf67fbb22905070b1f3.jpg",
        :unogsdate "2019-02-01",
        :synopsis
        "James Bond is sent to the United States to go after a master criminal scheming to take over the country by turning the populace into heroin junkies.",
        :download "0",
        :imdbid "tt0070328",
        :rating "6.8",
        :netflixid "707294"}
       {:largeimage "",
        :type "movie",
        :title "LEGO Marvel Super Heroes: Black Panther",
        :released "2018",
        :runtime "22m",
        :image
        "http://occ-2-3451-3446.1.nflxso.net/art/a9d8d/e5b6122ff13a6a60ed7105cf73c92db9daea9d8d.jpg",
        :unogsdate "2019-02-01",
        :synopsis
        "When Thanos joins forces with villains Killmonger and Klaue to destroy Earth, Black Panther rushes to stop them from stealing Wakanda's vibranium.",
        :download "0",
        :imdbid "tt8205250",
        :rating "7.1",
        :netflixid "81048914"}
       {:largeimage "http://cdn1.nflximg.net/images/3109/8483109.jpg",
        :type "movie",
        :title "Into the Blue",
        :released "2005",
        :runtime "1h50m",
        :image
        "http://occ-0-3451-3446.1.nflxso.net/art/d3b6a/dc37a6f4d6283d51441087e71268b735e4cd3b6a.jpg",
        :unogsdate "2019-02-01",
        :synopsis
        "While scouting the deep blue waters off the Bahamas, a group of divers finds a sunken plane with an illegal cargo worth millions.",
        :download "0",
        :imdbid "tt0378109",
        :rating "5.9",
        :netflixid "70021647"}
       {:largeimage "",
        :type "movie",
        :title "LEGO Marvel Super Heroes: Guardians of the Galaxy",
        :released "2017",
        :runtime "22m",
        :image
        "http://occ-1-3451-3446.1.nflxso.net/art/59d10/76a3fc4ea552eeb750cb81bb085ebdb94cf59d10.jpg",
        :unogsdate "2019-02-02",
        :synopsis
        "The Guardians are on a mission to deliver the Build Stone to the Avengers before the Ravagers, Thanos and his underlings steal it from them.",
        :download "0",
        :imdbid "tt7387224",
        :rating "7.2",
        :netflixid "81048916"}
       {:largeimage "",
        :type "movie",
        :title "Logan's Run",
        :released "1976",
        :runtime "1h58m",
        :image
        "http://occ-2-3451-3446.1.nflxso.net/art/ce868/8egade Soviet general and an exiled Afghan prince to launch a nuclear attack against NATO forces in Europe.",
        :download "0",
        :imdbid "tt0086034",
        :rating "6.6",
        :netflixid "60002472"}
       {:largeimage "",
        :type "movie",
        :title "Ten Years",
        :released "2015",
        :runtime "1h41m",
        :image
        "https://occ-0-53-2774.1.nflxso.net/art/4277b/151c7b59f3fe3a299c679e3b7e0495093f54277b.jpg",
        :unogsdate "2019-02-02",
        :synopsis
        "Five shorts reveal a fictional Hong Kong in 2025, depicting a dystopian city where residents and activists face crackdowns under iron-fisted rule.",
        :download "0",
        :imdbid "tt5269560",
        :rating "7.1",
        :netflixid "81012294"}
       {:largeimage "",
        :type "movie",
        :title "All Light Will End",
        :released "2018",
        :runtime "1h24m",
        :image
        "https://occ-0-3451-3446.1.nflxso.net/art/d41ba/3ed218ee071c200146ceb1eae1045e2b4aed41ba.jpg",
        :unogsdate "2019-02-02",
        :synopsis
        "A horror novelist with a traumatic past returns to her childhood hometown, where she revisits her night terrors and loses sight of reality.",
        :download "0",
        :imdbid "tt6023386",
        :rating "4.4",
        :netflixid "81060040"}
       {:largeimage "",
        :type "movie",
        :title "Await Further Instructions",
        :released "2018",
        :runtime "1h30m",
        :image
        "https://occ-0-3451-3446.1.nflxso.net/art/4b867/f4bba5b03886d86e8f0400afb2320ef67804b867.jpg",
        :unogsdate "2019-02-02",
        :synopsis
        "A family’s tense reunion turns terrifying when they get trapped in their home by an unknown force, and sinister commands begin appearing on their TV.",
        :download "0",
        :imdbid "tt4971408",
        :rating "4.9",
        :netflixid "81005266"}
       {:largeimage "",
        :type "movie",
        :title "Viking Destiny",
        :released "2018",
        :runtime "1h30m",
        :image
        "https://occ-0-3451-3446.1.nflxso.net/art/b4d34/1e59055e4e552fbd7a366e48640a7503d50b4d34.jpg",
        :unogsdate "2019-02-02",
        :synopsis
        "Framed for her father’s murder, an exiled Viking princess is guided by the god Odin as she prepares to return to her kingdom and reclaim the throne.",
        :download "0",
        :imdbid "tt5657280",
        :rating "4",
        :netflixid "80219974"}
       {:largeimage "",
        :type "series",
        :title "Romance is a bonus book",
        :released "2019",
        :runtime "",
        :image
        "https://occ-0-53-2774.1.nflxso.net/art/7b5ee/8fd6410f7aa44e65e8176baef56fe667da17b5ee.jpg",
        :unogsdate "2019-02-02",
        :synopsis
        "A gifted writer who's the youngest editor-in-chief ever at his publishing company gets enmeshed in the life of a former copywriter desperate for a job.",
        :download "0",
        :imdbid "tt9130542",
        :rating "7.7",
        :netflixid "81045349"}
       {:largeimage "",
        :type "movie",
        :title "Malicious",
        :released "2018",
        :runtime "1h30m",
        :image
        "http://occ-0-3451-3446.1.nflxso.net/art/f0a6a/e9e9208827b866f3de83259dbeac3c5bdaaf0a6a.jpg",
        :unogsdate "2019-02-02",
        :synopsis
        "After receiving a strange present, a professor and his pregnant wife are plagued by tragedy and a paranormal presence that's determined to kill.",
        :download "0",
        :imdbid "tt6197494",
        :rating "4.9",
        :netflixid "80220228"}
       {:largeimage "",
        :type "movie",
        :title "Manusangada",
        :released "2017",
        :runtime "1h33m",
        :image
        "http://occ-2-2219-1361.1.nflxso.net/art/eca9c/fe99c46200be0fef200284acc14712e3946eca9c.jpg",
        :unogsdate "2019-02-02",
        :synopsis
        "When caste discrimination prevents a villager from giving his deceased father a rightful burial, he takes his fight for equality to court.",
        :download "0",
        :imdbid "tt7475710",
        :rating "8.8",
        :netflixid "81063743"}
       {:largeimage "http://cdn0.nflximg.net/images/4568/11274568.jpg",
        :type "movie",
        :title "Beverly Hills Chihuahua",
        :released "2008",
        :runtime "1h31m",
        :image
        "https://occ-0-3451-3446.1.nflxso.net/art/94c44/8ea3b17e4040c56df01fda1ceca063bcf6794c44.jpg",
        :unogsdate "2019-02-03",
        :synopsis
        "Chihuahua Chloe rides in style in her owner's designer bag -- until the pampered pooch gets lost during a Mexican vacation and must find her way home.",
        :download "0",
        :imdbid "tt1014775",
        :rating "3.8",
        :netflixid "70098897"}
       {:largeimage "",
        :type "series",
        :title "Violet Evergarden",
        :released "2018",
        :runtime "",
        :image
        "https://occ-0-784-778.1.nflxso.net/art/95293/895ca93debe3503939ab1ae3723475533ec95293.jpg",
        :unogsdate "2019-02-04",
        :synopsis
        "The war is over, and Violet Evergarden needs a job. Scarred and emotionless, she takes a job as a letter writer to understand herself and her past.",
        :download "0",
        :imdbid "tt7078180",
        :rating "8.5",
        :netflixid "80182123"}
       {:largeimage "",
        :type "series",
        :title "Live",
        :released "2018",
        :runtime "",
        :image
        "https://occ-0-53-2774.1.nflxso.net/art/5836b/16704e286d903c0ecb88bac1960dc7297cd5836b.jpg",
        :unogsdate "2019-02-04",
        :synopsis
        "The police officers at South Korea:download "0",
        :imdbid "tt6816530",
        :rating "7.2",
        :netflixid "80201500"}
       {:largeimage "",
        :type "series",
        :title "Hidden Worlds",
        :released "2018",
        :runtime "",
        :image
        "https://occ-0-2219-1361.1.nflxso.net/art/a5a1f/cb8baa394afca42399e7bb053096930648ba5a1f.jpg",
        :unogsdate "2019-02-05",
        :synopsis
        "While investigating an actress’s supposed suicide and her connection to the mafia, a veteran journalist discovers that corruption runs deep.",
        :download "0",
        :imdbid "tt8409854",
        :rating "7.7",
        :netflixid "81049811"}
       {:largeimage "",
        :type "series",
        :title "Tayee",
        :released "2018",
        :runtime "",
        :image
        "https://occ-0-2219-1361.1.nflxso.net/art/9d7ec/c20ad95b90e16e0ebfbe7745fb947535f669d7ec.jpg",
        :unogsdate "2019-02-05",
        :synopsis
        "An Egyptian doctor becomes a police informant and uses his rare gift of tracking ancient artifacts in the smuggling business.",
        :download "0",
        :imdbid "tt8413890",
        :rating "",
        :netflixid "81049774"}
       {:largeimage "",
        :type "movie",
        :title "Di Renjie zhi Sidatianwang",
        :released "2018",
        :runtime "2h11m",
        :image
        "https://occ-0-3451-3446.1.nflxso.net/art/cca4d/4a7a14f2a0f8d83b4d7aac53a1fa7891dbfcca4d.jpg",
        :unogsdate "2019-02-06",
        :synopsis
        "Framed by an empress who plans to steal a dragon-taming mace, imperial magistrate Dee Renjie soon uncovers a greater plot that threatens the kingdom.",
        :download "0",
        :imdbid "tt6869538",
        :rating "6.3",
        :netflixid "81010962"}
       {:largeimage "http://cdn1.nflximg.net/images/8681/8598681.jpg",
        :type "movie",
        :title "Zodiac",
        :released "2007",
        :runtime "2h37m",
        :image
        "https://occ-0-38-1501.1.nflxso.net/art/e07f8/9928822f73a77ebbe0bdf7800f3c50426b6e07f8.jpg",
        :unogsdate "2019-02-06",
        :synopsis
        "Based on real events, this chilling drama recounts the actions of a killer who stalked the streets of San Francisco and left clues in the newspaper.",
        :download "0",
        :imdbid "tt0443706",
        :rating "7.7",
        :netflixid "70044686"}
       ...)},
     :trace-redirects []}


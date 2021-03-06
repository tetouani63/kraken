[![Circle CI](https://circleci.com/gh/echsylon/kraken/tree/master.svg?style=shield)](https://circleci.com/gh/echsylon/kraken/tree/master) [![Coverage Status](https://coveralls.io/repos/github/echsylon/kraken/badge.svg)](https://coveralls.io/github/echsylon/kraken) [![JitPack Snapshot](https://jitpack.io/v/echsylon/kraken.svg)](https://jitpack.io/#echsylon/kraken) [![Download](https://api.bintray.com/packages/echsylon/maven/kraken/images/download.svg)](https://bintray.com/echsylon/maven/kraken/_latestVersion)

# What's the meaning of this?
This third party library aims to wrap the official [Kraken Exchange API](https://www.kraken.com/help/api) and offer convenient means of asynchronously querying it in a way that's suitable for the Android platform.

# How to use it
You simply add the below dependency to your Android project (preferrably picking the latest version):

```javascript
compile 'com.echsylon.kraken:kraken:0.4.0'
```

You can then instantiate a Kraken API client and start requesting. The client will abstract away any and all queueing and asynchronous http request handling for you. The returned `RequestBuilder` will allow you to add any optional query data before you finally enqueue the request. The `enqueue()` call will append the request to the internal request queue and return a callback interface to which you can attach any (optional) result listeners. There is no guarantee that the requests will be executed in the order they are enqueued.

```java
Kraken krakenClient = new Kraken();
krakenClient.getAssetInfo()
        .useInfo("info")
        .useAssetClass("currency")
        .useAssets("XETH", "ZEUR")
        .enqueue()
        .withFinishListener(() -> {
            // Hide progress spinner etc.
        })
        .withSuccessListener(assets -> {
            // Do something
        })
        .withErrorListener(throwable -> {
            // Oh dear!
        });
```

The `FinishListener` will always be called prior to any `SuccessListener` or `ErrorListener` callbacks. It offers a common callback for stuff you'd do regardless the nature of the result, like dismissing any progress dialogs or so.

The `SuccessListener` will provide the requested resource if the request terminates successfully. In the above example it's a `Dictionary<Asset>` object. Note that you'll get native domain objects, saving you the effort of transforming and parsing JSON.

The `ErrorListener` will notify you about something going wrong. This may be an HTTP error state, a Kraken application error (invalid input data etc) or even the very unlikely case of the client itself producing an exception during execution.

You can attach [0..n] listeners to a request and they will all be called on the main thread.

# Call rate limit management
You have the option of enabling automatic call rate limit management on the client side as well. This will help you dodge unnecessary API request blocks by simply pausing the processing of your enqueued requests until the the call rate counter has chilled down enough to safely accept a new request. You enable the manager by calling:

```java
int tier = 2; // The tier of your account
Kraken.setCallRateLimit(tier);
```

And you cancel it by:

```java
Kraken.clearCallRateLimit();
```

Note that the server has the last saying in determining the actual state of the call rate limit.

# Client side caching
This client offers means of caching responses from the server. Some responses are safe to cache (e.g. supported assets and assetpairs) while others are directly unwise to cache (like account balances etc). The choice and responsibility is a privilege of yours. If you want to cache content you have to do a (`static`) configuration of the Kraken client telling where to cache and how much disk space to allow for it at most:

```java
Kraken.setupCache(context.getCacheDir(), 4 * 1024 * 1024); // 4MB cache
```

The actual cache control is exposed on a per-request level and is offered in two variants; a "soft-cache" and a "hard-cache". Soft-cache means that your supplied cache control will only be applied if the server doesn't itself provide any specific cache control:

```java
krakenClient
        .getAssetInfo()        // Get all available info on all currencies
        .softCache(86400)      // Cache for a day if server doesn't say otherwise
        .maxStale(3600)        // Serve expired cache content for an hour if no connection
        .enqueue()
        ...
```

The hard-cache, on the other hand, will override any server provided cache control directives:

```java
krakenClient
        .getAssetInfo()        // Get all available info on all currencies
        .hardCache(86400)      // Cache for a day regardless what the server says
        .maxStale(3600)        // Serve expired cache content for an hour if no connection
        .enqueue()
        ...
```

# Public vs. Private requests
You can use the Kraken client in a "public" mode if you only want to request public end points. In such case you don't need to provide an API key and secret. The above examples all show how this is done. If you want to access private data you'll need to configure an API key (see Kraken documentation on how to do this) and instantiate the Kraken client accordingly:

```java
Kraken privateKrakenClient = new Kraken("MyApiKey", "MyApiKeySecret");
privateKrakenClient
        .getAccountBalance()
        .enqueue()
        .withSuccessListener(balanceMap -> {
            // Do something
        })
        .withErrorListener(throwable -> {
            // Why, oh, why?!
        });
```

Note that the secret needs to be provided in its Base64 encoded form, as presented when you create the corresponding API key in the Kraken Web UI.

# Dependencies
This Kraken API client has only one direct dependency; the [Blocks network abstraction layer](https://github.com/echsylon/blocks-network). That library in turn depends on:

* [Square okhttp](https://github.com/square/okhttp) for networking
* [Google gson](https://github.com/google/gson) for JSON parsing
* [Annimon stream API](https://github.com/aNNiMON/Lightweight-Stream-API) a Java 8 stream backport
* [Evant Gradle Retrolambda](https://github.com/evant/gradle-retrolambda) a Java 8 lambdas backport

All dependencies are open sourced under the Apache 2.0 License.

# Questions?
Feel free to [raise a ticket](https://github.com/echsylon/kraken/issues) if you find a bug, would like something to change (actually I'd consider buying you a beer if you submit a pull request with a suggested change), or just want to discuss a topic.

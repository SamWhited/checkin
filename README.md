# Check In

This is a simple app for android that sends current GPS coordinates and an API
key as an HTTP post request to a specified server. That is all it does.
Implementation of the server is up to you, but an example server written in Go
can be found [here](https://gist.github.com/SamWhited/9941159).

## Why?

In April 2014 I decided to attempt a thru-hike of the 2100 mile Appalachian
Trail from Georgia to Maine. I thought it might be nice to keep track of my
journey and have some data to play with later, but all the apps for keeping
track of things like that required an internet connection while in use, consumed
a lot of battery, or required the use of a proprietary service that wouldn't
allow me to get all the data I wanted. So, two weeks before the trip, I wrote my
own client and server (similar to the example server above).

# API

Check In expects any server it communicates with to provide the following API. Your server MUST implement this API.

## GET / (OPTIONAL)

Data is returned from the server as a [GeoJSON][geojson] [feature collection][featurecol].

## POST / (REQUIRED)

Data is sent to the server in the form of a GeoJSON [point][point]
feature or as a feature collection object if multiple points are
being uploaded in a single HTTP POST, so your server should be able to handle
both cases. There will often be extra metadata attached in the `properties` key
of each `geometry` object, so your server MUST tolerate unknown fields.

Three fields are set in the POST request:

 - `location` — The actual location data as a GeoJSON object
 - `type` — The type of the GeoJSON object in `location`
 - `api_key` — An API key for authentication (if provided by the user)

While the GeoJSON type can always be retreived from the JSON itself after
parsing, it is sometimes useful to have this information before you've actually
parsed the JSON string. The `type` field will always be one of
`FeatureCollection`, `Feature`, or `Unknown` if an error occurs (not likely).

# Building

To build run:

    ./gradlew build

Make sure you have `Google Play Services` and `Google Repository` installed in
the Android SDK manager. Also, API level 19 and the latest build tools.

[geojson]: http://geojson.org/geojson-spec.html#feature-collection-objects
[point]: http://geojson.org/geojson-spec.html#point
[featurecol]: http://geojson.org/geojson-spec.html#feature-collection-objects

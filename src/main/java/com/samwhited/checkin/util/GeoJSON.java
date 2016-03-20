package com.samwhited.checkin.util;

import android.location.Location;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Functions for dealing with GeoJSON
 */
public final class GeoJSON {

	/**
	 * Constructs a GeoJSON point object from a Location.
	 * @param location The location to construct a point from.
	 * @return A GeoJSON point object as a JSONObject.
	 * @throws JSONException If the JSON could not be constructed.
	 * @throws IllegalArgumentException If a null location was supplied.
	 */
	public static JSONObject constructPoint(final Location location) throws JSONException, IllegalArgumentException {
		if (location == null) throw (new IllegalArgumentException("Location must not be null."));

		// Construct a geoJSON object
		final JSONObject json_data = new JSONObject();
		final JSONObject geometry = new JSONObject();
		final JSONObject properties = new JSONObject();
		final JSONArray coordinates = new JSONArray();
		// Setup the root (feature) object
		json_data.put("type", "Feature");

		// Add some geometry to the feature
		json_data.put("geometry", geometry);
		geometry.put("type", "Point");
		geometry.put("coordinates", coordinates);

		// Set the coordinates
		coordinates.put(location.getLongitude());
		coordinates.put(location.getLatitude());
		if (location.hasAltitude()) {
			coordinates.put(location.getAltitude());
		}

		// Set up the properties object.
		json_data.put("properties", properties);
		if (location.hasAccuracy()) {
			properties.put("accuracy", location.getAccuracy());
		}
		if (location.hasBearing()) {
			properties.put("bearing", location.getBearing());
		}
		properties.put("provider", location.getProvider());
		if (location.hasSpeed()) {
			properties.put("speed", location.getSpeed());
		}
		properties.put("time", location.getTime());
		properties.put("elapsed_realtime_nanos", location.getElapsedRealtimeNanos());
		properties.put("raw", location.toString());

		return json_data;
	}
}

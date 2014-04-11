package com.samwhited.checkin.model;

import android.database.Cursor;
import android.location.Location;

import com.samwhited.checkin.database.CheckInDB;
import com.samwhited.checkin.database.CheckInOpenHelper;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Represents a single check in.
 * @author Sam Whited
 */
public class CheckIn {
	private final Location location;
	private final String icon;

	/**
	 * Create a new CheckIn from the data at the given cursor's location.
	 * @param cursor A cursor pointing to a DB row containing the check in.
	 */
	public CheckIn(final Cursor cursor) {
		this(CheckInDB.getLocation(cursor), CheckInDB.getString(cursor, CheckInOpenHelper.ICON_NAME));
	}

	public CheckIn(final String provider,
				   final double latitude,
				   final double longitude,
				   final long time,
				   final long realtime,
				   final float accuracy) {
		this(provider, latitude, longitude, time, realtime, accuracy, 0);
	}

	public CheckIn(final String provider,
				   final double latitude,
				   final double longitude,
				   final long time,
				   final long realtime,
				   final float accuracy,
				   final double altitude) {
		this(provider, latitude, longitude, time, realtime, accuracy, altitude, 0);
	}

	public CheckIn(final String provider,
				   final double latitude,
				   final double longitude,
				   final long time,
				   final long realtime,
				   final float accuracy,
				   final double altitude,
				   final float bearing) {
		this(provider, latitude, longitude, time, realtime, accuracy, altitude, bearing, 0);
	}

	public CheckIn(final String provider,
				   final double latitude,
				   final double longitude,
				   final long time,
				   final long realtime,
				   final float accuracy,
				   final double altitude,
				   final float bearing,
				   final float speed) {
		this(provider, latitude, longitude, time, realtime, accuracy, altitude, bearing, speed, "");
	}

	public CheckIn(final String provider,
				   final double latitude,
				   final double longitude,
				   final long time,
				   final long realtime,
				   final float accuracy,
				   final double altitude,
				   final float bearing,
				   final float speed,
				   final String icon) {
		this.location = new Location(provider);
		location.setLatitude(latitude);
		location.setLongitude(longitude);
		location.setTime(time);
		location.setElapsedRealtimeNanos(realtime);
		location.setAccuracy(accuracy);
		location.setAltitude(altitude);
		location.setBearing(bearing);
		location.setSpeed(speed);
		this.icon = icon;
	}

	public CheckIn(final Location location, final String icon) {
		this.location = location;
		this.icon = icon;
	}

	public CheckIn(final Location location) {
		this(location, "");
	}

	public String getIcon() {
		return this.icon;
	}

	public String getProvider() {
		return location.getProvider();
	}

	public double getLongitude() {
		return location.getLongitude();
	}

	public double getLatitude() {
		return location.getLatitude();
	}

	public boolean hasAltitude() {
		return location.hasAltitude();
	}

	public double getAltitude() {
		return location.getAltitude();
	}

	public boolean hasBearing() {
		return location.hasBearing();
	}

	public float getBearing() {
		return location.getBearing();
	}

	public boolean hasSpeed() {
		return location.hasSpeed();
	}

	public float getSpeed() {
		return location.getSpeed();
	}

	public long getTime() {
		return location.getTime();
	}

	public long getElapsedRealtimeNanos() {
		return location.getElapsedRealtimeNanos();
	}

	public boolean hasAccuracy() {
		return location.hasAccuracy();
	}

	public float getAccuracy() {
		return location.getAccuracy();
	}

	public Location getLocation() {
		return this.location;
	}

	public JSONObject toGeoJSON() throws JSONException {
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
		coordinates.put(getLongitude());
		coordinates.put(getLatitude());
		if (hasAltitude()) {
			coordinates.put(getAltitude());
		}

		// Set up the properties object.
		json_data.put("properties", properties);
		if (hasAccuracy()) {
			properties.put("accuracy", getAccuracy());
		}
		if (hasBearing()) {
			properties.put("bearing", getBearing());
		}
		properties.put("provider", getProvider());
		if (hasSpeed()) {
			properties.put("speed", getSpeed());
		}
		if (getIcon() != null && !getIcon().isEmpty() && !getIcon().equalsIgnoreCase("none")) {
			properties.put("marker-symbol", getIcon());
		}
		properties.put("time", location.getTime());
		properties.put("elapsed_realtime_nanos", location.getElapsedRealtimeNanos());
		properties.put("raw", location.toString());

		return json_data;
	}
}

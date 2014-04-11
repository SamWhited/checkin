package com.samwhited.checkin.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDatabase;
import android.location.Location;

import com.samwhited.checkin.model.CheckIn;

public class CheckInDB {
	final private SQLiteDatabase db;

	public CheckInDB(final Context context) {
		final CheckInOpenHelper helper = new CheckInOpenHelper(context);
		db = helper.getWritableDatabase();
	}

	public static long getLong(final Cursor cursor, final String column) {
		return cursor.getLong(cursor.getColumnIndex(column));
	}

	public static double getDouble(final Cursor cursor, final String column) {
		return cursor.getDouble(cursor.getColumnIndex(column));
	}

	public static float getFloat(final Cursor cursor, final String column) {
		return cursor.getFloat(cursor.getColumnIndex(column));
	}

	public static String getString(final Cursor cursor, final String column) {
		return cursor.getString(cursor.getColumnIndex(column));
	}

	public static boolean isNull(final Cursor cursor, final String column) {
		return cursor.isNull(cursor.getColumnIndex(column));
	}

	public static Location getLocation(final Cursor cursor) {
		// Recreate a location from the DB.
		final Location location = new Location(getString(cursor, CheckInOpenHelper.PROVIDER_NAME));

		// Set the guaranteed, non-nullable values.
		location.setLatitude(getDouble(cursor, CheckInOpenHelper.LATITUDE_NAME));
		location.setLongitude(getDouble(cursor, CheckInOpenHelper.LONGITUDE_NAME));
		location.setTime(getLong(cursor, CheckInOpenHelper.TIME_NAME));
		location.setElapsedRealtimeNanos(getLong(cursor, CheckInOpenHelper.REALTIME_NAME));

		// Set the values that might be nullable.
		if (!isNull(cursor, CheckInOpenHelper.ALTITUDE_NAME)) {
			location.setAltitude(getDouble(cursor, CheckInOpenHelper.ALTITUDE_NAME));
		}

		if (!isNull(cursor, CheckInOpenHelper.BEARING_NAME)) {
			location.setBearing(getFloat(cursor, CheckInOpenHelper.BEARING_NAME));
		}

		if (!isNull(cursor, CheckInOpenHelper.ACCURACY_NAME)) {
			location.setAccuracy(getFloat(cursor, CheckInOpenHelper.ACCURACY_NAME));
		}

		if (!isNull(cursor, CheckInOpenHelper.SPEED_NAME)) {
			location.setSpeed(getFloat(cursor, CheckInOpenHelper.SPEED_NAME));
		}

		return location;
	}

	public long createRecords(final CheckIn checkIn) {
		final ContentValues values = new ContentValues();

		// Add the values
		values.put(CheckInOpenHelper.PROVIDER_NAME, checkIn.getProvider());
		values.put(CheckInOpenHelper.LATITUDE_NAME, checkIn.getLatitude());
		values.put(CheckInOpenHelper.LONGITUDE_NAME, checkIn.getLongitude());
		values.put(CheckInOpenHelper.TIME_NAME, checkIn.getTime());
		values.put(CheckInOpenHelper.REALTIME_NAME, checkIn.getElapsedRealtimeNanos());

		if (checkIn.getAltitude() > 0.0) {
			values.put(CheckInOpenHelper.ALTITUDE_NAME, checkIn.getAltitude());
		}

		if (checkIn.getAccuracy() > 0.0) {
			values.put(CheckInOpenHelper.ACCURACY_NAME, checkIn.getAccuracy());
		}

		if (checkIn.getBearing() > 0.0) {
			values.put(CheckInOpenHelper.BEARING_NAME, checkIn.getBearing());
		}

		if (checkIn.getSpeed() > 0.0) {
			values.put(CheckInOpenHelper.SPEED_NAME, checkIn.getSpeed());
		}

		if (checkIn.getIcon() != null && !checkIn.getIcon().isEmpty()) {
			values.put(CheckInOpenHelper.ICON_NAME, checkIn.getIcon());
		}

		return db.insert(CheckInOpenHelper.CHECKIN_TABLE_NAME, null, values);
	}

	public Cursor selectRecords() {
		final Cursor mCursor = db.query(true, CheckInOpenHelper.CHECKIN_TABLE_NAME, null, null,
				null, null, null, null, null);
		if (mCursor != null) {
			mCursor.moveToFirst();
		}
		return mCursor;
	}

	public int deleteRecord(final long id) {
		return db.delete(CheckInOpenHelper.CHECKIN_TABLE_NAME,
				CheckInOpenHelper.ID_NAME + "=" + id,
				null);
	}

	public int deleteAllRecords() {
		return db.delete(CheckInOpenHelper.CHECKIN_TABLE_NAME, null, null);
	}

	public long numRecords() {
		return DatabaseUtils.queryNumEntries(db, CheckInOpenHelper.CHECKIN_TABLE_NAME);
	}
}

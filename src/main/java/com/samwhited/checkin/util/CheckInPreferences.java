package com.samwhited.checkin.util;
import android.content.Context;
import android.preference.PreferenceManager;

/**
 * A utility class for setting preferences without remembering a lot of keys everywhere.
 */
public final class CheckInPreferences {

	private static final String SERVER_PREF  = "pref_server";
	private static final String APIKEY_PREF  = "pref_api_key";
	private static final String LAST_CHECKIN = "last_checkin";
	private static final String NUM_CHECKINS = "num_checkins";

	public static String getServerPref(final Context context) {
		return getStringPreference(context, SERVER_PREF, "");
	}

	public static String getApikeyPref(final Context context) {
		return getStringPreference(context, APIKEY_PREF, "");
	}

	public static long getLastCheckin(final Context context) {
		return getLongPreference(context, LAST_CHECKIN, 0);
	}

	public static void setLastCheckin(final Context context, final long time) {
		setLongPreference(context, LAST_CHECKIN, time);
	}

	public static long getNumCheckins(final Context context) {
		return getLongPreference(context, NUM_CHECKINS, 0);
	}

	public static void setNumCheckins(final Context context, final long time) {
		setLongPreference(context, NUM_CHECKINS, time);
	}

	private static String getStringPreference(final Context context,
											  final String key,
											  final String defaultValue) {
		return PreferenceManager.getDefaultSharedPreferences(context).getString(key, defaultValue);
	}

	private static long getLongPreference(final Context context,
										  final String key,
										  final long defaultValue) {
		return PreferenceManager.getDefaultSharedPreferences(context).getLong(key, defaultValue);
	}

	private static void setLongPreference(final Context context,
										 final String key,
										 final long value) {
		PreferenceManager.getDefaultSharedPreferences(context).edit().putLong(key, value).commit();
	}
}

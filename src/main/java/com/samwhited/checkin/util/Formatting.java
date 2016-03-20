package com.samwhited.checkin.util;

import android.content.Context;
import android.location.Location;
import android.text.format.DateUtils;

/**
 * Utility class for formatting things (dates, coordinates, etc.)
 */
public class Formatting {

	// TODO: Possibly make this a preference?
	public final static int dateFormatFlags = DateUtils.FORMAT_ABBREV_MONTH |
			DateUtils.FORMAT_SHOW_YEAR |
			DateUtils.FORMAT_SHOW_DATE |
			DateUtils.FORMAT_SHOW_TIME;

	public static String formatDateTime(final Context context, final long time) {
		return DateUtils.formatDateTime(context, time, dateFormatFlags);
	}

	// TODO: Make this a preference?
	public static String formatCoordinates(final Context context, final Location location) {
		final String strLat  = Location.convert(location.getLatitude(), Location.FORMAT_SECONDS);
		final String strLong = Location.convert(location.getLongitude(), Location.FORMAT_SECONDS);
		return strLat + ", " + strLong;
	}
}

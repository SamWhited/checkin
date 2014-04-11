package com.samwhited.checkin;

import android.content.Context;
import android.content.CursorLoader;
import android.database.Cursor;
import android.net.Uri;

import com.samwhited.checkin.database.CheckInDB;

public class CheckInListLoader extends CursorLoader {

	/**
	 * The context for the {@link com.samwhited.checkin.database.CheckInDB}.
	 */
	private final Context context;

	/**
	 * Creates an empty unspecified CursorLoader.  You must follow this with
	 * calls to {@link #setUri(Uri)}, {@link #setSelection(String)}, etc
	 * to specify the query to perform.
	 *
	 * @param context The context for the cursor loader.
	 */
	public CheckInListLoader(final Context context) {
		super(context);
		this.context = context.getApplicationContext();
	}

	@Override
	public Cursor loadInBackground() {
		return (new CheckInDB(context)).selectRecords();
	}
}

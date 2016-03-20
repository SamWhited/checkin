package com.samwhited.checkin;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class CheckInOpenHelper extends SQLiteOpenHelper {

	private static final int    DATABASE_VERSION   = 1;
	private static final String DATABASE_NAME      = "checkindb";

	public  static final String CHECKIN_TABLE_NAME = "checkins";
	public  static final String ID_NAME            = "_id";
	public  static final String BLOB_NAME          = "blob";

	/**
	 * Format the data as a geojson blob and store just the blob in the DB. Individual
	 * datum will not be stored.
	 */
	private static final String CHECKIN_TABLE_CREATE =
			"CREATE TABLE " + CHECKIN_TABLE_NAME + " ("
					+ ID_NAME + " INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, "
					+  BLOB_NAME + " TEXT NOT NULL);";

	CheckInOpenHelper(final Context context) {
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
	}

	@Override
	public void onCreate(final SQLiteDatabase db) {
		db.execSQL(CHECKIN_TABLE_CREATE);
	}

	/**
	 * Called when the database needs to be upgraded. The implementation
	 * should use this method to drop tables, add tables, or do anything else it
	 * needs to upgrade to the new schema version.
	 * <p/>
	 * <p>
	 * The SQLite ALTER TABLE documentation can be found
	 * <a href="http://sqlite.org/lang_altertable.html">here</a>. If you add new columns
	 * you can use ALTER TABLE to insert them into a live table. If you rename or remove columns
	 * you can use ALTER TABLE to rename the old table, then create the new table and then
	 * populate the new table with the contents of the old table.
	 * </p><p>
	 * This method executes within a transaction.  If an exception is thrown, all changes
	 * will automatically be rolled back.
	 * </p>
	 *
	 * @param db         The database.
	 * @param oldVersion The old database version.
	 * @param newVersion The new database version.
	 */
	@Override
	public void onUpgrade(final SQLiteDatabase db, final int oldVersion, final int newVersion) {
		Log.w(CheckInOpenHelper.class.getName(),
				"Upgrading database from version " + oldVersion + " to "
						+ newVersion + ", which will destroy all old data"
		);
		db.execSQL("DROP TABLE IF EXISTS " + CHECKIN_TABLE_NAME);
		onCreate(db);
	}
}
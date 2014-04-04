package com.samwhited.checkin;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDatabase;

public class CheckInDB {
	final private SQLiteDatabase db;

	public CheckInDB(final Context context) {
		final CheckInOpenHelper helper = new CheckInOpenHelper(context);
		db = helper.getWritableDatabase();
	}

	public long createRecords(final String blob){
		final ContentValues values = new ContentValues();
		values.put(CheckInOpenHelper.BLOB_NAME, blob);
		return db.insert(CheckInOpenHelper.CHECKIN_TABLE_NAME, null, values);
	}

	public Cursor selectRecords() {
		final String[] cols = new String[]{CheckInOpenHelper.ID_NAME, CheckInOpenHelper.BLOB_NAME};
		final Cursor mCursor = db.query(true, CheckInOpenHelper.CHECKIN_TABLE_NAME, cols, null,
				null, null, null, null, null);
		if (mCursor != null) {
			mCursor.moveToFirst();
		}
		return mCursor;
	}

	public int deleteRecord(final long id) {
		return db.delete(CheckInOpenHelper.CHECKIN_TABLE_NAME,
				CheckInOpenHelper.ID_NAME + id,
				null);
	}

	public long numRecords() {
		return DatabaseUtils.queryNumEntries(db, CheckInOpenHelper.CHECKIN_TABLE_NAME);
	}

}

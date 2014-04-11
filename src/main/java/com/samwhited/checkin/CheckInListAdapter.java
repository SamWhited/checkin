package com.samwhited.checkin;

import android.content.Context;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.CursorAdapter;
import android.widget.TextView;

import com.samwhited.checkin.database.CheckInOpenHelper;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class CheckInListAdapter extends CursorAdapter implements AbsListView.RecyclerListener {

	private boolean batchMode;
	private final LayoutInflater inflater;

	private final Set<Long> batchSet = Collections.synchronizedSet(new HashSet<Long>());

	public Set<Long> getBatchSet() {
		return batchSet;
	}

	public void initializeBatchMode(final boolean batchMode) {
		this.batchMode = batchMode;
		unselectAllCheckIns();
	}

	private void unselectAllCheckIns() {
		this.batchSet.clear();
		this.notifyDataSetChanged();
	}

	/**
	 * Recommended constructor.
	 *
	 * @param context The context
	 * @param c       The cursor from which to get the data.
	 * @param flags   Flags used to determine the behavior of the adapter; may
	 *                be any combination of {@link #FLAG_AUTO_REQUERY} and
	 *                {@link #FLAG_REGISTER_CONTENT_OBSERVER}.
	 */
	public CheckInListAdapter(final Context context, final Cursor c, final int flags) {
		super(context, c, flags);
		this.inflater = LayoutInflater.from(context);
	}

	/**
	 * Makes a new view to hold the data pointed to by cursor.
	 *
	 * @param context Interface to application's global information
	 * @param cursor  The cursor from which to get the data. The cursor is already
	 *                moved to the correct position.
	 * @param parent  The parent to which the new view is attached to
	 * @return the newly created view.
	 */
	@Override
	public View newView(final Context context, final Cursor cursor, final ViewGroup parent) {
		return inflater.inflate(R.layout.checkin_list_item_view, parent, false);
	}

	/**
	 * Bind an existing view to the data pointed to by cursor
	 *
	 * @param view    Existing view, returned earlier by newView
	 * @param context Interface to application's global information
	 * @param cursor  The cursor from which to get the data. The cursor is already
	 */
	@Override
	public void bindView(final View view, final Context context, final Cursor cursor) {
		final long id = cursor.getLong(cursor.getColumnIndex(CheckInOpenHelper.ID_NAME));
		if (id != -1) {
			((CheckInListItem) view).set(cursor, batchSet, batchMode);
		}
	}

	public void toggleInBatchSet(final long id) {
		if (batchSet.contains(id)) {
			batchSet.remove(id);
		} else {
			batchSet.add(id);
		}
	}

	/**
	 * Indicates that the specified View was moved into the recycler's scrap heap.
	 * The view is not displayed on screen any more and any expensive resource
	 * associated with the view should be discarded.
	 *
	 * @param view The {@link com.samwhited.checkin.CheckInListItem} to unbind.
	 */
	@Override
	public void onMovedToScrapHeap(final View view) {
		// Discard refrences to icon drawables so they can be garbage collected.
		final TextView dateView = (TextView)view.findViewById(R.id.date);
		dateView.setCompoundDrawables(null, null, null, null);
	}
}

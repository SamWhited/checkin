package com.samwhited.checkin;

import android.content.Context;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;

import com.samwhited.checkin.database.CheckInOpenHelper;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class CheckInListAdapter extends SimpleCursorAdapter implements AbsListView.RecyclerListener {

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
	 * Standard constructor.
	 *
	 * @param context The context where the ListView associated with this
	 *                SimpleListItemFactory is running
	 * @param layout  resource identifier of a layout file that defines the views
	 *                for this list item. The layout file should include at least
	 *                those named views defined in "to"
	 * @param c       The database cursor.  Can be null if the cursor is not available yet.
	 * @param from    A list of column names representing the data to bind to the UI.  Can be null
	 *                if the cursor is not available yet.
	 * @param to      The views that should display column in the "from" parameter.
	 *                These should all be TextViews. The first N views in this list
	 *                are given the values of the first N columns in the from
	 *                parameter.  Can be null if the cursor is not available yet.
	 * @param flags   Flags used to determine the behavior of the adapter,
	 *                as per {@link android.widget.CursorAdapter#CursorAdapter(android.content.Context, android.database.Cursor, int)}.
	 */
	public CheckInListAdapter(final Context context,
							  final int layout,
							  final Cursor c,
							  final String[] from,
							  final int[] to,
							  final int flags) {
		super(context, layout, c, from, to, flags);
		this.inflater = LayoutInflater.from(context);
		this.batchMode = false;
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
	 * Binds all of the field names passed into the "to" parameter of the
	 * constructor with their corresponding cursor columns as specified in the
	 * "from" parameter.
	 * <p/>
	 * Binding occurs in two phases. First, if a
	 * {@link android.widget.SimpleCursorAdapter.ViewBinder} is available,
	 * {@link android.widget.SimpleCursorAdapter.ViewBinder#setViewValue(android.view.View, android.database.Cursor, int)}
	 * is invoked. If the returned value is true, binding has occured. If the
	 * returned value is false and the view to bind is a TextView,
	 * {@link #setViewText(android.widget.TextView, String)} is invoked. If the returned value is
	 * false and the view to bind is an ImageView,
	 * {@link #setViewImage(android.widget.ImageView, String)} is invoked. If no appropriate
	 * binding can be found, an {@link IllegalStateException} is thrown.
	 *
	 * @param view The view to bind.
	 * @param context The context to use.
	 * @param cursor A cursor pointing to the data to use.
	 * @throws IllegalStateException if binding cannot occur
	 * @see android.widget.CursorAdapter#bindView(android.view.View,
	 * android.content.Context, android.database.Cursor)
	 * @see #getViewBinder()
	 * @see #setViewBinder(android.widget.SimpleCursorAdapter.ViewBinder)
	 * @see #setViewImage(android.widget.ImageView, String)
	 * @see #setViewText(android.widget.TextView, String)
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
	 * Called when the {@link android.database.ContentObserver} on the cursor receives a change notification.
	 * The default implementation provides the auto-requery logic, but may be overridden by
	 * sub classes.
	 *
	 * @see android.database.ContentObserver#onChange(boolean)
	 */
	@Override
	protected void onContentChanged() {
		super.onContentChanged();
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
		// Discard references to icon drawables so they can be garbage collected.
		final TextView dateView = (TextView)view.findViewById(R.id.date);
		dateView.setCompoundDrawables(null, null, null, null);
	}
}

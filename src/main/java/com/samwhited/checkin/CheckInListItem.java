package com.samwhited.checkin;


import android.content.Context;
import android.content.res.TypedArray;
import android.database.Cursor;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.samwhited.checkin.database.CheckInOpenHelper;
import com.samwhited.checkin.model.CheckIn;
import com.samwhited.checkin.util.Formatting;

import java.util.Set;

public class CheckInListItem extends RelativeLayout {

	private long              itemId;
	private Set<Long>         selectedItems;
	private TextView          coordinatesView;
	private TextView          dateView;

	public CheckInListItem(final Context context) {
		super(context);
	}

	public CheckInListItem(final Context context, final AttributeSet attrs) {
		super(context, attrs);
	}

	@Override
	protected void onFinishInflate() {
		this.coordinatesView = (TextView) findViewById(R.id.coordinates);
		this.dateView        = (TextView) findViewById(R.id.date);
	}

	public void set(final Cursor cursor, final Set<Long> selectedItems, final boolean batchMode) {
		if (cursor != null) {
			final CheckIn checkIn = new CheckIn(cursor);
			this.itemId = cursor.getLong(cursor.getColumnIndex(CheckInOpenHelper.ID_NAME));
			if (coordinatesView != null && dateView != null) {
				coordinatesView.setText(Formatting.formatCoordinates(getContext(), checkIn.getLocation()));
				dateView.setText(Formatting.formatDateTime(getContext(), checkIn.getTime()));
				// Show an icon by the date view.
				final String icon = checkIn.getIcon();
				if (!icon.isEmpty()
						&& !icon.equalsIgnoreCase("none")
						&& getContext() != null
						&& getResources() != null) {
					dateView.setCompoundDrawablesWithIntrinsicBounds(
							getResources().getIdentifier(icon.replace("-", "_"),
									"drawable",
									getContext().getPackageName()), 0, 0, 0);
				} else {
					dateView.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0);
				}
			}
		}

		if (batchMode && selectedItems.contains(this.itemId)) {
			setBackground();
		}
	}

	public long getItemId() {
		return itemId;
	}

	private void setBackground() {
		if (this.getContext() != null) {
			int[] attributes = new int[]{
					R.attr.list_item_selected_background_color,
			};

			final TypedArray drawables = getContext().obtainStyledAttributes(attributes);
			if (drawables != null) {
				setBackground(drawables.getDrawable(0));
				drawables.recycle();
			}
		}
	}
}

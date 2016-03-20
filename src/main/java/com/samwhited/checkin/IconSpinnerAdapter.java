package com.samwhited.checkin;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.SpinnerAdapter;
import android.widget.TextView;

public class IconSpinnerAdapter extends ArrayAdapter implements SpinnerAdapter {

	final String[] icons;

	/**
	 * Constructor
	 *
	 * @param context  The current context.
	 * @param resource The resource ID for a layout file containing a TextView to use when
	 *                 instantiating views.
	 * @param names    The objects to represent in the ListView.
	 * @param icons    The icons to show next to the names in the ListView.
	 */
	public IconSpinnerAdapter(final Context context,
							  final int resource,
							  final String[] names,
							  final String[] icons) {
		super(context, resource, names);
		this.icons = icons;
	}

	/**
	 * {@inheritDoc}
	 *
	 * @param position
	 * @param convertView
	 * @param parent
	 */
	@Override
	public View getView(final int position, final View convertView, final ViewGroup parent) {
		final View view = super.getView(position, convertView, parent);
		if (view != null && view instanceof TextView) {
			if (!icons[position].isEmpty() && !icons[position].equalsIgnoreCase("none")) {
				((TextView) view).setCompoundDrawablesWithIntrinsicBounds(
						getContext().getResources().getIdentifier(icons[position].replace("-", "_"),
								"drawable",
								getContext().getPackageName()), 0, 0, 0);
			} else {
				((TextView) view).setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0);
			}
		}
		return view;
	}

	/**
	 * {@inheritDoc}
	 *
	 * @param position
	 * @param convertView
	 * @param parent
	 */
	@Override
	public View getDropDownView(final int position, final View convertView, final ViewGroup parent) {
		final View view = super.getDropDownView(position, convertView, parent);
		if (view != null && view instanceof TextView) {
			if (!icons[position].isEmpty() && !icons[position].equalsIgnoreCase("none")) {
				((TextView) view).setCompoundDrawablesWithIntrinsicBounds(
						getContext().getResources().getIdentifier(icons[position].replace("-", "_"),
								"drawable",
								getContext().getPackageName()), 0, 0, 0);
			} else {
				((TextView) view).setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0);
			}
		}
		return view;
	}
}

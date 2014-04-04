package com.samwhited.checkin;

import android.app.Activity;
import android.app.Fragment;
import android.os.Bundle;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.samwhited.checkin.util.CheckInPreferences;

/**
 * A simple {@link android.app.Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link CheckInFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 */
public class CheckInFragment extends Fragment implements Button.OnClickListener {

	// TODO: Possibly make this a preference?
	public final static int dateFormatFlags = DateUtils.FORMAT_ABBREV_MONTH |
			DateUtils.FORMAT_SHOW_YEAR |
			DateUtils.FORMAT_SHOW_DATE |
			DateUtils.FORMAT_SHOW_TIME;
	private OnFragmentInteractionListener mListener;

	public CheckInFragment() {
		// Required empty public constructor
	}

	private void updateLastCheckinText(final View view) {
		if (view == null) {
			return;
		}
		final TextView lastCheckin = (TextView) view.findViewById(R.id.last_check_in);
		final TextView lastCheckinTitle = (TextView) view.findViewById(R.id.last_check_in_title);

		if (lastCheckin != null && getActivity() != null) {
			// Update the last checkin time.
			final long checkinTime = CheckInPreferences.getLastCheckin(getActivity());
			if (checkinTime != 0) {
				final String formatted = DateUtils.formatDateTime(getActivity(),
						checkinTime,
						dateFormatFlags);
				lastCheckin.setText(formatted);
				lastCheckinTitle.setVisibility(View.VISIBLE);
			}
		}

		final TextView unuploaded = (TextView) view.findViewById(R.id.unuploaded_checkins);
		final View unuploadedLayout = view.findViewById(R.id.unuploaded_layout);

		if (unuploaded != null && getActivity() != null) {
			// Update the last checkin time.
			final long numCheckins = CheckInPreferences.getNumCheckins(getActivity());
			unuploaded.setText(String.valueOf(numCheckins));
			unuploadedLayout.setVisibility(View.VISIBLE);
		}
	}

	protected void updateLastCheckinText() {
		updateLastCheckinText(getView());
	}

	@Override
	public View onCreateView(final LayoutInflater inflater,
							 final ViewGroup container,
							 final Bundle savedInstanceState) {
		// Inflate the layout for this fragment
		final View view = inflater.inflate(R.layout.fragment_check_in, container, false);
		if (view != null) {
			final Button button = (Button) view.findViewById(R.id.button_check_in);
			if (button != null) {
				button.setOnClickListener(this);
			}

			updateLastCheckinText(view);
		}
		return view;
	}

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		try {
			mListener = (OnFragmentInteractionListener) activity;
		} catch (ClassCastException e) {
			throw new ClassCastException(activity.toString()
					+ " must implement OnFragmentInteractionListener");
		}
	}

	@Override
	public void onDetach() {
		super.onDetach();
		mListener = null;
	}

	/**
	 * Called when the check in button has been clicked.
	 *
	 * @param v The view that was clicked.
	 */
	@Override
	public void onClick(final View v) {
		if (mListener != null) {
			mListener.onFragmentInteraction(v);
		}
	}

	/**
	 * This interface must be implemented by activities that contain this
	 * fragment to allow an interaction in this fragment to be communicated
	 * to the activity and potentially other fragments contained in that
	 * activity.
	 */
	public interface OnFragmentInteractionListener {
		public void onFragmentInteraction(final View view);
	}

}

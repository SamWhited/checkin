package com.samwhited.checkin;

import android.app.Activity;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Intent;
import android.content.IntentSender;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesClient;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.location.LocationClient;
import com.samwhited.checkin.util.CheckInPreferences;
import com.samwhited.checkin.util.GeoJSON;
import com.samwhited.checkin.util.NetworkUtils;

import org.json.JSONException;

import java.util.Calendar;

public class CheckIn extends Activity implements CheckInFragment.OnFragmentInteractionListener,
		GooglePlayServicesClient.OnConnectionFailedListener,
		GooglePlayServicesClient.ConnectionCallbacks {

	/*
	 * Define a request code to send to Google Play services
     * This code is returned in Activity.onActivityResult
     */
	private final static int CONNECTION_FAILURE_RESOLUTION_REQUEST = 9000;
	/**
	 * A handler to use for long running tasks that shouldn't block the UI thread (uploads).
	 */
	private CheckInHandler mHandler;
	/**
	 * The location client for getting location data.
	 */
	private LocationClient mLocationClient;

	/**
	 * The database to use for storing checkins.
	 */
	private CheckInDB db;

	/**
	 * The options menu used by this activity.
	 */
	private Menu mOptionsMenu;

	@Override
	protected void onCreate(final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_check_in);
		if (savedInstanceState == null) {
			getFragmentManager().beginTransaction()
					.add(R.id.container, new CheckInFragment())
					.commit();
		}

		/*
		 * Create a new location client, using the enclosing class to
         * handle callbacks.
         */
		mLocationClient = new LocationClient(this, this, this);

		/**
		 * Create or open the database which we'll use to store check in's.
		 */
		db = new CheckInDB(this);

		/**
		 * Create a handler for handling events on the UI thread
		 */
		mHandler = new CheckInHandler(this);

	}

	/*
     * Called when the Activity becomes visible.
     */
	@Override
	protected void onStart() {
		super.onStart();
		// Connect the client.
		mLocationClient.connect();
	}

	/*
	 * Called when the Activity is no longer visible.
	 */
	@Override
	protected void onStop() {
		// Disconnecting the client invalidates it.
		mLocationClient.disconnect();
		super.onStop();
	}

	public Menu getOptionsMenu() {
		return mOptionsMenu;
	}

	@Override
	public boolean onCreateOptionsMenu(final Menu menu) {
		mOptionsMenu = menu;
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.check_in, menu);
		return true;
	}

	protected void updateLastCheckinText() {
		final CheckInFragment fragment = (CheckInFragment) getFragmentManager().findFragmentById(R.id.container);
		if (fragment != null) {
			fragment.updateLastCheckinText();
		}
	}

	private void handleCheckIn() {
		final Location location = getLocation();
		if (location == null) {
			Toast.makeText(
					this,
					getResources().getString(R.string.error_failed_to_get_location),
					Toast.LENGTH_LONG
			).show();
			return;
		}
		final String json;
		try {
			json = GeoJSON.constructPoint(location).toString();
		} catch (JSONException e) {
			e.printStackTrace();
			Toast.makeText(
					this,
					getResources().getString(R.string.error_failed_to_construct_geojson),
					Toast.LENGTH_LONG
			).show();
			return;
		}

		if (db.createRecords(json) != -1) {
			CheckInPreferences.setLastCheckin(this, Calendar.getInstance().getTimeInMillis());
			CheckInPreferences.setNumCheckins(this, db.numRecords());
			updateLastCheckinText();
			Toast.makeText(this,
					getResources().getString(R.string.checked_in)
							+ " " + location.getLatitude() + ", " + location.getLongitude(),
					Toast.LENGTH_LONG
			).show();
		}
	}

	private void handleUpload() {
		NetworkUtils.uploadCheckIns(db, mHandler);
	}

	@Override
	public boolean onOptionsItemSelected(final MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		switch (item.getItemId()) {
			case R.id.action_settings:
				startActivity(new Intent(this, SettingsActivity.class));
				return true;
			case R.id.action_checkin:
				handleCheckIn();
				return true;
			case R.id.action_upload:
				handleUpload();
				break;
		}
		return super.onOptionsItemSelected(item);
	}

	/**
	 * Fetch some GPS data to be posted to the server.
	 *
	 * @return The location.
	 */
	private Location getLocation() {
		final Location location;
		if (servicesConnected()) {
			location = mLocationClient.getLastLocation();
		} else {
			location = null;
		}

		return location;
	}

	/**
	 * When a button in the view is pressed...
	 */
	@Override
	public void onFragmentInteraction(final View view) {
		switch (view.getId()) {
			case R.id.button_check_in:
				handleCheckIn();
				break;
		}
	}

	/**
	 * Called when we've connected to Google Play Services.
	 *
	 * @param bundle The bundle
	 */
	@Override
	public void onConnected(final Bundle bundle) {
	}

	/**
	 * Called when we've disconnected from Google Play Services.
	 */
	@Override
	public void onDisconnected() {
		if (this.getResources() != null) {
			Toast.makeText(this,
					getResources().getString(R.string.error_location_lost),
					Toast.LENGTH_SHORT).show();
		}
	}

	/**
	 * Called if we can't connect to Google Play Services.
	 *
	 * @param connectionResult The connection result.
	 */
	@Override
	public void onConnectionFailed(final ConnectionResult connectionResult) {
		if (connectionResult.hasResolution()) {
			try {
				// Start an Activity that tries to resolve the error
				connectionResult.startResolutionForResult(
						this,
						CONNECTION_FAILURE_RESOLUTION_REQUEST);
                /*
                 * Thrown if Google Play services canceled the original
                 * PendingIntent
                 */
			} catch (IntentSender.SendIntentException e) {
				// Log the error
				e.printStackTrace();
			}
		} else {
            /*
             * If no resolution is available, display a dialog to the
             * user with the error.
             */
			GooglePlayServicesUtil.getErrorDialog(connectionResult.getErrorCode(),
					this,
					CONNECTION_FAILURE_RESOLUTION_REQUEST).show();
		}
	}

	private boolean servicesConnected() {
		// Check that Google Play services is available
		final int resultCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);
		// If Google Play services is available
		if (ConnectionResult.SUCCESS == resultCode) {
			// In debug mode, log the status
			Log.d("Location Updates", "Google Play services is available.");
			// Continue
			return true;
		} else {
			// Get the error dialog from Google Play services
			final Dialog errorDialog = GooglePlayServicesUtil.getErrorDialog(
					resultCode,
					this,
					CONNECTION_FAILURE_RESOLUTION_REQUEST);

			// If Google Play services can provide an error dialog
			if (errorDialog != null) {
				// Create a new DialogFragment for the error dialog
				final ErrorDialogFragment errorFragment = new ErrorDialogFragment();
				// Set the dialog in the DialogFragment
				errorFragment.setDialog(errorDialog);
				// Show the error dialog in the DialogFragment
				if (getResources() != null) {
					errorFragment.show(getFragmentManager(),
							getResources().getString(R.string.title_location_updates)
					);
				}
			}
			return false;
		}
	}

	// Define a DialogFragment that displays the error dialog
	public static class ErrorDialogFragment extends DialogFragment {
		// Global field to contain the error dialog
		private Dialog mDialog;

		// Default constructor. Sets the dialog field to null
		public ErrorDialogFragment() {
			super();
			mDialog = null;
		}

		// Set the dialog to display
		public void setDialog(Dialog dialog) {
			mDialog = dialog;
		}

		// Return a Dialog to the DialogFragment.
		@Override
		public Dialog onCreateDialog(Bundle savedInstanceState) {
			return mDialog;
		}
	}
}

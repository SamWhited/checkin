package com.samwhited.checkin;

import android.app.Activity;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Intent;
import android.content.IntentSender;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
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

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class CheckIn extends Activity implements CheckInFragment.OnFragmentInteractionListener, GooglePlayServicesClient.OnConnectionFailedListener, GooglePlayServicesClient.ConnectionCallbacks {

	/**
	 * The names for fields in the HTTP POST.
	 */
	public final static String POST_GPS_DATA = "location";
	public final static String POST_API_KEY  = "api_key";

	/**
	 * Messages which can be sent to the UI thread handler.
	 */
	public final static int SHOW_TOAST     = 0;
	public final static int ENABLE_VIEW    = 1;
	public final static int UPDATE_CHECKIN = 2;

	/*
     * Define a request code to send to Google Play services
     * This code is returned in Activity.onActivityResult
     */
	private final static int CONNECTION_FAILURE_RESOLUTION_REQUEST = 9000;

	/**
	 * The location client for getting location data.
	 */
	private LocationClient mLocationClient;


	// Handler for callbacks to the UI thread
	private final Handler mHandler = new Handler() {
		/**
		 * Handle a message sent from the networking thread.
		 * @param msg The message object.
		 */
		@Override
		public void handleMessage(final Message msg) {
			if (msg == null) {
				return;
			}

			if (msg.obj != null) {
				final View view;
				switch (msg.arg1) {
					case SHOW_TOAST:
						final String text = msg.obj.toString();
						if (text != null && !text.isEmpty() && getApplicationContext() != null) {
							Toast.makeText(getApplicationContext(),
									text,
									Toast.LENGTH_LONG).show();
						}
						break;
					case ENABLE_VIEW:
						view = (View)msg.obj;
						view.setEnabled(true);
						break;
					case UPDATE_CHECKIN:
						view = (View) msg.obj;
						final CheckInFragment checkInFragment =
								(CheckInFragment)getFragmentManager().findFragmentById(R.id.container);
						if (checkInFragment != null) {
							checkInFragment.updateLastCheckinText(view);
						}
				}
			}

		}
	};


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


	@Override
    public boolean onCreateOptionsMenu(final Menu menu) {
        
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.check_in, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(final MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.action_settings) {
			startActivity(new Intent(this, SettingsActivity.class));
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	/**
	 * When the check in button is pressed...
	 */
	@Override
	public void onFragmentInteraction(final View view) {
		if (view != null && view.getContext() != null) {
			view.setEnabled(false);
			final HttpClient client = new DefaultHttpClient();
			final String url = CheckInPreferences.getServerPref(view.getContext());

			if (url == null || url.isEmpty()) {
				if (getResources() != null) {
					Toast.makeText(this,
							getResources().getString(R.string.error_no_url_in_settings),
							Toast.LENGTH_LONG).show();
				}
				view.setEnabled(true);
				return;
			}
			final HttpPost post = new HttpPost(CheckInPreferences.getServerPref(view.getContext()));
			/**
			 * The data to be posted to the server.
			 */
			final List<NameValuePair> pairs = new ArrayList<>();
			new Thread(new Runnable() {

				/**
				 * Show a toast on the UI thread.
				 * @param toast The text of the toast to show.
				 */
				private void showToast(final String toast) {
					if (toast != null && !toast.isEmpty()) {
						final Message msg = mHandler.obtainMessage();
						msg.arg1 = SHOW_TOAST;
						msg.obj = toast;
						mHandler.sendMessage(msg);
					}
				}

				/**
				 * Enable the given view on the UI thread.
				 * @param view the view to enable.
				 */
				private void enableView(final View view) {
					if (view != null) {
						final Message msg = mHandler.obtainMessage();
						msg.arg1 = ENABLE_VIEW;
						msg.obj = view;
						mHandler.sendMessage(msg);
					}
				}

				/**
				 * Update the last checkin text
				 * @param view The view which contains the last checkin text and lable.
				 */
				private void updateLastCheckin(final View view) {
					if (view != null) {
						final Message msg = mHandler.obtainMessage();
						msg.arg1 = UPDATE_CHECKIN;
						msg.obj = view;
						mHandler.sendMessage(msg);
					}
				}

				/**
				 * Fetch some GPS data to be posted to the server.
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
				 * Starts executing the active part of the class' code. This method is
				 * called when a thread is started that has been created with a class which
				 * implements {@code Runnable}.
				 */
				@Override
				public void run() {
					// Get the location data for the post...
					final Location location = getLocation();
					if (location != null) {
						pairs.add(new BasicNameValuePair(POST_GPS_DATA, location.toString()));
						pairs.add(new BasicNameValuePair(POST_API_KEY, CheckInPreferences.getApikeyPref(getApplicationContext())));
					} else {
						if (getResources() != null) {
							showToast(
									getResources().getString(R.string.error_failed_to_get_location)
							);
						}
						return;
					}

					// Add the data to the post request...
					try {
						post.setEntity(new UrlEncodedFormEntity(pairs));
					} catch (UnsupportedEncodingException e) {
						if (getResources() != null) {
							showToast(getResources().getString(R.string.error_unsupported_encoding));
						}
						enableView(view);
						return;
					}

					// Run the HTTP post...
					final HttpResponse response;
					try {
						response = client.execute(post);
					} catch (IOException e) {
						if (getResources() != null) {
							showToast(getResources().getString(R.string.error_connection_failed));
						}
						enableView(view);
						return;
					}
					if (response != null) {
						showToast(response.getStatusLine().toString());
						if (response.getStatusLine().getStatusCode() == 200
								&& getApplicationContext() != null) {
							CheckInPreferences.setLastCheckin(getApplicationContext(),
									Calendar.getInstance().getTimeInMillis());
						}
					}

					// Enable the button again.
					enableView(view);
					updateLastCheckin(view.getRootView());
				}
			}).start();
		}
	}

	/**
	 * Called when we've connected to Google Play Services.
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
}

package com.samwhited.checkin.util;

import android.content.Context;
import android.database.Cursor;
import android.os.Message;
import android.widget.Toast;

import com.samwhited.checkin.CheckInDB;
import com.samwhited.checkin.CheckInHandler;
import com.samwhited.checkin.CheckInOpenHelper;
import com.samwhited.checkin.R;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

/**
 * Utility class for network related functions.
 */
public class NetworkUtils {

	/**
	 * The names for fields in the HTTP POST.
	 */
	public final static String POST_GPS_DATA = "location";
	public final static String POST_DATA_TYPE = "type";
	public final static String POST_API_KEY = "api_key";

	public static void uploadCheckIns(final CheckInDB db, final CheckInHandler mHandler) {
		final HttpClient client = new DefaultHttpClient();
		final Context context = mHandler.getContext();
		if (context == null) {
			throw new NullPointerException();
		}
		final String url = CheckInPreferences.getServerPref(context);

		if (url == null || url.isEmpty()) {
			if (context.getResources() != null) {
				Toast.makeText(context,
						context.getResources().getString(R.string.error_no_url_in_settings),
						Toast.LENGTH_LONG).show();
			}
			return;
		}
		final HttpPost post = new HttpPost(CheckInPreferences.getServerPref(context));

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
					msg.arg1 = CheckInHandler.SHOW_TOAST;
					msg.obj = toast;
					mHandler.sendMessage(msg);
				}
			}

			/**
			 * Update any views that change based on the state of the DB (eg. unuploaded count)
			 */
			private void updateView() {
				final Message msg = mHandler.obtainMessage();
				msg.arg1 = CheckInHandler.UPDATE_VIEW;
				mHandler.sendMessage(msg);
			}

			/**
			 * Enable or disable the upload menu item.
			 * @param enabled Is the menu item enabled?
			 */
			private void setMenuEnabled(final boolean enabled) {
				final Message msg = mHandler.obtainMessage();
				if (enabled) {
					msg.arg1 = CheckInHandler.ENABLE_UPLOAD;
				} else {
					msg.arg1 = CheckInHandler.DISABLE_UPLOAD;
				}
				mHandler.sendMessage(msg);
			}

			/**
			 * Starts executing the active part of the class' code. This method is
			 * called when a thread is started that has been created with a class which
			 * implements {@code Runnable}.
			 */
			@Override
			public void run() {
				setMenuEnabled(false);
				final Cursor cursor = db.selectRecords();
				cursor.moveToFirst();

				final JSONObject geojson;

				// A list of posted IDs (for deleting later)
				final List<Long> ids = new ArrayList<>();

				switch ((int) db.numRecords()) {
					case 0:
						showToast(context.getResources().getString(R.string.error_nothing_to_upload));
						setMenuEnabled(true);
						return;
					case 1:
						// Post a feature object
						pairs.add(new BasicNameValuePair(POST_DATA_TYPE, "Feature"));

						try {
							geojson = new JSONObject(cursor.getString(
									cursor.getColumnIndex(CheckInOpenHelper.BLOB_NAME)));
							geojson.put("type", "Feature");
						} catch (JSONException e) {
							showToast(context.getResources().getString(R.string.error_failed_to_construct_geojson));
							e.printStackTrace();
							setMenuEnabled(true);
							return;
						}
						ids.add(cursor.getLong(cursor.getColumnIndex(CheckInOpenHelper.ID_NAME)));
						break;
					default:
						// Post a feature collection
						geojson = new JSONObject();
						final JSONArray feature_array = new JSONArray();

						// Add each json blob to the array.
						for (JSONObject point; !cursor.isAfterLast(); cursor.moveToNext()) {
							try {
								point = new JSONObject(
										cursor.getString(
												cursor.getColumnIndex(CheckInOpenHelper.BLOB_NAME))
								);
							} catch (JSONException e1) {
								// If the record is invalid, just delete it.
								db.deleteRecord(cursor.getLong(
										cursor.getColumnIndex(CheckInOpenHelper.ID_NAME)));
								continue;
							}
							feature_array.put(point);
							// SELECT id FROM table WHERE name IS myVar
							ids.add(cursor.getLong(cursor.getColumnIndex(CheckInOpenHelper.ID_NAME)));
						}

						try {
							geojson.put("features", feature_array);
							geojson.put("type", "FeatureCollection");
						} catch (JSONException e) {
							e.printStackTrace();
						}
						break;
				}

				try {
					final String type = geojson.get("type").toString();
					pairs.add(new BasicNameValuePair(POST_DATA_TYPE, type));
				} catch (final JSONException e) {
					e.printStackTrace();
					pairs.add(new BasicNameValuePair(POST_DATA_TYPE, "Unknown"));
				}
				pairs.add(new BasicNameValuePair(POST_GPS_DATA, geojson.toString()));
				pairs.add(new BasicNameValuePair(POST_API_KEY,
						CheckInPreferences.getApikeyPref(context)));

				// Add the data to the post request...
				try {
					post.setEntity(new UrlEncodedFormEntity(pairs));
				} catch (UnsupportedEncodingException e) {
					if (context.getResources() != null) {
						showToast(context.getResources().getString(R.string.error_unsupported_encoding));
					}
					setMenuEnabled(true);
					return;
				}

				// Run the HTTP post...
				final HttpResponse response;
				try {
					response = client.execute(post);
				} catch (IOException e) {
					if (context.getResources() != null) {
						showToast(context.getResources().getString(R.string.error_connection_failed));
					}
					setMenuEnabled(true);
					return;
				}
				if (response != null) {
					showToast(response.getStatusLine().toString());
					if (response.getStatusLine().getStatusCode() == 200) {
						// Delete DB objects that have been posted
						for (final long id : ids) {
							db.deleteRecord(id);
						}
						CheckInPreferences.setNumCheckins(context, db.numRecords());
					}
				}

				// Update the views
				setMenuEnabled(true);
				updateView();
			}
		}).start();
	}
}

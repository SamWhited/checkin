package com.samwhited.checkin;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.CursorAdapter;
import android.widget.SimpleCursorAdapter;

import com.samwhited.checkin.database.CheckInDB;
import com.samwhited.checkin.database.CheckInOpenHelper;
import com.samwhited.checkin.util.NetworkUtils;


public class CheckInList extends Activity
		implements CheckInListFragment.OnListInteractionListener {

	private CheckInListFragment fragment;

	public CheckInList() {
		// Empty constructor.
	}

	/**
	 * A handler to use for long running tasks that shouldn't block the UI thread (uploads).
	 */
	private CheckInHandler mHandler;

	/**
	 * The database to use for storing checkins.
	 */
	private CheckInDB db;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_check_in_list);

		db = new CheckInDB(this);
		mHandler = new CheckInHandler(this);

		fragment = new CheckInListFragment();
		fragment.setListAdapter(new SimpleCursorAdapter(this,
				android.R.layout.simple_list_item_1,
				db.selectRecords(),
				new String[]{CheckInOpenHelper.ID_NAME},
				new int[]{android.R.id.text1},
				CursorAdapter.FLAG_REGISTER_CONTENT_OBSERVER
		));
        if (savedInstanceState == null) {
            getFragmentManager().beginTransaction()
                    .add(R.id.container, fragment)
					.addToBackStack(null)
					.commit();
        }
    }


    @Override
    public boolean onCreateOptionsMenu(final Menu menu) {
        
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.check_in_list, menu);
        return true;
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
			case R.id.action_discard:
				handleDiscard();
				return true;
			case R.id.action_upload:
				handleUpload();
				return true;
		}
		return super.onOptionsItemSelected(item);
	}

	private void handleDiscard() {
		final AlertDialog.Builder alertDialog = new AlertDialog.Builder(CheckInList.this);
		alertDialog.setTitle(R.string.title_discard_all_checkins);
		alertDialog.setMessage(R.string.confirm_discard_all_checkins);
		alertDialog.setPositiveButton(R.string.delete, new DialogInterface.OnClickListener() {
			public void onClick(final DialogInterface dialogInterface, final int arg1) {
				new AsyncTask<Void, Void, Void>() {
					@Override
					protected Void doInBackground(Void... params) {
						db.deleteAllRecords();
						return null;
					}
					@Override
					protected void onPostExecute(Void result) {
						notifyDatasetChanged();
					}
				}.execute();
			}
		});
		alertDialog.setNegativeButton(android.R.string.cancel, null);
		alertDialog.show();
	}

	private void handleUpload() {
		NetworkUtils.uploadCheckIns(db, mHandler);
		notifyDatasetChanged();
	}

	private void notifyDatasetChanged() {
		if (fragment.getListAdapter() != null) {
			((CheckInListAdapter) fragment.getListAdapter()).notifyDataSetChanged();
		}
	}

	/**
	 * Fires when the list fragment is interacted with.
	 * @param id The id of the list element which was clicked.
	 */
	@Override
	public void onListInteraction(final long id) {
		// TODO: Show a map view?
	}
}

package com.samwhited.checkin;

import android.app.Activity;
import android.app.ListFragment;
import android.app.LoaderManager;
import android.content.Loader;
import android.database.Cursor;
import android.os.Bundle;
import android.view.ActionMode;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.CursorAdapter;
import android.widget.ListView;

/**
 * A fragment representing a list of check in's.
 * <p />
 * <p />
 * Activities containing this fragment MUST implement the {@link com.samwhited.checkin.CheckInListFragment.OnListInteractionListener}
 * interface.
 */
public class CheckInListFragment extends ListFragment implements
		AbsListView.MultiChoiceModeListener,
		LoaderManager.LoaderCallbacks<Cursor>,
		ActionMode.Callback {

    private OnListInteractionListener mListener;
	private boolean batchMode;

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public CheckInListFragment() {
    }

	private void initializeBatchListener() {
		if (getListView() != null) {
			getListView().setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
				@Override
				public boolean onItemLongClick(AdapterView<?> adapterView, View v, int position, long id) {
					final CheckInListAdapter adapter = (CheckInListAdapter)adapterView.getAdapter();
					if (getActivity() != null) {
						batchMode = true;

						if (adapter != null) {
							adapter.initializeBatchMode(true);
							adapter.toggleInBatchSet(id);
							adapter.notifyDataSetChanged();
							return true;
						} else {
							return false;
						}
					} else {
						return false;
					}
				}
			});
		}
	}

	private void initializeListAdapter() {
		if (getListView() != null && getLoaderManager() != null) {
			getListView().setRecyclerListener((CheckInListAdapter) getListAdapter());
			getLoaderManager().restartLoader(0, null, this);
		}
	}

    @Override
    public void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

	/**
	 * Initializes the list adapter when the activity has been created.
	 * @param bundle The bundle of arguments passed to the activity.
	 */
	@Override
	public void onActivityCreated(final Bundle bundle) {
		super.onActivityCreated(bundle);

		setHasOptionsMenu(true);
		initializeListAdapter();
		initializeBatchListener();

		if (getLoaderManager() != null) {
			getLoaderManager().initLoader(0, null, this);
		}
	}


	@Override
	public void onAttach(final Activity activity) {
		super.onAttach(activity);
		try {
			mListener = (OnListInteractionListener) activity;
		} catch (ClassCastException e) {
			throw new ClassCastException(activity.toString()
					+ " must implement OnFragmentInteractionListener");
		}
	}

	/**
	 * When the view is created,add the contextual action bar.
	 * @param view The root view that has been created.
	 * @param savedInstanceState Saved state if we're resuming this activity.
	 */
	@Override
	public void onViewCreated(final View view, final Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);

		final ListView listView = getListView();
		if (listView != null) {
			listView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE_MODAL);
			listView.setMultiChoiceModeListener(this);
		}
	}

	@Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }


    @Override
    public void onListItemClick(final ListView l,
								final View v,
								final int position,
								final long id) {
        super.onListItemClick(l, v, position, id);

		if (v instanceof CheckInListItem) {
			final CheckInListItem listItem = (CheckInListItem)v;
			if (!batchMode) {
				if (null != mListener) {
					// Notify the active callbacks interface (the activity, if the
					// fragment is attached to one) that an item has been selected.
					// mListener.onFragmentInteraction(DummyContent.ITEMS.get(position).id);
					mListener.onListInteraction(listItem.getItemId());
				}
			} else {
				final CheckInListAdapter adapter = (CheckInListAdapter)getListAdapter();
				if (null != adapter) {
					adapter.toggleInBatchSet(id);
					((CheckInListItem) v).set(null, adapter.getBatchSet(), true);
					adapter.notifyDataSetChanged();
				}
			}
		}
    }

	/**
	 * Instantiate and return a new Loader for the given ID.
	 *
	 * @param id   The ID whose loader is to be created.
	 * @param args Any arguments supplied by the caller.
	 * @return Return a new Loader instance that is ready to start loading.
	 */
	@Override
	public Loader<Cursor> onCreateLoader(final int id, final Bundle args) {
		return new CheckInListLoader(getActivity());
	}

	/**
	 * Called when a previously created loader has finished its load.  Note
	 * that normally an application is <em>not</em> allowed to commit fragment
	 * transactions while in this call, since it can happen after an
	 * activity's state is saved.  See {@link android.app.FragmentManager#beginTransaction()
	 * FragmentManager.openTransaction()} for further discussion on this.
	 * <p/>
	 * <p>This function is guaranteed to be called prior to the release of
	 * the last data that was supplied for this Loader.  At this point
	 * you should remove all use of the old data (since it will be released
	 * soon), but should not do your own release of the data since its Loader
	 * owns it and will take care of that.  The Loader will take care of
	 * management of its data so you don't have to.  In particular:
	 * <p/>
	 * <ul>
	 * <li> <p>The Loader will monitor for changes to the data, and report
	 * them to you through new calls here.  You should not monitor the
	 * data yourself.  For example, if the data is a {@link android.database.Cursor}
	 * and you place it in a {@link android.widget.CursorAdapter}, use
	 * the {@link android.widget.CursorAdapter#CursorAdapter(android.content.Context,
	 * android.database.Cursor, int)} constructor <em>without</em> passing
	 * in either {@link android.widget.CursorAdapter#FLAG_AUTO_REQUERY}
	 * or {@link android.widget.CursorAdapter#FLAG_REGISTER_CONTENT_OBSERVER}
	 * (that is, use 0 for the flags argument).  This prevents the CursorAdapter
	 * from doing its own observing of the Cursor, which is not needed since
	 * when a change happens you will get a new Cursor throw another call
	 * here.
	 * <li> The Loader will release the data once it knows the application
	 * is no longer using it.  For example, if the data is
	 * a {@link android.database.Cursor} from a {@link android.content.CursorLoader},
	 * you should not call close() on it yourself.  If the Cursor is being placed in a
	 * {@link android.widget.CursorAdapter}, you should use the
	 * {@link android.widget.CursorAdapter#swapCursor(android.database.Cursor)}
	 * method so that the old Cursor is not closed.
	 * </ul>
	 *
	 * @param loader The Loader that has finished.
	 * @param data   The data generated by the Loader.
	 */
	@Override
	public void onLoadFinished(final Loader<Cursor> loader, final Cursor data) {
		if (getListAdapter() != null) {
			((CursorAdapter) getListAdapter()).changeCursor(data);
		}
	}

	/**
	 * Called when a previously created loader is being reset, and thus
	 * making its data unavailable.  The application should at this point
	 * remove any references it has to the Loader's data.
	 *
	 * @param loader The Loader that is being reset.
	 */
	@Override
	public void onLoaderReset(final Loader<Cursor> loader) {
		if (getListAdapter() != null) {
			((CursorAdapter) getListAdapter()).changeCursor(null);
		}
	}

	/**
	 * Called when action mode is first created. The menu supplied will be used to
	 * generate action buttons for the action mode.
	 *
	 * @param mode ActionMode being created
	 * @param menu Menu used to populate action buttons
	 * @return true if the action mode should be created, false if entering this
	 * mode should be aborted.
	 */
	@Override
	public boolean onCreateActionMode(final ActionMode mode, final Menu menu) {
		if (getActivity() != null) {
			final MenuInflater inflater = getActivity().getMenuInflater();
			inflater.inflate(R.menu.context_list, menu);

			//final LayoutInflater layoutInflater = getActivity().getLayoutInflater();
			//final View actionModeView = layoutInflater.inflate(R.layout.conversation_fragment_cab, null);

			return true;
		} else {
			return false;
		}
	}

	/**
    * This interface must be implemented by activities that contain this
    * fragment to allow an interaction in this fragment to be communicated
    * to the activity and potentially other fragments contained in that
    * activity.
    * <p>
    * See the Android Training lesson <a href=
    * "http://developer.android.com/training/basics/fragments/communicating.html"
    * >Communicating with Other Fragments</a> for more information.
    */
    public interface OnListInteractionListener {
        public void onListInteraction(final long id);
    }


	/**
	 * Called when an item is checked or unchecked during selection mode.
	 *
	 * @param mode     The {@link android.view.ActionMode} providing the selection mode
	 * @param position Adapter position of the item that was checked or unchecked
	 * @param id       Adapter ID of the item that was checked or unchecked
	 * @param checked  <code>true</code> if the item is now checked, <code>false</code>
	 */
	@Override
	public void onItemCheckedStateChanged(final ActionMode mode, final int position, final long id, final boolean checked) {

	}

	/**
	 * Called to refresh an action mode's action menu whenever it is invalidated.
	 *
	 * @param mode ActionMode being prepared
	 * @param menu Menu used to populate action buttons
	 * @return true if the menu or action mode was updated, false otherwise.
	 */
	@Override
	public boolean onPrepareActionMode(final ActionMode mode, final Menu menu) {
		return false;
	}

	/**
	 * Called to report a user click on an action button.
	 *
	 * @param mode The current ActionMode
	 * @param item The item that was clicked
	 * @return true if this callback handled the event, false if the standard MenuItem
	 * invocation should continue.
	 */
	@Override
	public boolean onActionItemClicked(final ActionMode mode, final MenuItem item) {
		switch (item.getItemId()) {
			case R.id.action_discard:
				deleteSelectedItems();
				// Action picked, so close the CAB
				mode.finish();
				return true;
			default:
				return false;
		}
	}

	private void deleteSelectedItems() {
		// Delete some items here.
	}

	/**
	 * Called when an action mode is about to be exited and destroyed.
	 *
	 * @param mode The current ActionMode being destroyed
	 */
	@Override
	public void onDestroyActionMode(final ActionMode mode) {
		if (getListAdapter() != null) {
			((CheckInListAdapter) getListAdapter()).initializeBatchMode(false);
		}
		batchMode  = false;
	}

}

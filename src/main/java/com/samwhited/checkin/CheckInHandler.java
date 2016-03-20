package com.samwhited.checkin;

import android.app.Activity;
import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.widget.Toast;

import java.lang.ref.WeakReference;


public final class CheckInHandler extends Handler {

	public static interface Handlable {
		void updateViews();
	}

	/**
	 * Messages which can be sent to the handler.
	 */
	public final static int SHOW_TOAST = 0;
	public final static int UPDATE_VIEW = 1;
	public final static int DISABLE_UPLOAD = 2;
	public final static int ENABLE_UPLOAD = 3;

	/**
	 * Hold a weak reference to the activity to prevent a memory leak where the activity would not
	 * be garbage collected if a long running thread was using this handler.
	 */
	private final WeakReference<Activity> mActivity;

	/**
	 * Constructor for our custom handler that takes in the outer activity.
	 *
	 * @param activity The activity to handle events for.
	 */
	public CheckInHandler(final Activity activity) {
		mActivity = new WeakReference<>(activity);
	}

	public Context getContext() {
		return mActivity.get();
	}

	/**
	 * Handle a message sent from the networking thread.
	 *
	 * @param msg The message object.
	 */
	@Override
	public void handleMessage(final Message msg) {
		if (msg == null) {
			return;
		}

		final Activity activity = mActivity.get();
		switch (msg.arg1) {
			case SHOW_TOAST:
				if (msg.obj != null) {

					final String text = msg.obj.toString();
					if (text != null && !text.isEmpty() && activity != null) {
						Toast.makeText(activity, text, Toast.LENGTH_LONG).show();
					}
				}
				break;
			case UPDATE_VIEW:
				if (activity != null) {
					try {
						 ((Handlable)activity).updateViews();
					} catch (final ClassCastException ignored) {
					}
				}
				break;
		}
	}
}
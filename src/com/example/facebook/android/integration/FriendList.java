package com.example.facebook.android.integration;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.example.facebook.android.integration.FacebookService.FacebookFriendListRequester;


public class FriendList extends Activity implements FacebookFriendListRequester {
	private ListView friendList;
	private List<BaseListElement> listElements;
	private List<BaseListElement> searchListElemnets;
	private ProgressDialog dialog;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.friend_list);
		dialog = new ProgressDialog(this);
		loadMyFriendList();
	}

	private void loadMyFriendList() {
		if (Utils.haveNetworkConnection(this)) {
			dialog.setMessage("Loading data...");
			dialog.show();
			FacebookService.instance().setContext(getApplicationContext());
			if (!Utils.isAuthenticated()) {

				FacebookService.instance().fetchFacebookProfile(this);
			} else {
				UserContext.authorized = true;
				((TextView)findViewById(R.id.my_name)).setText(UserContext.MyDisplayName);
				ImageView myimage=(ImageView)findViewById(R.id.my_pic);
					Utils.loadImageFromUrl(myimage, UserContext.MyPicUrl);
				FacebookService.instance().getFacebookFriends(this);
			}
		}

	}

	public void onStart() {
		super.onStart();
		if (!Utils.haveNetworkConnection(this)) {
			showNoConnectionDialog();
		}
	}

	private void showNoConnectionDialog() {
		AlertDialog.Builder alt_bld = new AlertDialog.Builder(this);
		alt_bld.setMessage("Error in Network Connection!").setCancelable(false)
				.setPositiveButton("OK", new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int id) {
						// Action for 'Yes' Button
						finish();
					}
				});
		AlertDialog alert = alt_bld.create();
		alert.setTitle("Error!");
		alert.setIcon(R.drawable.ic_launcher);
		alert.show();
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		// super.onActivityResult(requestCode, resultCode, data);
		super.onActivityResult(requestCode, resultCode, data);
		if (!UserContext.authorized) {
			FacebookService.instance().authorizeCallback(requestCode,
					resultCode, data);
			UserContext.authorized = true;
		}

	}

	void onFacebookProfileRetreived(boolean isSuccess) {
		// override this method
		if (isSuccess) {
			((TextView)findViewById(R.id.my_name)).setText(UserContext.MyDisplayName);
			ImageView myimage=(ImageView)findViewById(R.id.my_pic);
			Utils.loadImageFromUrl(myimage, UserContext.MyPicUrl);
			FacebookService.instance().getFacebookFriends(this);
		} else {
			dialog.dismiss();
			System.out.println("Facebook profile not is retrived");
		}
	}

	/*
	 * * This method is called when a Activty is stop disable all the events if
	 * occuring (non-Javadoc)
	 * 
	 * @see android.app.Activity#onStop()
	 */
	public void onStop() {
		super.onStop();

	}

	/*
	 * This method is called when a Activty is finished or user press the back
	 * button (non-Javadoc)
	 * 
	 * @override method of superclass
	 * 
	 * @see android.app.Activity#onDestroy()
	 */
	public void onDestroy() {
		super.onDestroy();
	}

	/*
	 * called when this activity is restart again
	 * 
	 * @override method of superclass
	 */
	public void onReStart() {
		super.onRestart();
	}

	/*
	 * called when activity is paused
	 * 
	 * @override method of superclass (non-Javadoc)
	 * 
	 * @see android.app.Activity#onPause()
	 */
	public void onPause() {
		super.onPause();
	}

	/*
	 * called when activity is resume
	 * 
	 * @override method of superclass (non-Javadoc)
	 * 
	 * @see android.app.Activity#onResume()
	 */
	public void onResume() {
		super.onResume();
	}

	private void showFriendList(ArrayList<JSONObject> friendsInfo) {
	
		friendList = (ListView) findViewById(R.id.friend_list);
		listElements = new ArrayList<BaseListElement>();
		searchListElemnets = new ArrayList<BaseListElement>();
		int size = friendsInfo.size();
		for (int i = 0; i < size; i++) {
			try {
				listElements.add(new BaseListElement(friendsInfo.get(i)
						.getString(Constants.KEYF_NAME), friendsInfo.get(i)
						.getString(Constants.KEYF_PIC_URL), friendsInfo.get(i)
						.getString(Constants.KEYF_PRESENCE), friendsInfo.get(i)
						.getString(Constants.KEYF_ID)));
			} catch (JSONException e) {

			}
		}
		friendList.setAdapter(new ActionListAdapter(this, R.id.friend_list,
				listElements));

		final EditText search = (EditText) findViewById(R.id.search);
		search.addTextChangedListener(new TextWatcher() {

			public void afterTextChanged(Editable s) {

			}

			public void beforeTextChanged(CharSequence s, int start, int count,
					int after) {

			}

			public void onTextChanged(CharSequence s, int start, int before,
					int count) {
				showSearchList(s, start, before, count, search);

			}
		});

	}

	private void showSearchList(CharSequence s, int start, int before,
			int count, EditText search) {
		int textlength = search.getText().length();
		searchListElemnets.clear();
		for (int i = 0; i < listElements.size(); i++) {
			if (textlength <= listElements.get(i).getName().length()) {
				if (search
						.getText()
						.toString()
						.equalsIgnoreCase(
								(String) listElements.get(i).getName()
										.subSequence(0, textlength))) {
					searchListElemnets.add(new BaseListElement(listElements
							.get(i).getName(), listElements.get(i).getUrl(),
							listElements.get(i).getStatus(), listElements
									.get(i).getFId()));
				}
			}
		}
		friendList.setAdapter(new ActionListAdapter(this, R.id.friend_list,
				searchListElemnets));

	}

	public void onListFetched(ArrayList<JSONObject> onlineFriends,
			ArrayList<JSONObject> idleFriends,
			ArrayList<JSONObject> offlineFriends) {
		ArrayList<JSONObject> friendsInfo = onlineFriends;
		friendsInfo.addAll(idleFriends);
		friendsInfo.addAll(offlineFriends);
		dialog.dismiss();
		showFriendList(friendsInfo);
	}

	private class ActionListAdapter extends ArrayAdapter<BaseListElement> {
		private List<BaseListElement> listElementAdapter;
		Context context;

		public ActionListAdapter(Context context, int resourceId,
				List<BaseListElement> listElements) {
			super(context, resourceId, listElements);
			this.context = context;
			this.listElementAdapter = listElements;
			for (int i = 0; i < listElements.size(); i++) {
				listElements.get(i).setAdapter(this);
			}
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			View view = convertView;
			if (view == null) {
				LayoutInflater inflater = (LayoutInflater) context
						.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
				view = inflater.inflate(R.layout.list_item, null);
			}

			BaseListElement listElement = listElementAdapter.get(position);
			if (listElement != null) {

				ImageView picIcon = (ImageView) view
						.findViewById(R.id.profile_pic);
				TextView friendName = (TextView) view
						.findViewById(R.id.friend_name);
				ImageView statusIcon = (ImageView) view
						.findViewById(R.id.status_icon);

				if (picIcon != null) {
					Utils.loadImageFromUrl(picIcon, listElement.getUrl());
				}

				if (friendName != null) {
					friendName.setText(listElement.getName());
				}
				if (statusIcon != null) {
					if (listElement.getStatus().equals(Constants.KEYF_ACTIVE)) {
						statusIcon.setImageResource(R.drawable.online);
					} else if (listElement.getStatus().equals(
							Constants.KEYF_IDLE)) {
						statusIcon.setImageResource(R.drawable.idle);
					} else {
						statusIcon.setImageResource(R.drawable.offline);
					}
				}

			}
			return view;
		}

	}

	/*
	 * used to create menu
	 */
	private void CreateMenu(Menu menu) {
		menu.setQwertyMode(true);
		menu.add(0, 0, 0, "Refresh").setIcon(R.drawable.refresh);

	}

	/*
	 * used to handle selection of option menu
	 */
	private boolean MenuChoice(MenuItem item) {
		switch (item.getItemId()) {
		case 0:
			loadMyFriendList();
			return true;

		}
		return false;

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.app.Activity#onCreateOptionsMenu(android.view.Menu)
	 */
	// ---only created once---
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);
		CreateMenu(menu);
		return true;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.app.Activity#onOptionsItemSelected(android.view.MenuItem)
	 */
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		return MenuChoice(item);
	}

	@Override
	public void onFbError() {
		System.out.println("Error is in getting friends");
		dialog.dismiss();

	}

}

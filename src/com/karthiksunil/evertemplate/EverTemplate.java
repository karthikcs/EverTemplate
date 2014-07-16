/*
 * Copyright 2012 Evernote Corporation
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification,
 * are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice, this
 *    list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.
 * IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT,
 * INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING,
 * BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE
 * OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF
 * ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package com.karthiksunil.evertemplate;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import android.annotation.SuppressLint;
import android.app.ActionBar;
import android.app.ActionBar.Tab;
import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.text.Html;
import android.util.Log;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import com.evernote.client.android.EvernoteSession;
import com.evernote.client.android.InvalidAuthenticationException;
import com.evernote.client.android.OnClientCallback;
import com.evernote.thrift.transport.TTransportException;
import com.karthiksunil.evertemplate.R;

/**
 * This simple Android app demonstrates how to integrate with the Evernote API
 * (aka EDAM).
 * <p/>
 * In this sample, the user authorizes access to their account using OAuth
 * <p/>
 * class created by @tylersmithnet
 */

public class EverTemplate extends ParentActivity {
	
	

	// Name of this application, for logging
	private static final String LOGTAG = "EverTemplate";
	public static final String ACTION_VIEW_NOTE = "com.evernote.action.VIEW_NOTE";
	public static final String EXTRA_NOTE_GUID = "NOTE_GUID";
	public static int everUserId;

	// UI elements that we update
	// private Button mLoginButton;
	private Button mLogoutButton;
	private ListView mListView;
	public ArrayAdapter mAdapter;
	ArrayList<String> temp_titles;
	ArrayList<String> temp_guid;
	Dialog progress;

	String noteContent;
	List<String> tagNames;



	// Listener to act on clicks
	private AdapterView.OnItemClickListener mItemClickListener = new AdapterView.OnItemClickListener() {
		
		public static final String ACTION_NEW_NOTE             = "com.evernote.action.CREATE_NEW_NOTE";
		@Override
		public void onItemClick(AdapterView<?> parent, View view, int position,
				long id) {

			Toast.makeText(getApplicationContext(), temp_titles.get(position),
					Toast.LENGTH_SHORT).show();
			createNoteAndDisplay(temp_guid.get(position),
					temp_titles.get(position)); // This method will
			// create note in
			// evernote server
			// and opens the
			// same

		}

		String gNoteGuid;

		private void createNoteAndDisplay(String noteGuid, String noteTitle) {
			// TODO Auto-generated method stub
			DatabaseConnector dbConnector1 = new DatabaseConnector(
					EverTemplate.this);
			dbConnector1.open();
			Cursor result2 = dbConnector1.getUserNoteContent(
					EverTemplate.everUserId, noteGuid);
			// Cursor result2 = dbConnector1.getUserNotes(everUserId);
			result2.moveToFirst();
			int NoteIndex = result2.getColumnIndex("note_content");
			String noteContent = result2.getString(NoteIndex);
			result2.close();
			dbConnector1.close();

			// Any code to manipulate the content
			
			String noteManipulatedContent = ManipulateContent(noteContent);

			// Trigger Evernote Intent
			Intent intent = new Intent();
			intent.setAction(ACTION_NEW_NOTE);
			intent.putExtra(Intent.EXTRA_TITLE, noteTitle);
		    intent.putExtra(Intent.EXTRA_TEXT, Html.fromHtml(noteManipulatedContent));

		    try {
		      startActivity(intent);
		    } catch (android.content.ActivityNotFoundException ex) {
		      Toast.makeText(EverTemplate.this, R.string.err_creating_note, Toast.LENGTH_SHORT).show();
		    } 


			/* Creating note on server directly 
			 * Note note = new Note(); note.setTitle(noteTitle);
			 * note.setContent(noteContent);
			 * 
			 * try { progress = ProgressDialog.show(EverTemplate.this,
			 * "Creating Note", "Please wait...");
			 * mEvernoteSession.getClientFactory().createNoteStoreClient()
			 * .createNote(note, new OnClientCallback<Note>() {
			 * 
			 * @Override public void onSuccess(Note data) { gNoteGuid =
			 * data.getGuid(); Toast.makeText(getApplicationContext(),
			 * R.string.note_saved, Toast.LENGTH_LONG) .show();
			 * progress.dismiss();
			 * 
			 * Intent LaunchIntent = getPackageManager()
			 * .getLaunchIntentForPackage( "com.evernote");
			 * startActivity(LaunchIntent);
			 * 
			 * Handler handler = new Handler(); handler.postDelayed(new
			 * Runnable() { public void run() { Intent it = new
			 * Intent("intent.my.action"); it.setComponent(new
			 * ComponentName("com.karthiksunil.evertemplate",
			 * EverTemplate.class.getName()));
			 * it.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK); startActivity(it);
			 * 
			 * 
			 * Intent intent = new Intent(); intent.setAction(ACTION_VIEW_NOTE);
			 * String lNoteGuid = gNoteGuid; intent.putExtra(EXTRA_NOTE_GUID,
			 * lNoteGuid); try { startActivity(intent); } catch
			 * (android.content.ActivityNotFoundException ex) { //
			 * Toast.makeText(this, // R.string.err_activity_not_found, //
			 * Toast.LENGTH_SHORT).show(); }
			 * 
			 * } }, 10000);
			 * 
			 * }
			 * 
			 * @Override public void onException(Exception exception) {
			 * Log.e(LOGTAG, "Error saving note", exception);
			 * Toast.makeText(getApplicationContext(),
			 * R.string.error_saving_note, Toast.LENGTH_LONG).show();
			 * progress.dismiss(); } });
			 * 
			 * } catch (TTransportException exception) { Log.e(LOGTAG,
			 * "Error creating notestore", exception);
			 * Toast.makeText(getApplicationContext(),
			 * R.string.error_creating_notestore, Toast.LENGTH_LONG) .show();
			 * progress.dismiss(); }
			 */

		}

		private String ManipulateContent(String noteContent) {
			// TODO Auto-generated method stub
			String outString = null;
			// Strip header and footer for noteContent
			outString = noteContent;
			Date date = new Date();
			
			SimpleDateFormat ft = null;
			SimpleDateFormat ft1 = null;
			SimpleDateFormat ftDay = null;
			SimpleDateFormat ftMonth = null;
			SimpleDateFormat ftYear = null;
			// Get date format 
			DatabaseConnector dbConnector1 = new DatabaseConnector(
					EverTemplate.this);
			dbConnector1.open();
			Cursor cursor = dbConnector1.getPref(everUserId, "date_format");
			Cursor cursor1 = dbConnector1.getPref(everUserId, "time_format");
			int index = cursor.getColumnIndex("pref_value");
			
			String dateFormat = null;
			String timeFormat = null;
			if(cursor.getCount() > 0 ){
				cursor.moveToFirst();
				dateFormat = cursor.getString(index);
			}
			if(cursor1.getCount() > 0 ){
				cursor1.moveToFirst();
				timeFormat = cursor1.getString(index);
			}
			dbConnector1.close();
			
			if (dateFormat.equals("03/17/2013")) {
				ft = new SimpleDateFormat ("dd/MM/yyyy");
			} else if(dateFormat.equals("Wednesday, March 17, 2013")){
				ft = new SimpleDateFormat ("E, M d, yyyy");
			} else if(dateFormat.equals("03/17") ){
				ft = new SimpleDateFormat ("MM/dd");
			} else if(dateFormat.equals("17-Mar")){
				ft = new SimpleDateFormat ("dd-MMM");
			} else if(dateFormat.equals("17-Mar-2013")  ){
				ft = new SimpleDateFormat ("dd-MMM-yyyy");
			} else if(dateFormat.equals("17-03-2013") ){
				ft = new SimpleDateFormat ("dd-MM-yyyy");
			} else{
				ft = new SimpleDateFormat ("dd/MM/yyyy");
			}
			
			if (timeFormat.equals("12 hrs")) {
				ft1 = new SimpleDateFormat ("hh:mm a");
			} else if(timeFormat.equals("24 hrs")){
				ft1 = new SimpleDateFormat ("HH:mm");
			} else{
				ft1 = new SimpleDateFormat ("hh:mm a");
			}
			
			ftDay = new SimpleDateFormat("E");
			ftMonth = new SimpleDateFormat("MMM");
			ftYear = new SimpleDateFormat("yyyy");
			
			
			String formattedDate = ft.format(date);
			String formattedTime = ft1.format(date);
			String formattedDay = ftDay.format(date);
			String formattedMonth = ftMonth.format(date);
			String formattedYear = ftYear.format(date);
			
			Calendar calendar = Calendar.getInstance();
			calendar.add(Calendar.DATE, -1);
			Date yesterday = calendar.getTime();			
			String formattedYDay = ft.format(yesterday);
			
			Calendar calendar1 = Calendar.getInstance();
			calendar1.add(Calendar.DATE, 1);
			Date tomorrow = calendar1.getTime();			
			String formattedTomorrow = ft.format(tomorrow);
			
			outString = outString.replaceAll("&lt;TODAY&gt;", formattedDate);
			outString = outString.replaceAll("&lt;YESTERDAY&gt;", formattedYDay);
			outString = outString.replaceAll("&lt;TOMORROW&gt;", formattedTomorrow);
			outString = outString.replaceAll("&lt;TIME&gt;", formattedTime);
			outString = outString.replaceAll("&lt;DAY&gt;", formattedDay);
			outString = outString.replaceAll("&lt;MONTH&gt;", formattedMonth);
			outString = outString.replaceAll("&lt;YEAR&gt;", formattedYear);
			
			//outString.replace("&lt;TODAY&gt;", formattedDate);
			//StringBuilder sb = new StringBuilder(outString);
			
			//sb.replace(outString.indexOf("&lt;TODAY&gt;"), (outString.indexOf("&lt;TODAY&gt;") + 13) , formattedDate);
			//sb.replace(outString.indexOf("&lt;YESTERDAY&gt;"), (outString.indexOf("&lt;YESTERDAY&gt;") + 17) , formattedYDay);
			//sb.replace(outString.indexOf("&lt;TOMORROW&gt;"), (outString.indexOf("&lt;TOMORROW&gt;") + 17) , formattedTomorrow);
			//outString = sb.toString();
			
			 
			
			return outString;
		}
	};

	/**
	 * Called when the activity is first created.
	 */

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		/*
		 * final boolean customTitle =
		 * requestWindowFeature(Window.FEATURE_CUSTOM_TITLE);
		 */

		setContentView(R.layout.main);



		// mLoginButton = (Button) findViewById(R.id.login);
		// mLogoutButton = (Button) findViewById(R.id.ibLogout);
		// mLogoutButton = (Button) findViewById(R.id.logout);

		mListView = (ListView) findViewById(R.id.list);
		temp_titles = new ArrayList<String>();
		temp_guid = new ArrayList<String>();

		/*
		 * if (customTitle) {
		 * getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE,
		 * R.layout.title_bar); }
		 */

		mAdapter = new ArrayAdapter<String>(this,
				android.R.layout.simple_list_item_1, android.R.id.text1,
				temp_titles);

		mListView.setAdapter(mAdapter);
		mListView.setOnItemClickListener(mItemClickListener);
		
		mListView.setLongClickable(true);
		registerForContextMenu(mListView);
		
		

		if (!mEvernoteSession.isLoggedIn()) {
			mEvernoteSession.authenticate(this);
		} else {
			checkDefaultTemplate();
		}

	}

	@Override
	public void onResume() {
		super.onResume();
		updateAuthUi();
		if (mEvernoteSession.isLoggedIn()) {
			checkDefaultTemplate();
		}

	}
	
	//Show context menu
	public void onCreateContextMenu(ContextMenu menu, View v,
            ContextMenuInfo menuInfo) {
				super.onCreateContextMenu(menu, v, menuInfo);
				MenuInflater inflater = getMenuInflater();
				inflater.inflate(R.menu.ctxmenulocal, menu);
	}
	
	DatabaseConnector dbConnector = new DatabaseConnector(EverTemplate.this);
	// ON Context Menu item selected
	public boolean onContextItemSelected(MenuItem item) {
		  final AdapterContextMenuInfo info = (AdapterContextMenuInfo) item.getMenuInfo();
		  switch (item.getItemId()) {
		  case R.id.edit :
			  String msg = temp_titles.get(info.position) + " Edit";
			  
			  Toast.makeText(EverTemplate.this, msg, 
					  Toast.LENGTH_LONG).show();
			  
			  //
			  Intent intent = new Intent();
			  intent.setAction("com.evernote.action.VIEW_NOTE");
			  intent.putExtra("NOTE_GUID", temp_guid.get(info.position));

			  try {
			      startActivity(intent);
			  } catch (android.content.ActivityNotFoundException ex) {
			      Toast.makeText(EverTemplate.this, R.string.err_creating_note, Toast.LENGTH_SHORT).show();
			  } 
			  return true;
			  
		  case R.id.delete :
			  try {
				mEvernoteSession.getClientFactory().createNoteStoreClient().
				  	deleteNote(temp_guid.get(info.position), new OnClientCallback<Integer>() {
						
						@Override
						public void onSuccess(Integer data) {
							// TODO Auto-generated method stub
							dbConnector.open();
							dbConnector.deleteSingleNote(everUserId, temp_guid.get(info.position));
							temp_guid.remove(info.position);
							temp_titles.remove(info.position);
							
							Toast.makeText(EverTemplate.this, "Template Deleted", 
									  Toast.LENGTH_LONG).show();
							startActivity(new Intent(getApplicationContext(), EverTemplate.class));
						}
						
						@Override
						public void onException(Exception exception) {
							// TODO Auto-generated method stub
							
						}
					});
			} catch (TTransportException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			  
			  
		  }
		  
		  return true;

	}


	/**
	 * Update the UI based on Evernote authentication state.
	 */
	private void updateAuthUi() {
		// show login button if logged out
		// mLoginButton.setEnabled(!mEvernoteSession.isLoggedIn());

		// Show logout button if logged in
		// mLogoutButton.setEnabled(mEvernoteSession.isLoggedIn());

		// disable clickable elements until logged in

		mListView.setEnabled(mEvernoteSession.isLoggedIn());

	}

	/**
	 * Called when the user taps the "Log in to Evernote" button. Initiates the
	 * Evernote OAuth process
	 */

	public void login(View view) {
		mEvernoteSession.authenticate(this);
	}

	/**
	 * Called when the user taps the "Log in to Evernote" button. Clears
	 * Evernote Session and logs out
	 */
	public void logout(View view) {
		try {
			mEvernoteSession.logOut(this);
		} catch (InvalidAuthenticationException e) {
			Log.e(LOGTAG, "Tried to call logout with not logged in", e);
		}
		// Remove all the templates from the screen
		temp_titles.removeAll(temp_titles);
		temp_guid.removeAll(temp_guid);
		mAdapter.notifyDataSetChanged();
		updateAuthUi();
		Toast.makeText(getApplicationContext(), "Logout Successful",
				Toast.LENGTH_SHORT).show();
		// Exit application --> Go home
		Intent intent = new Intent(Intent.ACTION_MAIN);
		intent.addCategory(Intent.CATEGORY_HOME);
		intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		startActivity(intent);

	}

	public void checkDefaultTemplate() {
		everUserId = mEvernoteSession.getAuthenticationResult().getUserId();
		DatabaseConnector dbConnector = new DatabaseConnector(EverTemplate.this);

		dbConnector.open();
		Cursor result = dbConnector.getOneTemplate(everUserId);
		int count = result.getCount();
		result.close();

		if (count != 0) {
			// DatabaseConnector dbConnector1 = new DatabaseConnector(
			// EverTemplate.this);
			// dbConnector1.open();
			// Cursor result1 = dbConnector1.getUserNotes(everUserId);
			// dbConnector1.close();
			temp_titles.clear();
			temp_guid.clear();
			mAdapter.notifyDataSetChanged();
			Cursor result1 = dbConnector.getUserNotes(everUserId);
			int count1 = result1.getCount();
			if (count1 != 0) {
				result1.moveToFirst();
				for (int index = 0; index < count1; index++) {
					int NoteIndex = result1.getColumnIndex("note_title");
					int noteGuidIndex = result1.getColumnIndex("note_guid");
					String noteTitle = result1.getString(NoteIndex);
					String noteGuid = result1.getString(noteGuidIndex);
					temp_titles.add(noteTitle);
					temp_guid.add(noteGuid);
					result1.moveToNext();
				}

				mAdapter.notifyDataSetChanged();
			}
			result1.close();
			/*
			 * NoteFilter filter = new NoteFilter();
			 * filter.setNotebookGuid(NotebookGuid); try { mEvernoteSession
			 * .getClientFactory() .createNoteStoreClient() .findNotes(filter,
			 * 0, 100, new OnClientCallback<NoteList>() {
			 * 
			 * @Override public void onSuccess( final NoteList noteList) {
			 * List<Note> notes = noteList.getNotes();
			 * 
			 * String noteName; // CharSequence[] names = new //
			 * CharSequence[notebooks.size()]; // int selected = -1; Note note =
			 * null; for (int index = 0; index < notes .size(); index++) {
			 * 
			 * note = notes.get(index); noteName = note.getTitle(); int len =
			 * note.getContentLength(); try { mEvernoteSession
			 * .getClientFactory() .createNoteStoreClient() .getNoteContent(
			 * note.getGuid(), new OnClientCallback<String>() {
			 * 
			 * @Override public void onSuccess( String data) { // TODO //
			 * Auto-generated // method // stub noteContent = data;
			 * 
			 * 
			 * }
			 * 
			 * @Override public void onException( Exception exception) { // TODO
			 * // Auto-generated // method // stub
			 * 
			 * }
			 * 
			 * }); mEvernoteSession .getClientFactory()
			 * .createNoteStoreClient().getNoteTagNames(note.getGuid(), new
			 * OnClientCallback<List<String>>(){
			 * 
			 * @Override public void onSuccess( List<String> data) { // TODO
			 * Auto-generated method stub tagNames = data;
			 * 
			 * }
			 * 
			 * @Override public void onException( Exception exception) { // TODO
			 * Auto-generated method stub
			 * 
			 * }
			 * 
			 * } ); } catch (TTransportException e) { // TODO Auto-generated
			 * catch block e.printStackTrace(); }
			 * 
			 * 
			 * temp_titles.add(noteName);
			 * 
			 * } mAdapter.notifyDataSetChanged();
			 * 
			 * }
			 * 
			 * @Override public void onException(Exception exception) {
			 * Log.e(LOGTAG, "Error listing notebooks", exception);
			 * Toast.makeText( getApplicationContext(),
			 * R.string.error_listing_notebooks, Toast.LENGTH_LONG).show();
			 * removeDialog(DIALOG_PROGRESS); }
			 * 
			 * }); } catch (TTransportException e) { // TODO Auto-generated
			 * catch block e.printStackTrace(); }
			 */
		} else {
			Toast.makeText(getApplicationContext(),
					"Please select default Template Notebook",
					Toast.LENGTH_SHORT).show();
		}
		dbConnector.close();
	}

	/**
	 * Called when the control returns from an activity that we launched.
	 */
	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		switch (requestCode) {
		// Update UI when oauth activity returns result
		case EvernoteSession.REQUEST_CODE_OAUTH:
			if (resultCode == Activity.RESULT_OK) {
				updateAuthUi();
				checkDefaultTemplate();

			}
			break;
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.menu, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		
		case R.id.sharedtemplate :
			startActivity(new Intent(getApplicationContext(), SharedTemplates.class));
			return true;
		
		case R.id.refresh :
			// Yet to implement
			
			return true;
			
		case R.id.options:
			startActivity(new Intent(getApplicationContext(), Options.class));
			return true;
		case R.id.logoff:
			try {
				DatabaseConnector dbConnector = new DatabaseConnector(
						EverTemplate.this);
				dbConnector.open();
				dbConnector.deleteNotes(everUserId);
				dbConnector.close();
				mEvernoteSession.logOut(this);
			} catch (InvalidAuthenticationException e) {
				Log.e(LOGTAG, "Tried to call logout with not logged in", e);
			}
			finish();

			return true;
		}
		return false;
	}

}

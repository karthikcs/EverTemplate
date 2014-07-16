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

import android.R.string;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.database.Cursor;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.evernote.client.android.EvernoteUtil;
import com.evernote.client.android.OnClientCallback;
import com.evernote.edam.error.EDAMNotFoundException;
import com.evernote.edam.error.EDAMSystemException;
import com.evernote.edam.error.EDAMUserException;
import com.evernote.edam.notestore.NoteCollectionCounts;
import com.evernote.edam.notestore.NoteFilter;
import com.evernote.edam.notestore.NoteList;
import com.evernote.edam.notestore.SyncChunk;
import com.evernote.edam.type.LinkedNotebook;
import com.evernote.edam.type.Note;
import com.evernote.edam.type.Notebook;
import com.evernote.edam.type.User;
import com.evernote.edam.userstore.PublicUserInfo;
import com.evernote.edam.userstore.UserStore;
import com.evernote.thrift.TException;
import com.evernote.thrift.transport.TTransportException;
import com.karthiksunil.evertemplate.R;

import java.util.Iterator;
import java.util.List;

/**
 * * <p/>
 * class created by @tylersmithnet
 */
public class Options extends ParentActivity {

	/**
	 * *************************************************************************
	 * The following values and code are simply part of the demo application. *
	 * *************************************************************************
	 */

	private static final String LOGTAG = "SimpleNote";

	private EditText mEditTextTitle;
	private EditText mEditTextContent;
	private Button mBtnSave;
	private Button mBtnSelect;
	private TextView tvSel;
	private int everUserId;

	private String mSelectedNotebookGuid;
	private String mSelectedNotebookName;
	boolean notesFetching = false;
	ProgressDialog progress;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.options);

		mBtnSelect = (Button) findViewById(R.id.select_button);
		tvSel = (TextView) findViewById(R.id.tvSelected);

		Spinner spinner = (Spinner) findViewById(R.id.date_format);
		ArrayAdapter<CharSequence> adapter = ArrayAdapter
				.createFromResource(this, R.array.date_format,
						android.R.layout.simple_spinner_item);
		
		Spinner spinnerTime = (Spinner) findViewById(R.id.time_format);
		ArrayAdapter<CharSequence> adapter1 = ArrayAdapter
				.createFromResource(this, R.array.time_format,
						android.R.layout.simple_spinner_item);
		
		everUserId = mEvernoteSession.getAuthenticationResult().getUserId();
		

		class SpinnerActivity extends Activity implements OnItemSelectedListener {

			@Override
			public void onItemSelected(AdapterView<?> parent, View view,
					int pos, long id) {
				// TODO Auto-generated method stub
				String str = parent.getItemAtPosition(pos).toString();
				DatabaseConnector dbConnector = new DatabaseConnector(
						Options.this);
				dbConnector.open();
				
				dbConnector.updatePrefs(everUserId, "date_format", str);
				dbConnector.close();
			}

			@Override
			public void onNothingSelected(AdapterView<?> arg0) {
				// TODO Auto-generated method stub
				
			}

	
		}
		
		class SpinnerTimeActivity extends Activity implements OnItemSelectedListener {

			@Override
			public void onItemSelected(AdapterView<?> parent, View view,
					int pos, long id) {
				// TODO Auto-generated method stub
				String str = parent.getItemAtPosition(pos).toString();
				DatabaseConnector dbConnector = new DatabaseConnector(
						Options.this);
				dbConnector.open();
				
				dbConnector.updatePrefs(everUserId, "time_format", str);
				dbConnector.close();
			}

			@Override
			public void onNothingSelected(AdapterView<?> arg0) {
				// TODO Auto-generated method stub
				
			}

	
		}
		
	
		// Specify the layout to use when the list of choices appears
		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		// Apply the adapter to the spinner
		spinner.setAdapter(adapter);
		SpinnerActivity spinnerAction = new SpinnerActivity();
		spinner.setOnItemSelectedListener(spinnerAction);
		
		// Specify the layout to use when the list of choices appears
		adapter1.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		// Apply the adapter to the spinner
		spinnerTime.setAdapter(adapter1);
		SpinnerTimeActivity spinnerAction1 = new SpinnerTimeActivity();
		spinnerTime.setOnItemSelectedListener(spinnerAction1);

		// Get date format if user has already chosen
		DatabaseConnector dbConnector1 = new DatabaseConnector(
				Options.this);
		dbConnector1.open();
		Cursor cursor = dbConnector1.getPref(everUserId, "date_format");
		
		int index = cursor.getColumnIndex("pref_value");
		
		String dateFormat = null;
		String timeFormat = null;
		if(cursor.getCount() > 0 ){
			cursor.moveToFirst();
			dateFormat = cursor.getString(index);
		}else{
			dbConnector1.insertPrefs(everUserId, "date_format", "03/17/2013");
			dateFormat = "03/17/2013";
		}
		cursor.close();
		dbConnector1.close();
		
		DatabaseConnector dbConnector2 = new DatabaseConnector(
				Options.this);
		dbConnector2.open();
		Cursor cursor1 = dbConnector2.getPref(everUserId, "time_format");
		if(cursor1.getCount() > 0 ){
			cursor1.moveToFirst();
			timeFormat = cursor1.getString(index);
		}else{
			dbConnector2.insertPrefs(everUserId, "time_format", "12 hrs");
			timeFormat = "12 hrs";
		}
		cursor1.close();
		dbConnector2.close();
		
		
		if (dateFormat.equals("03/17/2013")) {
			spinner.setSelection(0);
		} else if(dateFormat.equals("Wednesday, March 17, 2013")){
			spinner.setSelection(1);
		} else if(dateFormat.equals("03/17") ){
			spinner.setSelection(2);
		} else if(dateFormat.equals("17-Mar")){
			spinner.setSelection(3);
		} else if(dateFormat.equals("17-Mar-2013")  ){
			spinner.setSelection(4);
		} else if(dateFormat.equals("17-03-2013") ){
			spinner.setSelection(5);
		} else{
			spinner.setSelection(0);
		}
		
		if (timeFormat.equals("12 hrs")) {
			spinnerTime.setSelection(0);
		} else if(timeFormat.equals("24 hrs")){
			spinnerTime.setSelection(1);
		} else{
			spinnerTime.setSelection(0);
		}
				
		
		DatabaseConnector dbConnector = new DatabaseConnector(Options.this);
		dbConnector.open();
		Cursor result = dbConnector.getOneTemplate(everUserId);
		int count = result.getCount();
		dbConnector.close();
		if (count != 0) {
			result.moveToFirst();
			int templateColumnIndex = result.getColumnIndex("template_name");
			String templatename = result.getString(templateColumnIndex);
			tvSel.setText(templatename);
		}

	}

	@Override
	public void onBackPressed() {
		// Do Here what ever you want do on back press;
		if (notesFetching) {

		} else {
			super.onBackPressed();
		}
	}

	@Override
	public void onResume() {
		super.onResume();
		/*
		 * everUserId = mEvernoteSession.getAuthenticationResult().getUserId();
		 * DatabaseConnector dbConnector = new DatabaseConnector(Options.this);
		 * dbConnector.open(); Cursor result =
		 * dbConnector.getOneTemplate(everUserId); int count =
		 * result.getCount(); dbConnector.close(); if (count != 0) {
		 * result.moveToFirst(); int templateColumnIndex =
		 * result.getColumnIndex("template_name"); String templatename =
		 * result.getString(templateColumnIndex); tvSel.setText(templatename); }
		 */
	}

	/**
	 * Saves text field content as note to selected notebook, or default
	 * notebook if no notebook select
	 */
	public void saveNote(View view) {
		String title = mEditTextTitle.getText().toString();
		String content = mEditTextContent.getText().toString();
		if (TextUtils.isEmpty(title) || TextUtils.isEmpty(content)) {
			Toast.makeText(getApplicationContext(),
					R.string.empty_content_error, Toast.LENGTH_LONG).show();
		}

		Note note = new Note();
		note.setTitle(title);

		// TODO: line breaks need to be converted to render in ENML
		note.setContent(EvernoteUtil.NOTE_PREFIX + content
				+ EvernoteUtil.NOTE_SUFFIX);

		// If User has selected a notebook guid, assign it now
		if (!TextUtils.isEmpty(mSelectedNotebookGuid)) {
			note.setNotebookGuid(mSelectedNotebookGuid);
		}
		showDialog(DIALOG_PROGRESS);
		try {
			mEvernoteSession.getClientFactory().createNoteStoreClient()
					.createNote(note, new OnClientCallback<Note>() {
						@Override
						public void onSuccess(Note data) {
							Toast.makeText(getApplicationContext(),
									R.string.note_saved, Toast.LENGTH_LONG)
									.show();
							removeDialog(DIALOG_PROGRESS);
						}

						@Override
						public void onException(Exception exception) {
							Log.e(LOGTAG, "Error saving note", exception);
							Toast.makeText(getApplicationContext(),
									R.string.error_saving_note,
									Toast.LENGTH_LONG).show();
							removeDialog(DIALOG_PROGRESS);
						}
					});
		} catch (TTransportException exception) {
			Log.e(LOGTAG, "Error creating notestore", exception);
			Toast.makeText(getApplicationContext(),
					R.string.error_creating_notestore, Toast.LENGTH_LONG)
					.show();
			removeDialog(DIALOG_PROGRESS);
		}

	}

	// Called when Refresh Button is clicked
	public  void refreshNotes(View view) {
	
		DatabaseConnector dbConnector = new DatabaseConnector(Options.this);
		dbConnector.open();
		Cursor cursor = dbConnector.getOneTemplate(everUserId);
		
		if (cursor.getCount() > 0 ){
			cursor.moveToFirst();
			int index = cursor.getColumnIndex("template_guid");
			String noteBookGuid = cursor.getString(index);
			getNotes(noteBookGuid);
		}
		dbConnector.close();
		
	}

	/**
	 * Select notebook, create AlertDialog to pick notebook guid
	 */
	int mSelectedPos = -1;

	public void selectNotebook(View view) {
		
		if (!isNetworkAvailable() ){
			Toast.makeText(getApplicationContext(), 
					R.string.check_internet, Toast.LENGTH_LONG).show();
			return;
		}
		
		try {
	    	progress = ProgressDialog.show(Options.this, getString(R.string.fetching_notebooks),
	    			getString(R.string.please_wait));
	    	mSelectedPos = -1;
	        mEvernoteSession.getClientFactory().createNoteStoreClient().listNotebooks(new OnClientCallback<List<Notebook>>() {
	          

	          @Override
	          public void onSuccess(final List<Notebook> notebooks) {
	            CharSequence[] names = new CharSequence[notebooks.size()];
	            int selected = -1;
	            Notebook notebook = null;
	            progress.dismiss();
	            
	            for (int index = 0; index < notebooks.size(); index++) {
	              notebook = notebooks.get(index);
	              names[index] = notebook.getName();
	              if (notebook.getGuid().equals(mSelectedNotebookGuid)) {
	                selected = index;
	              }
	            }

	            AlertDialog.Builder builder = new AlertDialog.Builder(Options.this);

	            builder
	                .setSingleChoiceItems(names, selected, new DialogInterface.OnClickListener() {
	                  @Override
	                  public void onClick(DialogInterface dialog, int which) {
	                    mSelectedPos = which;
	                  }
	                })
	                .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
	                  @Override
	                  public void onClick(DialogInterface dialog, int which) {
	                	  //mSelectedPos = which;
	                    if (mSelectedPos > -1) {
	                      mSelectedNotebookGuid = notebooks.get(mSelectedPos).getGuid();
	                      mSelectedNotebookName = notebooks.get(mSelectedPos).getName();
	                      DatabaseConnector dbConnector = new DatabaseConnector(Options.this);
	                      dbConnector.open();
	                      Cursor result = dbConnector.getOneTemplate(EverTemplate.everUserId);
	                      int count = result.getCount();
	                      if (count == 0) {
								dbConnector.insertTemplate(
												EverTemplate.everUserId,
												mSelectedNotebookGuid,
												mSelectedNotebookName);
							} else {
								dbConnector.updateTemplate(
												EverTemplate.everUserId,
												mSelectedNotebookGuid,
												mSelectedNotebookName);
							}
	                      dbConnector.close();
	                      tvSel.setText(mSelectedNotebookName);
	                      dialog.dismiss();
	                      getNotes(mSelectedNotebookGuid);
	                      

	                    }
	                    //while (notesFetching);
	                   
	                  }
	                })
	                .create()
	                .show();
	            
	          }

	          @Override
	          public void onException(Exception exception) {
	            Log.e(LOGTAG, "Error listing notebooks", exception);
	            Toast.makeText(getApplicationContext(), R.string.error_listing_notebooks, Toast.LENGTH_LONG).show();
	            removeDialog(DIALOG_PROGRESS);
	            progress.dismiss();
	          }
	        });
	      } catch (TTransportException exception) {
	        Log.e(LOGTAG, "Error creating notestore", exception);
	        Toast.makeText(getApplicationContext(), R.string.error_creating_notestore, Toast.LENGTH_LONG).show();
	        removeDialog(DIALOG_PROGRESS);
	      }
	    
	}
	
	
	String noteContent;

	String gNotebookGuid;
	Note note = null;
	Note noteData = null;
	List<Note> notes;
	List<String> tagNames;
	int dataIndex = 0;

	DatabaseConnector dbConnector = new DatabaseConnector(Options.this);
	
	
	public void getNotes(String NotebookGuid) {
		// TODO Auto-generated method stub
		//Cursor result;
		gNotebookGuid = NotebookGuid;
		// result.
		NoteFilter filter = new NoteFilter();

		// Trying to shwo progress indicator

		progress = ProgressDialog.show(Options.this, getString(R.string.fetching_notes),
				getString(R.string.please_wait));
		notesFetching = true;
		filter.setNotebookGuid(NotebookGuid);
		try {
			mEvernoteSession
					.getClientFactory()
					.createNoteStoreClient()
					.findNotes(filter, 0, 100,
							new OnClientCallback<NoteList>() {
								@Override
								public void onSuccess(final NoteList noteList) {

									dbConnector.open();
									notes = noteList.getNotes();
									//progress.dismiss();

									dbConnector.deleteNotes(everUserId);

									for (int index = 0; index < notes.size(); index++) {

										note = notes.get(index);
										try {
											mEvernoteSession
													.getClientFactory()
													.createNoteStoreClient()
													.getNoteContent(
															note.getGuid(),
															new OnClientCallback<String>() {

																@Override
																public void onSuccess(
																		String data) {
																	// TODO
																	// Auto-generated
																	// method
																	// stub /// Karthik need to work on the issue
																	progress.setMessage(getString(R.string.fetching) + " "+ dataIndex 
																			+ " " + getString(R.string.of) + " " + notes.size() );
																	noteData = notes
																			.get(dataIndex);
																	noteContent = data;
																	dbConnector
																			.insertNote(
																					everUserId,
																					noteData.getGuid(),
																					gNotebookGuid,
																					noteData.getTitle(),
																					noteContent);
																	dataIndex = dataIndex + 1;
																	if (dataIndex == notes
																			.size()) {
																		progress.dismiss();
																		Toast.makeText(
																				getApplicationContext(),
																				getString(R.string.notes_refreshed),
																				Toast.LENGTH_LONG)
																				.show();
																		notesFetching = false;
																	}

																}

																@Override
																public void onException(
																		Exception exception) {
																	// TODO
																	// Auto-generated
																	// method
																	// stub
																	progress.dismiss();
																}

															});
											mEvernoteSession
													.getClientFactory()
													.createNoteStoreClient()
													.getNoteTagNames(
															note.getGuid(),
															new OnClientCallback<List<String>>() {

																@Override
																public void onSuccess(
																		List<String> data) {
																	// TODO
																	// Auto-generated
																	// method
																	// stub
																	tagNames = data;

																}

																@Override
																public void onException(
																		Exception exception) {
																	// TODO
																	// Auto-generated
																	// method
																	// stub

																}

															});
										} catch (TTransportException e) {
											// TODO Auto-generated catch block
											e.printStackTrace();
										}

										// dbConnector.close();

									}

								}

								@Override
								public void onException(Exception exception) {
									progress.dismiss();
									Log.e(LOGTAG, "Error fetching notes",
											exception);
									Toast.makeText(getApplicationContext(),
											R.string.error_listing_notebooks,
											Toast.LENGTH_LONG).show();
									removeDialog(DIALOG_PROGRESS);
								}

							});
		} catch (TTransportException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}
}
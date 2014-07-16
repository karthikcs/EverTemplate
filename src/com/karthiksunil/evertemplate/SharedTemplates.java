
package com.karthiksunil.evertemplate;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Currency;
import java.util.Date;
import java.util.List;

import com.evernote.client.android.OnClientCallback;
import com.evernote.edam.notestore.SyncChunk;
import com.evernote.edam.type.LinkedNotebook;
import com.evernote.edam.type.Note;
import com.evernote.thrift.transport.TTransportException;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.text.Html;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ContextMenu.ContextMenuInfo;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;
import android.widget.AdapterView.AdapterContextMenuInfo;

/**
 * * <p/>
 * class created by Karthik Sunil
 */
public class SharedTemplates extends ParentActivity {
	
	// class variables 
	List <Note> SharedNotes ;
	ListView mListView ;
	//DatabaseConnector dbConnector;
	private int everUserId;
	int dataIndex = 0;
	boolean notesFetching = false;
	ProgressDialog progress;
	public ArrayAdapter mAdapter;
	ArrayList<String> temp_titles;
	ArrayList<String> temp_guid;
	//@Override
	public void onCreate(Bundle savedInstanceState) {
		
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.sharedtemplates);
		
		//everUserId = mEvernoteSession.getAuthenticationResult().getUserId();
		temp_titles = new ArrayList<String>();
		temp_guid = new ArrayList<String>();

		if (fillSharedTemplates() > 0 ){
			
		} else {
			getSharedTemplates(); // This fills up global variable Note list (SharedNotes).
			fillSharedTemplates();
		}
		
		mListView = (ListView) findViewById(R.id.sharedtemplatelist);
		
		mAdapter = new ArrayAdapter<String>(this,
				android.R.layout.simple_list_item_1, android.R.id.text1,
				temp_titles);
		
		mListView.setAdapter(mAdapter);
		mListView.setOnItemClickListener(mItemClickListener);
		
		mListView.setLongClickable(true);
		registerForContextMenu(mListView);
		
	}
	
	private int fillSharedTemplates() { // Populate arrays from DB for display purpose
		// TODO Auto-generated method stub
		everUserId = mEvernoteSession.getAuthenticationResult().getUserId();
		DatabaseConnector dbConnector = new DatabaseConnector(SharedTemplates.this);
		dbConnector.open();
		temp_titles.clear();
		temp_guid.clear();
		Cursor result1 = dbConnector.getSharedNotes(everUserId);
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
			return count1;
		} else {
			return 0;
		}
		
	}

	private AdapterView.OnItemClickListener mItemClickListener = new AdapterView.OnItemClickListener() {

		@Override
		public void onItemClick(AdapterView<?> parent, View view, int position,
				long id) {
			// TODO Auto-generated method stub
			createNoteAndDisplay(temp_guid.get(position),
					temp_titles.get(position)); 
			
		}
		
	};
	
	private void createNoteAndDisplay(String noteGuid, String noteTitle) {
		// TODO Auto-generated method stub
		final String ACTION_NEW_NOTE             = "com.evernote.action.CREATE_NEW_NOTE";
		everUserId = mEvernoteSession.getAuthenticationResult().getUserId();
		DatabaseConnector dbConnector1 = new DatabaseConnector(
				SharedTemplates.this);
		dbConnector1.open();
		Cursor result2 = dbConnector1.getSharedNoteContent(
				everUserId, noteGuid);
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
	      Toast.makeText(SharedTemplates.this, R.string.err_creating_note, Toast.LENGTH_SHORT).show();
	    } 
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
				SharedTemplates.this);
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



	Note noteData = null;
	String noteContent ;
	DatabaseConnector dbConnector = new DatabaseConnector(SharedTemplates.this);
	
	private void getNoteContents() {
		// TODO Auto-generated method stub
		Note note;
		
		dbConnector.open();
		dbConnector.deleteSharedNotes(everUserId);
		notesFetching = true;
		
		final ProgressDialog progress2 = ProgressDialog.show(SharedTemplates.this, getString(R.string.fetching_notes),
				getString(R.string.please_wait));
		for (int index = 0; index < SharedNotes.size(); index++) {
			note = SharedNotes.get(index);
			
			
			try {
				progress2.setMessage("Fetching Note contents");
				mEvernoteSession
				.getClientFactory()
				.createNoteStoreClient()
				.getNoteContent(note.getGuid(), new OnClientCallback<String>() {

					@Override
					public void onSuccess(String data) {
						// TODO Auto-generated method stub
						progress2.setMessage(getString(R.string.fetching) + " " + dataIndex 
								+ " " + getString(R.string.of) + " " + SharedNotes.size() );
						noteData = SharedNotes.get(dataIndex);
						noteContent = data;
						dbConnector
						.insertSharedNote(
								everUserId,
								noteData.getGuid(),
								null,
								noteData.getTitle(),
								noteContent);
						dataIndex = dataIndex + 1;
						if (dataIndex == SharedNotes
								.size()) {
							progress2.dismiss();
							dbConnector.close();
							Toast.makeText(
									getApplicationContext(),
									getString(R.string.notes_refreshed),
									Toast.LENGTH_LONG)
									.show();
							notesFetching = false;
							startActivity(new Intent(getApplicationContext(), SharedTemplates.class));
						}
						
					}

					@Override
					public void onException(Exception exception) {
						// TODO Auto-generated method stub
						//progress2.dismiss();
						exception.printStackTrace();
						Toast.makeText(getApplicationContext(), 
								"Kirrik", Toast.LENGTH_LONG).show();
						progress2.dismiss();
						
						
					}
					
				});
			} catch (TTransportException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				//progress2.dismiss();
				Toast.makeText(getApplicationContext(), 
						"Kirrik", Toast.LENGTH_LONG).show();
				progress2.dismiss();
			}
			
		}
	}
	boolean fetching = false;
	private void getSharedTemplates() {
		// TODO Auto-generated method stub
		if (!isNetworkAvailable() ){
			Toast.makeText(getApplicationContext(), 
					R.string.check_internet, Toast.LENGTH_LONG).show();
			return;
		}
		
		fetching = false;
		/*********** Code for fetching public notebook **********/
		
		final ProgressDialog progress1 = ProgressDialog.show(this, getString(R.string.fetching_notes),
				getString(R.string.please_wait));
		
		fetching = true;
		 try {
			mEvernoteSession.getClientFactory().createNoteStoreClient().
			 	listLinkedNotebooks(new OnClientCallback<List<LinkedNotebook>>() {

					@Override
					public void onSuccess(List<LinkedNotebook> data) {
						// TODO Auto-generated method stub
						
						int size = data.size();
						
						
						if(size < 1) {
							progress1.setMessage("Linking the notebook to your Account");
							LinkedNotebook linkedNotebook1 = new LinkedNotebook();
							linkedNotebook1.setShareName("EverTemplate");
                            linkedNotebook1.setUsername("evertemplate138");
                            linkedNotebook1.setUri("evertemplate");
                            
                            try {
								mEvernoteSession.getClientFactory().createNoteStoreClient().
									createLinkedNotebook(linkedNotebook1, new OnClientCallback<LinkedNotebook>() {

										@Override
										public void onSuccess(LinkedNotebook data) {
											// TODO Auto-generated method stub
											progress1.setMessage("Linking successful. Fetching notes...");
											try {
												mEvernoteSession.getClientFactory().createNoteStoreClient().
													getLinkedNotebookSyncChunk(data,
												            0, 100, true, new OnClientCallback<SyncChunk>() {

																@Override
																public void onSuccess(
																		SyncChunk data) {
																	progress1.dismiss();
																	if(data.getNotesSize() > 0 ) {
																		SharedNotes = data.getNotes();
																		getNoteContents();
																		
																	}
																	fetching = false;
																	// TODO Auto-generated method stub
																	
																}

																@Override
																public void onException(
																		Exception exception) {
																	// TODO Auto-generated method stub
																	
																}
														
															});
											} catch (TTransportException e) {
												// TODO Auto-generated catch block
												e.printStackTrace();
												progress1.dismiss();
											}
											
										}

										@Override
										public void onException(Exception exception) {
											// TODO Auto-generated method stub
											progress1.dismiss();
											
										}
										
									});
							} catch (TTransportException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
								progress1.dismiss();
								Toast.makeText(getApplicationContext(), 
										"Kirrik", Toast.LENGTH_LONG).show();
							}
							
						} else { // If Shared notebook is already linked
							progress1.setMessage("Fetching Notes from already linked Notebook");
							
							for (int i = 0; i < size; i++)  {
								LinkedNotebook lnb = data.get(i);
                                String lnbUri = lnb.getUri();
                                if (  lnbUri.equals("evertemplate")) {
                                	try {
										mEvernoteSession.getClientFactory().createNoteStoreClient().
											getLinkedNotebookSyncChunk(lnb, 0, 100, true, new OnClientCallback<SyncChunk>() {

												@Override
												public void onSuccess(SyncChunk data) {
													// TODO Auto-generated method stub
													progress1.dismiss();
													SharedNotes = data.getNotes();
													fetching = false;
													getNoteContents();
													
												}

												@Override
												public void onException(
														Exception exception) {
													// TODO Auto-generated method stub
													progress1.dismiss();
												}
												
											});
									} catch (TTransportException e) {
										// TODO Auto-generated catch block
										e.printStackTrace();
										progress1.dismiss();
									}
                                }
							}
						}
					}

					@Override
					public void onException(Exception exception) {
						// TODO Auto-generated method stub
						progress1.dismiss();
					}
				 
			});
		} catch (TTransportException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			progress1.dismiss();
		}
		//progress1.dismiss();
		
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.menu_1, menu);
		return true;
	}
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		
		case R.id.back :
			startActivity(new Intent(getApplicationContext(), EverTemplate.class));
			return true;
		
		case R.id.refresh:
			getSharedTemplates();
			//startActivity(new Intent(getApplicationContext(), SharedTemplates.class));
			
			return true;
			
		case R.id.options:
			startActivity(new Intent(getApplicationContext(), Options.class));
			return true;

		}
		return false;
	}
	
	//Show context menu
	public void onCreateContextMenu(ContextMenu menu, View v,
            ContextMenuInfo menuInfo) {
				super.onCreateContextMenu(menu, v, menuInfo);
				MenuInflater inflater = getMenuInflater();
				inflater.inflate(R.menu.ctxmenushared, menu);
	}
	public boolean onContextItemSelected(MenuItem item) {
		final AdapterContextMenuInfo info = (AdapterContextMenuInfo) item.getMenuInfo();
		  switch (item.getItemId()) {
		  case R.id.copy:
			  // Need to copy note to user notebook. 
			  return true;
		  }
		return true;
		
	}
}
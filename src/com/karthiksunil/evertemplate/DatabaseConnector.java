package com.karthiksunil.evertemplate;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;

public class DatabaseConnector {

	private static final String DB_NAME = "EverTemplate";
	private SQLiteDatabase database;
	private DatabaseOpenHelper dbOpenHelper;

	public DatabaseConnector(Context context) {
		dbOpenHelper = new DatabaseOpenHelper(context, DB_NAME, null, 1);
	}

	public void open() throws SQLException {
		// open database in reading/writing mode
		database = dbOpenHelper.getWritableDatabase();
	}

	public void close() {
		if (database != null)
			database.close();
	}

	public void insertTemplate(int userid, String template_guid, String template_name) {
		ContentValues newCon = new ContentValues();
		newCon.put("userid", userid);
		newCon.put("template_guid", template_guid);
		newCon.put("template_name", template_name);
		

		open();
		database.insert("EverUserTemplates", null, newCon);
		close();
	}

	public void updateTemplate(int userid, String template_guid, String template_name) {
		ContentValues editCon = new ContentValues();
		editCon.put("userid", userid);
		editCon.put("template_guid", template_guid);
		editCon.put("template_name", template_name);
		

		open();
		database.update("EverUserTemplates", editCon, "userid=" + userid, null);
		close();
	}

	public Cursor getAllTemplates() {
		return database.query("EverUserTemplates", new String[] { "userid", "template_guid", "template_name" }, null,
				null, null, null, "userid");
	}

	public Cursor getOneTemplate(int userid) {
		return database.query("EverUserTemplates", null, "userid=" + userid, null, null, null,
				null);
	}

	
	public void deleteTemplate(int  userid) {
		open();
		database.delete("EverUserTemplates", "userid=" + userid, null);
		close();
	}
	
	public void insertNote(int userid, String note_guid, String template_guid, String note_title, String note_content) {
		ContentValues newCon = new ContentValues();
		newCon.put("userid", userid);
		newCon.put("note_guid", note_guid);
		newCon.put("notebook_guid", template_guid);
		newCon.put("note_title", note_title);
		newCon.put("note_content", note_content);
		

		open();
		database.insert("EverUserNotes", null, newCon);
		close();
	}
	
	public Cursor getUserNotes(int userid) {
		return database.query("EverUserNotes", null, "userid=" + userid, null, null, null,
				null);
	}
	
	public Cursor getUserNoteContent(int everUserId, String noteGuid) {
		return database.query("EverUserNotes", null, "note_guid=?", new String[] {noteGuid } , null, null,
				null);
	}
	
	public void deleteNotes(int  userid) {
		open();
		database.delete("EverUserNotes", "userid=" + userid, null);
		close();
	}
	
	public void deleteSingleNote(int  userid, String noteGuid) {
		open();
		database.delete("EverUserNotes", "userid=? and note_guid=?", new String[] { Integer.toString(userid), noteGuid} );
		close();
	}
	
	// SQLs for Preferences 
	public void insertPrefs(int userid, String pref_name, String pref_value){
		ContentValues newCon = new ContentValues();
		newCon.put("userid", userid);
		newCon.put("pref_name", pref_name);
		newCon.put("pref_value", pref_value);
		
		open();
		database.insert("EverTempPrefs", null, newCon);
		close();
	}
	public void updatePrefs(int userid, String pref_name, String pref_value){
		ContentValues newCon = new ContentValues();
		newCon.put("userid", userid);
		newCon.put("pref_name", pref_name);
		newCon.put("pref_value", pref_value);
		
		open();
		database.update("EverTempPrefs", newCon, "pref_name=?" , new String[] {pref_name});
		//update("EverTempPrefs", null, newCon);
		close();
		
	}
	
	
	public Cursor getPref(int userid, String pref_name){
		return database.query("EverTempPrefs", null, "pref_name=?", new String[] {pref_name } , null, null,
				null);
	}
	
	// Methods needed for maintaining Shared notes
	public void deleteSharedNotes(int userid) {
		open();
		database.delete("EverSharedNotes", "userid=" + userid, null);
		close();
	}
	
	public void insertSharedNote(int userid, String note_guid, String template_guid, String note_title, String note_content) {
		ContentValues newCon = new ContentValues();
		newCon.put("userid", userid);
		newCon.put("note_guid", note_guid);
		newCon.put("notebook_guid", template_guid);
		newCon.put("note_title", note_title);
		newCon.put("note_content", note_content);
		

		open();
		database.insert("EverSharedNotes", null, newCon);
		close();
	}
	
	public Cursor getSharedNotes(int userid) {
		return database.query("EverSharedNotes", null, "userid=" + userid, null, null, null,
				null);
	}
	
	public Cursor getSharedNoteContent(int everUserId, String noteGuid) {
		return database.query("EverSharedNotes", null, "note_guid=?", new String[] {noteGuid } , null, null,
				null);
	}
	
}

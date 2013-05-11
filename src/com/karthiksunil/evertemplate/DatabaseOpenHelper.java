package com.karthiksunil.evertemplate;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteOpenHelper;

public class DatabaseOpenHelper extends SQLiteOpenHelper {

	public DatabaseOpenHelper(Context context, String name,
			CursorFactory factory, int version) {
		super(context, name, factory, version);
	}

	@Override
	public void onCreate(SQLiteDatabase db) {		
        
		String createQuery = "CREATE TABLE EverUserTemplates (userid integer primary key, template_guid, template_name);";                 
        db.execSQL(createQuery);
        
        String createQuery1 = "CREATE TABLE EverUserNotes (userid integer, note_guid, notebook_guid, note_title, note_content, primary key (userid,note_guid));";                 
        db.execSQL(createQuery1);	
        
        String createQuery2 = "CREATE TABLE EverTempPrefs (userid integer, pref_name, pref_value, primary key (userid,pref_name));";                 
        db.execSQL(createQuery2);
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
	
	}

}

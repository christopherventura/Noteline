package com.chrisventura.apps.noteline.Data;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.chrisventura.apps.noteline.R;

import java.util.Date;

/**
 * Created by ventu on 15/5/2017.
 */

public class NoteDbHelper extends SQLiteOpenHelper {

    public static String DB_NAME = "noteline.db";
    public static int DB_VERSION = 1;
    Context context;

    public static NoteDbHelper instance;

    public static NoteDbHelper getInstance(Context c) {
        if (instance == null) {
            instance = new NoteDbHelper(c);
        }
        return instance;
    }


    public interface Tables {
        String NOTES = "notes";
        String CATEGORIES = "categories";
    }

    public interface References {
        String CATEGORY_ID = String.format(
                " REFERENCES %s(%s) ON DELETE CASCADE",
                Tables.CATEGORIES,
                ContractNotes.CategoriesEntries.ID);
    }

    private NoteDbHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
        this.context =context;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(String.format(
                "CREATE TABLE %s ("
                + "%s INTEGER PRIMARY KEY AUTOINCREMENT, "
                + "%s TEXT NOT NULL UNIQUE, "
                + "%s TEXT, "
                + "%s TEXT, "
                + "%s INTEGER DEFAULT 0, "
                + "%s TEXT %s, "
                + "%s DATETIME NOT NULL,"
                + "%s DATETIME )",
                Tables.NOTES,
                ContractNotes.NotesEntries._ID,
                ContractNotes.NotesEntries.ID,
                ContractNotes.NotesEntries.TITLE,
                ContractNotes.NotesEntries.BODY,
                ContractNotes.NotesEntries.CHECKED,
                ContractNotes.NotesEntries.CATEGORY_ID,
                References.CATEGORY_ID,
                ContractNotes.NotesEntries.CREATED_AT,
                ContractNotes.NotesEntries.DELETED_AT
        ));

        db.execSQL(String.format(
                "CREATE TABLE %s ("
                + "%s INTEGER PRIMARY KEY AUTOINCREMENT, "
                + "%s TEXT NOT NULL UNIQUE, "
                + "%s TEXT NOT NULL UNIQUE,"
                + "%s DATETIME NOT NULL)",
                Tables.CATEGORIES,
                ContractNotes.CategoriesEntries._ID,
                ContractNotes.CategoriesEntries.ID,
                ContractNotes.CategoriesEntries.NAME,
                ContractNotes.CategoriesEntries.CREATED_AT
        ));

        String idCat = ContractNotes.CategoriesEntries.getId();
        ContentValues values = new ContentValues();
        values.put(ContractNotes.CategoriesEntries.ID, idCat);
        values.put(ContractNotes.CategoriesEntries.NAME,
                context.getResources().getString(R.string.uncategorized));
        values.put(ContractNotes.CategoriesEntries.CREATED_AT, new Date().getTime());

        db.insertOrThrow(Tables.CATEGORIES, null, values);

        ContentValues values1 = new ContentValues();
        values1.put(ContractNotes.NotesEntries.ID, ContractNotes.NotesEntries.getId());
        values1.put(ContractNotes.NotesEntries.TITLE,
                context.getResources().getString(R.string.default_note));
        values1.put(ContractNotes.NotesEntries.CATEGORY_ID, idCat);
        values1.put(ContractNotes.NotesEntries.BODY,
                context.getResources().getString(R.string.example_note_body));
        values1.put(ContractNotes.NotesEntries.CHECKED, 1); // true
        values1.put(ContractNotes.NotesEntries.CREATED_AT, new Date().getTime());
        db.insertOrThrow(Tables.NOTES, null, values1);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + Tables.NOTES);
        db.execSQL("DROP TABLE IF EXISTS " + Tables.CATEGORIES);

        onCreate(db);
    }
}

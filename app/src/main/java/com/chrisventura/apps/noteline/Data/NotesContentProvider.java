package com.chrisventura.apps.noteline.Data;

import android.content.ContentProvider;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.text.TextUtils;
import android.util.Log;

import java.util.Date;

public class NotesContentProvider extends ContentProvider {

    ContentResolver resolver;
    NoteDbHelper dbHelper;

    public static UriMatcher uriMatcher;
    public static final String AUTHORITY = ContractNotes.AUTHORITY;

    public static final int NOTES = 100;
    public static final int NOTES_ID = 101;
    public static final int CATEGORIES = 200;
    public static final int CATEGORIES_ID = 201;
    public static final int CATEGORIES_COUNT = 202;

    public static final String NOTE_JOIN_CATEGORY = String.format(
            "%s INNER JOIN %s ON %s.%s = %s.%s",
            NoteDbHelper.Tables.NOTES,
            NoteDbHelper.Tables.CATEGORIES,
            NoteDbHelper.Tables.CATEGORIES,
            ContractNotes.CategoriesEntries.ID,
            NoteDbHelper.Tables.NOTES,
            ContractNotes.NotesEntries.CATEGORY_ID
    );

    public static final String[] projectionNotes = new String[] {
            NoteDbHelper.Tables.NOTES + "." + ContractNotes.NotesEntries.ID,
            ContractNotes.NotesEntries.TITLE,
            ContractNotes.NotesEntries.BODY,
            ContractNotes.NotesEntries.CHECKED,
            NoteDbHelper.Tables.NOTES + "." + ContractNotes.NotesEntries.CREATED_AT,
            ContractNotes.NotesEntries.CATEGORY_ID,
            NoteDbHelper.Tables.CATEGORIES + "." + ContractNotes.CategoriesEntries.NAME
                    + " AS " + ContractNotes.NotesEntries.CATEGORY_NAME
    };


    static {
        uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
        uriMatcher.addURI(AUTHORITY, NoteDbHelper.Tables.NOTES, NOTES);
        uriMatcher.addURI(AUTHORITY, NoteDbHelper.Tables.NOTES + "/*", NOTES_ID);
        uriMatcher.addURI(AUTHORITY, NoteDbHelper.Tables.CATEGORIES, CATEGORIES);
        uriMatcher.addURI(AUTHORITY, NoteDbHelper.Tables.CATEGORIES + "/count", CATEGORIES_COUNT);
        uriMatcher.addURI(AUTHORITY, NoteDbHelper.Tables.CATEGORIES + "/*", CATEGORIES_ID);
    }


    public NotesContentProvider() {
    }

    @Override
    public boolean onCreate() {
        Log.d("DATABASE", "Content Provider Created");
        resolver = getContext().getContentResolver();
        dbHelper = NoteDbHelper.getInstance(getContext());
        return true;
    }

    @Override
    public String getType(Uri uri) {
        switch (uriMatcher.match(uri)) {
            case NOTES:
                return ContractNotes.DIR_MIME_TYPE + "." + NoteDbHelper.Tables.NOTES;
            case NOTES_ID:
                return ContractNotes.ITEM_MIME_TYPE + "." + NoteDbHelper.Tables.NOTES;
            case CATEGORIES:
                return ContractNotes.DIR_MIME_TYPE + "." + NoteDbHelper.Tables.CATEGORIES;
            case CATEGORIES_ID:
                return ContractNotes.ITEM_MIME_TYPE + "." + NoteDbHelper.Tables.CATEGORIES;
            default:
                throw new IllegalArgumentException("Type not found with Uri: " + uri);
        }
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection,
                        String[] selectionArgs, String sortOrder) {
        Log.d("DATABASE", "Querying " + uri);
        Cursor c;
        String id;
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        SQLiteQueryBuilder builder;
        String mainSelection;

        switch (uriMatcher.match(uri)) {
            case NOTES:
                builder = new SQLiteQueryBuilder();
                builder.setTables(NOTE_JOIN_CATEGORY);
                c = builder.query(db, projectionNotes, selection, selectionArgs, null, null, sortOrder);
                break;
            case NOTES_ID:
                builder = new SQLiteQueryBuilder();
                builder.setTables(NOTE_JOIN_CATEGORY);
                id = ContractNotes.NotesEntries.getIdFromUri(uri);
                mainSelection = String.format("%s = \'%s\'", NoteDbHelper.Tables.NOTES + "." + ContractNotes.NotesEntries.ID, id)
                        + (!TextUtils.isEmpty(selection) ? " AND (" + selection + ")" : "");
                c = builder.query(db, projectionNotes, mainSelection, selectionArgs, null, null, sortOrder);
                break;
            case CATEGORIES:
                c = db.query(NoteDbHelper.Tables.CATEGORIES, projection, selection, selectionArgs, null, null, sortOrder);
                break;
            case CATEGORIES_ID:
                id = ContractNotes.NotesEntries.getIdFromUri(uri);
                mainSelection = String.format("%s = \'%s\'", ContractNotes.CategoriesEntries.ID, id)
                        + (!TextUtils.isEmpty(selection) ? " AND (" + selection + ")" : "");
                c = db.query(NoteDbHelper.Tables.CATEGORIES, projection, mainSelection, selectionArgs, null, null, sortOrder);
                break;
            case CATEGORIES_COUNT:
                builder = new SQLiteQueryBuilder();
                builder.setTables(NOTE_JOIN_CATEGORY);
                c = builder.query(
                        db,
                        new String[] {
                                ContractNotes.NotesEntries.CATEGORY_ID,
                                ContractNotes.CategoriesEntries.NAME,
                                "COUNT(*) AS " +
                                        ContractNotes.CategoriesEntries.CATEGORY_COUNT,
                                 NoteDbHelper.Tables.CATEGORIES + "." +
                                         ContractNotes.CategoriesEntries.CREATED_AT
                        },
                        selection,
                        selectionArgs,
                        ContractNotes.NotesEntries.CATEGORY_ID,
                        null,
                        sortOrder
                );
                break;
            default:
                throw new IllegalArgumentException("Unsupported Uri: " + uri);
        }
        c.setNotificationUri(resolver, uri);
        return c;
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        Log.d("DATABASE", "Inserting " + uri);
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        Uri newItemId;
        String id = null;

        switch (uriMatcher.match(uri)) {
            case NOTES:
                if (values.getAsString(ContractNotes.NotesEntries.ID) == null) {
                    id = ContractNotes.NotesEntries.getId();
                    values.put(ContractNotes.NotesEntries.ID, id);
                }
                db.insertOrThrow(NoteDbHelper.Tables.NOTES, null, values);
                newItemId = ContractNotes.NotesEntries.createUriFromId(id);
                break;
            case CATEGORIES:
                if (values.getAsString(ContractNotes.CategoriesEntries.ID) == null) {
                    id = ContractNotes.CategoriesEntries.getId();
                    values.put(ContractNotes.CategoriesEntries.ID, id);
                }
                db.insertOrThrow(NoteDbHelper.Tables.CATEGORIES, null, values);
                newItemId = ContractNotes.CategoriesEntries.createUriFromId(id);
                break;
            default:
                throw new UnsupportedOperationException("Unsupported Uri: " + uri);
        }
        resolver.notifyChange(uri, null);
        return newItemId;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection,
                      String[] selectionArgs) {
        Log.d("DATABASE", "Updating " + uri);
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        String id, mainSelection;
        int affected;

        switch (uriMatcher.match(uri)) {
            case NOTES_ID:
                id = ContractNotes.NotesEntries.getIdFromUri(uri);
                mainSelection = String.format("%s = \'%s\'", ContractNotes.NotesEntries.ID, id)
                        + (!TextUtils.isEmpty(selection) ? " AND (" + selection + ")" : "");
                affected = db.update(NoteDbHelper.Tables.NOTES, values, mainSelection, selectionArgs);
                break;
            case CATEGORIES_ID:
                id = ContractNotes.CategoriesEntries.getIdFromUri(uri);
                mainSelection = String.format("%s = \'%s\'", ContractNotes.CategoriesEntries.ID, id)
                        + (!TextUtils.isEmpty(selection) ? " AND (" + selection + ")" : "");
                affected = db.update(NoteDbHelper.Tables.CATEGORIES, values, mainSelection, selectionArgs);
                break;
            default:
                throw new IllegalArgumentException("Unsupported Uri: " + uri);
        }

        resolver.notifyChange(uri, null);
        return affected;
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        Log.d("DATABASE", "Deleting " + uri);
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        String id, mainSelection;
        int affected;

        switch (uriMatcher.match(uri)) {
            case NOTES_ID:
                id = ContractNotes.NotesEntries.getIdFromUri(uri);
                Cursor c = query(uri, null, null, null, null);
                c.moveToFirst();
                mainSelection = String.format("%s = \'%s\'", ContractNotes.NotesEntries.ID, id)
                        + (!TextUtils.isEmpty(selection) ? " AND (" + selection + ")" : "");
                ContentValues values = new ContentValues();
                values.put(ContractNotes.NotesEntries.DELETED_AT, new Date().getTime());

                affected = db.update(NoteDbHelper.Tables.NOTES, values, mainSelection, selectionArgs);
                break;
            case CATEGORIES_ID:
                id = ContractNotes.CategoriesEntries.getIdFromUri(uri);
                mainSelection = String.format("%s = \'%s\'", ContractNotes.CategoriesEntries.ID, id)
                        + (!TextUtils.isEmpty(selection) ? " AND (" + selection + ")" : "");
                affected = db.delete(NoteDbHelper.Tables.CATEGORIES, mainSelection, selectionArgs);
                break;
            default:
                throw new UnsupportedOperationException("Unsupported Uri: " + uri);
        }

        return affected;

    }
}

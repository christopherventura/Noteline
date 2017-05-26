package com.chrisventura.apps.noteline.Data;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.net.Uri;
import android.os.AsyncTask;
import android.text.TextUtils;

import com.chrisventura.apps.noteline.BuildConfig;
import com.chrisventura.apps.noteline.Presenter.NoteListViewContract;
import com.chrisventura.apps.noteline.Utils.NoteUtils;

import java.util.Date;

/**
 * Created by ventu on 26/5/2017.
 */

public class NoteListInteractorImpl implements NoteListViewContract.NoteListInteractor {
    ContentResolver resolver;
    NoteListViewContract.OnNoteTasksFinishedListener listener;

    public NoteListInteractorImpl(ContentResolver resolver) {
        this.resolver = resolver;
    }

    @Override
    public void setOnNoteTasksFinishedListener(NoteListViewContract.OnNoteTasksFinishedListener listener) {
        this.listener = listener;
    }

    @Override
    public void loadNotes() {
        new LoadNotesTask().execute();
    }

    @Override
    public void searchNotes(String query) {
        new SearchNoteTask().execute(query);
    }

    @Override
    public void checkNote(NoteUtils.CheckingNotePackage notePackage) {
        new CheckNoteTask().execute(notePackage);
    }

    @Override
    public void deleteNote(Uri uri) {
        new DeleteNoteTask().execute(uri);
    }

    @Override
    public void moveNote(NoteUtils.MovingNotePackage notePackage) {
        new MoveNoteTask().execute(notePackage);
    }

    @Override
    public void loadNotesByCategory(String catName) {
        new LoadNotesByCategoryTask().execute(catName);
    }

    @Override
    public void loadCategories() {
        new LoadCategoriesTask().execute();
    }

    private class LoadNotesTask extends AsyncTask<Void, Void, Cursor> {

        @Override
        protected Cursor doInBackground(Void... params) {

            if (BuildConfig.DEBUG) {
                DatabaseUtils.dumpCursor(resolver.query(
                        ContractNotes.NotesEntries.CONTENT_URI,
                        null,
                        null,
                        null,
                        NoteDbHelper.Tables.NOTES + "." + ContractNotes.NotesEntries.CREATED_AT + " DESC"
                ));
            }

            return resolver.query(
                    ContractNotes.NotesEntries.CONTENT_URI,
                    null,
                    ContractNotes.NotesEntries.DELETED_AT + " IS NULL",
                    null,
                    ContractNotes.NotesEntries.CHECKED + " DESC, " +
                            NoteDbHelper.Tables.NOTES + "." + ContractNotes.NotesEntries.CREATED_AT + " DESC"
            );

        }
        @Override
        protected void onPostExecute(Cursor cursor) {
            super.onPostExecute(cursor);
            listener.onLoadNotesFinished(cursor);
        }

    }

    private class SearchNoteTask extends AsyncTask<String, Void, Cursor> {

        @Override
        protected Cursor doInBackground(String... params) {
            if (!TextUtils.isEmpty(params[0])) {
                return resolver.query(
                        ContractNotes.NotesEntries.CONTENT_URI,
                        null,
                        ContractNotes.NotesEntries.DELETED_AT + " IS NULL AND ( " +
                                ContractNotes.NotesEntries.TITLE + " LIKE \'%"+ params[0] +"%\'"
                                + " OR "
                                + ContractNotes.NotesEntries.BODY + " LIKE \'%"+ params[0] +"%\'"
                                + " OR "
                                + ContractNotes.NotesEntries.CATEGORY_NAME + " LIKE \'%"+ params[0] +"%\' )",
                        null,
                        ContractNotes.NotesEntries.CHECKED + " DESC, " +
                                NoteDbHelper.Tables.NOTES + "." + ContractNotes.NotesEntries.CREATED_AT
                                + " DESC"
                );
            }
            return null;
        }

        @Override
        protected void onPostExecute(Cursor cursor) {
            super.onPostExecute(cursor);
            listener.onSearchNotesFinished(cursor);
        }

    }

    private class CheckNoteTask extends AsyncTask<NoteUtils.CheckingNotePackage, Void, Integer> {

        @Override
        protected Integer doInBackground(NoteUtils.CheckingNotePackage... params) {
            ContentValues values = new ContentValues();
            if (params[0].checked) {
                values.put(ContractNotes.NotesEntries.CHECKED, 0);
            } else {
                values.put(ContractNotes.NotesEntries.CHECKED, 1);
                values.put(ContractNotes.NotesEntries.CREATED_AT,
                        new Date().getTime());
            }

            return resolver.update(
                    params[0].uri,
                    values,
                    null,
                    null
            );
        }
        @Override
        protected void onPostExecute(Integer anInt) {
            super.onPostExecute(anInt);
            listener.onCheckNoteFinished();
        }

    }

    private class DeleteNoteTask extends AsyncTask<Uri, Void, Integer> {

        @Override
        protected Integer doInBackground(Uri... params) {
            return resolver.delete(params[0], null, null);
        }
        @Override
        protected void onPostExecute(Integer integer) {
            super.onPostExecute(integer);
            listener.onDeleteNoteFinished();
        }

    }

    private class MoveNoteTask extends AsyncTask<NoteUtils.MovingNotePackage, Void, Integer> {

        @Override
        protected Integer doInBackground(NoteUtils.MovingNotePackage... params) {
            Uri uri = ContractNotes.NotesEntries.createUriFromId(params[0].id);
            ContentValues values = new ContentValues();
            String categoryId = ContractNotes.CategoriesEntries.getIdFromUri(params[0].uri);
            values.put(ContractNotes.NotesEntries.CATEGORY_ID, categoryId);

            return resolver.update(
                    uri,
                    values,
                    null,
                    null
            );
        }
        @Override
        protected void onPostExecute(Integer integer) {
            super.onPostExecute(integer);
            listener.onMoveNoteFinished();
        }

    }

    private class LoadNotesByCategoryTask extends AsyncTask<String, Void, Cursor> {

        @Override
        protected Cursor doInBackground(String... params) {

            if (BuildConfig.DEBUG) {
                DatabaseUtils.dumpCursor(resolver.query(
                        ContractNotes.NotesEntries.CONTENT_URI,
                        null,
                        ContractNotes.NotesEntries.DELETED_AT + " IS NULL AND ("
                                + ContractNotes.NotesEntries.CATEGORY_NAME + " = ?)",
                        new String[]{params[0]},
                        NoteDbHelper.Tables.NOTES + "." + ContractNotes.NotesEntries.CREATED_AT + " DESC"
                ));
            }

            return resolver.query(
                    ContractNotes.NotesEntries.CONTENT_URI,
                    null,
                    ContractNotes.NotesEntries.DELETED_AT + " IS NULL AND ("
                            + ContractNotes.NotesEntries.CATEGORY_NAME + " = ?)",
                    new String[]{params[0]},
                    ContractNotes.NotesEntries.CHECKED + " DESC, " +
                            NoteDbHelper.Tables.NOTES + "." + ContractNotes.NotesEntries.CREATED_AT + " DESC"
            );

        }

        @Override
        protected void onPostExecute(Cursor cursor) {
            super.onPostExecute(cursor);
            listener.onLoadNotesByCategoryFinished(cursor);
        }
    }

    private class LoadCategoriesTask extends AsyncTask<Void, Void, Cursor> {

        @Override
        protected Cursor doInBackground(Void... params) {

            if (BuildConfig.DEBUG) {
                DatabaseUtils.dumpCursor(resolver.query(
                        ContractNotes.CategoriesEntries.CONTENT_URI,
                        null, null, null, ContractNotes.CategoriesEntries.NAME));
            }

            return resolver.query(
                    ContractNotes.CategoriesEntries.CONTENT_URI.buildUpon()
                            .appendPath("count").build(),
                    null,
                    ContractNotes.NotesEntries.DELETED_AT + " IS NULL",
                    null,  ContractNotes.CategoriesEntries.NAME);
        }

        @Override
        protected void onPostExecute(Cursor cursor) {
            super.onPostExecute(cursor);
            listener.onLoadCategoriesFinished(cursor);
        }
    }


}

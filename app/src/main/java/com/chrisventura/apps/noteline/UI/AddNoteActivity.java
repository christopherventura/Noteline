package com.chrisventura.apps.noteline.UI;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.afollestad.materialdialogs.MaterialDialog;
import com.chrisventura.apps.noteline.BuildConfig;
import com.chrisventura.apps.noteline.Data.ContractNotes;
import com.chrisventura.apps.noteline.Data.NotesContentProvider;
import com.chrisventura.apps.noteline.Model.Note;
import com.chrisventura.apps.noteline.R;
import com.chrisventura.apps.noteline.Utils.DateUtils;

import java.util.Calendar;
import java.util.Date;

import jp.wasabeef.richeditor.RichEditor;

public class AddNoteActivity extends AppCompatActivity {

    EditText mTitleText;
    RichEditor mBodyText;
    String mCatId;
    TextView mLastUpdate;
    Uri mUriId;
    boolean mIsEditing;
    String mOldTitle;
    String mOldBody;
    String mOldCategory;
    public static int CATEGORY_SELECTION_REQUEST_CODE = 100;
    private int REQUEST_PHOTO_INSERTION = 101;
    public static String NOTE_TITLE_DRAFT =
            NotesContentProvider.AUTHORITY + "." + "NOTE_TITLE_DRAFT";
    public static String NOTE_BODY_DRAFT =
            NotesContentProvider.AUTHORITY + "." + "NOTE_BODY_DRAFT";
    public static String NOTE_CATEGORY_DRAFT =
            NotesContentProvider.AUTHORITY + "." + "NOTE_CATEGORY_DRAFT";
    public static String NOTE_OLD_CATEGORY_DRAFT =
            NotesContentProvider.AUTHORITY + "." + "NOTE_OLD_CATEGORY_DRAFT";
    public static String NOTE_OLD_BODY_DRAFT =
            NotesContentProvider.AUTHORITY + "." + "NOTE_OLD_BODY_DRAFT";
    public static String NOTE_OLD_TITLE_DRAFT =
            NotesContentProvider.AUTHORITY + "." + "NOTE_OLD_TITLE_DRAFT";
    public static String NOTE_URI_ID_DRAFT =
            NotesContentProvider.AUTHORITY + "." + "NOTE_URI_ID_DRAFT";
    public static String NOTE_IS_EDITING_DRAFT =
            NotesContentProvider.AUTHORITY + "." + "NOTE_IS_EDITING_DRAFT";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_note);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar_addnote);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(false);
            getSupportActionBar().setDisplayShowTitleEnabled(false);
        }

        mTitleText = (EditText) findViewById(R.id.addnote_TitleText);
        mBodyText = (RichEditor) findViewById(R.id.addnote_BodyText);
        mLastUpdate = (TextView) findViewById(R.id.addnote_lastupdate_text);
        mBodyText.setInputEnabled(false);
        mBodyText.loadCSS("file:///android_res/raw/style.css");
        mBodyText.setHorizontalScrollBarEnabled(false);

        mBodyText.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                mBodyText.setInputEnabled(true);
                mLastUpdate.setVisibility(View.GONE);
            }
        });

        mBodyText.setPlaceholder(getResources().getString(R.string.whatsabout));
        mBodyText.setPadding(12,12,12,12);
        mBodyText.setEditorFontSize(16);
        mBodyText.setEditorFontColor(R.color.colorSecondaryText);

        if (savedInstanceState != null) {
            onRestoreInstanceState(savedInstanceState);
        }

        String type = getIntent().getType();
        String action = getIntent().getAction() ;
        if(type != null && action != null && type.equals("text/plain") && action.equals(Intent.ACTION_SEND)) {
            String body = getIntent().getStringExtra(Intent.EXTRA_TEXT);

            if (body != null && !TextUtils.isEmpty(body)) {
                mBodyText.setHtml(body);
            }
            //mBodyText.setInputType(InputType.TYPE_TEXT_FLAG_MULTI_LINE);
            mLastUpdate.setVisibility(View.GONE);
            recoveryCurrentCategory();
            mBodyText.focusEditor();
        } else {
            String uriId = getIntent().getStringExtra(NotesListFragment.EDIT_NOTE_ID);
            if (uriId != null) {
                mUriId = Uri.parse(uriId);
                mIsEditing = true;
                new LoadNoteTask().execute(mUriId);
            } else {
                mBodyText.focusEditor();
                recoveryCurrentCategory();
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
//        Linkify.addLinks(mBodyText, Linkify.WEB_URLS |
//                Linkify.PHONE_NUMBERS | Linkify.EMAIL_ADDRESSES
//                | Linkify.MAP_ADDRESSES );
    }

    private void recoveryCurrentCategory() {
        SharedPreferences preferences = getApplicationContext().getSharedPreferences(NotesListFragment.PREFERENCES_CURRENT_CAT,
                MODE_PRIVATE);
        String currentCat = preferences.getString(NotesListFragment.PREFERENCES_CURRENT_CAT,
                getResources().getString(R.string.uncategorized));
        currentCat = (currentCat.equals(getResources().getString(R.string.all_notes))
                ? getResources().getString(R.string.uncategorized) : currentCat);
        new LoadCurrentCategoryTask().execute(currentCat);
    }

    @Override
    public void onSaveInstanceState(Bundle outState, PersistableBundle outPersistentState) {
        super.onSaveInstanceState(outState, outPersistentState);
        String title = mTitleText.getText().toString().trim();
        String body = mBodyText.getHtml().trim();
        outState.putString(NOTE_TITLE_DRAFT, title);
        outState.putString(NOTE_BODY_DRAFT, body);
        outState.putString(NOTE_CATEGORY_DRAFT, mCatId);
        if (mIsEditing) {
            outState.putBoolean(NOTE_IS_EDITING_DRAFT, true);
            outState.putString(NOTE_OLD_TITLE_DRAFT, mOldTitle);
            outState.putString(NOTE_OLD_BODY_DRAFT, mOldBody);
            outState.putString(NOTE_OLD_CATEGORY_DRAFT, mOldCategory);
            outState.putString(NOTE_URI_ID_DRAFT, mUriId.toString());
        }
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        String title, body;
        if (savedInstanceState != null) {
            title = savedInstanceState.getString(NOTE_TITLE_DRAFT);
            body = savedInstanceState.getString(NOTE_BODY_DRAFT);
            mCatId = savedInstanceState.getString(NOTE_CATEGORY_DRAFT);
            mIsEditing = savedInstanceState.getBoolean(NOTE_IS_EDITING_DRAFT);
            if (mIsEditing) {
                mOldTitle = savedInstanceState.getString(NOTE_OLD_TITLE_DRAFT);
                mOldBody = savedInstanceState.getString(NOTE_OLD_BODY_DRAFT);
                mOldCategory = savedInstanceState.getString(NOTE_OLD_CATEGORY_DRAFT);
                mUriId = Uri.parse(savedInstanceState.getString(NOTE_URI_ID_DRAFT));
            }
            mTitleText.setText(title);
            mBodyText.setHtml(body);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.addnote_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }


    private void insertPhoto() {
        Intent i = new Intent(Intent.ACTION_GET_CONTENT);
        i.setType("image/*");
        startActivityForResult(i, REQUEST_PHOTO_INSERTION);
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_photo:
                insertPhoto();
                break;
            case R.id.action_underline:
                mBodyText.setUnderline();
                break;
            case R.id.action_bold:
                mBodyText.setBold();
                break;
            case R.id.action_italic:
                mBodyText.setItalic();
                break;
            case R.id.action_strikethrough:
                mBodyText.setStrikeThrough();
                break;
            case R.id.action_link:
                new MaterialDialog.Builder(this)
                        .cancelable(true)
                        .input("Insert link", null, false,
                                new MaterialDialog.InputCallback() {
                                    @Override
                                    public void onInput(@NonNull MaterialDialog dialog, CharSequence input) {
                                        mBodyText.insertLink(input.toString().trim(), input.toString().trim());
                                    }
                                })
                        .show();
                break;
            case R.id.action_list:
                mBodyText.setBullets();
                break;
            case R.id.action_save:
                startSaving();
                break;
            case R.id.action_setcategory:
                Intent i = new Intent(AddNoteActivity.this, CategorySelectionActivity.class);
                i.setAction(Intent.ACTION_PICK);
                i.setData(Uri.parse(mCatId != null ? mCatId : ""));
                startActivityForResult(i,
                        CATEGORY_SELECTION_REQUEST_CODE);
                break;
            default:
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == CATEGORY_SELECTION_REQUEST_CODE && resultCode == RESULT_OK) {
            Uri result = data.getData();

            mCatId = ContractNotes.CategoriesEntries.getIdFromUri(result);
        } else if (requestCode == REQUEST_PHOTO_INSERTION && resultCode == RESULT_OK) {
            Uri result = data.getData();
            mBodyText.insertImage(result.toString(),
                    getResources().getString(R.string.app_name));
        }
    }

    protected void startSaving() {
        String title = mTitleText.getText().toString();
        String body = mBodyText.getHtml();
        if (body != null)
            body = body.trim();
        if (title != null)
            title = title.trim();

        if (TextUtils.isEmpty(title) && TextUtils.isEmpty(body)) {
            Toast.makeText(this, R.string.both_fields_are_empty, Toast.LENGTH_LONG)
                    .show();
        } else {
            Note note = new Note(null, title, body, null);
            note.setCreatedAt(new Date().getTime());
            note.setIdCategory(mCatId);
            new SaveNoteTask().execute(note);
        }

    }

    public void onBack() {
        super.onBackPressed();
    }

    @Override
    public void onBackPressed() {
        String title = mTitleText.getText().toString();
        String body = mBodyText.getHtml();
        if (body != null) {
            body = body.trim();
        }

        if ((TextUtils.isEmpty(title) && TextUtils.isEmpty(body)) && !mIsEditing) {
           onBack();
        } else {
            if (title.equals(mOldTitle) && body.equals(mOldBody) && mCatId.equals(mOldCategory)) {
                onBack();
            } else {
                AlertDialog dialog = new AlertDialog.Builder(this)
                        .setMessage(R.string.save_your_changes_or_discard_them)
                        .setNegativeButton(R.string.discard, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                onBack();
                            }
                        })
                        .setPositiveButton(R.string.save, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                startSaving();
                            }
                        })
                        .setNeutralButton(R.string.cancel, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                            }
                        })
                        .setCancelable(true)
                        .create();

                dialog.show();
            }
        }

    }

    private class SaveNoteTask extends AsyncTask<Note, Void, Uri> {

        @Override
        protected Uri doInBackground(Note... params) {
            ContentResolver r = getContentResolver();
            ContentValues values = new ContentValues();
            values.put(ContractNotes.NotesEntries.TITLE, params[0].getTitle());
            values.put(ContractNotes.NotesEntries.BODY, params[0].getBody());
            values.put(ContractNotes.NotesEntries.CATEGORY_ID, params[0].getIdCategory());
            values.put(ContractNotes.NotesEntries.CREATED_AT, params[0].getCreatedAt());

            if (mIsEditing) {
                int affected = r.update(mUriId, values, null, null);
                return (affected > 0) ? mUriId : null;
            } else {
                return r.insert(
                        ContractNotes.NotesEntries.CONTENT_URI,
                        values
                );
            }

        }

        @Override
        protected void onPostExecute(Uri uri) {
            super.onPostExecute(uri);
            finish();
        }
    }


    private class LoadNoteTask extends AsyncTask<Uri, Void, Cursor> {

        @Override
        protected Cursor doInBackground(Uri... params) {
            ContentResolver r = getContentResolver();

            if (BuildConfig.DEBUG) {

                DatabaseUtils.dumpCursor(r.query(
                        params[0],
                        null,
                        null,
                        null,
                        null
                ));
            }


            return r.query(
                    params[0],
                    null,
                    null,
                    null,
                    null
            );
        }

        @Override
        protected void onPostExecute(Cursor cursor) {
            super.onPostExecute(cursor);
            cursor.moveToFirst();
            mOldTitle = cursor.getString(cursor.getColumnIndex(ContractNotes.NotesEntries.TITLE));
            mOldBody = cursor.getString(cursor.getColumnIndex(ContractNotes.NotesEntries.BODY));
            mOldCategory = cursor.getString(cursor.getColumnIndex(ContractNotes.NotesEntries.CATEGORY_ID));
            mCatId = cursor.getString(cursor.getColumnIndex(ContractNotes.NotesEntries.CATEGORY_ID));
            long update = cursor.getLong(cursor.getColumnIndex(
                    ContractNotes.NotesEntries.CREATED_AT
            ));

            if (!TextUtils.isEmpty(mOldTitle)) {
                mTitleText.setText(mOldTitle);
            }
            if (!TextUtils.isEmpty(mOldBody)) {
                mBodyText.setHtml(mOldBody + " ");
            }
            String last_update = DateUtils.getRelativeTime(getApplicationContext(), update);
            mLastUpdate.setText(getResources().getString(R.string.last_update, last_update));
        }
    }

    private class LoadCurrentCategoryTask extends AsyncTask<String, Void, Cursor> {

        @Override
        protected Cursor doInBackground(String... params) {
            ContentResolver r = getContentResolver();
            return r.query(
                    ContractNotes.CategoriesEntries.CONTENT_URI,
                    null,
                    ContractNotes.CategoriesEntries.NAME + " = ?",
                    new String[] { params[0] }, null);
        }

        @Override
        protected void onPostExecute(Cursor cursor) {
            cursor.moveToFirst();
            mCatId = cursor.getString(cursor.getColumnIndex(
                    ContractNotes.CategoriesEntries.ID
            ));
        }
    }


}

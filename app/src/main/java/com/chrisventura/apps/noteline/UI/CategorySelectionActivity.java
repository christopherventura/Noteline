package com.chrisventura.apps.noteline.UI;

import android.app.SearchManager;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.text.InputType;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.afollestad.materialdialogs.MaterialDialog;
import com.chrisventura.apps.noteline.BuildConfig;
import com.chrisventura.apps.noteline.Data.ContractNotes;
import com.chrisventura.apps.noteline.Model.Category;
import com.chrisventura.apps.noteline.R;

import java.util.Date;
import java.util.concurrent.CancellationException;

public class CategorySelectionActivity extends AppCompatActivity implements CategoryAdapter.OnCategoryClickListener, SearchView.OnCloseListener, SearchView.OnQueryTextListener {

    RecyclerView mRecyclerView;
    CategoryAdapter mAdapter;
    TextView mAddCategory;
    String mCatId;
    MaterialDialog dialog;
    RelativeLayout addCategoryClickable;
    String catName;
    SearchView mSearchView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_category_selection);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar_categoryselect);
        setSupportActionBar(toolbar);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle(R.string.select_category);
        }

        mRecyclerView = (RecyclerView) findViewById(R.id.categories_recyclerview);
        mAddCategory = (TextView) findViewById(R.id.addcategory);
        addCategoryClickable = (RelativeLayout) findViewById(R.id.clickable_add_category);

        mCatId = getIntent().getDataString();
        if (mCatId != null) {
            new LoadCategoryNameTask().execute(mCatId);
        }

        addCategoryClickable.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog = new MaterialDialog.Builder(CategorySelectionActivity.this)
                        .title(R.string.add_category)
                        .cancelable(true)
                        .input(getResources().getString(R.string.category_name), null, false, new MaterialDialog.InputCallback() {
                            @Override
                            public void onInput(@NonNull MaterialDialog dialog, CharSequence input) {
                                String catName = input.toString().trim();
                                Category newCat = new Category(null, catName);
                                new InsertCategoryTask().execute(newCat);
                            }
                        })
                        .show();
            }
        });

        mAdapter = new CategoryAdapter(this, null, this);
        LinearLayoutManager manager = new LinearLayoutManager(this);
        manager.setOrientation(LinearLayoutManager.VERTICAL);
        mRecyclerView.setLayoutManager(manager);
        mRecyclerView.setAdapter(mAdapter);
        new LoadCategoriesTask().execute();

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.categoryselect, menu);
        SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        mSearchView = (SearchView) menu.findItem(R.id.action_search).getActionView();
        mSearchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));
        mSearchView.setOnCloseListener(this);
        mSearchView.setInputType(InputType.TYPE_CLASS_TEXT);
        mSearchView.setOnQueryTextListener(this);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public String getCurrentCategoryName() {
        return catName;
    }

    @Override
    public void onCategoryClick(int position) {
        if (getIntent().getAction().equals(Intent.ACTION_PICK)) {
            Cursor c = mAdapter.getCursor();
            c.moveToPosition(position);
            Intent i = new Intent();
            Uri result = ContractNotes.CategoriesEntries.createUriFromId(c.getString(c.getColumnIndex(
                    ContractNotes.CategoriesEntries.ID
            )));
            i.setData(result);
            setResult(RESULT_OK, i);
            finish();
        }
    }

    @Override
    public boolean onCategoryLongClick(int position) {
        return false;
    }

    @Override
    public boolean onContextItemClick(MenuItem item, final String categoryId, final String categoryName) {
        switch (item.getItemId()) {
            case R.id.action_edit_category:
                dialog = new MaterialDialog.Builder(CategorySelectionActivity.this)
                        .title(R.string.edit_category)
                        .cancelable(true)
                        .inputType(InputType.TYPE_TEXT_FLAG_CAP_WORDS)
                        .input(getResources().getString(R.string.category_name), categoryName, false, new MaterialDialog.InputCallback() {
                            @Override
                            public void onInput(@NonNull MaterialDialog dialog, CharSequence input) {
                                String catName = input.toString().trim();
                                new UpdateCategoryTask()
                                        .execute(categoryId, catName);
                            }
                        })
                        .show();
                break;
            case R.id.action_delete_category:
                new AlertDialog.Builder(this)
                        .setTitle(R.string.delete_category)
                        .setMessage(R.string.deleting_note_delete_notes)
                        .setCancelable(true)
                        .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                            }
                        })
                        .setPositiveButton(R.string.delete, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                                new DeleteCategoryTask().execute(categoryId);
                            }
                        })
                        .show();
                break;
            default:
        }

        return false;
    }

    @Override
    public boolean onClose() {
        return false;
    }

    @Override
    public boolean onQueryTextSubmit(String query) {
        return false;
    }

    @Override
    public boolean onQueryTextChange(String newText) {
        String filter = newText.trim();
        if (!TextUtils.isEmpty(filter)) {
            new SearchCategoryTask().execute(filter);
        } else {
            new LoadCategoriesTask().execute();
        }
        return false;
    }

    private class SearchCategoryTask extends AsyncTask<String, Void, Cursor> {

        @Override
        protected Cursor doInBackground(String... params) {
            ContentResolver r = getContentResolver();

            return r.query(
                    ContractNotes.CategoriesEntries.CONTENT_URI,
                    null,
                    ContractNotes.CategoriesEntries.NAME + " LIKE \"%" + params[0] + "%\"",
                    null,
                    ContractNotes.CategoriesEntries.CREATED_AT
            );
        }

        @Override
        protected void onPostExecute(Cursor cursor) {
            super.onPostExecute(cursor);
            mAdapter.swapCursor(cursor);
        }
    }

    public class LoadCategoriesTask extends AsyncTask<Void, Void, Cursor> {

        @Override
        protected Cursor doInBackground(Void... params) {
            ContentResolver r = getContentResolver();

            if (BuildConfig.DEBUG) {
                DatabaseUtils.dumpCursor(r.query(
                        ContractNotes.CategoriesEntries.CONTENT_URI,
                        null, null, null, null));
            }

            return r.query(
                    ContractNotes.CategoriesEntries.CONTENT_URI,
                    null, null, null,
                    ContractNotes.CategoriesEntries.NAME);
        }

        @Override
        protected void onPostExecute(Cursor cursor) {
            super.onPostExecute(cursor);
            mAdapter.swapCursor(cursor);
        }
    }

    public class LoadCategoryNameTask extends AsyncTask<String, Void, Cursor> {

        @Override
        protected Cursor doInBackground(String... params) {
            ContentResolver r = getContentResolver();

            Uri uri = ContractNotes.CategoriesEntries.createUriFromId(params[0]);

            if (BuildConfig.DEBUG) {
                DatabaseUtils.dumpCursor(r.query(
                        uri, null, null, null, null));
            }

            return r.query(
                    uri, null, null, null, null);
        }

        @Override
        protected void onPostExecute(Cursor cursor) {
            super.onPostExecute(cursor);
            cursor.moveToFirst();
            catName = cursor.getString(cursor.getColumnIndex(
                    ContractNotes.CategoriesEntries.NAME
            ));
        }
    }

    public class InsertCategoryTask extends AsyncTask<Category, Void, Uri> {

        @Override
        protected Uri doInBackground(Category... params) {
            ContentResolver r = getContentResolver();
            ContentValues values = new ContentValues();
            values.put(ContractNotes.CategoriesEntries.NAME, params[0].getNombre());
            values.put(ContractNotes.CategoriesEntries.CREATED_AT, new Date().getTime());
            return r.insert(ContractNotes.CategoriesEntries.CONTENT_URI, values);
        }

        @Override
        protected void onPostExecute(Uri uri) {
            super.onPostExecute(uri);
            new LoadCategoriesTask().execute();
        }
    }

    public class DeleteCategoryTask extends AsyncTask<String, Void, Integer> {

        @Override
        protected Integer doInBackground(String... params) {
            ContentResolver r = getContentResolver();
            Uri uri = ContractNotes.CategoriesEntries.createUriFromId(params[0]);
            return r.delete(
                    uri,
                    null,
                    null
            );
        }

        @Override
        protected void onPostExecute(Integer integer) {
            super.onPostExecute(integer);
            new LoadCategoriesTask().execute();
        }

    }

    public class UpdateCategoryTask extends AsyncTask<String, Void, Integer> {

        @Override
        protected Integer doInBackground(String... params) {
            ContentResolver r = getContentResolver();
            ContentValues values = new ContentValues();
            values.put(ContractNotes.CategoriesEntries.NAME, params[1]);
            Uri uri = ContractNotes.CategoriesEntries.createUriFromId(params[0]);
            return r.update(
                    uri,
                    values, null, null);
        }

        @Override
        protected void onPostExecute(Integer integer) {
            super.onPostExecute(integer);
            new LoadCategoriesTask().execute();
        }
    }


}

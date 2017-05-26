package com.chrisventura.apps.noteline.UI;

import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.text.InputType;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.chrisventura.apps.noteline.R;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener, SearchView.OnCloseListener, SearchView.OnQueryTextListener {

    FloatingActionButton fab_add;
    DrawerLayout drawerLayout;
    NavigationView navView;
    SearchView mSearchView;
    searchFragment mSearchListener;

    public interface searchFragment {
        void onSearchClose();
        void onQueryTextChange(String query);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //deleteDatabase(NoteDbHelper.DB_NAME);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar_main);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(R.string.notes_title);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        fab_add = (FloatingActionButton) findViewById(R.id.fab_addnote);
        drawerLayout = (DrawerLayout) findViewById(R.id.drawerlayout);
        navView = (NavigationView) findViewById(R.id.navview);
        navView.setNavigationItemSelectedListener(this);
        drawerLayout.setStatusBarBackground(android.R.color.transparent);


        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this,
                drawerLayout, toolbar,
                R.string.closing_navigation_bar,
                R.string.opening_navigation_bar);

        toggle.syncState();

        fab_add.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(MainActivity.this, AddNoteActivity.class);
                i.setType("text/plain");
                i.setAction(Intent.ACTION_SEND);
                startActivity(i);
            }
        });

        NotesListFragment fragment = (NotesListFragment) getSupportFragmentManager()
                .findFragmentById(R.id.content_main);
        if (fragment == null) {
            fragment = new NotesListFragment();
            getSupportFragmentManager()
                    .beginTransaction()
                    .add(R.id.content_main, fragment)
                    .commit();
        }

        mSearchListener = fragment;

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        mSearchView = (SearchView) menu.findItem(R.id.action_search).getActionView();
        mSearchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));
        mSearchView.setOnCloseListener(this);
        mSearchView.setInputType(InputType.TYPE_CLASS_TEXT);
        mSearchView.setOnQueryTextListener(this);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        if (drawerLayout.isDrawerOpen(Gravity.START)) {
            drawerLayout.closeDrawer(Gravity.START);
        }
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            default:
        }
        drawerLayout.closeDrawer(Gravity.START);
        return true;
    }

    @Override
    public boolean onClose() {
        mSearchListener.onSearchClose();
        return false;
    }

    @Override
    public boolean onQueryTextSubmit(String query) {
        return false;
    }

    @Override
    public boolean onQueryTextChange(String newText) {
        mSearchListener.onQueryTextChange(newText.trim());
        return false;
    }
}

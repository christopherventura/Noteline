package com.chrisventura.apps.noteline.UI;


import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.view.ActionMode;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.SubMenu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import com.afollestad.materialdialogs.MaterialDialog;
import com.bignerdranch.android.multiselector.ModalMultiSelectorCallback;
import com.chrisventura.apps.noteline.BuildConfig;
import com.chrisventura.apps.noteline.Data.ContractNotes;
import com.chrisventura.apps.noteline.Data.NoteDbHelper;
import com.chrisventura.apps.noteline.Data.NoteListInteractorImpl;
import com.chrisventura.apps.noteline.Data.NotesContentProvider;
import com.chrisventura.apps.noteline.Data.PreferencesHelper;
import com.chrisventura.apps.noteline.Presenter.NoteListPresenter;
import com.chrisventura.apps.noteline.Presenter.NoteListViewContract;
import com.chrisventura.apps.noteline.R;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 */
public class NotesListFragment extends Fragment
        implements NotesAdapter.OnNoteClickListener, MainActivity.searchFragment,
        NoteListViewContract.NoteListView {

    RecyclerView mRvNoteList;
    NotesAdapter mAdapter;
    ModalMultiSelectorCallback mActionModeCallback;
    ActionMode mActionMode;
    RelativeLayout mEmptyLayout;
    Cursor mCategories;
    NoteListPresenter presenter;



    public static int REQUEST_CATEGORY_TO_MOVE = 200;
    ArrayList<String> idsToMove;

    public static String IS_SELECTING_MODE_ACTIVATED =
            NotesContentProvider.AUTHORITY + "." + "IS_SELECTING_MODE_ACTIVATED";

    public static String EDIT_NOTE_ID =
            NotesContentProvider.AUTHORITY + "." + "EDIT_NOTE_ID";
    public static String PREFERENCES_CURRENT_CAT =
            NotesContentProvider.AUTHORITY + "." + "PREFERENCES_CURRENT_CAT";

    public NotesListFragment() {
        // Required empty public constructor
    }


    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        Bundle state = mAdapter.multiSelector.saveSelectionStates();
        outState.putAll(state);
        if (mAdapter.multiSelector.isSelectable())  {
            outState.putBoolean(IS_SELECTING_MODE_ACTIVATED, true);
        }
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        PreferencesHelper preferencesHelper =
                new PreferencesHelper(getActivity().getApplicationContext());
        NoteListInteractorImpl interactor = new NoteListInteractorImpl(getActivity().getContentResolver());
        presenter = new NoteListPresenter(preferencesHelper, interactor);
        presenter.onAttach(this);
        if (savedInstanceState != null) {
            if (savedInstanceState.getBoolean(IS_SELECTING_MODE_ACTIVATED)) {
                ((AppCompatActivity) getActivity()).startSupportActionMode(mActionModeCallback);
                mAdapter.multiSelector.restoreSelectionStates(savedInstanceState);
                onSelectedNoteItem();
            }

        }
    }


    @Override
    public View onCreateView(final LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View root = inflater.inflate(R.layout.fragment_notes_list, container, false);
        idsToMove = new ArrayList<>();
        mRvNoteList = (RecyclerView) root.findViewById(R.id.rv_noteslist);
        LinearLayoutManager manager = new LinearLayoutManager(getContext());
        manager.setOrientation(LinearLayoutManager.VERTICAL);
        manager.setSmoothScrollbarEnabled(false);
        mRvNoteList.setLayoutManager(manager);
        mAdapter = new NotesAdapter(getContext(), this);
        mRvNoteList.setAdapter(mAdapter);
        mEmptyLayout = (RelativeLayout) root.findViewById(R.id.emptylist_view);

        mActionModeCallback = new ModalMultiSelectorCallback(mAdapter.multiSelector) {
            @Override
            public boolean onCreateActionMode(ActionMode actionMode, Menu menu) {
                super.onCreateActionMode(actionMode, menu);
                actionMode.getMenuInflater().inflate(R.menu.selected_note_menu, menu);
                mActionMode = actionMode;
                ((MainActivity) getActivity()).fab_add.setVisibility(View.INVISIBLE);
                return true;
            }



            @Override
            public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
                final List<Integer> selected = mAdapter.multiSelector.getSelectedPositions();
                final Cursor c = mAdapter.getCursor();
                mode.finish();
                idsToMove.clear();

                if (item.getItemId() == R.id.action_check) {
                    presenter.prepareNotesChecking(selected, c);
                }


                if (item.getItemId() == R.id.action_delete) {

                    new AlertDialog.Builder(getContext())
                            .setCancelable(true)
                            .setMessage(R.string.are_you_sure)
                            .setPositiveButton(R.string.delete, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    presenter.prepareNotesDeleting(selected, c);
                                }
                            })
                            .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {

                                }
                            })
                            .show();

                    return true;
                } else if (item.getItemId() == R.id.action_share) {
                    Intent i = new Intent(Intent.ACTION_SEND);
                    i.setType("text/plain");
                    i.putExtra(Intent.EXTRA_TEXT, presenter.getBodyFromSelectedNote(selected, c));
                    Intent chooser = Intent.createChooser(i,
                            getResources().getString(R.string.share_note));
                    startActivity(chooser);

                    return true;
                } else if (item.getItemId() == R.id.action_move) {
                    idsToMove = presenter.getIdFromSelectedNotes(selected, c);

                    Intent i = new Intent(getActivity(), CategorySelectionActivity.class);
                    i.setAction(Intent.ACTION_PICK);
                    if (selected.size() == 1) {
                        String idCat = presenter.getCategoryFromSelectedNote(selected, c);
                        i.setData(Uri.parse(idCat));

                    }

                    startActivityForResult(i, REQUEST_CATEGORY_TO_MOVE);
                    return true;
                }
                return false;
            }

            @Override
            public void onDestroyActionMode(ActionMode actionMode) {
                // don't call super here - leads to crash!
                // clear selections
                mAdapter.multiSelector.clearSelections();
                // here we need to change the mIsSelectable property without refreshing all the holders,
                // so we cant use mMultiSelector.setSelectable(false)
                try {
                    Field field = mAdapter.multiSelector.getClass().getDeclaredField("mIsSelectable");
                    if (field != null) {
                        if (!field.isAccessible())
                            field.setAccessible(true);
                        field.set(mAdapter.multiSelector, false);
                    }
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                } catch (NoSuchFieldException e) {
                    e.printStackTrace();
                }
                ((MainActivity) getActivity()).fab_add.setVisibility(View.VISIBLE);
                ActionBar actionBar = ( (AppCompatActivity) getActivity()).getSupportActionBar();
                if (actionBar != null) {
                    actionBar.setTitle(R.string.notes_title);
                }
            }
        };
        mActionModeCallback.setMultiSelector(mAdapter.multiSelector);

        return root;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CATEGORY_TO_MOVE
                && resultCode == AppCompatActivity.RESULT_OK) {
            Uri categoryToMove = data.getData();
            for (String id : idsToMove) {
                presenter.moveNote(id, categoryToMove);
            }
        }
    }

    public void onSelectedNoteItem() {
        int selectedCount = mAdapter.multiSelector.getSelectedPositions().size();
        if (selectedCount != 1) {
            mActionMode.getMenu().removeItem(R.id.action_share);
        } else {
            mActionMode.getMenu().clear();
            mActionMode.getMenuInflater().inflate(R.menu.selected_note_menu,
                    mActionMode.getMenu());
        }
        String title = (selectedCount > 0)
                ? getResources().getQuantityString(R.plurals.selected_notes,
                selectedCount, selectedCount)
                : getResources().getString(R.string.select_notes);
        mActionMode.setTitle(title);
    }

    @Override
    public void onEmptyCursor() {
        Log.d("CURSOR", "Empty Cursor");
        mRvNoteList.setVisibility(View.GONE);
        mEmptyLayout.setVisibility(View.VISIBLE);
    }

    public void onPoblatedCursor() {
        Log.d("CURSOR", "Poblated Cursor");
        mRvNoteList.setVisibility(View.VISIBLE);
        mEmptyLayout.setVisibility(View.GONE);
    }

    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    public void onResume() {
        super.onResume();
        Menu navMenu = ((MainActivity) getActivity()).navView.getMenu();
        SubMenu subMenu = navMenu.findItem(R.id.categories_submenu).getSubMenu();
        subMenu.clear();
        presenter.populateCursorWithCurrentCategory();
    }

    @Override
    public void setNotesTitleAsBarTitle() {
        setActionBarTitle(getAppContext().getResources().getString(R.string.notes_title));
    }

    @Override
    public String getAllNotesStringResource() {
        return getAppContext().getResources().getString(R.string.all_notes);
    }

    @Override
    public void swapCursorAdapter(Cursor c) {
        mAdapter.swapCursor(c);
    }

    @Override
    public void setActionBarTitle(String title) {
        ActionBar actionBar = ((MainActivity) getActivity()).getSupportActionBar();
        if (actionBar != null) {
            actionBar.setTitle(title);
        }
    }

    public Context getAppContext() {
        return getActivity().getApplicationContext();
    }


    @Override
    public void onNoteClick(String idNote) {
        Intent i = new Intent(getActivity(), AddNoteActivity.class);
        Uri uriId = ContractNotes.NotesEntries.createUriFromId(idNote);
        i.setType("text/plain");
        i.putExtra(EDIT_NOTE_ID, uriId.toString());
        startActivity(i);
    }

    @Override
    public void onLongClick() {
        ((AppCompatActivity) getActivity()).startSupportActionMode(mActionModeCallback);
    }

    @Override
    public void onSearchClose() {

    }

    @Override
    public void onQueryTextChange(String newText) {
        presenter.validateSearchQuery(newText);
    }

    @Override
    public void createCategoryMenu(Cursor c) {
        mCategories = c;
        mCategories.moveToFirst();
        MenuItem.OnMenuItemClickListener listener = new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                item.setChecked(true);
                String title;
                if (item.getItemId() != R.id.action_all_notes) {
                    title = item.getTitle().toString();

                    presenter.setCurrentCategoryToPreferences(title);
                    presenter.loadNotesByCategory(title);
                    setActionBarTitle(title);
                } else {
                    presenter.setCurrentCategoryToPreferences(getResources().getString(R.string.all_notes));
                    presenter.loadNotes();
                }
                return false;
            }
        };
        Menu navMenu = ((MainActivity) getActivity()).navView.getMenu();
        MenuItem item1 = navMenu.findItem(R.id.action_all_notes);
        item1.setOnMenuItemClickListener(listener);
        item1.setChecked(true);
        MenuItem catsItem = navMenu.findItem(R.id.categories_submenu);
        SubMenu subMenu = catsItem.getSubMenu();
        subMenu.clear();


        if (mCategories != null && mCategories.getCount() > 0) {
            String current = presenter.getCurrentCategoryFromPreferences();
            do {
                int position = mCategories.getPosition() + 1;
                String name = mCategories.getString(
                        mCategories.getColumnIndex(ContractNotes.CategoriesEntries.NAME)
                );


                MenuItem item = subMenu.add(getId(), position, position, name);
                item.setOnMenuItemClickListener(listener);

                if (name.equals(current)) {
                    item1.setChecked(false);
                    item.setChecked(true);
                }


            } while(mCategories.moveToNext());
        }
    }



}

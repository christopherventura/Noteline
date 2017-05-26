package com.chrisventura.apps.noteline.Presenter;

import android.database.Cursor;
import android.net.Uri;
import android.text.TextUtils;

import com.chrisventura.apps.noteline.Data.ContractNotes;
import com.chrisventura.apps.noteline.Data.NoteListInteractorImpl;
import com.chrisventura.apps.noteline.Data.PreferencesHelper;
import com.chrisventura.apps.noteline.Utils.NoteUtils;

import java.util.ArrayList;
import java.util.List;

import static com.chrisventura.apps.noteline.UI.NotesListFragment.PREFERENCES_CURRENT_CAT;

/**
 * Created by ventu on 25/5/2017.
 */

public class NoteListPresenter  implements NoteListViewContract.NoteListPresenter,
        NoteListViewContract.OnNoteTasksFinishedListener {

    NoteListViewContract.NoteListView view;
    PreferencesHelper preferencesHelper;
    NoteListInteractorImpl interactor;

    public NoteListPresenter(PreferencesHelper preferencesHelper,
                             NoteListInteractorImpl interactor) {
        this.preferencesHelper = preferencesHelper;
        this.interactor = interactor;
    }

    @Override
    public void onAttach(NoteListViewContract.NoteListView view) {
        this.view = view;
        interactor.setOnNoteTasksFinishedListener(this);
    }

    @Override
    public void loadNotes() {
        view.setNotesTitleAsBarTitle();
        interactor.loadNotes();
    }

    @Override
    public void prepareNotesChecking(List<Integer> selectedItems, Cursor cursor) {
        for (Integer position : selectedItems) {
            cursor.moveToPosition(position);
            Uri uri = ContractNotes.NotesEntries.createUriFromId(cursor.getString(cursor.getColumnIndex(
                    ContractNotes.NotesEntries.ID
            )));
            int checked = cursor.getInt(cursor.getColumnIndex(
                    ContractNotes.NotesEntries.CHECKED
            ));
            NoteUtils.CheckingNotePackage notePackage = new NoteUtils.CheckingNotePackage(uri, checked > 0);
            checkNote(notePackage);
        }
    }

    @Override
    public void checkNote(NoteUtils.CheckingNotePackage notePackage) {
        interactor.checkNote(notePackage);
    }

    @Override
    public void prepareNotesDeleting(List<Integer> selectedItems, Cursor cursor) {
        for (Integer position : selectedItems) {
            cursor.moveToPosition(position);
            Uri uri = ContractNotes.NotesEntries.createUriFromId(cursor.getString(cursor.getColumnIndex(
                    ContractNotes.NotesEntries.ID
            )));
            deleteNote(uri);
        }
    }

    @Override
    public void deleteNote(Uri uri) {
        interactor.deleteNote(uri);

    }

    @Override
    public String getBodyFromSelectedNote(List<Integer> selectedItem, Cursor cursor) {
        int position = selectedItem.get(0);
        if(cursor.moveToPosition(position)) {
            return cursor.getString(
                    cursor.getColumnIndex(ContractNotes.NotesEntries.BODY)
            );
        }
        return null;
    }

    public String getCurrentCategoryFromPreferences() {
        return preferencesHelper.getStringValue(PREFERENCES_CURRENT_CAT,
                view.getAllNotesStringResource());
    }

    @Override
    public boolean setCurrentCategoryToPreferences(String catName) {
        return preferencesHelper.setStringValue(PREFERENCES_CURRENT_CAT, catName);
    }

    @Override
    public void validateSearchQuery(String query) {
        if (!TextUtils.isEmpty(query)) {
            searchNotes(query);
        } else {
            loadNotes();
        }
    }

    @Override
    public void searchNotes(String query) {
        interactor.searchNotes(query.trim().replace("\'", "")
                .replace("\"",""));
    }

    @Override
    public ArrayList<String> getIdFromSelectedNotes(List<Integer> selectedItems, Cursor cursor) {
        ArrayList<String> idsToMove = new ArrayList<>();
        String id;
        for (Integer position : selectedItems) {
            cursor.moveToPosition(position);
            id = cursor.getString(cursor.getColumnIndex(
                    ContractNotes.NotesEntries.ID
            ));
            idsToMove.add(id);
        }

        return idsToMove;
    }


    @Override
    public String getCategoryFromSelectedNote(List<Integer> selectedItem, Cursor cursor) {
        cursor.moveToPosition(selectedItem.get(0));
        return cursor.getString(
                cursor.getColumnIndex(ContractNotes.NotesEntries.CATEGORY_ID)
        );
    }

    @Override
    public void moveNote(String idNote, Uri category) {
        interactor.moveNote(new NoteUtils.MovingNotePackage(idNote, category));
    }

    @Override
    public void loadNotesByCategory(String catName) {
        view.setActionBarTitle(catName);
        interactor.loadNotesByCategory(catName);
    }

    @Override
    public void loadCategories() {
        interactor.loadCategories();
    }

    @Override
    public void prepareCategoryMenu(Cursor c) {
        view.createCategoryMenu(c);
    }

    @Override
    public void populateCursorWithCurrentCategory() {
        String currentCat = getCurrentCategoryFromPreferences();
        if (currentCat.equals(view.getAllNotesStringResource())) {
            loadNotes();
        } else {
            loadNotesByCategory(currentCat);
        }
    }

    @Override
    public void onCursorReceived(Cursor c) {
        view.swapCursorAdapter(c);
    }

    @Override
    public void onLoadNotesFinished(Cursor c) {
        view.swapCursorAdapter(c);
        loadCategories();
    }

    @Override
    public void onSearchNotesFinished(Cursor c) {
        onCursorReceived(c);
    }

    @Override
    public void onCheckNoteFinished() {
        populateCursorWithCurrentCategory();
    }

    @Override
    public void onDeleteNoteFinished() {
        populateCursorWithCurrentCategory();
    }

    @Override
    public void onMoveNoteFinished() {
        populateCursorWithCurrentCategory();
    }

    @Override
    public void onLoadNotesByCategoryFinished(Cursor c) {
        onCursorReceived(c);
        loadCategories();
    }

    @Override
    public void onLoadCategoriesFinished(Cursor c) {
        prepareCategoryMenu(c);
    }

}

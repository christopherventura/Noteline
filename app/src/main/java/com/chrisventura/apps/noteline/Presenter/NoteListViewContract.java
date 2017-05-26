package com.chrisventura.apps.noteline.Presenter;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import com.chrisventura.apps.noteline.Utils.NoteUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by ventu on 25/5/2017.
 */

public class NoteListViewContract {

    public interface NoteListView {
        void setNotesTitleAsBarTitle();
        void swapCursorAdapter(Cursor c);
        void setActionBarTitle(String title);
        void createCategoryMenu(Cursor c);
        String getAllNotesStringResource();
    }

    public interface NoteListPresenter extends BasePresenter<NoteListView> {

        void loadNotes();
        void loadNotesByCategory(String catName);
        void loadCategories();
        void prepareCategoryMenu(Cursor c);

        void prepareNotesChecking(List<Integer> selectedItems, Cursor cursor);
        void checkNote(NoteUtils.CheckingNotePackage notePackage);

        void prepareNotesDeleting(List<Integer> selectedItems, Cursor cursor);
        void deleteNote(Uri uri);

        String getBodyFromSelectedNote(List<Integer> selectedItem, Cursor cursor);
        String getCategoryFromSelectedNote(List<Integer> selectedItem, Cursor cursor);

        String getCurrentCategoryFromPreferences();
        boolean setCurrentCategoryToPreferences(String catName);

        void populateCursorWithCurrentCategory();

        void validateSearchQuery(String query);
        void searchNotes(String query);

        ArrayList<String> getIdFromSelectedNotes(List<Integer> selectedItems, Cursor cursor);
        void moveNote(String idNote, Uri category);

        void onCursorReceived(Cursor c);

    }

    public interface NoteListInteractor {
        void setOnNoteTasksFinishedListener(OnNoteTasksFinishedListener listener);
        void loadNotes();
        void searchNotes(String query);
        void checkNote(NoteUtils.CheckingNotePackage notePackage);
        void deleteNote(Uri uri);
        void moveNote(NoteUtils.MovingNotePackage notePackage);
        void loadNotesByCategory(String catName);
        void loadCategories();
    }

    public interface OnNoteTasksFinishedListener {
        void onLoadNotesFinished(Cursor c);
        void onSearchNotesFinished(Cursor c);
        void onCheckNoteFinished();
        void onDeleteNoteFinished();
        void onMoveNoteFinished();
        void onLoadNotesByCategoryFinished(Cursor c);
        void onLoadCategoriesFinished(Cursor c);
    }
}

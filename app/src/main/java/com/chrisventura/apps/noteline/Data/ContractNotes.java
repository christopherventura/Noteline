package com.chrisventura.apps.noteline.Data;

import android.net.Uri;
import android.provider.BaseColumns;

import java.util.UUID;

/**
 * Created by ventu on 15/5/2017.
 */

public class ContractNotes {

    public static String AUTHORITY = "com.chrisventura.apps.noteline";
    public static Uri CONTENT_BASE = Uri.parse("content://" + AUTHORITY);

    public static String DIR_MIME_TYPE = "vnd.android.cursor.dir/vnd." + AUTHORITY;
    public static String ITEM_MIME_TYPE = "vnd.android.cursor.item/vnd." + AUTHORITY;

    public interface NotesColums {
        String ID = "id";
        String TITLE = "title";
        String BODY = "body";
        String CHECKED = "checked";
        String CATEGORY_ID = "category_id";
        String CREATED_AT = "created_at";
        String DELETED_AT = "deleted_at";

        // ALIAS EXTRA COLUMN FOR JOIN
        String CATEGORY_NAME = "category_name";
    }

    public interface CategoriesColumns {
        String ID = "id";
        String NAME = "name";
        String CREATED_AT = "created_at";

        // ALIAS EXTRA FOR COUNT
        String CATEGORY_COUNT = "category_count";
    }

    public static class NotesEntries implements NotesColums, BaseColumns {
        public static Uri CONTENT_URI = CONTENT_BASE.buildUpon()
                .appendPath(NoteDbHelper.Tables.NOTES).build();

        public static Uri createUriFromId(String id) {
            return CONTENT_URI.buildUpon().appendPath(id).build();
        }

        public static String getIdFromUri(Uri uri) {
            return uri.getLastPathSegment();
        }

        public static String getId() {
            return "NT-" + UUID.randomUUID().toString();
        }
    }

    public static class CategoriesEntries implements CategoriesColumns, BaseColumns {
        public static Uri CONTENT_URI = CONTENT_BASE.buildUpon()
                .appendPath(NoteDbHelper.Tables.CATEGORIES).build();

        public static Uri createUriFromId(String id) {
            return CONTENT_URI.buildUpon().appendPath(id).build();
        }

        public static String getIdFromUri(Uri uri) {
            return uri.getLastPathSegment();
        }

        public static String getId() {
            return "CT-"+ UUID.randomUUID().toString();
        }
    }
 }

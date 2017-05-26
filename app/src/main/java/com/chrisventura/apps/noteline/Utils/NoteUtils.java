package com.chrisventura.apps.noteline.Utils;

import android.net.Uri;

/**
 * Created by ventu on 25/5/2017.
 */

public class NoteUtils {
    public static class CheckingNotePackage {
        public Uri uri;
        public boolean checked;

        public CheckingNotePackage(Uri uri, boolean checked) {
            this.uri = uri;
            this.checked = checked;
        }
    }

    public static class MovingNotePackage {
        public String id;
        public Uri uri;

        public MovingNotePackage(String id, Uri uri) {
            this.id = id;
            this.uri = uri;
        }
    }
}

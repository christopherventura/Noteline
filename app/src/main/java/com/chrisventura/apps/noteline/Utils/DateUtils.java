package com.chrisventura.apps.noteline.Utils;

import android.content.Context;

import java.util.Calendar;

/**
 * Created by ventu on 25/5/2017.
 */

public class DateUtils {

    public static long getTime() {
        return Calendar.getInstance().getTimeInMillis();
    }

    public static String getRelativeTime(Context context, long then) {
        CharSequence dateText = android.text.format.DateUtils.getRelativeDateTimeString(
                context,
                then,
                getTime(),
                0L,
                android.text.format.DateUtils.FORMAT_ABBREV_ALL
        );

        return dateText.toString();
    }
}

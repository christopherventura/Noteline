package com.chrisventura.apps.noteline.Utils;

/**
 * Created by ventu on 24/5/2017.
 */

public class StringUtils {

    public static String removeHtmlTags(String html) {
        if (html == null) {
            return "";
        }
        String parsed = html
                .replace("<li>", " - ")
                .replace("</li>", "")
                .replace("<div>", " ")
                .replace("</div>", "")
                .replace("<ul>", " [ ")
                .replace("</ul>", " ] ")
                .replace("<b>", "")
                .replace("</b>", "")
                .replace("<br>", "\n")
                .replace("</br>", "")
                .replace("<p>", "")
                .replace("</p>", "")
                .replace("<u>", "")
                .replace("</u>", "")
                .replace("<i>", "")
                .replace("</i>", "")
                .replace("<h2>", "")
                .replace("</h2>", "")
                .replace("&nbsp;", " ")
                .replace("<strike>", "")
                .replace("</strike>", "");

        return parsed;
    }
}

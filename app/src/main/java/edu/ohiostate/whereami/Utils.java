package edu.ohiostate.whereami;

import android.os.Build;
import android.text.Html;
import android.text.Spanned;

public class Utils {

    /*
     * Code from Stack Overflow (J. Burrows):
     * https://stackoverflow.com/questions/37904739/html-fromhtml-deprecated-in-android-n
     */
    @SuppressWarnings("deprecation")
    public static Spanned fromHtml(String htmlStr) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            return Html.fromHtml(htmlStr, Html.FROM_HTML_MODE_LEGACY);
        }
        else {
            return Html.fromHtml(htmlStr);
        }
    }
}

package edu.ohiostate.whereami;

import android.text.Html;
import android.text.Spanned;

public class Utils {

    /*
     * Code from Stack Overflow (J. Burrows):
     * https://stackoverflow.com/questions/37904739/html-fromhtml-deprecated-in-android-n
     */
    public static Spanned fromHtml(String htmlStr) {
		return Html.fromHtml(htmlStr, Html.FROM_HTML_MODE_LEGACY);
	}
}

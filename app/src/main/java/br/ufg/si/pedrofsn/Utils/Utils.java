package br.ufg.si.pedrofsn.Utils;

import android.app.Activity;
import android.content.Context;
import android.graphics.Typeface;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import java.util.UUID;

public class Utils {

    public static boolean isConectado(Context context) {
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Activity.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
        return networkInfo != null && networkInfo.isConnected() ? true : false;
    }

    public static void aplicarFonteElis(Context context, View view) {
        if (context != null && view != null) {
            Typeface fonteElis = Typeface.createFromAsset(context.getAssets(), "elis.ttf");

            if (view instanceof TextView) {
                ((TextView) view).setTypeface(fonteElis);
            } else if (view instanceof EditText) {
                ((EditText) view).setTypeface(fonteElis);
            } else if (view instanceof Button) {
                ((Button) view).setTypeface(fonteElis);
            }
        }
    }

    public static void ocultarTecladoAndroid(Context context, View editText) {
        InputMethodManager imm = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(editText.getWindowToken(), 0);
    }

    public static int generateUniqueId() {
        UUID idOne = UUID.randomUUID();
        String str = "" + idOne;
        int uid = str.hashCode();
        String filterStr = "" + uid;
        str = filterStr.replaceAll("-", "");
        return Integer.parseInt(str);
    }
}

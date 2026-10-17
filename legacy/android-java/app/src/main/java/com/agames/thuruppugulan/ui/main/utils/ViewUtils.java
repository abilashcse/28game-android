package com.agames.thuruppugulan.ui.main.utils;

import android.content.Context;
import android.widget.Toast;

public class ViewUtils {

    public static void showToast(Context ctx, String message) {
        if (ctx != null) {
            Toast.makeText(ctx, message, Toast.LENGTH_SHORT).show();
        }
    }
}

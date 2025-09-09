package com.example.mykeyboard;

import android.content.Context;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.RelativeSizeSpan;
import android.widget.Toast;

import androidx.annotation.StringRes;

public class ToastUtil {

    public static void show(Context ctx, String message, int duration) {
        SpannableString ss = new SpannableString(message);
        ss.setSpan(
                new RelativeSizeSpan(0.8f),
                0,
                message.length(),
                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
        );
        // Call the method that accepts CharSequence
        show(ctx, (CharSequence) ss, duration);
    }

    // ✅ This is the missing method
    public static void show(Context ctx, CharSequence message, int duration) {
        Toast.makeText(ctx, message, duration).show();
    }

    public static void show(Context ctx, @StringRes int resId, int duration) {
        String message = ctx.getString(resId);
        show(ctx, message, duration);
    }
}
